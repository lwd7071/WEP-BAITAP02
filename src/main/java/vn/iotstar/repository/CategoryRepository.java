package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByCategoryId(Integer id);

    Optional<Category> findByCategoryNameIgnoreCaseAndOwnerRole(String name, Role role);

    boolean existsByCategoryNameIgnoreCaseAndOwnerRole(String name, Role role);

    boolean existsByCategoryNameIgnoreCaseAndOwnerRoleAndCategoryIdNot(String name, Role role, Integer id);

    Page<Category> findByOwnerRoleAndCategoryNameContainingIgnoreCase(Role role, String keyword, Pageable pageable);

    Page<Category> findByOwnerRole(Role role, Pageable pageable);

    List<Category> findByOwnerRoleAndStatusOrderByCategoryNameAsc(Role role, int status);
}