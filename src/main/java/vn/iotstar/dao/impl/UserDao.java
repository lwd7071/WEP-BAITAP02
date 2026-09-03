package vn.iotstar.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.entity.User;

public class UserDao implements IUserDao {
    @Override
    public void insert(User user) {
        EntityTransaction transaction = null;
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(user);
            transaction.commit();
        } catch (RuntimeException exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        }
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return entityManager.createQuery(
                            "SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)", User.class)
                    .setParameter("username", username.trim())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return exists("email", email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return exists("username", username);
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return exists("phone", phone);
    }

    @Override
    public boolean existsByPhoneAndNotId(String phone, int id) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            Long count = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.phone = :phone AND u.id != :id", Long.class)
                    .setParameter("phone", phone.trim())
                    .setParameter("id", id)
                    .getSingleResult();
            return count > 0;
        }
    }

    @Override
    public User updateProfile(int id, String fullName, String phone, String avatar) {
        EntityTransaction transaction = null;
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();
            User user = entityManager.find(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("Không tìm thấy người dùng");
            }
            user.setFullName(fullName);
            user.setPhone(phone);
            if (avatar != null && !avatar.isBlank()) {
                user.setAvatar(avatar);
            }
            transaction.commit();
            return user;
        } catch (RuntimeException exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        }
    }

    private boolean exists(String property, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            Long count = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE LOWER(u." + property + ") = LOWER(:value)", Long.class)
                    .setParameter("value", value.trim())
                    .getSingleResult();
            return count > 0;
        }
    }
}
