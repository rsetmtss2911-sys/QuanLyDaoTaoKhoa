package com.qldtkhoa.qldtkhoa_app.entity.view;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "vw_BangDiemSinhVien")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BangDiemSinhVienView {

    @EmbeddedId
    private DangKyHocPhanId id;

    @Column(name = "HoTen")
    private String hoTen;

    @Column(name = "MaHK")
    private String maHK;

    @Column(name = "TenHK")
    private String tenHK;

    @Column(name = "NamHoc")
    private Integer namHoc;

    @Column(name = "MaLHP", insertable = false, updatable = false)
    private String maLHP;

    @Column(name = "TrangThaiLop")
    private TrangThaiLop trangThaiLop;

    @Column(name = "MaMon")
    private String maMon;

    @Column(name = "TenMon")
    private String tenMon;

    @Column(name = "SoTinChi")
    private Integer soTinChi;

    @Column(name = "TrongSoQT")
    private Double trongSoQT;

    @Column(name = "TrongSoGK")
    private Double trongSoGK;

    @Column(name = "TrongSoCK")
    private Double trongSoCK;

    @Column(name = "TinhVaoGPA")
    private Boolean tinhVaoGPA;

    @Column(name = "TrangThaiDangKy")
    private TrangThaiDangKy trangThaiDangKy;

    @Column(name = "LyDoCamThi")
    private String lyDoCamThi;

    @Column(name = "DiemQuaTrinh")
    private Double diemQuaTrinh;

    @Column(name = "DiemGiuaKy")
    private Double diemGiuaKy;

    @Column(name = "DiemCuoiKy")
    private Double diemCuoiKy;

    @Column(name = "DiemTong")
    private Double diemTong;

    @Column(name = "DiemHe4")
    private Double diemHe4;

    @Column(name = "DiemChu")
    private String diemChu;

    @Column(name = "KetQuaHocPhan")
    private String ketQuaHocPhan;

    public String getMaSV() {
        return id != null ? id.getMaSV() : null;
    }

    public String getMaLHP() {
        return id != null ? id.getMaLHP() : null;
    }
}
