package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.VaiTroRow;
import com.qldtkhoa.qldtkhoa_app.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VaiTroService {

    private final VaiTroRepository vaiTroRepository;

    public List<VaiTroRow> findAll() {
        return vaiTroRepository.findAll().stream()
                .map(v -> new VaiTroRow(v.getMaVaiTro(), v.getTenVaiTro()))
                .toList();
    }
}
