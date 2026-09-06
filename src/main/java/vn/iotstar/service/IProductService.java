package vn.iotstar.service;

import vn.iotstar.entity.Product;

import java.util.List;

public interface IProductService {
    void insert(Product product, int categoryId, int ownerId);
    void update(Product product, int categoryId, int ownerId);
    void delete(int productId, int ownerId);
    Product findById(int productId, int ownerId);
    Product findActiveById(int productId, int ownerId);
    List<Product> findAll(int ownerId, int page, int pageSize);
    int count(int ownerId);
    List<Product> findActive(int ownerId, int page, int pageSize);
    int countActive(int ownerId);
    List<Product> findLatestActive(int ownerId, int limit);
}
