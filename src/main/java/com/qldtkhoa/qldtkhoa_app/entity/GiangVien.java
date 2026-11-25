package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GiangVien")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiangVien extends AssignedIdEntity<String> {

    @Id
    @Column(name = "MaNS", length = 20)
    @Setter(AccessLevel.NONE)
    private String maNS;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaNS")
    private NhanSu nhanSu;

    @Column(name = "HocHam", length = 50)
    private String hocHam;

    @Column(name = "HocVi", length = 50, nullable = false)
    private String hocVi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaBoMon", nullable = false)
    private BoMon boMon;

    public GiangVien(NhanSu nhanSu, String hocHam, String hocVi, BoMon boMon) {
        this.nhanSu = nhanSu;
        this.maNS = nhanSu != null ? nhanSu.getMaNS() : null;
        this.hocHam = hocHam;
        this.hocVi = hocVi;
        this.boMon = boMon;
    }

    @Override
    public String getId() {
        return maNS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GiangVien other)) return false;
        return getMaNS() != null && getMaNS().equals(other.getMaNS());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
