package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {

    @EntityGraph(attributePaths = "owner")
    Optional<Category> findByCategoryId(Integer id);

    Optional<Category> findByCategoryNameIgnoreCaseAndOwnerRole(String name, Role role);

    boolean existsByCategoryNameIgnoreCaseAndOwnerRole(String name, Role role);

    boolean existsByCategoryNameIgnoreCaseAndOwnerRoleAndCategoryIdNot(String name, Role role, Integer id);

    @EntityGraph(attributePaths = "owner")
    Page<Category> findByOwnerRoleAndCategoryNameContainingIgnoreCase(Role role, String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Page<Category> findByOwnerRole(Role role, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    List<Category> findByOwnerRoleAndStatusOrderByCategoryNameAsc(Role role, int status);

    @EntityGraph(attributePaths = "owner")
    List<Category> findByOwnerRoleOrderByCategoryNameAsc(Role role);

    @Override
    @EntityGraph(attributePaths = "owner")
    Optional<Category> findById(Integer id);
}
