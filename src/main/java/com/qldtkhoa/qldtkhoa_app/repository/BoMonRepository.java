package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoMonRepository extends JpaRepository<BoMon, String> {

    boolean existsByKhoa_MaKhoa(String maKhoa);

    long countByKhoa_MaKhoa(String maKhoa);

    @EntityGraph(attributePaths = "khoa")
    List<BoMon> findByKhoa_MaKhoaOrderByTenBoMon(String maKhoa);

    @EntityGraph(attributePaths = "khoa")
    @Query("""
           select bm from BoMon bm
           where (:maKhoa is null or :maKhoa = '' or bm.khoa.maKhoa = :maKhoa)
             and (:q = '' or lower(bm.maBoMon) like lower(concat('%', :q, '%'))
                         or lower(bm.tenBoMon) like lower(concat('%', :q, '%')))
           """)
    Page<BoMon> search(@Param("q") String q, @Param("maKhoa") String maKhoa, Pageable pageable);

    @EntityGraph(attributePaths = "khoa")
    List<BoMon> findAllByOrderByTenBoMonAsc();
}
