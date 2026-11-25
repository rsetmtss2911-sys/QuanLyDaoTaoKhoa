package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.id.ChuongTrinhDaoTaoId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ChuongTrinhDaoTao")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChuongTrinhDaoTao extends AssignedIdEntity<ChuongTrinhDaoTaoId> {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private ChuongTrinhDaoTaoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maNganh")
    @JoinColumn(name = "MaNganh", nullable = false)
    private NganhHoc nganhHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maKhoaHoc")
    @JoinColumn(name = "MaKhoaHoc", nullable = false)
    private KhoaNhapHoc khoaNhapHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maMon")
    @JoinColumn(name = "MaMon", nullable = false)
    private MonHoc monHoc;

    @Column(name = "KhoiKienThuc", length = 50)
    private KhoiKienThuc khoiKienThuc;

    @Column(name = "LoaiHocPhan", length = 50)
    private LoaiHocPhan loaiHocPhan = LoaiHocPhan.BAT_BUOC;

    @Column(name = "HocKyChuan")
    private Integer hocKyChuan;

    public ChuongTrinhDaoTao(NganhHoc nganhHoc, KhoaNhapHoc khoaNhapHoc, MonHoc monHoc,
                             KhoiKienThuc khoiKienThuc, LoaiHocPhan loaiHocPhan, Integer hocKyChuan) {
        this.id = new ChuongTrinhDaoTaoId(nganhHoc.getMaNganh(), khoaNhapHoc.getMaKhoaHoc(), monHoc.getMaMon());
        this.nganhHoc = nganhHoc;
        this.khoaNhapHoc = khoaNhapHoc;
        this.monHoc = monHoc;
        this.khoiKienThuc = khoiKienThuc;
        this.loaiHocPhan = loaiHocPhan != null ? loaiHocPhan : LoaiHocPhan.BAT_BUOC;
        this.hocKyChuan = hocKyChuan;
    }

    @Override
    public ChuongTrinhDaoTaoId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChuongTrinhDaoTao other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
