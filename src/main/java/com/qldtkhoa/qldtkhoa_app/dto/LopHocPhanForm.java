package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
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
public class LopHocPhanForm {

    @NotBlank(message = "{lophp.maLHP.required}")
    @Size(max = 20, message = "{lophp.maLHP.size}")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maLHP;

    @NotBlank(message = "{lophp.monHoc.required}")
    private String maMon;

    @NotBlank(message = "{lophp.giangVien.required}")
    private String maGV;

    @NotBlank(message = "{lophp.hocKy.required}")
    private String maHK;

    @NotNull(message = "{lophp.siSoToiDa.required}")
    @Min(value = 10, message = "{lophp.siSoToiDa.min}")
    @Max(value = 150, message = "{lophp.siSoToiDa.max}")
    private Integer siSoToiDa = 50;

    private TrangThaiLop trangThaiLop = TrangThaiLop.CHO_MO;
}
