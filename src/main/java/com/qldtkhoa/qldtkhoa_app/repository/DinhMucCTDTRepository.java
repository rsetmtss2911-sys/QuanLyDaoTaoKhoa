package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.DinhMucCTDT;
import com.qldtkhoa.qldtkhoa_app.entity.id.DinhMucCTDTId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DinhMucCTDTRepository extends JpaRepository<DinhMucCTDT, DinhMucCTDTId> {

    List<DinhMucCTDT> findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHoc(String maNganh, String maKhoaHoc);
}
