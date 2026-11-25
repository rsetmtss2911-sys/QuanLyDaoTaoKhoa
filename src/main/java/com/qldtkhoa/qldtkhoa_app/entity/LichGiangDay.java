package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.id.LichGiangDayId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LichGiangDay")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LichGiangDay extends AssignedIdEntity<LichGiangDayId> {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private LichGiangDayId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maLHP")
    @JoinColumn(name = "MaLHP", nullable = false)
    private LopHocPhan lopHocPhan;

    @Column(name = "TietKetThuc", nullable = false)
    private Integer tietKetThuc;

    @Column(name = "PhongHoc", length = 50, nullable = false)
    private String phongHoc;

    public LichGiangDay(LopHocPhan lopHocPhan, Integer thuTrongTuan, Integer tietBatDau, Integer tietKetThuc, String phongHoc) {
        this.id = new LichGiangDayId(lopHocPhan.getMaLHP(), thuTrongTuan, tietBatDau);
        this.lopHocPhan = lopHocPhan;
        this.tietKetThuc = tietKetThuc;
        this.phongHoc = phongHoc;
    }

    public Integer getThuTrongTuan() {
        return id != null ? id.getThuTrongTuan() : null;
    }

    public Integer getTietBatDau() {
        return id != null ? id.getTietBatDau() : null;
    }

    @Override
    public LichGiangDayId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LichGiangDay other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
