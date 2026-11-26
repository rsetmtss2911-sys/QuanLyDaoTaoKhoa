package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaRow;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KhoaRepository extends JpaRepository<Khoa, String> {

    @Query("""
           select k from Khoa k
           where :q = ''
              or lower(k.maKhoa) like lower(concat('%', :q, '%')) escape '\\'
              or lower(k.tenKhoa) like lower(concat('%', :q, '%')) escape '\\'
           """)
    Page<Khoa> search(@Param("q") String q, Pageable pageable);

    @Query("""
           select new com.qldtkhoa.qldtkhoa_app.dto.KhoaRow(
               k.maKhoa, k.tenKhoa, k.namThanhLap,
               (select count(bm) from BoMon bm where bm.khoa.maKhoa = k.maKhoa),
               (select count(nh) from NganhHoc nh where nh.khoa.maKhoa = k.maKhoa)
           )
           from Khoa k
           where :q = ''
              or lower(k.maKhoa) like lower(concat('%', :q, '%')) escape '\\'
              or lower(k.tenKhoa) like lower(concat('%', :q, '%')) escape '\\'
           """)
    Page<KhoaRow> searchRows(@Param("q") String q, Pageable pageable);

    List<Khoa> findAllByOrderByTenKhoaAsc();
}
