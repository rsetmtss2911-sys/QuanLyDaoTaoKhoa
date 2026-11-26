package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.KetQuaHocTap;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KetQuaHocTapRepository extends JpaRepository<KetQuaHocTap, DangKyHocPhanId> {

    @EntityGraph(attributePaths = {"dangKy", "dangKy.sinhVien", "dangKy.lopHocPhan", "dangKy.lopHocPhan.monHoc"})
    List<KetQuaHocTap> findByDangKy_LopHocPhan_MaLHPOrderByDangKy_SinhVien_HoTenAsc(String maLHP);

    @EntityGraph(attributePaths = {"dangKy", "dangKy.sinhVien", "dangKy.lopHocPhan"})
    Optional<KetQuaHocTap> findById_MaSVAndId_MaLHP(String maSV, String maLHP);

    List<KetQuaHocTap> findById_MaLHP(String maLHP);

    boolean existsByDangKy_LopHocPhan_MaLHP(String maLHP);

    boolean existsByDangKy_LopHocPhan_MonHoc_MaMonAndDiemTongIsNotNull(String maMon);

    @Query("""
           select count(kq) > 0 from KetQuaHocTap kq
           where kq.dangKy.sinhVien.maSV = :maSV
             and kq.dangKy.lopHocPhan.monHoc.maMon = :maMon
             and kq.diemTong is not null
             and kq.diemTong >= :passScore
           """)
    boolean hasPassedSubject(@Param("maSV") String maSV,
                             @Param("maMon") String maMon,
                             @Param("passScore") double passScore);

    @Query("""
           select count(kq) > 0 from KetQuaHocTap kq
           where kq.dangKy.sinhVien.maSV = :maSV
             and kq.dangKy.lopHocPhan.monHoc.maMon = :maMon
             and kq.dangKy.trangThaiDangKy = :validStatus
             and kq.dangKy.lopHocPhan.trangThaiLop <> :cancelledStatus
             and (:currentTermStart is null or kq.dangKy.lopHocPhan.hocKy.ngayBatDau < :currentTermStart)
             and kq.diemTong is not null
             and kq.diemTong >= :passScore
           """)
    boolean hasPassedSubjectBeforeTerm(@Param("maSV") String maSV,
                                       @Param("maMon") String maMon,
                                       @Param("passScore") double passScore,
                                       @Param("currentTermStart") LocalDate currentTermStart,
                                       @Param("validStatus") TrangThaiDangKy validStatus,
                                       @Param("cancelledStatus") TrangThaiLop cancelledStatus);

    @Query("""
           select distinct kq.dangKy.lopHocPhan.monHoc.maMon from KetQuaHocTap kq
           where kq.dangKy.sinhVien.maSV = :maSV
             and kq.dangKy.trangThaiDangKy = :validStatus
             and kq.dangKy.lopHocPhan.trangThaiLop <> :cancelledStatus
             and (:currentTermStart is null or kq.dangKy.lopHocPhan.hocKy.ngayBatDau < :currentTermStart)
             and kq.diemTong is not null
             and kq.diemTong >= :passScore
           """)
    List<String> findPassedMaMonBeforeTerm(@Param("maSV") String maSV,
                                           @Param("passScore") double passScore,
                                           @Param("currentTermStart") LocalDate currentTermStart,
                                           @Param("validStatus") TrangThaiDangKy validStatus,
                                           @Param("cancelledStatus") TrangThaiLop cancelledStatus);

    @Query("""
           select distinct kq.dangKy.lopHocPhan.monHoc.maMon from KetQuaHocTap kq
           where kq.dangKy.sinhVien.maSV = :maSV
             and kq.dangKy.trangThaiDangKy in :activeStatuses
             and kq.dangKy.lopHocPhan.trangThaiLop <> :cancelledStatus
             and (kq.diemTong is not null or kq.dangKy.trangThaiDangKy = :camThiStatus)
           """)
    List<String> findGradedMaMonForStudent(@Param("maSV") String maSV,
                                           @Param("activeStatuses") Collection<TrangThaiDangKy> activeStatuses,
                                           @Param("camThiStatus") TrangThaiDangKy camThiStatus,
                                           @Param("cancelledStatus") TrangThaiLop cancelledStatus);
}
