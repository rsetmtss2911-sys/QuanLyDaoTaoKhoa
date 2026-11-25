package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuyetDinhHocVuRepository extends JpaRepository<QuyetDinhHocVu, String> {

    @EntityGraph(attributePaths = {"sinhVien"})
    List<QuyetDinhHocVu> findBySinhVien_MaSVOrderByNgayHieuLucDesc(String maSV);

    boolean existsBySinhVien_MaSV(String maSV);

    @EntityGraph(attributePaths = {"sinhVien", "sinhVien.lopHanhChinh"})
    List<QuyetDinhHocVu> findTop5ByOrderByNgayHieuLucDesc();

    @EntityGraph(attributePaths = {"sinhVien", "sinhVien.lopHanhChinh"})
    @Query("""
           select qd from QuyetDinhHocVu qd
           where (:maSV is null or :maSV = '' or qd.sinhVien.maSV = :maSV)
             and (:q = '' or lower(qd.maQuyetDinh) like lower(concat('%', :q, '%'))
                         or lower(qd.sinhVien.hoTen) like lower(concat('%', :q, '%'))
                         or lower(qd.sinhVien.maSV) like lower(concat('%', :q, '%')))
           """)
    Page<QuyetDinhHocVu> search(@Param("q") String q, @Param("maSV") String maSV, Pageable pageable);
}
