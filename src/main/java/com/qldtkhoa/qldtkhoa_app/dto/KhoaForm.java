package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KhoaForm {

    @NotBlank(message = "{khoa.maKhoa.required}")
    @Size(max = 20, message = "{khoa.maKhoa.size}")
    @Pattern(regexp = "[A-Za-z0-9_-]*", message = "{khoa.maKhoa.pattern}")
    private String maKhoa;

    @NotBlank(message = "{khoa.tenKhoa.required}")
    @Size(max = 100, message = "{khoa.tenKhoa.size}")
    private String tenKhoa;

    @Min(value = 1900, message = "{khoa.namThanhLap.range}")
    @Max(value = 2100, message = "{khoa.namThanhLap.range}")
    private Integer namThanhLap;
}
