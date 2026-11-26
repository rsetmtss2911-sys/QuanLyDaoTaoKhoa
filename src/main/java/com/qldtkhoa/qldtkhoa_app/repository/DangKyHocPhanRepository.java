package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DangKyHocPhanRepository extends JpaRepository<DangKyHocPhan, DangKyHocPhanId> {

    @EntityGraph(attributePaths = {"sinhVien", "lopHocPhan", "lopHocPhan.monHoc", "lopHocPhan.giangVien", "lopHocPhan.giangVien.nhanSu"})
    List<DangKyHocPhan> findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(String maSV, String maHK);

    @EntityGraph(attributePaths = {"sinhVien", "lopHocPhan", "lopHocPhan.monHoc", "lopHocPhan.hocKy"})
    List<DangKyHocPhan> findBySinhVien_MaSV(String maSV);

    boolean existsBySinhVien_MaSV(String maSV);

    boolean existsByLopHocPhan_MaLHP(String maLHP);

    @EntityGraph(attributePaths = {"sinhVien", "sinhVien.lopHanhChinh", "lopHocPhan"})
    List<DangKyHocPhan> findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc(String maLHP);

    @Query("select count(dk) from DangKyHocPhan dk where dk.lopHocPhan.maLHP = :maLHP and dk.trangThaiDangKy in :statuses")
    long countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(@Param("maLHP") String maLHP,
                                                     @Param("statuses") Collection<TrangThaiDangKy> statuses);

    @Query("""
           select dk.lopHocPhan.maLHP, count(dk)
           from DangKyHocPhan dk
           where dk.lopHocPhan.maLHP in :maLHPs
             and dk.trangThaiDangKy in :statuses
           group by dk.lopHocPhan.maLHP
           """)
    List<Object[]> countActiveByMaLHPIn(@Param("maLHPs") Collection<String> maLHPs,
                                        @Param("statuses") Collection<TrangThaiDangKy> statuses);

    @Query("""
           select count(dk) > 0 from DangKyHocPhan dk
           where dk.sinhVien.maSV = :maSV
             and dk.lopHocPhan.monHoc.maMon = :maMon
             and dk.lopHocPhan.hocKy.maHK = :maHK
             and dk.trangThaiDangKy in :statuses
             and dk.lopHocPhan.trangThaiLop <> :excludedSectionStatus
           """)
    boolean existsActiveCourseInTerm(@Param("maSV") String maSV,
                                     @Param("maMon") String maMon,
                                     @Param("maHK") String maHK,
                                     @Param("statuses") Collection<TrangThaiDangKy> statuses,
                                     @Param("excludedSectionStatus") TrangThaiLop excludedSectionStatus);

    @Query("""
           select dk from DangKyHocPhan dk
           join fetch dk.lopHocPhan lhp
           join fetch lhp.monHoc
           where dk.sinhVien.maSV in :maSVs
             and lhp.hocKy.maHK = :maHK
             and lhp.maLHP <> :excludedMaLHP
             and dk.trangThaiDangKy in :statuses
             and lhp.trangThaiLop <> :cancelledStatus
           """)
    List<DangKyHocPhan> findOtherActiveRegistrationsForStudents(@Param("maSVs") Collection<String> maSVs,
                                                                @Param("maHK") String maHK,
                                                                @Param("excludedMaLHP") String excludedMaLHP,
                                                                @Param("statuses") Collection<TrangThaiDangKy> statuses,
                                                                @Param("cancelledStatus") TrangThaiLop cancelledStatus);

    @Query("""
           select dk from DangKyHocPhan dk
           join fetch dk.lopHocPhan lhp
           where dk.sinhVien.maSV = :maSV
             and lhp.trangThaiLop = :openStatus
             and dk.trangThaiDangKy in :statuses
           """)
    List<DangKyHocPhan> findActiveInSectionsWithStatus(@Param("maSV") String maSV,
                                                       @Param("openStatus") TrangThaiLop openStatus,
                                                       @Param("statuses") Collection<TrangThaiDangKy> statuses);

    @EntityGraph(attributePaths = {"sinhVien", "lopHocPhan"})
    Optional<DangKyHocPhan> findById_MaSVAndId_MaLHP(String maSV, String maLHP);

    @Procedure(procedureName = "dbo.sp_DangKyHocPhan")
    void executeSpDangKyHocPhan(@Param("MaSV") String maSV, @Param("MaLHP") String maLHP);
}
