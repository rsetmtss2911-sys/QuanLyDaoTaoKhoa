package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "LopHanhChinh")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LopHanhChinh extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaLopHC", length = 20, nullable = false)
    private String maLopHC;

    @Column(name = "TenLopHC", length = 100, nullable = false)
    private String tenLopHC;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaNganh", nullable = false)
    private NganhHoc nganhHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKhoaHoc", nullable = false)
    private KhoaNhapHoc khoaNhapHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaCovan", nullable = false)
    private GiangVien coVan;

    public LopHanhChinh(String maLopHC, String tenLopHC, NganhHoc nganhHoc, KhoaNhapHoc khoaNhapHoc, GiangVien coVan) {
        this.maLopHC = maLopHC;
        this.tenLopHC = tenLopHC;
        this.nganhHoc = nganhHoc;
        this.khoaNhapHoc = khoaNhapHoc;
        this.coVan = coVan;
    }

    @Override
    public String getId() {
        return maLopHC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LopHanhChinh other)) return false;
        return getMaLopHC() != null && getMaLopHC().equals(other.getMaLopHC());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
