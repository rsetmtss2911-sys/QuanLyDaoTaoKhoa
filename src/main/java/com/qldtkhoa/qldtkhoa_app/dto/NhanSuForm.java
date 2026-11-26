package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhanSuForm {

    @NotBlank(message = "{nhansu.maNS.required}")
    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maNS;

    @NotBlank(message = "{nhansu.hoTen.required}")
    @Size(max = 100)
    private String hoTen;

    @NotBlank(message = "{nhansu.email.required}")
    @Email(message = "{nhansu.email.invalid}")
    @Size(max = 100)
    private String email;

    private String matKhau;

    @NotBlank(message = "{nhansu.vaiTro.required}")
    private String maVaiTro;

    // Các trường dành riêng cho Giảng viên (nếu maVaiTro == "GIANG_VIEN")
    private String hocHam;
    private String hocVi;
    private String maBoMon;
}
