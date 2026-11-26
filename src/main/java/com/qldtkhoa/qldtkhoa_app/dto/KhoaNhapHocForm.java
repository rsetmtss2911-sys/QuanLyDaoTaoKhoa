package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KhoaNhapHocForm {

    @NotBlank(message = "{khoahoc.maKhoaHoc.required}")
    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maKhoaHoc;

    @NotNull(message = "{khoahoc.namBatDau.required}")
    @Min(value = 1990, message = "Năm bắt đầu không hợp lệ.")
    @Max(value = 2100, message = "Năm bắt đầu không hợp lệ.")
    private Integer namBatDau;
}
