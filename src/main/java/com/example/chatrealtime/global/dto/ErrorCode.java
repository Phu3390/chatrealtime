package com.example.chatrealtime.global.dto;


import lombok.Getter;

@Getter
public enum ErrorCode {

    UNKNOWN_ERROR(9999, "Lỗi không xác định", 503),
    IVALID_KEY(1000, "Lỗi do nhập sai key để validate", 400),
    NOT_EXITS(1001, "Không tìm thấy", 400),
    ACCOUNT_NOT_EXITS(1002, "Tài khoản không tồn tại", 400),
    PASSWORD_IVALID(1003, "Email hoặc mật khẩu không chính xác!", 400),
    UNAUTHORIZED(401, "Chưa đăng nhập", 401),
    FORBIDDEN(403, "Không có quyền truy cập", 403),

    NOT_FOUND(404, "Không tìm thấy endpoint", 404),
    EMAIL_PASSWORD_INVALID(1004, "Email hoặc mật khẩu không chính xác!", 400),
    INTERNAL_SERVER_ERROR(500, "Lỗi máy chủ", 500),

    METHOD_NOT_ALLOWED(405, "Phương thức này không tồn tại", 405),

    EMAIL_EXITS(1005, "Email đã tồn tại", 400),

    FRIEND_REQUEST_ALREADY_SENT(1006, "Yêu cầu kết bạn đã được gửi", 400),
    ALREADY_FRIENDS(1007, "Đã là bạn bè", 400),

    INVALID_REQUEST(1008, "Yêu cầu không hợp lệ", 400),
    NO_PERMISSION(1009, "Không có quyền thực hiện hành động này", 403),
    ALREADY_EXISTS(1010, "Đã tồn tại", 400),

    INVALID_STATUS(1011, "Trạng thái không hợp lệ", 400),

    NOT_FRIENDS(1012, "Không phải là bạn bè", 400),
    INVALID_TOKEN(1013, "Token không hợp lệ", 401),

    IVALID_SIZE_FILE(1014, "File vượt quá kích thước cho phép", 400),

    GOOGLE_EMAIL_NOT_VERIFIED(1015, "Email Google chưa được xác minh", 400),
    NOT_LOGIN_WITH_GOOGLE(1016, "Tài khoản đã tồn tại và không đăng nhập bằng Google", 400);
    

    private final int code;
    private final String message;
    private final int status;

    ErrorCode(int code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
