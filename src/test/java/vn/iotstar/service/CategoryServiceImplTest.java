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
    void insertPersistsValidUniqueCategory() {
        Category category = new Category(" Điện thoại ", null, 1);
        when(categoryDao.findByCategoryName("Điện thoại")).thenReturn(null);

        service.insert(category);

        verify(categoryDao).insert(category);
    }

    @Test
    void insertRejectsDuplicateName() {
        Category category = new Category("Điện thoại", null, 1);
        when(categoryDao.findByCategoryName("Điện thoại")).thenReturn(new Category());

        assertThrows(IllegalArgumentException.class, () -> service.insert(category));

        verify(categoryDao, never()).insert(category);
    }

    @Test
    void updateRejectsMissingCategory() {
        Category category = new Category("Thiết bị", null, 1);
        category.setCategoryId(99);
        when(categoryDao.findById(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.update(category));

        verify(categoryDao, never()).update(category);
    }

    @Test
    void invalidStatusIsRejectedBeforeDaoWrite() {
        Category category = new Category("Thiết bị", null, 3);

        assertThrows(IllegalArgumentException.class, () -> service.insert(category));

        verify(categoryDao, never()).insert(category);
    }
}
