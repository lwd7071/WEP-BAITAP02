package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.dao.IProductDao;
import vn.iotstar.entity.Product;
import vn.iotstar.service.impl.ProductServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock private IProductDao productDao;
    private ProductServiceImpl service;

    @BeforeEach void setUp() { service = new ProductServiceImpl(productDao); }

    @Test
    void insertTrimsAndPersistsValidProductForOwner() {
        Product product = validProduct();
        product.setProductName("  Điện thoại  ");
        service.insert(product, 4, 10);
        assertEquals("Điện thoại", product.getProductName());
        verify(productDao).insert(product, 4, 10);
    }

    @Test
    void insertRejectsInvalidCategoryBeforeWrite() {
        Product product = validProduct();
        assertThrows(IllegalArgumentException.class, () -> service.insert(product, 0, 10));
        verify(productDao, never()).insert(product, 0, 10);
    }

    @Test
    void insertRejectsNegativePriceAndQuantity() {
        Product negativePrice = validProduct();
        negativePrice.setUnitPrice(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class, () -> service.insert(negativePrice, 4, 10));
        Product negativeQuantity = validProduct();
        negativeQuantity.setQuantity(-1);
        assertThrows(IllegalArgumentException.class, () -> service.insert(negativeQuantity, 4, 10));
    }

    @Test
    void catalogOperationsAlwaysUseOwnerAndPaging() {
        service.findActive(10, 1, 6);
        service.countActive(10);
        service.findLatestActive(10, 10);
        verify(productDao).findActive(10, 1, 6);
        verify(productDao).countActive(10);
        verify(productDao).findLatestActive(10, 10);
    }

    @Test
    void invalidStatusDoesNotReachDao() {
        Product product = validProduct();
        product.setStatus(2);
        assertThrows(IllegalArgumentException.class, () -> service.insert(product, 4, 10));
        verify(productDao, never()).insert(product, 4, 10);
    }

    private Product validProduct() {
        Product product = new Product();
        product.setProductName("Sản phẩm");
        product.setUnitPrice(new BigDecimal("100000"));
        product.setQuantity(2);
        product.setStatus(1);
        return product;
    }
}
