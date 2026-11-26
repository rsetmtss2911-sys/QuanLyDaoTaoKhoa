package com.qldtkhoa.qldtkhoa_app.entity.id;

import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class DinhMucCTDTId implements Serializable {

    private static final long serialVersionUID = 1L;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaNganh", length = 20, nullable = false)
    private String maNganh;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaKhoaHoc", length = 20, nullable = false)
    private String maKhoaHoc;

    @Column(name = "KhoiKienThuc", length = 50, nullable = false)
    private KhoiKienThuc khoiKienThuc;
}
