package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "KetQuaHocTap")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KetQuaHocTap extends AssignedIdEntity<DangKyHocPhanId> {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private DangKyHocPhanId id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "MaSV", referencedColumnName = "MaSV"),
        @JoinColumn(name = "MaLHP", referencedColumnName = "MaLHP")
    })
    private DangKyHocPhan dangKy;

    @Column(name = "DiemQuaTrinh")
    private Double diemQuaTrinh;

    @Column(name = "DiemGiuaKy")
    private Double diemGiuaKy;

    @Column(name = "DiemCuoiKy")
    private Double diemCuoiKy;

    @Column(name = "DiemTong", insertable = false, updatable = false)
    private Double diemTong;

    public KetQuaHocTap(DangKyHocPhan dangKy, Double diemQuaTrinh, Double diemGiuaKy, Double diemCuoiKy) {
        this.id = dangKy.getId();
        this.dangKy = dangKy;
        this.diemQuaTrinh = diemQuaTrinh;
        this.diemGiuaKy = diemGiuaKy;
        this.diemCuoiKy = diemCuoiKy;
    }

    @Override
    public DangKyHocPhanId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KetQuaHocTap other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
