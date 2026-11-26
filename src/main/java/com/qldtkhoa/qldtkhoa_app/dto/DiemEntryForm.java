package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiemEntryForm {
    private String maSV;

    @DecimalMin(value = "0.0", message = "{diem.min}")
    @DecimalMax(value = "10.0", message = "{diem.max}")
    private Double diemQuaTrinh;

    @DecimalMin(value = "0.0", message = "{diem.min}")
    @DecimalMax(value = "10.0", message = "{diem.max}")
    private Double diemGiuaKy;

    @DecimalMin(value = "0.0", message = "{diem.min}")
    @DecimalMax(value = "10.0", message = "{diem.max}")
    private Double diemCuoiKy;

    private boolean camThi;

    @Size(max = 255, message = "{diem.lyDoCamThi.size}")
    private String lyDoCamThi;
}
