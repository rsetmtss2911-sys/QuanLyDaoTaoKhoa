package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.ChuongTrinhDaoTao;
import com.qldtkhoa.qldtkhoa_app.entity.id.ChuongTrinhDaoTaoId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChuongTrinhDaoTaoRepository extends JpaRepository<ChuongTrinhDaoTao, ChuongTrinhDaoTaoId> {

    @EntityGraph(attributePaths = {"nganhHoc", "khoaNhapHoc", "monHoc"})
    List<ChuongTrinhDaoTao> findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc(
            String maNganh, String maKhoaHoc);

    boolean existsByNganhHoc_MaNganh(String maNganh);

    boolean existsByKhoaNhapHoc_MaKhoaHoc(String maKhoaHoc);

    boolean existsByMonHoc_MaMon(String maMon);
}
