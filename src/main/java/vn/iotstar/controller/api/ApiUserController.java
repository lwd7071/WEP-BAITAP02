package vn.iotstar.controller.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.dto.ApiResponse;
import vn.iotstar.dto.UserAdminCreateForm;
import vn.iotstar.dto.UserAdminUpdateForm;
import vn.iotstar.dto.UserDto;
import vn.iotstar.dto.UserStatusUpdateDto;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.service.UserService;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class ApiUserController {

    private final UserService userService;

    public ApiUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<Page<UserDto>> getUsers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) UserStatus status,
            @RequestParam(value = "provider", required = false) AuthProvider provider,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        Page<User> userPage = userService.search(q, status, provider, pageable);
        return ApiResponse.success(userPage.map(userService::toDto));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUserById(@PathVariable("id") int id) {
        User user = userService.findById(id);
        return ApiResponse.success(userService.toDto(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody UserAdminCreateForm form) {
        User created = userService.createByAdmin(form);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userService.toDto(created)));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDto> updateUser(
            @PathVariable("id") int id,
            @Valid @RequestBody UserAdminUpdateForm form) {
        User updated = userService.updateByAdmin(id, form);
        return ApiResponse.success(userService.toDto(updated));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserDto> updateStatus(
            @PathVariable("id") int id,
            @RequestBody UserStatusUpdateDto request) {
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Trạng thái không được để trống");
        }
        userService.updateStatus(id, request.getStatus());
        User user = userService.findById(id);
        return ApiResponse.success(userService.toDto(user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable("id") int id) {
        userService.delete(id);
        return ApiResponse.success("Xóa người dùng thành công", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        int code = ex.getMessage().contains("Không tìm thấy") ? HttpStatus.NOT_FOUND.value() : HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(code).body(ApiResponse.error(code, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
}