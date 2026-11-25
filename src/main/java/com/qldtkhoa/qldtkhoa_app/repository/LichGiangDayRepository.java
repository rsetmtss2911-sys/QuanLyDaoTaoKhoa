package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.LichGiangDay;
import com.qldtkhoa.qldtkhoa_app.entity.id.LichGiangDayId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LichGiangDayRepository extends JpaRepository<LichGiangDay, LichGiangDayId> {

       @EntityGraph(attributePaths = { "lopHocPhan", "lopHocPhan.monHoc", "lopHocPhan.giangVien",
                     "lopHocPhan.giangVien.nhanSu" })
       @Query("select lgd from LichGiangDay lgd where lgd.lopHocPhan.maLHP = :maLHP order by lgd.id.thuTrongTuan asc, lgd.id.tietBatDau asc")
       List<LichGiangDay> findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(@Param("maLHP") String maLHP);

       @EntityGraph(attributePaths = { "lopHocPhan", "lopHocPhan.monHoc" })
       List<LichGiangDay> findByLopHocPhan_HocKy_MaHKAndPhongHoc(String maHK, String phongHoc);

       @EntityGraph(attributePaths = { "lopHocPhan", "lopHocPhan.monHoc" })
       List<LichGiangDay> findByLopHocPhan_HocKy_MaHKAndLopHocPhan_GiangVien_MaNS(String maHK, String maNS);

       @EntityGraph(attributePaths = { "lopHocPhan", "lopHocPhan.monHoc", "lopHocPhan.giangVien",
                     "lopHocPhan.giangVien.nhanSu" })
       @Query("""
                     select lgd from LichGiangDay lgd
                     join lgd.lopHocPhan lhp
                     join DangKyHocPhan dk on dk.id.maLHP = lhp.maLHP
                     where dk.id.maSV = :maSV
                       and dk.trangThaiDangKy in (
                           com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy.HOP_LE,
                           com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy.CAM_THI
                       )
                       and lhp.hocKy.maHK = :maHK
                       and lhp.trangThaiLop <> com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop.DA_HUY
                     order by lgd.id.thuTrongTuan asc, lgd.id.tietBatDau asc
                     """)
       List<LichGiangDay> findStudentWeeklyTimetable(@Param("maSV") String maSV, @Param("maHK") String maHK);

       @EntityGraph(attributePaths = { "lopHocPhan", "lopHocPhan.monHoc", "lopHocPhan.giangVien",
                     "lopHocPhan.giangVien.nhanSu" })
       @Query("""
                     select lgd from LichGiangDay lgd
                     join lgd.lopHocPhan lhp
                     where lhp.maLHP in :maLHPs
                     order by lgd.id.thuTrongTuan asc, lgd.id.tietBatDau asc
                     """)
       List<LichGiangDay> findByMaLHPIn(@Param("maLHPs") List<String> maLHPs);

       @EntityGraph(attributePaths = { "lopHocPhan", "lopHocPhan.monHoc", "lopHocPhan.giangVien",
                     "lopHocPhan.giangVien.nhanSu" })
       @Query("""
                     select lgd from LichGiangDay lgd
                     join lgd.lopHocPhan lhp
                     where lhp.giangVien.maNS = :maNS
                       and lhp.hocKy.maHK = :maHK
                       and lhp.trangThaiLop <> com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop.DA_HUY
                     order by lgd.id.thuTrongTuan asc, lgd.id.tietBatDau asc
                     """)
       List<LichGiangDay> findLecturerWeeklyTimetable(@Param("maNS") String maNS, @Param("maHK") String maHK);

       @EntityGraph(attributePaths = { "lopHocPhan", "lopHocPhan.monHoc", "lopHocPhan.giangVien",
                     "lopHocPhan.giangVien.nhanSu" })
       @Query("""
                     select lgd from LichGiangDay lgd
                     join lgd.lopHocPhan lhp
                     where lgd.phongHoc = :phongHoc
                       and lhp.hocKy.maHK = :maHK
                       and lhp.trangThaiLop <> com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop.DA_HUY
                     order by lgd.id.thuTrongTuan asc, lgd.id.tietBatDau asc
                     """)
       List<LichGiangDay> findRoomWeeklyTimetable(@Param("phongHoc") String phongHoc, @Param("maHK") String maHK);

       @Query("""
                     select distinct lgd.phongHoc from LichGiangDay lgd
                     join lgd.lopHocPhan lhp
                     where lhp.hocKy.maHK = :maHK
                     order by lgd.phongHoc asc
                     """)
       List<String> findDistinctPhongHocByHocKy(@Param("maHK") String maHK);
}
