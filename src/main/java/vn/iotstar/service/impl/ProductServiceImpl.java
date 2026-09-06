package vn.iotstar.service.impl;

import vn.iotstar.dao.IProductDao;
import vn.iotstar.dao.impl.ProductDao;
import vn.iotstar.entity.Product;
import vn.iotstar.service.IProductService;

import java.util.List;

public class ProductServiceImpl implements IProductService {
    private final IProductDao productDao;

    public ProductServiceImpl() { this(new ProductDao()); }
    public ProductServiceImpl(IProductDao productDao) { this.productDao = productDao; }

    @Override
    public void insert(Product product, int categoryId, int ownerId) {
        validateOwnerAndCategory(ownerId, categoryId);
        validate(product);
        productDao.insert(product, categoryId, ownerId);
    }

    @Override
    public void update(Product product, int categoryId, int ownerId) {
        validateOwnerAndCategory(ownerId, categoryId);
        validate(product);
        if (product.getProductId() <= 0) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        productDao.update(product, categoryId, ownerId);
    }

    @Override public void delete(int productId, int ownerId) { validateIds(productId, ownerId); productDao.delete(productId, ownerId); }
    @Override public Product findById(int productId, int ownerId) { validateIds(productId, ownerId); return productDao.findById(productId, ownerId); }
    @Override public Product findActiveById(int productId, int ownerId) { validateIds(productId, ownerId); return productDao.findActiveById(productId, ownerId); }
    @Override public List<Product> findAll(int ownerId, int page, int pageSize) { validatePage(ownerId, page, pageSize); return productDao.findAll(ownerId, page, pageSize); }
    @Override public int count(int ownerId) { validateOwner(ownerId); return productDao.count(ownerId); }
    @Override public List<Product> findActive(int ownerId, int page, int pageSize) { validatePage(ownerId, page, pageSize); return productDao.findActive(ownerId, page, pageSize); }
    @Override public int countActive(int ownerId) { validateOwner(ownerId); return productDao.countActive(ownerId); }
    @Override public List<Product> findLatestActive(int ownerId, int limit) { validateOwner(ownerId); if (limit <= 0) throw new IllegalArgumentException("Giới hạn sản phẩm không hợp lệ"); return productDao.findLatestActive(ownerId, limit); }

    private void validate(Product product) {
        if (product == null || product.getProductName() == null || product.getProductName().isBlank())
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        product.setProductName(product.getProductName().trim());
        if (product.getUnitPrice() == null || product.getUnitPrice().signum() < 0)
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn hoặc bằng 0");
        if (product.getQuantity() < 0) throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 0");
        if (product.getStatus() != 0 && product.getStatus() != 1)
            throw new IllegalArgumentException("Trạng thái sản phẩm không hợp lệ");
        if (product.getDescription() != null) product.setDescription(product.getDescription().trim());
    }

    private void validateOwnerAndCategory(int ownerId, int categoryId) { validateOwner(ownerId); if (categoryId <= 0) throw new IllegalArgumentException("Vui lòng chọn danh mục"); }
    private void validateIds(int productId, int ownerId) { validateOwner(ownerId); if (productId <= 0) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ"); }
    private void validateOwner(int ownerId) { if (ownerId <= 0) throw new IllegalArgumentException("Tài khoản sở hữu sản phẩm không hợp lệ"); }
    private void validatePage(int ownerId, int page, int pageSize) { validateOwner(ownerId); if (page < 0 || pageSize <= 0) throw new IllegalArgumentException("Trang và kích thước trang không hợp lệ"); }
}
