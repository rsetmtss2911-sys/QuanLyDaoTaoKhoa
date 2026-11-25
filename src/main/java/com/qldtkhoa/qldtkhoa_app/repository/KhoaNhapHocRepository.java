package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KhoaNhapHocRepository extends JpaRepository<KhoaNhapHoc, String> {

    @Query("""
           select knh from KhoaNhapHoc knh
           where :q = ''
              or lower(knh.maKhoaHoc) like lower(concat('%', :q, '%'))
           """)
    Page<KhoaNhapHoc> search(@Param("q") String q, Pageable pageable);

    List<KhoaNhapHoc> findAllByOrderByNamBatDauDesc();
}
