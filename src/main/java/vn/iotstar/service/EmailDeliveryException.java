package vn.iotstar.service;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(Throwable cause) {
        super("Không thể gửi email xác thực. Vui lòng thử lại sau", cause);
    }
}
