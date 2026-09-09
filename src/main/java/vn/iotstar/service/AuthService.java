package vn.iotstar.service;

import vn.iotstar.dto.RegisterRequestDto;
import vn.iotstar.entity.User;

public interface AuthService {
    User register(RegisterRequestDto form);
    boolean activate(String email, String otp);
    boolean resendActivationOtp(String email);
    Integer requestPasswordReset(String username);
    boolean resetPassword(Integer userId, String otp, String newPassword);
}