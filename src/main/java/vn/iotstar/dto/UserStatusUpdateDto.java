package vn.iotstar.dto;

import vn.iotstar.entity.UserStatus;

public class UserStatusUpdateDto {
    private UserStatus status;

    public UserStatusUpdateDto() {
    }

    public UserStatusUpdateDto(UserStatus status) {
        this.status = status;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}