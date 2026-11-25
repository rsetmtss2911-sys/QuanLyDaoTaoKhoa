package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.KetQuaHocTap;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KetQuaHocTapRepository extends JpaRepository<KetQuaHocTap, DangKyHocPhanId> {

    @EntityGraph(attributePaths = {"dangKy", "dangKy.sinhVien", "dangKy.lopHocPhan", "dangKy.lopHocPhan.monHoc"})
    List<KetQuaHocTap> findByDangKy_LopHocPhan_MaLHPOrderByDangKy_SinhVien_HoTenAsc(String maLHP);

    @EntityGraph(attributePaths = {"dangKy", "dangKy.sinhVien", "dangKy.lopHocPhan"})
    Optional<KetQuaHocTap> findById_MaSVAndId_MaLHP(String maSV, String maLHP);

    List<KetQuaHocTap> findById_MaLHP(String maLHP);

    boolean existsByDangKy_LopHocPhan_MonHoc_MaMonAndDiemTongIsNotNull(String maMon);

    @org.springframework.data.jpa.repository.Query("""
           select count(kq) > 0 from KetQuaHocTap kq
           where kq.dangKy.sinhVien.maSV = :maSV
             and kq.dangKy.lopHocPhan.monHoc.maMon = :maMon
             and kq.diemTong is not null
             and kq.diemTong >= :passScore
           """)
    boolean hasPassedSubject(@org.springframework.data.repository.query.Param("maSV") String maSV,
                             @org.springframework.data.repository.query.Param("maMon") String maMon,
                             @org.springframework.data.repository.query.Param("passScore") double passScore);
}
