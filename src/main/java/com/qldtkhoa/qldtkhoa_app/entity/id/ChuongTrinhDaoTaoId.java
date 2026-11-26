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
public class ChuongTrinhDaoTaoId implements Serializable {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaNganh", length = 20, nullable = false)
    private String maNganh;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaKhoaHoc", length = 20, nullable = false)
    private String maKhoaHoc;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaMon", length = 20, nullable = false)
    private String maMon;

    public ChuongTrinhDaoTaoId(String maNganh, String maKhoaHoc, String maMon) {
        this.maNganh = maNganh;
        this.maKhoaHoc = maKhoaHoc;
        this.maMon = maMon;
    }
}
