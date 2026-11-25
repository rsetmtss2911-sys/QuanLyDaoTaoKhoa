package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LopHocPhan")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LopHocPhan extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "MaLHP", length = 20, nullable = false)
    private String maLHP;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaMon", nullable = false)
    private MonHoc monHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaGV", nullable = false)
    private GiangVien giangVien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaHK", nullable = false)
    private HocKyNamHoc hocKy;

    @Column(name = "SiSoToiDa", nullable = false)
    private Integer siSoToiDa;

    @Column(name = "TrangThaiLop", length = 50, nullable = false)
    private TrangThaiLop trangThaiLop = TrangThaiLop.CHO_MO;

    public LopHocPhan(String maLHP, MonHoc monHoc, GiangVien giangVien, HocKyNamHoc hocKy,
                      Integer siSoToiDa, TrangThaiLop trangThaiLop) {
        this.maLHP = maLHP;
        this.monHoc = monHoc;
        this.giangVien = giangVien;
        this.hocKy = hocKy;
        this.siSoToiDa = siSoToiDa;
        this.trangThaiLop = trangThaiLop != null ? trangThaiLop : TrangThaiLop.CHO_MO;
    }

    @Override
    public String getId() {
        return maLHP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LopHocPhan other)) return false;
        return getMaLHP() != null && getMaLHP().equals(other.getMaLHP());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
