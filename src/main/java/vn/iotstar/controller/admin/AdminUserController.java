package vn.iotstar.controller.admin;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserAdminCreateForm;
import vn.iotstar.dto.UserAdminUpdateForm;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) UserStatus status,
            @RequestParam(value = "provider", required = false) AuthProvider provider,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<User> userPage = userService.search(q, status, provider, PageRequest.of(Math.max(0, page), Math.max(1, size)));
        model.addAttribute("users", userPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("provider", provider);
        return "admin/user-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserAdminCreateForm());
        }
        return "admin/user-add";
    }

    @PostMapping
    public String createUser(
            @Valid @ModelAttribute("userForm") UserAdminCreateForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user-add";
        }
        try {
            userService.createByAdmin(form);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm người dùng mới thành công!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            result.reject("globalError", ex.getMessage());
            return "admin/user-add";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        User user = userService.findById(id);
        if (!model.containsAttribute("userForm")) {
            UserAdminUpdateForm form = new UserAdminUpdateForm();
            form.setFullName(user.getFullName());
            form.setEmail(user.getEmail());
            form.setPhone(user.getPhone());
            form.setAvatar(user.getAvatar());
            model.addAttribute("userForm", form);
        }
        model.addAttribute("user", user);
        return "admin/user-edit";
    }

    @PostMapping("/{id}/update")
    public String updateUser(
            @PathVariable("id") int id,
            @Valid @ModelAttribute("userForm") UserAdminUpdateForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            User user = userService.findById(id);
            model.addAttribute("user", user);
            return "admin/user-edit";
        }
        try {
            userService.updateByAdmin(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin người dùng thành công!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            User user = userService.findById(id);
            model.addAttribute("user", user);
            result.reject("globalError", ex.getMessage());
            return "admin/user-edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi trạng thái tài khoản thành công!");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}