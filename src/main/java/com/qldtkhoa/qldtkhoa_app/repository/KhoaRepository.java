package com.qldtkhoa.qldtkhoa_app.repository;

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
              or lower(k.maKhoa) like lower(concat('%', :q, '%'))
              or lower(k.tenKhoa) like lower(concat('%', :q, '%'))
           """)
    Page<Khoa> search(@Param("q") String q, Pageable pageable);

    List<Khoa> findAllByOrderByTenKhoaAsc();
}
