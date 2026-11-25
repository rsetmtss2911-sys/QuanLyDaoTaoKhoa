package com.qldtkhoa.qldtkhoa_app.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * Lớp cha dùng chung cho các Entity có khóa chính tự gán (business-assigned key).
 * Giúp Spring Data JPA phân biệt bản ghi mới để gọi thẳng persist (INSERT)
 * thay vì thực hiện một câu lệnh SELECT merge không cần thiết.
 */
@MappedSuperclass
public abstract class AssignedIdEntity<ID extends Serializable> implements Persistable<ID> {

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
