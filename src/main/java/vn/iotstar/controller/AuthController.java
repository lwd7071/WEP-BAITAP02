package vn.iotstar.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.RegisterRequestDto;
import vn.iotstar.service.AuthService;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "activated", required = false) String activated,
                            @RequestParam(value = "resetSuccess", required = false) String resetSuccess,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác hoặc tài khoản chưa kích hoạt");
        }
        if (logout != null) {
            model.addAttribute("message", "Đã đăng xuất thành công");
        }
        if (activated != null) {
            model.addAttribute("message", "Tài khoản đã kích hoạt thành công. Vui lòng đăng nhập");
        }
        if (resetSuccess != null) {
            model.addAttribute("message", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập với mật khẩu mới");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterRequestDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("form") RegisterRequestDto form,
                                  RedirectAttributes redirectAttributes) {
        try {
            authService.register(form);
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP kích hoạt");
            return "redirect:/verify-otp?email=" + form.getEmail();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(value = "email", required = false) String email,
                                @RequestParam(value = "resent", required = false) String resent,
                                Model model) {
        model.addAttribute("email", email);
        if (resent != null) {
            model.addAttribute("message", "Đã gửi lại mã OTP mới. Vui lòng kiểm tra email");
        }
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("email") String email,
                                   @RequestParam("otp") String otp,
                                   RedirectAttributes redirectAttributes) {
        try {
            authService.activate(email, otp);
            redirectAttributes.addFlashAttribute("message", "Kích hoạt tài khoản thành công!");
            return "redirect:/login?activated=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/verify-otp?email=" + email;
        }
    }

    @PostMapping("/verify-otp/resend")
    public String resendOtp(@RequestParam("email") String email,
                            RedirectAttributes redirectAttributes) {
        try {
            authService.resendActivationOtp(email);
            return "redirect:/verify-otp?email=" + email + "&resent=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/verify-otp?email=" + email;
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("username") String username,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            Integer userId = authService.requestPasswordReset(username);
            session.setAttribute("passwordResetUserId", userId);
            redirectAttributes.addFlashAttribute("message", "Mã xác thực đã được gửi về email đăng ký");
            return "redirect:/reset-password";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("passwordResetUserId") == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập tên đăng nhập để yêu cầu đặt lại mật khẩu trước");
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("otp") String otp,
                                       @RequestParam("newPassword") String newPassword,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("passwordResetUserId");
        if (userId == null) {
            return "redirect:/forgot-password";
        }
        try {
            authService.resetPassword(userId, otp, newPassword);
            session.removeAttribute("passwordResetUserId");
            return "redirect:/login?resetSuccess=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/reset-password";
        }
    }
}