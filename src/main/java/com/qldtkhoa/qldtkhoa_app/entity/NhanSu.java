package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "NhanSu")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NhanSu extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaNS", length = 20, nullable = false)
    private String maNS;

    @Column(name = "HoTen", length = 100, nullable = false)
    private String hoTen;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Email", unique = true, nullable = false)
    private TaiKhoan taiKhoan;

    public NhanSu(String maNS, String hoTen, TaiKhoan taiKhoan) {
        this.maNS = maNS;
        this.hoTen = hoTen;
        this.taiKhoan = taiKhoan;
    }

    @Override
    public String getId() {
        return maNS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NhanSu other)) return false;
        return getMaNS() != null && getMaNS().equals(other.getMaNS());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
