package com.qldtkhoa.qldtkhoa_app.entity;

import com.qldtkhoa.qldtkhoa_app.entity.id.DieuKienMonHocId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DieuKienMonHoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DieuKienMonHoc extends AssignedIdEntity<DieuKienMonHocId> {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private DieuKienMonHocId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maMon")
    @JoinColumn(name = "MaMon", nullable = false)
    private MonHoc monHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("maMonTienQuyet")
    @JoinColumn(name = "MaMonTienQuyet", nullable = false)
    private MonHoc monTienQuyet;

    public DieuKienMonHoc(MonHoc monHoc, MonHoc monTienQuyet) {
        this.id = new DieuKienMonHocId(monHoc.getMaMon(), monTienQuyet.getMaMon());
        this.monHoc = monHoc;
        this.monTienQuyet = monTienQuyet;
    }

    @Override
    public DieuKienMonHocId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DieuKienMonHoc other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
