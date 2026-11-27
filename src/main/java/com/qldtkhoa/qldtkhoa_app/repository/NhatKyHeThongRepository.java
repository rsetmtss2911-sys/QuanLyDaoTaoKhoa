package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.dto.NhatKyRow;
import com.qldtkhoa.qldtkhoa_app.entity.NhatKyHeThong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NhatKyHeThongRepository extends JpaRepository<NhatKyHeThong, Long> {

    Page<NhatKyHeThong> findAllByOrderByThoiGianDesc(Pageable pageable);

    List<NhatKyHeThong> findByDoiTuongAndMaBanGhiOrderByThoiGianDesc(String doiTuong, String maBanGhi);

    @Query("""
           select new com.qldtkhoa.qldtkhoa_app.dto.NhatKyRow(
               n.maNhatKy, n.thoiGian, n.nguoiThucHien, n.hanhDong, n.doiTuong, n.maBanGhi, n.giaTriCu, n.giaTriMoi, n.lyDo
           )
           from NhatKyHeThong n
           where (:doiTuong is null or :doiTuong = '' or n.doiTuong = :doiTuong)
             and (:hanhDong is null or :hanhDong = '' or n.hanhDong = :hanhDong)
             and (:q = '' or lower(n.nguoiThucHien) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(n.maBanGhi) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(n.lyDo) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(n.hanhDong) like lower(concat('%', :q, '%')) escape '\\')
           """)
    Page<NhatKyRow> search(@Param("q") String q,
                           @Param("doiTuong") String doiTuong,
                           @Param("hanhDong") String hanhDong,
                           Pageable pageable);

    @Query("""
           select new com.qldtkhoa.qldtkhoa_app.dto.NhatKyRow(
               n.maNhatKy, n.thoiGian, n.nguoiThucHien, n.hanhDong, n.doiTuong, n.maBanGhi, n.giaTriCu, n.giaTriMoi, n.lyDo
           )
           from NhatKyHeThong n
           where (:doiTuong is null or :doiTuong = '' or n.doiTuong = :doiTuong)
             and (:hanhDong is null or :hanhDong = '' or n.hanhDong = :hanhDong)
             and (:q = '' or lower(n.nguoiThucHien) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(n.maBanGhi) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(n.lyDo) like lower(concat('%', :q, '%')) escape '\\'
                         or lower(n.hanhDong) like lower(concat('%', :q, '%')) escape '\\')
           order by n.thoiGian desc, n.maNhatKy desc
           """)
    List<NhatKyRow> findForExport(@Param("q") String q,
                                  @Param("doiTuong") String doiTuong,
                                  @Param("hanhDong") String hanhDong,
                                  Pageable pageable);

    @Query("select distinct n.doiTuong from NhatKyHeThong n order by n.doiTuong asc")
    List<String> findDistinctDoiTuong();

    @Query("select distinct n.hanhDong from NhatKyHeThong n order by n.hanhDong asc")
    List<String> findDistinctHanhDong();
}
