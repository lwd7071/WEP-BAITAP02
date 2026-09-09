package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.ProductDto;
import vn.iotstar.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Page<Product> searchPublic(String keyword, Integer categoryId, Pageable pageable);

    Page<Product> searchAdmin(String keyword, Integer categoryId, Integer status, Pageable pageable);

    List<Product> getLatestPublic(int limit);

    Optional<Product> findPublicById(int id);

    Optional<Product> findById(int id);

    Product create(ProductDto dto);

    Product update(int id, ProductDto dto);

    void delete(int id);
}