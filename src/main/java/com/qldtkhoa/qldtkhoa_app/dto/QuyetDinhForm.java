package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class QuyetDinhForm {

    @NotBlank(message = "{quyetdinh.maQuyetDinh.required}")
    @Size(max = 50, message = "{quyetdinh.maQuyetDinh.size}")
    private String maQuyetDinh;

    @NotBlank(message = "{quyetdinh.sinhVien.required}")
    private String maSV;

    @NotNull(message = "{quyetdinh.loaiQuyetDinh.required}")
    private LoaiQuyetDinh loaiQuyetDinh = LoaiQuyetDinh.BAO_LUU;

    @NotNull(message = "{quyetdinh.ngayHieuLuc.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayHieuLuc = LocalDate.now();

    @Size(max = 255, message = "{quyetdinh.lyDo.size}")
    private String lyDo;
}
