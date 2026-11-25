package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "MonHoc")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonHoc extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "MaMon", length = 20, nullable = false)
    private String maMon;

    @Column(name = "TenMon", length = 100, nullable = false)
    private String tenMon;

    @Column(name = "SoTinChi", nullable = false)
    private Integer soTinChi;

    @Column(name = "TinChiLT", nullable = false)
    private Integer tinChiLT = 0;

    @Column(name = "TinChiTH", nullable = false)
    private Integer tinChiTH = 0;

    @Column(name = "TrongSoQT")
    private Double trongSoQT = 0.2;

    @Column(name = "TrongSoGK")
    private Double trongSoGK = 0.3;

    @Column(name = "TrongSoCK")
    private Double trongSoCK = 0.5;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaBoMon", nullable = false)
    private BoMon boMon;

    public MonHoc(String maMon, String tenMon, Integer soTinChi, Integer tinChiLT, Integer tinChiTH,
                  Double trongSoQT, Double trongSoGK, Double trongSoCK, BoMon boMon) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.tinChiLT = tinChiLT != null ? tinChiLT : 0;
        this.tinChiTH = tinChiTH != null ? tinChiTH : 0;
        this.trongSoQT = trongSoQT != null ? trongSoQT : 0.2;
        this.trongSoGK = trongSoGK != null ? trongSoGK : 0.3;
        this.trongSoCK = trongSoCK != null ? trongSoCK : 0.5;
        this.boMon = boMon;
    }

    @Override
    public String getId() {
        return maMon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonHoc other)) return false;
        return getMaMon() != null && getMaMon().equals(other.getMaMon());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
