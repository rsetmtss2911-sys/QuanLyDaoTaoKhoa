package com.qldtkhoa.qldtkhoa_app.repository;

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
             and (:q = '' or lower(ns.maNS) like lower(concat('%', :q, '%'))
                         or lower(ns.hoTen) like lower(concat('%', :q, '%'))
                         or lower(ns.taiKhoan.email) like lower(concat('%', :q, '%')))
           """)
    Page<NhanSu> search(@Param("q") String q, @Param("role") String role, Pageable pageable);
}
