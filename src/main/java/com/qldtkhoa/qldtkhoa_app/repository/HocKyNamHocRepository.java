package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HocKyNamHocRepository extends JpaRepository<HocKyNamHoc, String> {

    @Query("""
           select hk from HocKyNamHoc hk
           where :q = ''
              or lower(hk.maHK) like lower(concat('%', :q, '%')) escape '\\'
              or lower(hk.tenHK) like lower(concat('%', :q, '%')) escape '\\'
           """)
    Page<HocKyNamHoc> search(@Param("q") String q, Pageable pageable);

    List<HocKyNamHoc> findAllByOrderByNamHocDescMaHKAsc();

    Optional<HocKyNamHoc> findTopByOrderByNamHocDescMaHKDesc();

    @Query("""
           select hk from HocKyNamHoc hk
           where hk.ngayBatDau is not null and hk.ngayKetThuc is not null
             and hk.ngayBatDau <= :today and hk.ngayKetThuc >= :today
           order by hk.ngayBatDau desc
           """)
    List<HocKyNamHoc> findCoveringTerms(@Param("today") LocalDate today);

    @Query("""
           select hk from HocKyNamHoc hk
           where hk.ngayBatDau is not null and hk.ngayBatDau <= :today
           order by hk.ngayBatDau desc
           """)
    List<HocKyNamHoc> findStartedBeforeOrOn(@Param("today") LocalDate today);

    @Query("""
           select count(hk) > 0 from HocKyNamHoc hk
           where (:excludeMaHK is null or hk.maHK <> :excludeMaHK)
             and hk.ngayBatDau is not null and hk.ngayKetThuc is not null
             and hk.ngayBatDau <= :end and hk.ngayKetThuc >= :start
           """)
    boolean existsOverlappingTerm(@Param("excludeMaHK") String excludeMaHK,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);
}
