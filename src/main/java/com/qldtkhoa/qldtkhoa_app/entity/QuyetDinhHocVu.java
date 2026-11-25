package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "QuyetDinhHocVu")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuyetDinhHocVu extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "MaQuyetDinh", length = 50, nullable = false)
    private String maQuyetDinh;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaSV", nullable = false)
    private SinhVien sinhVien;

    @Column(name = "LoaiQuyetDinh", length = 50, nullable = false)
    private LoaiQuyetDinh loaiQuyetDinh;

    @Column(name = "NgayHieuLuc", nullable = false)
    private LocalDate ngayHieuLuc;

    @Column(name = "LyDo", length = 255)
    private String lyDo;

    public QuyetDinhHocVu(String maQuyetDinh, SinhVien sinhVien, LoaiQuyetDinh loaiQuyetDinh,
                          LocalDate ngayHieuLuc, String lyDo) {
        this.maQuyetDinh = maQuyetDinh;
        this.sinhVien = sinhVien;
        this.loaiQuyetDinh = loaiQuyetDinh;
        this.ngayHieuLuc = ngayHieuLuc;
        this.lyDo = lyDo;
    }

    @Override
    public String getId() {
        return maQuyetDinh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuyetDinhHocVu other)) return false;
        return getMaQuyetDinh() != null && getMaQuyetDinh().equals(other.getMaQuyetDinh());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
