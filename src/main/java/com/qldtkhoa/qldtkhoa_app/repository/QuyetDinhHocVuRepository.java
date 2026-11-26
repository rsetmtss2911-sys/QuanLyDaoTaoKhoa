package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuyetDinhHocVuRepository extends JpaRepository<QuyetDinhHocVu, String> {

    @EntityGraph(attributePaths = {"sinhVien", "sinhVien.lopHanhChinh"})
    List<QuyetDinhHocVu> findBySinhVien_MaSVOrderByNgayHieuLucDesc(String maSV);

    Optional<QuyetDinhHocVu> findTopBySinhVien_MaSVOrderByNgayHieuLucDescMaQuyetDinhDesc(String maSV);

    @Query("""
           select qd from QuyetDinhHocVu qd
           where qd.sinhVien.maSV = :maSV
             and qd.ngayHieuLuc <= :today
           order by qd.ngayHieuLuc desc, qd.maQuyetDinh desc
           """)
    List<QuyetDinhHocVu> findEffectiveForStudentOrderByLatest(@Param("maSV") String maSV,
                                                              @Param("today") LocalDate today);

    @Query("""
           select distinct qd.sinhVien.maSV from QuyetDinhHocVu qd
           where qd.ngayHieuLuc <= :today
           """)
    List<String> findStudentIdsWithDueDecisions(@Param("today") LocalDate today);

    boolean existsBySinhVien_MaSV(String maSV);

    @EntityGraph(attributePaths = {"sinhVien", "sinhVien.lopHanhChinh"})
    List<QuyetDinhHocVu> findTop5ByOrderByNgayHieuLucDesc();

    @EntityGraph(attributePaths = {"sinhVien", "sinhVien.lopHanhChinh"})
    @Query("""
           select qd from QuyetDinhHocVu qd
           where (:maSV is null or :maSV = '' or qd.sinhVien.maSV = :maSV)
             and (:q = '' or lower(qd.maQuyetDinh) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(qd.sinhVien.hoTen) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(qd.sinhVien.maSV) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<QuyetDinhHocVu> search(@Param("q") String q, @Param("maSV") String maSV, Pageable pageable);
}
