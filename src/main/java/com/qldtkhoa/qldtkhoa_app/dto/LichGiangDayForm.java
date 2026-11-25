package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LichGiangDayForm {

    @NotNull(message = "{lich.thu.required}")
    @Min(value = 2, message = "{lich.thu.min}")
    @Max(value = 8, message = "{lich.thu.max}")
    private Integer thuTrongTuan = 2; // 2 -> 7 (Thứ 2 đến Thứ 7), 8 (Chủ nhật)

    @NotNull(message = "{lich.tietBatDau.required}")
    @Min(value = 1, message = "{lich.tietBatDau.min}")
    @Max(value = 15, message = "{lich.tietBatDau.max}")
    private Integer tietBatDau = 1;

    @NotNull(message = "{lich.tietKetThuc.required}")
    @Min(value = 1, message = "{lich.tietKetThuc.min}")
    @Max(value = 15, message = "{lich.tietKetThuc.max}")
    private Integer tietKetThuc = 3;

    @NotBlank(message = "{lich.phongHoc.required}")
    @Size(max = 50, message = "{lich.phongHoc.size}")
    private String phongHoc = "P301-G2";
}
