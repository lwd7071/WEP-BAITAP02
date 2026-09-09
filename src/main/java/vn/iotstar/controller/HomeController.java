package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "Trang chủ mua sắm");
        model.addAttribute("pageDescription", "Trải nghiệm mua sắm trực tuyến cao cấp.");
        model.addAttribute("categories", categoryService.findAllActiveAdmin());
        model.addAttribute("latestProducts", productService.getLatestPublic(24));
        return "home";
    }
}