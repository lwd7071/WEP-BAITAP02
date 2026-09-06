package vn.iotstar.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.User;

import java.util.List;

public class CategoryDao implements ICategoryDao {
    @Override
    public void insert(Category category, int ownerId) {
        executeWrite(entityManager -> {
            User owner = entityManager.find(User.class, ownerId);
            if (owner == null) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản sở hữu danh mục");
            }
            category.setOwner(owner);
            entityManager.persist(category);
        });
    }

    @Override
    public void update(Category category, int ownerId) {
        executeWrite(entityManager -> {
            Category current = findById(entityManager, category.getCategoryId(), ownerId);
            if (current == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục hoặc bạn không có quyền sửa");
            }
            current.setCategoryName(category.getCategoryName());
            current.setImages(category.getImages());
            current.setStatus(category.getStatus());
        });
    }

    @Override
    public void delete(int categoryId, int ownerId) {
        executeWrite(entityManager -> {
            Category category = findById(entityManager, categoryId, ownerId);
            if (category == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục hoặc bạn không có quyền xóa");
            }
            if (!category.getVideos().isEmpty()) {
                throw new IllegalStateException("Không thể xóa danh mục đang có video liên kết");
            }
            entityManager.remove(category);
        });
    }

    @Override
    public Category findById(int categoryId, int ownerId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return findById(entityManager, categoryId, ownerId);
        }
    }

    private Category findById(EntityManager entityManager, int categoryId, int ownerId) {
        return entityManager.createQuery(
                        "SELECT c FROM Category c WHERE c.categoryId = :categoryId AND c.owner.id = :ownerId",
                        Category.class)
                .setParameter("categoryId", categoryId)
                .setParameter("ownerId", ownerId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Category findByCategoryName(String name, int ownerId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT c FROM Category c WHERE LOWER(c.categoryName) = LOWER(:name) AND c.owner.id = :ownerId",
                            Category.class)
                    .setParameter("name", name.trim())
                    .setParameter("ownerId", ownerId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public List<Category> findAll(int ownerId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createNamedQuery("Category.findAllByOwner", Category.class)
                    .setParameter("ownerId", ownerId)
                    .getResultList();
        }
    }

    @Override
    public List<Category> searchByName(String keyword, int ownerId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT c FROM Category c WHERE c.owner.id = :ownerId "
                                    + "AND LOWER(c.categoryName) LIKE LOWER(:keyword) ORDER BY c.categoryId",
                            Category.class)
                    .setParameter("ownerId", ownerId)
                    .setParameter("keyword", "%" + keyword.trim() + "%")
                    .getResultList();
        }
    }

    @Override
    public List<Category> findAll(int ownerId, int page, int pageSize) {
        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Trang và kích thước trang không hợp lệ");
        }
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            TypedQuery<Category> query = entityManager.createNamedQuery("Category.findAllByOwner", Category.class);
            query.setParameter("ownerId", ownerId);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        }
    }

    @Override
    public int count(int ownerId) {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT COUNT(c) FROM Category c WHERE c.owner.id = :ownerId", Long.class)
                    .setParameter("ownerId", ownerId)
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
