package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BangDiemLopForm {

    @Valid
    private List<DiemEntryForm> entries = new ArrayList<>();

    @Size(max = 255, message = "{grade.editReason.size}")
    private String lyDoChinhSua;
}
