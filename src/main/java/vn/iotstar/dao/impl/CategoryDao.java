package vn.iotstar.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.entity.Category;

import java.util.List;

public class CategoryDao implements ICategoryDao {
    @Override
    public void insert(Category category) {
        executeWrite(entityManager -> entityManager.persist(category));
    }

    @Override
    public void update(Category category) {
        executeWrite(entityManager -> entityManager.merge(category));
    }

    @Override
    public void delete(int categoryId) {
        executeWrite(entityManager -> {
            Category category = entityManager.find(Category.class, categoryId);
            if (category == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục có id " + categoryId);
            }
            if (!category.getVideos().isEmpty()) {
                throw new IllegalStateException("Không thể xóa danh mục đang có video liên kết");
            }
            entityManager.remove(category);
        });
    }

    @Override
    public Category findById(int categoryId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.find(Category.class, categoryId);
        }
    }

    @Override
    public Category findByCategoryName(String name) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT c FROM Category c WHERE LOWER(c.categoryName) = LOWER(:name)", Category.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public List<Category> findAll() {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createNamedQuery("Category.findAll", Category.class).getResultList();
        }
    }

    @Override
    public List<Category> searchByName(String keyword) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(:keyword) " +
                                    "ORDER BY c.categoryId", Category.class)
                    .setParameter("keyword", "%" + keyword.trim() + "%")
                    .getResultList();
        }
    }

    @Override
    public List<Category> findAll(int page, int pageSize) {
        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Trang và kích thước trang không hợp lệ");
        }
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            TypedQuery<Category> query = entityManager.createNamedQuery("Category.findAll", Category.class);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        }
    }

    @Override
    public int count() {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery("SELECT COUNT(c) FROM Category c", Long.class)
                    .getSingleResult()
                    .intValue();
        }
    }

    private void executeWrite(EntityManagerAction action) {
        EntityTransaction transaction = null;
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();
            action.accept(entityManager);
            transaction.commit();
        } catch (RuntimeException exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        }
    }

    @FunctionalInterface
    private interface EntityManagerAction {
        void accept(EntityManager entityManager);
    }
}
