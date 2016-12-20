package quarkbrothBlog.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import quarkbrothBlog.bindingModel.FileBindingModel;
import quarkbrothBlog.bindingModel.UserEditBindingModel;
import quarkbrothBlog.entity.Article;
import quarkbrothBlog.entity.Comment;
import quarkbrothBlog.entity.Role;
import quarkbrothBlog.entity.User;
import quarkbrothBlog.repository.ArticleRepository;
import quarkbrothBlog.repository.CommentRepository;
import quarkbrothBlog.repository.RoleRepository;
import quarkbrothBlog.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

@Controller
@RequestMapping("/admin/users/")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/")
    public String listUsers(Model model){
        List<User> users = this.userRepository.findAll();

        model.addAttribute("users", users);
        model.addAttribute("view", "admin/user/list");

        return "base-layout";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);
        List<Role> roles = this.roleRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("view", "admin/user/edit");

        return "base-layout";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(@PathVariable Integer id,
                              UserEditBindingModel userBindingModel, FileBindingModel fileBindingModel){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);

        if(!StringUtils.isEmpty(userBindingModel.getPassword())
                && !StringUtils.isEmpty(userBindingModel.getConfirmPassword())){

            if(userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                user.setPassword(bCryptPasswordEncoder.encode(userBindingModel.getPassword()));
            }
        }

        user.setFullName(userBindingModel.getFullName());
        user.setEmail(userBindingModel.getEmail());

        Set<Role> roles = new HashSet<>();

        for (Integer roleId : userBindingModel.getRoles()){
            roles.add(this.roleRepository.findOne(roleId));
        }

        user.setRoles(roles);

        MultipartFile image = fileBindingModel.getPicture();
        int index = image.getOriginalFilename().lastIndexOf(".");
        if(index != -1){

            // Delete current article picture if there is one
            if (!user.getAvatarName().equals("Quark_structure_proton.jpg")) {
                try {
                    Files.deleteIfExists(Paths.get(
                            new File("").getAbsolutePath() +                        // get project path
                                    "\\src\\main\\resources\\static\\pics\\" +      // get pics folder path
                                    user.getAvatarName()                            // image name
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

                user.setAvatarName(hashedFileName);
            }
        }


        this.userRepository.saveAndFlush(user);

        return "redirect:/admin/users/";

    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);

        model.addAttribute("user", user);
        model.addAttribute("view", "admin/user/delete");

        return "base-layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }
        User user = this.userRepository.findOne(id);

        for(Comment comment : user.getComments()){
            this.commentRepository.delete(comment);
        }

        for(Article article : user.getArticles()){
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

            this.articleRepository.delete(article);
        }

        if(!user.getAvatarName().equals("Quark_structure_proton.jpg")){
            try {
                Files.deleteIfExists(Paths.get(
                        new File("").getAbsolutePath() +                        // get project path
                                "\\src\\main\\resources\\static\\pics\\" +      // get pics folder path
                                user.getAvatarName()                            // image name
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if(!user.getAvatarName().equals("Quark_structure_proton.jpg")){
            try {
                Files.deleteIfExists(Paths.get(
                        new File("").getAbsolutePath() +                        // get project path
                                "\\src\\main\\resources\\static\\pics\\" +      // get pics folder path
                                user.getAvatarName()                            // image name
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.userRepository.delete(user);

        return "redirect:/admin/users/";
    }
}
