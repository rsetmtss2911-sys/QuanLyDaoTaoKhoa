package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "DangKyHocPhan")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DangKyHocPhan extends AssignedIdEntity<DangKyHocPhanId> {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private DangKyHocPhanId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maSV")
    @JoinColumn(name = "MaSV", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maLHP")
    @JoinColumn(name = "MaLHP", nullable = false)
    private LopHocPhan lopHocPhan;

    @Column(name = "NgayDangKy")
    private LocalDateTime ngayDangKy;

    public LocalDateTime getThoiGianDK() {
        return ngayDangKy;
    }

    @Column(name = "TrangThaiDangKy", length = 50, nullable = false)
    private TrangThaiDangKy trangThaiDangKy = TrangThaiDangKy.HOP_LE;

    public static DangKyHocPhan of(SinhVien sv, LopHocPhan lhp) {
        DangKyHocPhan dk = new DangKyHocPhan();
        dk.id = new DangKyHocPhanId(sv.getMaSV(), lhp.getMaLHP());
        dk.sinhVien = sv;
        dk.lopHocPhan = lhp;
        dk.ngayDangKy = LocalDateTime.now();
        dk.trangThaiDangKy = TrangThaiDangKy.HOP_LE;
        return dk;
    }

    @Override
    public DangKyHocPhanId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DangKyHocPhan other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
