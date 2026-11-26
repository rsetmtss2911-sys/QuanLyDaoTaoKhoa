package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class SinhVienForm {

    @NotBlank(message = "{sinhvien.maSV.required}")
    @Size(max = 20, message = "{sinhvien.maSV.size}")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maSV;

    @NotBlank(message = "{sinhvien.hoTen.required}")
    @Size(max = 100, message = "{sinhvien.hoTen.size}")
    private String hoTen;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Past(message = "{sinhvien.ngaySinh.past}")
    private LocalDate ngaySinh;

    private GioiTinh gioiTinh = GioiTinh.NAM;

    @NotBlank(message = "{sinhvien.lopHanhChinh.required}")
    private String maLopHC;

    @NotBlank(message = "{sinhvien.email.required}")
    @Email(message = "{sinhvien.email.invalid}")
    private String email;

    private String matKhau;
}
