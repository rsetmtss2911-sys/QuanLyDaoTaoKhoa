package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GiangVienRepository extends JpaRepository<GiangVien, String> {

    @EntityGraph(attributePaths = {"nhanSu", "nhanSu.taiKhoan", "boMon", "boMon.khoa"})
    Optional<GiangVien> findByNhanSu_TaiKhoan_Email(String email);

    @EntityGraph(attributePaths = {"nhanSu", "nhanSu.taiKhoan", "boMon", "boMon.khoa"})
    @Query("select gv from GiangVien gv where gv.maNS = :maNS")
    Optional<GiangVien> findByIdWithDetails(@Param("maNS") String maNS);

    @EntityGraph(attributePaths = {"nhanSu", "boMon"})
    List<GiangVien> findByBoMon_MaBoMonOrderByNhanSu_HoTen(String maBoMon);

    boolean existsByBoMon_MaBoMon(String maBoMon);

    @EntityGraph(attributePaths = {"nhanSu", "boMon", "boMon.khoa"})
    @Query("""
           select gv from GiangVien gv
           where (:maBoMon is null or :maBoMon = '' or gv.boMon.maBoMon = :maBoMon)
             and (:q = '' or lower(gv.maNS) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(gv.nhanSu.hoTen) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<GiangVien> search(@Param("q") String q, @Param("maBoMon") String maBoMon, Pageable pageable);

    @EntityGraph(attributePaths = {"nhanSu", "boMon"})
    List<GiangVien> findAllByOrderByNhanSu_HoTenAsc();
}
