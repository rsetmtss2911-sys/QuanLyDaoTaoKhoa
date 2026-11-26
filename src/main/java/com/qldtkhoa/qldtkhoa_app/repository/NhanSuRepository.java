package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.dto.NhanSuRow;
import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NhanSuRepository extends JpaRepository<NhanSu, String> {

    @EntityGraph(attributePaths = {"taiKhoan", "taiKhoan.vaiTro"})
    Optional<NhanSu> findByTaiKhoan_Email(String email);

    @EntityGraph(attributePaths = {"taiKhoan", "taiKhoan.vaiTro"})
    @Query("""
           select ns from NhanSu ns
           where (:role is null or :role = '' or ns.taiKhoan.vaiTro.maVaiTro = :role)
             and (:q = '' or lower(ns.maNS) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(ns.hoTen) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(ns.taiKhoan.email) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<NhanSu> search(@Param("q") String q, @Param("role") String role, Pageable pageable);

    @Query("""
           select new com.qldtkhoa.qldtkhoa_app.dto.NhanSuRow(
               ns.maNS,
               ns.hoTen,
               tk.email,
               vt.maVaiTro,
               vt.tenVaiTro,
               tk.trangThaiKichHoat,
               (gv is not null),
               gv.hocHam,
               gv.hocVi,
               bm.tenBoMon
           )
           from NhanSu ns
           join ns.taiKhoan tk
           join tk.vaiTro vt
           left join GiangVien gv on gv.maNS = ns.maNS
           left join gv.boMon bm
           where (:role is null or :role = '' or vt.maVaiTro = :role)
             and (:q = '' or lower(ns.maNS) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(ns.hoTen) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(tk.email) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<NhanSuRow> searchRows(@Param("q") String q, @Param("role") String role, Pageable pageable);
}
