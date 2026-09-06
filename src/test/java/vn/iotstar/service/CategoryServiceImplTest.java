package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.entity.Category;
import vn.iotstar.service.impl.CategoryServiceImpl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private ICategoryDao categoryDao;
    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryDao);
    }

    @Test
    void insertPersistsValidCategoryForOwner() {
        Category category = new Category(" Điện thoại ", null, 1);
        when(categoryDao.findByCategoryName("Điện thoại", 10)).thenReturn(null);

        service.insert(category, 10);

        verify(categoryDao).insert(category, 10);
    }

    @Test
    void insertRejectsDuplicateNameForSameOwner() {
        Category category = new Category("Điện thoại", null, 1);
        when(categoryDao.findByCategoryName("Điện thoại", 10)).thenReturn(new Category());

        assertThrows(IllegalArgumentException.class, () -> service.insert(category, 10));
        verify(categoryDao, never()).insert(category, 10);
    }

    @Test
    void sameNameCanBeUsedByDifferentOwners() {
        Category category = new Category("Điện thoại", null, 1);
        when(categoryDao.findByCategoryName("Điện thoại", 11)).thenReturn(null);

        service.insert(category, 11);

        verify(categoryDao).insert(category, 11);
    }

    @Test
    void updateRejectsCategoryOwnedByAnotherUser() {
        Category category = new Category("Thiết bị", null, 1);
        category.setCategoryId(99);
        when(categoryDao.findById(99, 10)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.update(category, 10));
        verify(categoryDao, never()).update(category, 10);
    }

    @Test
    void updateAllowsKeepingCurrentName() {
        Category category = new Category("Thiết bị", null, 1);
        category.setCategoryId(99);
        Category current = new Category("Thiết bị", null, 1);
        current.setCategoryId(99);
        when(categoryDao.findById(99, 10)).thenReturn(current);
        when(categoryDao.findByCategoryName("Thiết bị", 10)).thenReturn(current);

        service.update(category, 10);

        verify(categoryDao).update(category, 10);
    }

    @Test
    void invalidOwnerIsRejectedBeforeDaoWrite() {
        Category category = new Category("Thiết bị", null, 1);

        assertThrows(IllegalArgumentException.class, () -> service.insert(category, 0));

        verify(categoryDao, never()).insert(category, 0);
    }

    @Test
    void invalidStatusIsRejectedBeforeDaoWrite() {
        Category category = new Category("Thiết bị", null, 3);

        assertThrows(IllegalArgumentException.class, () -> service.insert(category, 10));

        verify(categoryDao, never()).insert(category, 10);
    }

    @Test
    void listSearchAndCountUseOwner() {
        service.findAll(10);
        service.searchByName("phone", 10);
        service.findAll(10, 0, 6);
        service.count(10);

        verify(categoryDao).findAll(10);
        verify(categoryDao).searchByName("phone", 10);
        verify(categoryDao).findAll(10, 0, 6);
        verify(categoryDao).count(10);
    }
}
