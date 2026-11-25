package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TaiKhoan")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaiKhoan extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "Email", length = 100, nullable = false)
    private String email;

    @Column(name = "MatKhau", length = 255, nullable = false)
    private String matKhau;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaVaiTro", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "TrangThaiKichHoat")
    private Boolean trangThaiKichHoat = true;

    public TaiKhoan(String email, String matKhau, VaiTro vaiTro, Boolean trangThaiKichHoat) {
        this.email = email;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThaiKichHoat = trangThaiKichHoat != null ? trangThaiKichHoat : true;
    }

    @Override
    public String getId() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaiKhoan other)) return false;
        return getEmail() != null && getEmail().equals(other.getEmail());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
