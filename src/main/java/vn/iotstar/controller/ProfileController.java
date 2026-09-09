package vn.iotstar.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserAdminUpdateForm;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;
import vn.iotstar.util.UploadUtil;

import java.io.IOException;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .or(() -> userRepository.findByEmailIgnoreCase(authentication.getName()))
                .orElse(null);

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            Authentication authentication,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .or(() -> userRepository.findByEmailIgnoreCase(authentication.getName()))
                .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            String avatar = avatarUrl;
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String saved = UploadUtil.saveImage(avatarFile); if (saved != null) avatar = saved;
            }

            UserAdminUpdateForm form = new UserAdminUpdateForm();
            form.setFullName(fullName);
            form.setEmail(user.getEmail());
            form.setPhone(phone);
            form.setAvatar(avatar);

            userService.updateByAdmin(user.getId(), form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công!");
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }
}