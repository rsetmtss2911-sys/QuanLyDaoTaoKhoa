package com.qldtkhoa.qldtkhoa_app.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class DangKyHocPhanId implements Serializable {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaSV", length = 20, nullable = false)
    private String maSV;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaLHP", length = 20, nullable = false)
    private String maLHP;

    public DangKyHocPhanId(String maSV, String maLHP) {
        this.maSV = maSV;
        this.maLHP = maLHP;
    }
}
