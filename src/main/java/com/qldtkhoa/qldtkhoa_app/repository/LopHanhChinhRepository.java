package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LopHanhChinhRepository extends JpaRepository<LopHanhChinh, String> {

    boolean existsByNganhHoc_MaNganh(String maNganh);

    boolean existsByKhoaNhapHoc_MaKhoaHoc(String maKhoaHoc);

    boolean existsByCoVan_MaNS(String maNS);

    @EntityGraph(attributePaths = {"nganhHoc", "khoaNhapHoc", "coVan", "coVan.nhanSu"})
    @Query("select lhc from LopHanhChinh lhc where lhc.maLopHC = :maLopHC")
    java.util.Optional<LopHanhChinh> findByIdWithDetails(@Param("maLopHC") String maLopHC);

    @EntityGraph(attributePaths = {"nganhHoc", "khoaNhapHoc", "coVan", "coVan.nhanSu"})
    List<LopHanhChinh> findByCoVan_MaNS(String maNS);

    @EntityGraph(attributePaths = {"nganhHoc", "khoaNhapHoc", "coVan", "coVan.nhanSu"})
    @Query("""
           select lhc from LopHanhChinh lhc
           where (:maNganh is null or :maNganh = '' or lhc.nganhHoc.maNganh = :maNganh)
             and (:maKhoaHoc is null or :maKhoaHoc = '' or lhc.khoaNhapHoc.maKhoaHoc = :maKhoaHoc)
             and (:q = '' or lower(lhc.maLopHC) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(lhc.tenLopHC) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<LopHanhChinh> search(@Param("q") String q,
                              @Param("maNganh") String maNganh,
                              @Param("maKhoaHoc") String maKhoaHoc,
                              Pageable pageable);

    @EntityGraph(attributePaths = {"nganhHoc", "khoaNhapHoc", "coVan", "coVan.nhanSu"})
    List<LopHanhChinh> findAllByOrderByTenLopHCAsc();
}
