package quarkbrothBlog.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import quarkbrothBlog.bindingModel.FileBindingModel;
import quarkbrothBlog.bindingModel.UserBindingModel;
import quarkbrothBlog.entity.Role;
import quarkbrothBlog.entity.User;
import quarkbrothBlog.repository.RoleRepository;
import quarkbrothBlog.repository.UserRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;


@Controller
public class UserController {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("view", "user/register");

        return "base-layout";
    }

    @PostMapping("/register")
    public String registerProcess(UserBindingModel userBindingModel){

        if(!userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
            return "redirect:/register";
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String defaultAvatarName = "Quark_structure_proton.jpg";

        User user = new User(
                userBindingModel.getEmail(),
                userBindingModel.getFullName(),
                bCryptPasswordEncoder.encode(userBindingModel.getPassword()),
                defaultAvatarName
        );

        Role userRole = this.roleRepository.findByName("ROLE_USER");

        user.addRole(userRole);

        this.userRepository.saveAndFlush(user);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("view", "user/login");

        return "base-layout";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login?logout";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());

        boolean hasAuthority = true;

        model.addAttribute("hasAuthority", hasAuthority);
        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("isAuthenticated()")
    public String userProfile(Model model, @PathVariable Integer id){
        if(!this.userRepository.exists(id)){
            return "redirect:/";
        }

        UserDetails userQ = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User me = this.userRepository.findByEmail(userQ.getUsername());
        User user = this.userRepository.findOne(id);

        boolean hasAuthority = me.getId().equals(user.getId()) || me.isAdmin();

        model.addAttribute("hasAuthority", hasAuthority);
        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }

    @GetMapping("/profile/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String profileEdit(Model model,@PathVariable Integer id,
                              UserBindingModel userBindingModel){

        if(!this.userRepository.exists(id)){
            return "redirect:/";
        }

        User user = this.userRepository.findOne(id);

        if(!isAdminOrMe(user)){
            return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("view", "user/edit");

        return "base-layout";
    }

    @PostMapping("/profile/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String profileEditProcess(@PathVariable Integer id, UserBindingModel userBindingModel,
                                     FileBindingModel fileBindingModel){
        if(!this.userRepository.exists(id)){
            return "redirect:/";
        }

        User user = this.userRepository.findOne(id);

        if(!StringUtils.isEmpty(userBindingModel.getPassword())
                && !StringUtils.isEmpty(userBindingModel.getConfirmPassword())){

            if(userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                user.setPassword(bCryptPasswordEncoder.encode(userBindingModel.getPassword()));
            }
        }

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

        user.setFullName(userBindingModel.getFullName());
        user.setEmail(userBindingModel.getEmail());

        this.userRepository.saveAndFlush(user);

        return "redirect:/profile";

    }

    private boolean isAdminOrMe(User me){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());

        return userEntity.isAdmin() || userEntity.isMe(me);
    }


}

