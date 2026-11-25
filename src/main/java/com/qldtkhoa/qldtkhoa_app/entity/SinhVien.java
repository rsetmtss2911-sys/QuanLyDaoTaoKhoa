package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "SinhVien")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SinhVien extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "MaSV", length = 20, nullable = false)
    private String maSV;

    @Column(name = "HoTen", length = 100, nullable = false)
    private String hoTen;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "GioiTinh", length = 10)
    private GioiTinh gioiTinh;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Email", unique = true, nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "TrangThaiHienTai", length = 50, nullable = false)
    private TrangThaiSinhVien trangThaiHienTai = TrangThaiSinhVien.DANG_HOC;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaLopHC", nullable = false)
    private LopHanhChinh lopHanhChinh;

    public SinhVien(String maSV, String hoTen, LocalDate ngaySinh, GioiTinh gioiTinh,
                    TaiKhoan taiKhoan, TrangThaiSinhVien trangThaiHienTai, LopHanhChinh lopHanhChinh) {
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.taiKhoan = taiKhoan;
        this.trangThaiHienTai = trangThaiHienTai != null ? trangThaiHienTai : TrangThaiSinhVien.DANG_HOC;
        this.lopHanhChinh = lopHanhChinh;
    }

    @Override
    public String getId() {
        return maSV;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SinhVien other)) return false;
        return getMaSV() != null && getMaSV().equals(other.getMaSV());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
