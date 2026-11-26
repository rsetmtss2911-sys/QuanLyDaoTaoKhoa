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
public class DieuKienMonHocId implements Serializable {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaMon", length = 20, nullable = false)
    private String maMon;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaMonTienQuyet", length = 20, nullable = false)
    private String maMonTienQuyet;

    public DieuKienMonHocId(String maMon, String maMonTienQuyet) {
        this.maMon = maMon;
        this.maMonTienQuyet = maMonTienQuyet;
    }
}
