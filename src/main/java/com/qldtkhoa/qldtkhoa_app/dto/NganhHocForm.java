package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NganhHocForm {

    @NotBlank(message = "{nganh.maNganh.required}")
    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maNganh;

    @NotBlank(message = "{nganh.tenNganh.required}")
    @Size(max = 100)
    private String tenNganh;

    @NotBlank(message = "{nganh.khoa.required}")
    private String maKhoa;
}
