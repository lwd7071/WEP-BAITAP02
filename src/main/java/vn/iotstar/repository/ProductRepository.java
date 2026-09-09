package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p JOIN p.category c " +
           "WHERE p.status = 1 AND c.status = 1 AND c.owner.role = vn.iotstar.entity.Role.ADMIN " +
           "AND (:categoryId IS NULL OR c.categoryId = :categoryId) " +
           "AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findPublicProducts(@Param("keyword") String keyword,
                                     @Param("categoryId") Integer categoryId,
                                     Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.category c " +
           "WHERE p.productId = :id AND p.status = 1 AND c.status = 1 AND c.owner.role = vn.iotstar.entity.Role.ADMIN")
    Optional<Product> findPublicById(@Param("id") Integer id);

    @Query("SELECT p FROM Product p JOIN p.category c " +
           "WHERE c.owner.role = vn.iotstar.entity.Role.ADMIN " +
           "AND (:categoryId IS NULL OR c.categoryId = :categoryId) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findAdminProducts(@Param("keyword") String keyword,
                                    @Param("categoryId") Integer categoryId,
                                    @Param("status") Integer status,
                                    Pageable pageable);

    long countByCategory_CategoryId(Integer categoryId);

    @Query("SELECT p FROM Product p JOIN p.category c " +
           "WHERE p.status = 1 AND c.status = 1 AND c.owner.role = vn.iotstar.entity.Role.ADMIN " +
           "ORDER BY p.createdDate DESC, p.productId DESC")
    List<Product> findLatestPublic(Pageable pageable);
}