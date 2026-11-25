package com.qldtkhoa.qldtkhoa_app.dto;

import java.time.LocalDate;

public record HocKyRow(String maHK, String tenHK, Integer namHoc, LocalDate ngayBatDau, LocalDate ngayKetThuc) {}
