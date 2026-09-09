const registrationForm = document.getElementById('registration-form');
registrationForm?.addEventListener('submit', () => {
    const button = registrationForm.querySelector('button[type="submit"]');
    button.disabled = true;
    button.textContent = 'Đang gửi OTP…';
});
window.addEventListener('pageshow', () => {
    const button = registrationForm?.querySelector('button[type="submit"]');
    if (button) {
        button.disabled = false;
        button.textContent = 'Tạo tài khoản';
    }
});
