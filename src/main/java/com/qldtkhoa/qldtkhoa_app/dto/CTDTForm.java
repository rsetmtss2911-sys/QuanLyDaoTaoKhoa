package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CTDTForm {

    @NotBlank(message = "{ctdt.nganhHoc.required}")
    private String maNganh;

    @NotBlank(message = "{ctdt.khoaNhapHoc.required}")
    private String maKhoaHoc;

    @NotBlank(message = "{ctdt.monHoc.required}")
    private String maMon;

    @NotNull(message = "{ctdt.khoiKienThuc.required}")
    private KhoiKienThuc khoiKienThuc = KhoiKienThuc.CO_SO_NGANH;

    @NotNull(message = "{ctdt.loaiHocPhan.required}")
    private LoaiHocPhan loaiHocPhan = LoaiHocPhan.BAT_BUOC;

    @NotNull(message = "{ctdt.hocKyChuan.required}")
    @Min(1)
    @Max(10)
    private Integer hocKyChuan = 1;
}
