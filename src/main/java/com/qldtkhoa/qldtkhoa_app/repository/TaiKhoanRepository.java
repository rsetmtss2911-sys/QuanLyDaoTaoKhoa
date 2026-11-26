package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {

    @EntityGraph(attributePaths = "vaiTro")
    Optional<TaiKhoan> findByEmail(String email);

    long countByVaiTro_MaVaiTroAndTrangThaiKichHoat(String maVaiTro, Boolean trangThaiKichHoat);

    @EntityGraph(attributePaths = "vaiTro")
    @Query("""
           select tk from TaiKhoan tk
           where (:maVaiTro is null or :maVaiTro = '' or tk.vaiTro.maVaiTro = :maVaiTro)
             and (:q = '' or lower(tk.email) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<TaiKhoan> search(@Param("q") String q, @Param("maVaiTro") String maVaiTro, Pageable pageable);
}
