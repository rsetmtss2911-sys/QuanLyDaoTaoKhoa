package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "KhoaNhapHoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KhoaNhapHoc extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "MaKhoaHoc", length = 20, nullable = false)
    private String maKhoaHoc;

    @Column(name = "NamBatDau", nullable = false)
    private Integer namBatDau;

    public KhoaNhapHoc(String maKhoaHoc, Integer namBatDau) {
        this.maKhoaHoc = maKhoaHoc;
        this.namBatDau = namBatDau;
    }

    @Override
    public String getId() {
        return maKhoaHoc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KhoaNhapHoc other)) return false;
        return getMaKhoaHoc() != null && getMaKhoaHoc().equals(other.getMaKhoaHoc());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
