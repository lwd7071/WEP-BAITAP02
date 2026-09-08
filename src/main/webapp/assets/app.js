/**
 * JPA Store - Micro-Interactions & UI Scripts
 */
document.addEventListener('DOMContentLoaded', () => {
    // 1. Khởi tạo Toast Container nếu chưa có
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }

    // 2. Hàm toàn cục hiển thị Toast Notification
    window.showToast = function (message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `toast-item ${type}`;
        
        const icon = type === 'success' ? '✓' : (type === 'error' ? '✕' : 'ℹ');
        toast.innerHTML = `
            <span class="toast-icon">${icon}</span>
            <span class="toast-message">${message}</span>
        `;
        
        toastContainer.appendChild(toast);

        // Tự động gỡ sau 2.8s
        setTimeout(() => {
            toast.classList.add('toast-hide');
            toast.addEventListener('animationend', () => {
                toast.remove();
            });
        }, 2800);
    };

    // 3. Xử lý Cart Badge Counter & Bounce
    let cartCount = parseInt(localStorage.getItem('jpa_cart_count') || '0', 10);
    const cartBadge = document.getElementById('cart-badge');
    const updateCartBadge = () => {
        if (!cartBadge) return;
        cartBadge.textContent = cartCount;
        cartBadge.classList.remove('badge-bounce');
        void cartBadge.offsetWidth; // Trigger reflow
        cartBadge.classList.add('badge-bounce');
        localStorage.setItem('jpa_cart_count', cartCount);
    };
    if (cartBadge && cartCount > 0) {
        cartBadge.textContent = cartCount;
    }

    // 4. Xử lý sự kiện "Thêm vào giỏ hàng" (Add to Cart Feedback)
    document.querySelectorAll('[data-action="add-to-cart"]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();

            if (btn.classList.contains('is-adding')) return;
            btn.classList.add('is-adding');
            const originalHtml = btn.innerHTML;

            // Chuyển sang trạng thái "Đang thêm..."
            btn.innerHTML = `
                <span class="spinner-mini"></span>
                <span>Đang thêm...</span>
            `;

            setTimeout(() => {
                // Chuyển sang trạng thái "✓ Đã thêm"
                btn.innerHTML = `<span>✓ Đã thêm</span>`;
                btn.classList.add('is-added');

                cartCount += 1;
                updateCartBadge();

                const productName = btn.getAttribute('data-product-name') || 'Sản phẩm';
                window.showToast(`Đã thêm "${productName}" vào giỏ hàng!`, 'success');

                setTimeout(() => {
                    btn.innerHTML = originalHtml;
                    btn.classList.remove('is-adding', 'is-added');
                }, 1600);
            }, 550);
        });
    });

    // 4.1 Xử lý sự kiện "Mua ngay"
    document.querySelectorAll('[data-action="buy-now"]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const productName = btn.getAttribute('data-product-name') || 'Sản phẩm';
            window.showToast(`Đang tiến hành đặt hàng nhanh cho "${productName}"...`, 'success');
        });
    });

    // 5. Xử lý Wishlist Heart Pop ❤️
    document.querySelectorAll('[data-action="wishlist"]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            
            btn.classList.toggle('active');
            btn.classList.add('heart-pop');
            
            const isLiked = btn.classList.contains('active');
            btn.innerHTML = isLiked ? '♥' : '♡';

            window.showToast(
                isLiked ? 'Đã thêm vào danh sách yêu thích!' : 'Đã bỏ khỏi danh sách yêu thích.',
                isLiked ? 'success' : 'info'
            );

            setTimeout(() => {
                btn.classList.remove('heart-pop');
            }, 450);
        });
    });

    // 6. Xử lý Bộ chọn số lượng (Quantity Stepper: [-] 1 [+])
    const qtyWrappers = document.querySelectorAll('.quantity-stepper');
    qtyWrappers.forEach(wrap => {
        const input = wrap.querySelector('.qty-input');
        const btnMinus = wrap.querySelector('.btn-qty-minus');
        const btnPlus = wrap.querySelector('.btn-qty-plus');

        if (!input || !btnMinus || !btnPlus) return;

        btnMinus.addEventListener('click', (e) => {
            e.preventDefault();
            let current = parseInt(input.value || '1', 10);
            if (current > 1) {
                input.value = current - 1;
                input.classList.add('num-bump');
                setTimeout(() => input.classList.remove('num-bump'), 200);
            }
        });

        btnPlus.addEventListener('click', (e) => {
            e.preventDefault();
            let current = parseInt(input.value || '1', 10);
            const max = parseInt(input.getAttribute('max') || '999', 10);
            if (current < max) {
                input.value = current + 1;
                input.classList.add('num-bump');
                setTimeout(() => input.classList.remove('num-bump'), 200);
            }
        });
    });

    // 7. Cart Drawer Toggle
    const cartTrigger = document.getElementById('cart-trigger');
    const cartDrawer = document.getElementById('cart-drawer');
    const drawerOverlay = document.getElementById('drawer-overlay');
    const drawerClose = document.getElementById('drawer-close');

    const openCart = () => {
        if (cartDrawer && drawerOverlay) {
            cartDrawer.classList.add('open');
            drawerOverlay.classList.add('open');
            document.body.style.overflow = 'hidden';
        } else {
            window.showToast(`Bạn đang có ${cartCount} sản phẩm trong giỏ hàng!`, 'info');
        }
    };

    const closeCart = () => {
        if (cartDrawer && drawerOverlay) {
            cartDrawer.classList.remove('open');
            drawerOverlay.classList.remove('open');
            document.body.style.overflow = '';
        }
    };

    if (cartTrigger) cartTrigger.addEventListener('click', openCart);
    if (drawerClose) drawerClose.addEventListener('click', closeCart);
    if (drawerOverlay) drawerOverlay.addEventListener('click', closeCart);

    // 8. Chuyển đổi danh mục trực tiếp trên trang Home
    const categoryTabs = document.querySelectorAll('#home-category-tabs .category-pill');
    const productGrid = document.getElementById('home-product-grid');
    const sectionTitle = document.getElementById('section-title');
    const emptyCategoryMsg = document.getElementById('empty-category-msg');

    if (categoryTabs.length > 0 && productGrid) {
        categoryTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                e.preventDefault();
                categoryTabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');

                const selectedCatId = tab.getAttribute('data-category-id');
                const catName = tab.getAttribute('data-category-name') || 'Tất cả sản phẩm';
                if (sectionTitle) {
                    sectionTitle.textContent = selectedCatId === 'all' ? 'Tất cả sản phẩm' : `Sản phẩm: ${catName}`;
                }

                const cards = productGrid.querySelectorAll('.product-card');
                let visibleCount = 0;
                cards.forEach(card => {
                    const cardCatId = card.getAttribute('data-category-id');
                    if (selectedCatId === 'all' || cardCatId === selectedCatId) {
                        card.style.display = '';
                        card.classList.remove('card-fade-in');
                        void card.offsetWidth;
                        card.classList.add('card-fade-in');
                        visibleCount++;
                    } else {
                        card.style.display = 'none';
                    }
                });

                if (emptyCategoryMsg) {
                    emptyCategoryMsg.style.display = visibleCount === 0 ? 'block' : 'none';
                }
            });
        });
    }
});
