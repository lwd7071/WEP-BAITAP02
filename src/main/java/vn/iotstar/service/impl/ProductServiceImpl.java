package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dao.IProductDao;
import vn.iotstar.dao.impl.ProductDao;
import vn.iotstar.dto.ProductDto;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.ProductService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService, IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IProductDao productDao;

    public ProductServiceImpl() {
        this(new ProductDao());
    }

    public ProductServiceImpl(IProductDao productDao) {
        this.productRepository = null;
        this.categoryRepository = null;
        this.productDao = productDao;
    }

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productDao = new ProductDao();
    }

    // ==========================================
    // Spring Boot ProductService Implementation
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchPublic(String keyword, Integer categoryId, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Pageable sortedPageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdDate", "productId")
            );
        }
        return productRepository.findPublicProducts(cleanKeyword, categoryId, sortedPageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchAdmin(String keyword, Integer categoryId, Integer status, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Pageable sortedPageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdDate", "productId")
            );
        }
        return productRepository.findAdminProducts(cleanKeyword, categoryId, status, sortedPageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLatestPublic(int limit) {
        int max = Math.max(1, limit);
        Pageable pageable = PageRequest.of(0, max, Sort.by(Sort.Direction.DESC, "createdDate", "productId"));
        return productRepository.findLatestPublic(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findPublicById(int id) {
        return productRepository.findPublicById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(int id) {
        return productRepository.findById(id);
    }

    @Override
    public Product create(ProductDto dto) {
        validateDto(dto);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));

        Product product = new Product();
        product.setProductName(dto.getProductName().trim());
        product.setUnitPrice(dto.getUnitPrice());
        product.setQuantity(dto.getQuantity());
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        product.setImages(dto.getImages());
        product.setStatus(dto.getStatus());
        product.setCreatedDate(LocalDateTime.now());
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Override
    public Product update(int id, ProductDto dto) {
        validateDto(dto);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));

        product.setProductName(dto.getProductName().trim());
        product.setUnitPrice(dto.getUnitPrice());
        product.setQuantity(dto.getQuantity());
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        product.setImages(dto.getImages());
        product.setStatus(dto.getStatus());
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Override
    public void delete(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        productRepository.delete(product);
    }

    private void validateDto(ProductDto dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu sản phẩm không được để trống");
        if (dto.getProductName() == null || dto.getProductName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (dto.getUnitPrice() == null || dto.getUnitPrice().signum() < 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn hoặc bằng 0");
        }
        if (dto.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 0");
        }
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new IllegalArgumentException("Trạng thái sản phẩm không hợp lệ (0 hoặc 1)");
        }
        if (dto.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục hợp lệ");
        }
    }

    // ==========================================
    // Legacy IProductService Implementation
    // ==========================================

    @Override
    public void insert(Product product, int categoryId, int ownerId) {
        validateOwnerAndCategory(ownerId, categoryId);
        validateLegacy(product);
        productDao.insert(product, categoryId, ownerId);
    }

    @Override
    public void update(Product product, int categoryId, int ownerId) {
        validateOwnerAndCategory(ownerId, categoryId);
        validateLegacy(product);
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

    private void validateLegacy(Product product) {
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