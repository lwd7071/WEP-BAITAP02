package vn.iotstar.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;
import vn.iotstar.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(@RequestParam(value = "q", required = false) String q,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "categoryId"));
        Page<Category> categoryPage = categoryService.searchAdmin(q, pageable);

        model.addAttribute("categories", categoryPage);
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/category-list";
    }

    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new CategoryDto());
        }
        return "admin/category-add";
    }

    @PostMapping
    public String createCategory(@ModelAttribute("category") CategoryDto dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.create(dto);
            redirectAttributes.addFlashAttribute("message", "Thêm danh mục thành công");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("category", dto);
            return "redirect:/admin/categories/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editCategoryForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        var categoryOpt = categoryService.findById(id);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục");
            return "redirect:/admin/categories";
        }
        Category cat = categoryOpt.get();
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(cat.getCategoryId());
        dto.setCategoryName(cat.getCategoryName());
        dto.setImages(cat.getImages());
        dto.setStatus(cat.getStatus());

        model.addAttribute("category", dto);
        return "admin/category-edit";
    }

    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable("id") Integer id,
                                 @ModelAttribute("category") CategoryDto dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.update(id, dto);
            redirectAttributes.addFlashAttribute("message", "Cập nhật danh mục thành công");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Integer id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("message", "Xóa danh mục thành công");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}