package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BangDiemSinhVienViewRepository extends JpaRepository<BangDiemSinhVienView, DangKyHocPhanId> {

    List<BangDiemSinhVienView> findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc(String maSV);

    List<BangDiemSinhVienView> findById_MaSVAndMaHKOrderByTenMonAsc(String maSV, String maHK);

    List<BangDiemSinhVienView> findById_MaLHPOrderByHoTenAsc(String maLHP);
}
