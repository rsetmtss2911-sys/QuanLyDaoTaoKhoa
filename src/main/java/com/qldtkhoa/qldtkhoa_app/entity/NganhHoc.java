package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "NganhHoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NganhHoc extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaNganh", length = 20, nullable = false)
    private String maNganh;

    @Column(name = "TenNganh", length = 100, nullable = false)
    private String tenNganh;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKhoa", nullable = false)
    private Khoa khoa;

    public NganhHoc(String maNganh, String tenNganh, Khoa khoa) {
        this.maNganh = maNganh;
        this.tenNganh = tenNganh;
        this.khoa = khoa;
    }

    @Override
    public String getId() {
        return maNganh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NganhHoc other)) return false;
        return getMaNganh() != null && getMaNganh().equals(other.getMaNganh());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
