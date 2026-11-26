package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.id.DinhMucCTDTId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DinhMucCTDT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DinhMucCTDT extends AssignedIdEntity<DinhMucCTDTId> {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private DinhMucCTDTId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maNganh")
    @JoinColumn(name = "MaNganh", nullable = false)
    private NganhHoc nganhHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maKhoaHoc")
    @JoinColumn(name = "MaKhoaHoc", nullable = false)
    private KhoaNhapHoc khoaNhapHoc;

    @Column(name = "TinChiTuChonToiThieu", nullable = false)
    private Integer tinChiTuChonToiThieu = 0;

    public DinhMucCTDT(NganhHoc nganhHoc, KhoaNhapHoc khoaNhapHoc, KhoiKienThuc khoiKienThuc,
                       Integer tinChiTuChonToiThieu) {
        this.id = new DinhMucCTDTId(nganhHoc.getMaNganh(), khoaNhapHoc.getMaKhoaHoc(), khoiKienThuc);
        this.nganhHoc = nganhHoc;
        this.khoaNhapHoc = khoaNhapHoc;
        this.tinChiTuChonToiThieu = tinChiTuChonToiThieu != null ? tinChiTuChonToiThieu : 0;
    }

    public KhoiKienThuc getKhoiKienThuc() {
        return id != null ? id.getKhoiKienThuc() : null;
    }

    @Override
    public DinhMucCTDTId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DinhMucCTDT other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
