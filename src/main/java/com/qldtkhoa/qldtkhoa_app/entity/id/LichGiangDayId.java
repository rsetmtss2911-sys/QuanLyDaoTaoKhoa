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
public class LichGiangDayId implements Serializable {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaLHP", length = 20, nullable = false)
    private String maLHP;

    @Column(name = "ThuTrongTuan", nullable = false)
    private Integer thuTrongTuan;

    @Column(name = "TietBatDau", nullable = false)
    private Integer tietBatDau;

    public LichGiangDayId(String maLHP, Integer thuTrongTuan, Integer tietBatDau) {
        this.maLHP = maLHP;
        this.thuTrongTuan = thuTrongTuan;
        this.tietBatDau = tietBatDau;
    }
}
