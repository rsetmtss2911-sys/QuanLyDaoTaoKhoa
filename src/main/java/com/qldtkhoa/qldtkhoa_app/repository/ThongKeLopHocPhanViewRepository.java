package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.view.ThongKeLopHocPhanView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThongKeLopHocPhanViewRepository extends ReadOnlyRepository<ThongKeLopHocPhanView, String> {

    List<ThongKeLopHocPhanView> findByMaHKOrderByTenMonAsc(String maHK);

    List<ThongKeLopHocPhanView> findByMaHKAndMaGVOrderByTenMonAsc(String maHK, String maGV);

    @Query("""
           select v from ThongKeLopHocPhanView v
           where (:maHK is null or :maHK = '' or v.maHK = :maHK)
             and (:q = '' or lower(v.maLHP) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(v.tenMon) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(v.tenGiangVien) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<ThongKeLopHocPhanView> search(@Param("q") String q, @Param("maHK") String maHK, Pageable pageable);
}
