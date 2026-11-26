package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "BoMon")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoMon extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaBoMon", length = 20, nullable = false)
    private String maBoMon;

    @Column(name = "TenBoMon", length = 100, nullable = false)
    private String tenBoMon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKhoa", nullable = false)
    private Khoa khoa;

    public BoMon(String maBoMon, String tenBoMon, Khoa khoa) {
        this.maBoMon = maBoMon;
        this.tenBoMon = tenBoMon;
        this.khoa = khoa;
    }

    @Override
    public String getId() {
        return maBoMon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoMon other)) return false;
        return getMaBoMon() != null && getMaBoMon().equals(other.getMaBoMon());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
