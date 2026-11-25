package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SinhVienRepository extends JpaRepository<SinhVien, String> {

    @EntityGraph(attributePaths = {"lopHanhChinh", "lopHanhChinh.nganhHoc", "lopHanhChinh.khoaNhapHoc", "lopHanhChinh.coVan", "lopHanhChinh.coVan.nhanSu", "taiKhoan"})
    Optional<SinhVien> findByTaiKhoan_Email(String email);

    @EntityGraph(attributePaths = {"lopHanhChinh", "lopHanhChinh.nganhHoc", "lopHanhChinh.khoaNhapHoc", "lopHanhChinh.coVan", "lopHanhChinh.coVan.nhanSu", "taiKhoan"})
    @Query("select sv from SinhVien sv where sv.maSV = :maSV")
    Optional<SinhVien> findByIdWithDetails(@Param("maSV") String maSV);

    @EntityGraph(attributePaths = {"lopHanhChinh", "taiKhoan"})
    List<SinhVien> findByLopHanhChinh_MaLopHC(String maLopHC);

    boolean existsByLopHanhChinh_MaLopHC(String maLopHC);

    long countByLopHanhChinh_MaLopHC(String maLopHC);

    @EntityGraph(attributePaths = {"lopHanhChinh", "taiKhoan"})
    @Query("""
           select sv from SinhVien sv
           left join sv.lopHanhChinh lhc
           left join sv.taiKhoan tk
           where (:maLopHC is null or :maLopHC = '' or lhc.maLopHC = :maLopHC)
             and (:status is null or sv.trangThaiHienTai = :status)
             and (:q = '' or lower(sv.maSV) like lower(concat('%', :q, '%'))
                         or lower(sv.hoTen) like lower(concat('%', :q, '%'))
                         or (tk is not null and lower(tk.email) like lower(concat('%', :q, '%'))))
           """)
    Page<SinhVien> search(@Param("q") String q,
                          @Param("maLopHC") String maLopHC,
                          @Param("status") TrangThaiSinhVien status,
                          Pageable pageable);

    long countByTrangThaiHienTai(TrangThaiSinhVien status);
}
