package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HocKyNamHocRepository extends JpaRepository<HocKyNamHoc, String> {

    @Query("""
           select hk from HocKyNamHoc hk
           where :q = ''
              or lower(hk.maHK) like lower(concat('%', :q, '%'))
              or lower(hk.tenHK) like lower(concat('%', :q, '%'))
           """)
    Page<HocKyNamHoc> search(@Param("q") String q, Pageable pageable);

    List<HocKyNamHoc> findAllByOrderByNamHocDescMaHKAsc();

    Optional<HocKyNamHoc> findTopByOrderByNamHocDescMaHKDesc();
}
