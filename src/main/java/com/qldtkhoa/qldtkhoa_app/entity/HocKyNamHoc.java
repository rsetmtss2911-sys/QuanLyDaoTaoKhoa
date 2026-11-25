package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "HocKyNamHoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HocKyNamHoc extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "MaHK", length = 20, nullable = false)
    private String maHK;

    @Column(name = "TenHK", length = 50, nullable = false)
    private String tenHK;

    public String getTenHocKy() {
        return tenHK;
    }

    @Column(name = "NamHoc", nullable = false)
    private Integer namHoc;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    public HocKyNamHoc(String maHK, String tenHK, Integer namHoc, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.maHK = maHK;
        this.tenHK = tenHK;
        this.namHoc = namHoc;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    @Override
    public String getId() {
        return maHK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HocKyNamHoc other)) return false;
        return getMaHK() != null && getMaHK().equals(other.getMaHK());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
