package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "NhatKyHeThong")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NhatKyHeThong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "MaNhatKy", nullable = false)
    private Long maNhatKy;

    @Column(name = "ThoiGian", nullable = false)
    private LocalDateTime thoiGian;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "NguoiThucHien", length = 100, nullable = false)
    private String nguoiThucHien;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "HanhDong", length = 50, nullable = false)
    private String hanhDong;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DoiTuong", length = 50, nullable = false)
    private String doiTuong;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaBanGhi", length = 100, nullable = false)
    private String maBanGhi;

    @Column(name = "GiaTriCu", length = 10000)
    private String giaTriCu;

    @Column(name = "GiaTriMoi", length = 10000)
    private String giaTriMoi;

    @Column(name = "LyDo", length = 255)
    private String lyDo;

    public NhatKyHeThong(LocalDateTime thoiGian, String nguoiThucHien, String hanhDong,
                         String doiTuong, String maBanGhi, String giaTriCu, String giaTriMoi, String lyDo) {
        this.thoiGian = thoiGian != null ? thoiGian : LocalDateTime.now();
        this.nguoiThucHien = nguoiThucHien;
        this.hanhDong = hanhDong;
        this.doiTuong = doiTuong;
        this.maBanGhi = maBanGhi;
        this.giaTriCu = giaTriCu;
        this.giaTriMoi = giaTriMoi;
        this.lyDo = lyDo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NhatKyHeThong other)) return false;
        return getMaNhatKy() != null && getMaNhatKy().equals(other.getMaNhatKy());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
