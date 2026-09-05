package vn.iotstar.service;

import vn.iotstar.entity.User;

public interface IUserService {
    User login(String username, String password);
    User findByUsername(String username);
    boolean register(String email, String password, String username, String fullName, String phone);
    boolean checkExistEmail(String email);
    boolean checkExistUsername(String username);
    boolean checkExistPhone(String phone);
    boolean checkExistPhoneForUser(String phone, int userId);
    User updateProfile(int userId, String fullName, String phone, String avatar);
    User findByEmail(String email);
    boolean activateUser(String email, String otp);
    boolean resendRegistrationOtp(String email);
    boolean sendForgotPasswordOtp(String emailOrUsername);
    boolean resetPassword(String email, String otp, String newPassword);
}
