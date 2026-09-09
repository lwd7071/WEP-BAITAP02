package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Category create(CategoryDto dto);
    Category update(Integer id, CategoryDto dto);
    void delete(Integer id);
    Optional<Category> findById(Integer id);
    Page<Category> searchAdmin(String keyword, Pageable pageable);
    List<Category> findAllActiveAdmin();
}