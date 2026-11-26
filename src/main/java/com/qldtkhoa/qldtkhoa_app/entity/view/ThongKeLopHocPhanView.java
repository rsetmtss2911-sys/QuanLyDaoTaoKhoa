package com.qldtkhoa.qldtkhoa_app.entity.view;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "vw_ThongKeLopHocPhan")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThongKeLopHocPhanView {

    @Id
    @Column(name = "MaLHP")
    private String maLHP;

    @Column(name = "MaHK")
    private String maHK;

    @Column(name = "MaMon")
    private String maMon;

    @Column(name = "TenMon")
    private String tenMon;

    @Column(name = "SoTinChi")
    private Integer soTinChi;

    @Column(name = "MaGV")
    private String maGV;

    @Column(name = "TenGiangVien")
    private String tenGiangVien;

    @Column(name = "SiSoToiDa")
    private Integer siSoToiDa;

    @Column(name = "SiSoHienTai")
    private Integer siSoHienTai;

    @Column(name = "SoChoConTrong")
    private Integer soChoConTrong;

    @Column(name = "SoDaChamDiem")
    private Integer soDaChamDiem;

    @Column(name = "TrangThaiLop")
    private TrangThaiLop trangThaiLop;
}
