package vn.iotstar.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import vn.iotstar.entity.Product;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
public class PublicProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public PublicProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {
        Page<Product> productPage = productService.searchPublic(q, categoryId, PageRequest.of(Math.max(0, page), Math.max(1, size)));
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categoryService.findAllActiveAdmin());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        return "product-list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") int id, Model model) {
        Product product = productService.findPublicById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại hoặc đã ngừng kinh doanh"));
        model.addAttribute("product", product);
        return "product-detail";
    }
}