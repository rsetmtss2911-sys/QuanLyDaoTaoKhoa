package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordForm {

    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại.")
    private String matKhauCu;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới.")
    @Size(min = 8, max = 72, message = "{auth.password.size}")
    private String matKhauMoi;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới.")
    private String xacNhanMatKhau;
}
