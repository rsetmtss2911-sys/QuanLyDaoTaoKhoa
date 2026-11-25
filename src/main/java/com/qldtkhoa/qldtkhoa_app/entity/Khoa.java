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
@Table(name = "Khoa")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Khoa extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "MaKhoa", length = 20, nullable = false)
    private String maKhoa;

    @Column(name = "TenKhoa", length = 100, nullable = false)
    private String tenKhoa;

    @Column(name = "NamThanhLap")
    private Integer namThanhLap;

    public Khoa(String maKhoa, String tenKhoa, Integer namThanhLap) {
        this.maKhoa = maKhoa;
        this.tenKhoa = tenKhoa;
        this.namThanhLap = namThanhLap;
    }

    @Override
    public String getId() {
        return maKhoa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Khoa other)) return false;
        return getMaKhoa() != null && getMaKhoa().equals(other.getMaKhoa());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
