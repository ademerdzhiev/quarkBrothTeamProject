package quarkbrothBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import quarkbrothBlog.bindingModel.CommentBindingModel;
import quarkbrothBlog.entity.Article;
import quarkbrothBlog.entity.Comment;
import quarkbrothBlog.entity.User;
import quarkbrothBlog.repository.ArticleRepository;
import quarkbrothBlog.repository.CommentRepository;
import quarkbrothBlog.repository.UserRepository;

import java.util.Date;

@Controller
public class CommentController {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;





    @PostMapping("/article/{id}")
    @PreAuthorize("isAuthenticated()")
    public String detailsProcess(@PathVariable Integer id, CommentBindingModel commentBindingMode) {

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();


        Article articleEntity = this.articleRepository.findOne(id);
        User userEntity = this.userRepository.findByEmail(user.getUsername());

        Comment comment = new Comment(
                commentBindingMode.getComment().trim(),
                new Date().toString(),
                userEntity,
                articleEntity
        );

        this.commentRepository.saveAndFlush(comment);

        return "redirect:/article/" + id;
    }

    @GetMapping("/comment/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.commentRepository.exists(id)){
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if(!this.isUserCommenterOrAdmin(comment)){
            return "redirect:/";
        }

        model.addAttribute("view", "comment/edit");
        model.addAttribute("comment", comment);

        return "base-layout";
    }

    @PostMapping("/comment/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, CommentBindingModel commentBindingModel) {
        if(!this.commentRepository.exists(id)) {
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);
        Article article = this.articleRepository.findOne(comment.getArticle().getId());

        if(!this.isUserCommenterOrAdmin(comment)){
            return "redirect:/article/" + article.getId();
        }

        comment.setComment(commentBindingModel.getComment());

        this.commentRepository.saveAndFlush(comment);

        return "redirect:/article/" + article.getId();
    }

    @GetMapping("/comment/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Integer id, Model model){
        if(!this.commentRepository.exists(id)){
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if(!this.isUserCommenterOrAdmin(comment)){
            return "redirect:/";
        }

        model.addAttribute("view", "comment/delete");
        model.addAttribute("comment", comment);

        return "base-layout";
    }

    @PostMapping("/comment/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id, CommentBindingModel commentBindingModel) {

        if(!this.commentRepository.exists(id)){
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if(!this.isUserCommenterOrAdmin(comment)){
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(comment.getArticle().getId());

        this.commentRepository.delete(id);

        return "redirect:/article/" + article.getId();
    }





    private boolean isUserCommenterOrAdmin(Comment comment) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());
        Comment commentEntity = this.commentRepository.findOne(comment.getId());

        return userEntity.isAdmin() || commentEntity.isCommenter(userEntity);
    }
}
