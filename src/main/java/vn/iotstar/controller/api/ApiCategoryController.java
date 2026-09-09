package vn.iotstar.controller.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.dto.ApiResponse;
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;
import vn.iotstar.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class ApiCategoryController {

    private final CategoryService categoryService;

    public ApiCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<Page<CategoryDto>> getCategories(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "categoryId"));
        Page<Category> categoryPage = categoryService.searchAdmin(q, pageable);
        Page<CategoryDto> dtoPage = categoryPage.map(CategoryDto::fromEntity);
        return ApiResponse.success(dtoPage);
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryDto> getCategoryById(@PathVariable("id") Integer id) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));
        return ApiResponse.success(CategoryDto.fromEntity(category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto dto) {
        Category created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CategoryDto.fromEntity(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryDto> updateCategory(@PathVariable("id") Integer id,
                                                   @Valid @RequestBody CategoryDto dto) {
        Category updated = categoryService.update(id, dto);
        return ApiResponse.success(CategoryDto.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable("id") Integer id) {
        categoryService.delete(id);
        return ApiResponse.success("Xóa danh mục thành công", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundOrBadRequest(IllegalArgumentException ex) {
        int code = ex.getMessage().contains("Không tìm thấy") ? HttpStatus.NOT_FOUND.value() : HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(code).body(ApiResponse.error(code, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }
}