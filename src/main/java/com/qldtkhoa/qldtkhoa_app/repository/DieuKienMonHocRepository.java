package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.DieuKienMonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.id.DieuKienMonHocId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DieuKienMonHocRepository extends JpaRepository<DieuKienMonHoc, DieuKienMonHocId> {

    @EntityGraph(attributePaths = {"monHoc", "monTienQuyet"})
    List<DieuKienMonHoc> findByMonHoc_MaMon(String maMon);

    @EntityGraph(attributePaths = {"monHoc", "monTienQuyet"})
    List<DieuKienMonHoc> findByMonHoc_MaMonIn(Collection<String> maMons);

    @EntityGraph(attributePaths = {"monHoc", "monTienQuyet"})
    List<DieuKienMonHoc> findByMonTienQuyet_MaMon(String maMonTienQuyet);

    boolean existsByMonHoc_MaMonAndMonTienQuyet_MaMon(String maMon, String maMonTienQuyet);

    long countByMonHoc_MaMon(String maMon);

    @Query("""
           select dk.monHoc.maMon, count(dk)
           from DieuKienMonHoc dk
           where dk.monHoc.maMon in :maMons
           group by dk.monHoc.maMon
           """)
    List<Object[]> countByMonHocIn(@Param("maMons") Collection<String> maMons);
}
