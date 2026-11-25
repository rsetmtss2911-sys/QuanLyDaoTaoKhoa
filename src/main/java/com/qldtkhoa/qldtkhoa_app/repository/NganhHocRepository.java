package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NganhHocRepository extends JpaRepository<NganhHoc, String> {

    boolean existsByKhoa_MaKhoa(String maKhoa);

    long countByKhoa_MaKhoa(String maKhoa);

    @EntityGraph(attributePaths = "khoa")
    List<NganhHoc> findByKhoa_MaKhoaOrderByTenNganh(String maKhoa);

    @EntityGraph(attributePaths = "khoa")
    @Query("""
           select nh from NganhHoc nh
           where (:maKhoa is null or :maKhoa = '' or nh.khoa.maKhoa = :maKhoa)
             and (:q = '' or lower(nh.maNganh) like lower(concat('%', :q, '%'))
                         or lower(nh.tenNganh) like lower(concat('%', :q, '%')))
           """)
    Page<NganhHoc> search(@Param("q") String q, @Param("maKhoa") String maKhoa, Pageable pageable);

    @EntityGraph(attributePaths = "khoa")
    List<NganhHoc> findAllByOrderByTenNganhAsc();
}
