package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonHocRepository extends JpaRepository<MonHoc, String> {

    boolean existsByBoMon_MaBoMon(String maBoMon);

    @EntityGraph(attributePaths = {"boMon", "boMon.khoa"})
    @Query("select m from MonHoc m where m.maMon = :maMon")
    java.util.Optional<MonHoc> findByIdWithDetails(@Param("maMon") String maMon);

    @EntityGraph(attributePaths = "boMon")
    @Query("""
           select m from MonHoc m
           where (:maBoMon is null or :maBoMon = '' or m.boMon.maBoMon = :maBoMon)
             and (:q = '' or lower(m.maMon) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(m.tenMon) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<MonHoc> search(@Param("q") String q, @Param("maBoMon") String maBoMon, Pageable pageable);

    @EntityGraph(attributePaths = "boMon")
    List<MonHoc> findAllByOrderByTenMonAsc();
}
