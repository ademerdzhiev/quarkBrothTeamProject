package quarkbrothBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import quarkbrothBlog.bindingModel.ArticleBindingModel;
import quarkbrothBlog.bindingModel.FileBindingModel;
import quarkbrothBlog.entity.*;
import quarkbrothBlog.repository.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Controller
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/article/create")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model) {
        model.addAttribute("view", "article/create");

        List<Category> categories = this.categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "base-layout";
    }

    @PostMapping("/article/create")
    @PreAuthorize("isAuthenticated()")
    public String createProcess(ArticleBindingModel articleBindingModel,
                                FileBindingModel fileBindingModel) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());
        Category category = this.categoryRepository.findOne(articleBindingModel.getCategoryId());
        HashSet<Tag> tags = this.findTagsFromString(articleBindingModel.getTagString());



        MultipartFile file = fileBindingModel.getPicture();
        String hashedFileName = null;

        int index = file.getOriginalFilename().lastIndexOf(".");
        if (index != -1) {

            String extension = file.getOriginalFilename().substring(index);
            if (extension.equals(".jpg") ||
                    extension.equals(".png") ||
                    extension.equals(".bmp") ||
                    extension.equals(".gif")) {

                hashedFileName = UUID.randomUUID().toString().concat(extension);
                File imageFile = new File(new File("").getAbsolutePath() +  // get project path
                        "\\src\\main\\resources\\static\\pics\\",           // pics folder path
                        hashedFileName);                                    // image name

                try {
                    file.transferTo(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        Article articleEntity = new Article(
                articleBindingModel.getTitle(),
                articleBindingModel.getContent(),
                userEntity,
                category,
                tags,
                hashedFileName
        );


        this.articleRepository.saveAndFlush(articleEntity);

        return "redirect:/";
    }

    @GetMapping("/article/{id}")
    public String details(Model model, @PathVariable Integer id) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }

        if (!(SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)) {
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            User entityUser = this.userRepository.findByEmail(principal.getUsername());

            model.addAttribute("user", entityUser);
        }

        Article article = this.articleRepository.findOne(id);

        List<Comment> comments = this.commentRepository.findByArticle(article);
        comments.stream()
                .sorted((object1, object2) -> object1.getId().compareTo(object2.getId()));
        Collections.reverse(comments);

        model.addAttribute("comments", comments);
        model.addAttribute("article", article);
        model.addAttribute("view", "article/details");

        return "base-layout";
    }

    @GetMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }
        Article article = this.articleRepository.findOne(id);

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/";
        }

        List<Category> categories = this.categoryRepository.findAll();

        String tagString = article.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "));

        model.addAttribute("view", "article/edit");
        model.addAttribute("article", article);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tagString);

        return "base-layout";
    }

    @PostMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id,
                              ArticleBindingModel articleBindingModel,
                              FileBindingModel fileBindingModel) {

        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/";
        }

        MultipartFile image = fileBindingModel.getPicture();
        int index = image.getOriginalFilename().lastIndexOf(".");
        if(index != -1){

            // Delete current article picture if there is one
            if (article.getImageName() != null) {
                try {
                    Files.deleteIfExists(Paths.get(
                            new File("").getAbsolutePath() +                        // get project path
                                    "\\src\\main\\resources\\static\\pics\\" +      // get pics folder path
                                    article.getImageName()                          // image name
                    ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Add new picture
            String extension = image.getOriginalFilename().substring(index);
            if (extension.equals(".jpg") ||
                    extension.equals(".png") ||
                    extension.equals(".bmp") ||
                    extension.equals(".gif")) {

                String hashedFileName = UUID.randomUUID().toString().concat(extension);
                File imageFile = new File(new File("").getAbsolutePath() +      // get project path
                        "\\src\\main\\resources\\static\\pics\\",               // pics folder path
                        hashedFileName);                                        // image name

                try {
                    image.transferTo(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                article.setImageName(hashedFileName);
            }
        }



        Category category = this.categoryRepository.findOne(articleBindingModel.getCategoryId());
        HashSet<Tag> tags = this.findTagsFromString(articleBindingModel.getTagString());

        article.setCategory(category);
        article.setContent(articleBindingModel.getContent());
        article.setTitle(articleBindingModel.getTitle());
        article.setTags(tags);

        this.articleRepository.saveAndFlush(article);

        return "redirect:/article/" + article.getId();
    }

    @GetMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(Model model, @PathVariable Integer id) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/article" + id;
        }

        model.addAttribute("article", article);
        model.addAttribute("view", "article/delete");

        return "base-layout";
    }

    @PostMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);
        String categoryId = article.getCategory().getId().toString();

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/article/" + id;
        }

        for (Comment comment : article.getComments()) {
            this.commentRepository.delete(comment);
        }

        if (article.getImageName() != null) {
            try {
                Files.deleteIfExists(Paths.get(
                        new File("").getAbsolutePath() +                // get project path
                        "\\src\\main\\resources\\static\\pics\\" +      // get pics folder path
                                article.getImageName()                  // image name
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        this.articleRepository.delete(article);
        return "redirect:/category/" + categoryId;

    }

    private boolean isUserAuthorOrAdmin(Article article) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByEmail(user.getUsername());

        return userEntity.isAdmin() || userEntity.isAuthor(article);
    }

    private HashSet<Tag> findTagsFromString(String tagString) {
        HashSet<Tag> tags = new HashSet<>();

        String[] tagNames = tagString.split(",\\s*");

        for (String tagName : tagNames) {
            Tag currentTag = this.tagRepository.findByName(tagName);

            if (currentTag == null) {
                currentTag = new Tag(tagName);
                this.tagRepository.saveAndFlush(currentTag);
            }

            tags.add(currentTag);
        }

        return tags;
    }
}
