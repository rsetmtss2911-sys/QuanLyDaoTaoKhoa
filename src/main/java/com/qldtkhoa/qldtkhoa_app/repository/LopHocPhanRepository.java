package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LopHocPhanRepository extends JpaRepository<LopHocPhan, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select l from LopHocPhan l where l.maLHP = :maLHP")
    Optional<LopHocPhan> findByIdForUpdate(@Param("maLHP") String maLHP);

    @EntityGraph(attributePaths = {"monHoc", "giangVien", "giangVien.nhanSu", "hocKy"})
    @Query("select l from LopHocPhan l where l.maLHP = :maLHP")
    Optional<LopHocPhan> findByIdWithDetails(@Param("maLHP") String maLHP);

    @EntityGraph(attributePaths = {"monHoc", "giangVien", "giangVien.nhanSu", "hocKy"})
    List<LopHocPhan> findByHocKy_MaHKAndGiangVien_MaNS(String maHK, String maNS);

    @EntityGraph(attributePaths = {"monHoc", "giangVien", "giangVien.nhanSu", "hocKy"})
    List<LopHocPhan> findByGiangVien_MaNS(String maNS);

    @EntityGraph(attributePaths = {"monHoc", "giangVien", "giangVien.nhanSu", "hocKy"})
    List<LopHocPhan> findByHocKy_MaHKAndTrangThaiLop(String maHK, TrangThaiLop trangThaiLop);

    boolean existsByMonHoc_MaMon(String maMon);

    boolean existsByGiangVien_MaNS(String maNS);

    boolean existsByHocKy_MaHK(String maHK);

    @EntityGraph(attributePaths = {"monHoc", "giangVien", "giangVien.nhanSu", "hocKy"})
    @Query("""
           select l from LopHocPhan l
           where (:maHK is null or :maHK = '' or l.hocKy.maHK = :maHK)
             and (:maMon is null or :maMon = '' or l.monHoc.maMon = :maMon)
             and (:maGV is null or :maGV = '' or l.giangVien.maNS = :maGV)
             and (:status is null or l.trangThaiLop = :status)
             and (:q = '' or lower(l.maLHP) like lower(concat('%', :q, '%'))
                         or lower(l.monHoc.tenMon) like lower(concat('%', :q, '%'))
                         or lower(l.giangVien.nhanSu.hoTen) like lower(concat('%', :q, '%')))
           """)
    Page<LopHocPhan> search(@Param("q") String q,
                            @Param("maHK") String maHK,
                            @Param("maMon") String maMon,
                            @Param("maGV") String maGV,
                            @Param("status") TrangThaiLop status,
                            Pageable pageable);

    long countByTrangThaiLop(TrangThaiLop status);

    @EntityGraph(attributePaths = {"monHoc", "giangVien", "giangVien.nhanSu", "hocKy"})
    @Query("""
           select l from LopHocPhan l
           where l.hocKy.maHK = :maHK
             and l.trangThaiLop <> com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop.DA_HUY
             and not exists (select 1 from LichGiangDay lgd where lgd.lopHocPhan.maLHP = l.maLHP)
           order by l.monHoc.tenMon asc, l.maLHP asc
           """)
    List<LopHocPhan> findUnscheduledSections(@Param("maHK") String maHK);

    @EntityGraph(attributePaths = {"monHoc", "giangVien", "giangVien.nhanSu", "hocKy"})
    @Query("""
           select l from LopHocPhan l
           where l.hocKy.maHK = :maHK
             and l.trangThaiLop <> com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop.DA_HUY
           order by l.monHoc.tenMon asc, l.maLHP asc
           """)
    List<LopHocPhan> findActiveSectionsByHocKy(@Param("maHK") String maHK);
}
