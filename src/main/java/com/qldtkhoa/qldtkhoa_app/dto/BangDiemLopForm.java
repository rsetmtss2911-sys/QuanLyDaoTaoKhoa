package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BangDiemLopForm {
    private String maLHP;

    @Valid
    private List<DiemEntryForm> entries = new ArrayList<>();
}

