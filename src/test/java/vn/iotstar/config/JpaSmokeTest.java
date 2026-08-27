package vn.iotstar.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Video;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "RUN_JPA_SMOKE", matches = "(?i)true")
class JpaSmokeTest {
    @AfterAll
    static void closeFactory() {
        JpaConfig.close();
    }

    @Test
    void persistsCategoryAndVideoThenRollsBack() {
        EntityTransaction transaction = null;
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();

            Category category = new Category("Smoke " + UUID.randomUUID(), null, 1);
            Video video = new Video();
            video.setVideoId("smoke-" + UUID.randomUUID());
            video.setTitle("JPA smoke test");
            video.setActive(true);
            category.addVideo(video);
            entityManager.persist(category);
            entityManager.flush();
            entityManager.clear();

            Category loaded = entityManager.find(Category.class, category.getCategoryId());
            assertNotNull(loaded);
            assertEquals(1, loaded.getVideos().size());
            assertEquals(video.getVideoId(), loaded.getVideos().getFirst().getVideoId());

            transaction.rollback();
        } finally {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        }
    }
}
