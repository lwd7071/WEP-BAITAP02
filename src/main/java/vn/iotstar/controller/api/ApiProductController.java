package vn.iotstar.controller.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.dto.ApiResponse;
import vn.iotstar.dto.ProductDto;
import vn.iotstar.entity.Product;
import vn.iotstar.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ApiProductController {

    private final ProductService productService;

    public ApiProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<?> getProducts(
            @RequestParam(value = "latest", required = false) Boolean latest,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication) {

        if (Boolean.TRUE.equals(latest)) {
            List<Product> latestProducts = productService.getLatestPublic(10);
            List<ProductDto> dtoList = latestProducts.stream().map(ProductDto::fromEntity).toList();
            return ApiResponse.success(dtoList);
        }

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdDate", "productId"));

        Page<Product> productPage;
        if (isAdmin) {
            productPage = productService.searchAdmin(null, null, null, pageable);
        } else {
            productPage = productService.searchPublic(null, null, pageable);
        }

        return ApiResponse.success(productPage.map(ProductDto::fromEntity));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getProductById(@PathVariable("id") int id, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        Product product;
        if (isAdmin) {
            product = productService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        } else {
            product = productService.findPublicById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        }

        return ApiResponse.success(ProductDto.fromEntity(product));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@Valid @RequestBody ProductDto dto) {
        Product created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ProductDto.fromEntity(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductDto> updateProduct(@PathVariable("id") int id, @Valid @RequestBody ProductDto dto) {
        Product updated = productService.update(id, dto);
        return ApiResponse.success(ProductDto.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable("id") int id) {
        productService.delete(id);
        return ApiResponse.success("Xóa sản phẩm thành công", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundOrBadRequest(IllegalArgumentException ex) {
        int code = ex.getMessage().contains("Không tìm thấy") ? HttpStatus.NOT_FOUND.value() : HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(code).body(ApiResponse.error(code, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
}