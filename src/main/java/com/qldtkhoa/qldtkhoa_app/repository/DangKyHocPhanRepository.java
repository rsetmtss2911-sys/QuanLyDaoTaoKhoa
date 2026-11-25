package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
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

    @EntityGraph(attributePaths = {"sinhVien", "sinhVien.lopHanhChinh", "lopHocPhan"})
    List<DangKyHocPhan> findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc(String maLHP);

    @Query("select count(dk) from DangKyHocPhan dk where dk.lopHocPhan.maLHP = :maLHP and dk.trangThaiDangKy in :statuses")
    long countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(@Param("maLHP") String maLHP,
                                                     @Param("statuses") Collection<TrangThaiDangKy> statuses);

    boolean existsBySinhVien_MaSVAndLopHocPhan_MonHoc_MaMonAndLopHocPhan_HocKy_MaHKAndTrangThaiDangKy(
            String maSV, String maMon, String maHK, TrangThaiDangKy status);

    @EntityGraph(attributePaths = {"sinhVien", "lopHocPhan"})
    Optional<DangKyHocPhan> findById_MaSVAndId_MaLHP(String maSV, String maLHP);

    @Procedure(procedureName = "dbo.sp_DangKyHocPhan")
    void executeSpDangKyHocPhan(@Param("MaSV") String maSV, @Param("MaLHP") String maLHP);
}
