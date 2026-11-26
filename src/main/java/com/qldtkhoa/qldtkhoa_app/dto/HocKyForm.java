package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class HocKyForm {

    @NotBlank(message = "{hocky.maHK.required}")
    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maHK;

    @NotBlank(message = "{hocky.tenHK.required}")
    @Size(max = 50)
    private String tenHK;

    @NotNull(message = "{hocky.namHoc.required}")
    private Integer namHoc;

    @NotNull(message = "{hocky.ngayBatDau.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayBatDau;

    @NotNull(message = "{hocky.ngayKetThuc.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayKetThuc;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayMoDangKy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayDongDangKy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hanRutHocPhan;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hanKhoaDiem;
}
