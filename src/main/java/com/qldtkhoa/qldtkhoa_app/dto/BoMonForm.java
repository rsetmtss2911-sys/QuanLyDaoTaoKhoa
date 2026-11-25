package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoMonForm {

    @NotBlank(message = "{bomon.maBoMon.required}")
    @Size(max = 20)
    private String maBoMon;

    @NotBlank(message = "{bomon.tenBoMon.required}")
    @Size(max = 100)
    private String tenBoMon;

    @NotBlank(message = "{bomon.khoa.required}")
    private String maKhoa;
}
