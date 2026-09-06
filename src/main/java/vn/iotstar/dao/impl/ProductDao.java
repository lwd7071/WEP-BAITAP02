package vn.iotstar.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.IProductDao;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;

import java.util.List;

public class ProductDao implements IProductDao {
    @Override
    public void insert(Product product, int categoryId, int ownerId) {
        executeWrite(entityManager -> {
            Category category = findOwnedCategory(entityManager, categoryId, ownerId);
            if (category == null) throw new IllegalArgumentException("Danh mục không hợp lệ hoặc không thuộc tài khoản của bạn");
            product.setCategory(category);
            entityManager.persist(product);
        });
    }

    @Override
    public void update(Product product, int categoryId, int ownerId) {
        executeWrite(entityManager -> {
            Product current = findById(entityManager, product.getProductId(), ownerId, false);
            Category category = findOwnedCategory(entityManager, categoryId, ownerId);
            if (current == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm hoặc bạn không có quyền sửa");
            if (category == null) throw new IllegalArgumentException("Danh mục không hợp lệ hoặc không thuộc tài khoản của bạn");
            current.setProductName(product.getProductName());
            current.setUnitPrice(product.getUnitPrice());
            current.setQuantity(product.getQuantity());
            current.setDescription(product.getDescription());
            current.setImages(product.getImages());
            current.setStatus(product.getStatus());
            current.setCategory(category);
        });
    }

    @Override
    public void delete(int productId, int ownerId) {
        executeWrite(entityManager -> {
            Product product = findById(entityManager, productId, ownerId, false);
            if (product == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm hoặc bạn không có quyền xóa");
            entityManager.remove(product);
        });
    }

    @Override
    public Product findById(int productId, int ownerId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return findById(entityManager, productId, ownerId, false);
        }
    }

    @Override
    public Product findActiveById(int productId, int ownerId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return findById(entityManager, productId, ownerId, true);
        }
    }

    private Product findById(EntityManager entityManager, int productId, int ownerId, boolean activeOnly) {
        String active = activeOnly ? " AND p.status = 1" : "";
        return entityManager.createQuery(
                        "SELECT p FROM Product p JOIN FETCH p.category c WHERE p.productId = :productId "
                                + "AND c.owner.id = :ownerId" + active, Product.class)
                .setParameter("productId", productId)
                .setParameter("ownerId", ownerId)
                .getResultStream().findFirst().orElse(null);
    }

    @Override
    public List<Product> findAll(int ownerId, int page, int pageSize) {
        return findPage(ownerId, page, pageSize, false);
    }

    @Override
    public int count(int ownerId) { return count(ownerId, false); }

    @Override
    public List<Product> findActive(int ownerId, int page, int pageSize) {
        return findPage(ownerId, page, pageSize, true);
    }

    @Override
    public int countActive(int ownerId) { return count(ownerId, true); }

    @Override
    public List<Product> findLatestActive(int ownerId, int limit) {
        if (limit <= 0) throw new IllegalArgumentException("Giới hạn sản phẩm không hợp lệ");
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT p FROM Product p JOIN FETCH p.category c WHERE c.owner.id = :ownerId "
                                    + "AND p.status = 1 ORDER BY p.createdDate DESC, p.productId DESC", Product.class)
                    .setParameter("ownerId", ownerId).setMaxResults(limit).getResultList();
        }
    }

    private List<Product> findPage(int ownerId, int page, int pageSize, boolean activeOnly) {
        if (page < 0 || pageSize <= 0) throw new IllegalArgumentException("Trang và kích thước trang không hợp lệ");
        String active = activeOnly ? " AND p.status = 1" : "";
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT p FROM Product p JOIN FETCH p.category c WHERE c.owner.id = :ownerId" + active
                                    + " ORDER BY p.createdDate DESC, p.productId DESC", Product.class)
                    .setParameter("ownerId", ownerId).setFirstResult(page * pageSize)
                    .setMaxResults(pageSize).getResultList();
        }
    }

    private int count(int ownerId, boolean activeOnly) {
        String active = activeOnly ? " AND p.status = 1" : "";
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT COUNT(p) FROM Product p WHERE p.category.owner.id = :ownerId" + active, Long.class)
                    .setParameter("ownerId", ownerId).getSingleResult().intValue();
        }
    }

    private Category findOwnedCategory(EntityManager entityManager, int categoryId, int ownerId) {
        return entityManager.createQuery(
                        "SELECT c FROM Category c WHERE c.categoryId = :categoryId AND c.owner.id = :ownerId", Category.class)
                .setParameter("categoryId", categoryId).setParameter("ownerId", ownerId)
                .getResultStream().findFirst().orElse(null);
    }

    private void executeWrite(EntityManagerAction action) {
        EntityTransaction transaction = null;
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();
            action.accept(entityManager);
            transaction.commit();
        } catch (RuntimeException exception) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw exception;
        }
    }

    @FunctionalInterface
    private interface EntityManagerAction { void accept(EntityManager entityManager); }
}
