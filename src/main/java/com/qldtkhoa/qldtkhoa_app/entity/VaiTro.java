package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "VaiTro")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VaiTro extends AssignedIdEntity<String> {

    @Id
    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaVaiTro", length = 20, nullable = false)
    private String maVaiTro;

    @Column(name = "TenVaiTro", length = 50, nullable = false)
    private String tenVaiTro;

    public VaiTro(String maVaiTro, String tenVaiTro) {
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
    }

    @Override
    public String getId() {
        return maVaiTro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VaiTro other)) return false;
        return getMaVaiTro() != null && getMaVaiTro().equals(other.getMaVaiTro());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
