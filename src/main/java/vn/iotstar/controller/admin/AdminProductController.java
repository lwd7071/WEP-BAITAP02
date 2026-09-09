package vn.iotstar.controller.admin;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDto;
import vn.iotstar.entity.Product;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<Product> productPage = productService.searchAdmin(q, categoryId, status, PageRequest.of(Math.max(0, page), Math.max(1, size)));
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categoryService.findAllAdmin());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        return "admin/product-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductDto());
        }
        model.addAttribute("categories", categoryService.findAllAdmin());
        return "admin/product-add";
    }

    @PostMapping
    public String createProduct(
            @Valid @ModelAttribute("productForm") ProductDto form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllAdmin());
            return "admin/product-add";
        }
        try {
            productService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("categories", categoryService.findAllAdmin());
            result.reject("globalError", ex.getMessage());
            return "admin/product-add";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        if (!model.containsAttribute("productForm")) {
            ProductDto form = ProductDto.fromEntity(product);
            model.addAttribute("productForm", form);
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAllAdmin());
        return "admin/product-edit";
    }

    @PostMapping("/{id}/update")
    public String updateProduct(
            @PathVariable("id") int id,
            @Valid @ModelAttribute("productForm") ProductDto form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Product product = productService.findById(id).orElse(null);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAllAdmin());
            return "admin/product-edit";
        }
        try {
            productService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            Product product = productService.findById(id).orElse(null);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAllAdmin());
            result.reject("globalError", ex.getMessage());
            return "admin/product-edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products";
    }
}