package com.qldtkhoa.qldtkhoa_app.util;

import java.io.Serializable;

public record FlashMessage(String type, String key, Object[] args) implements Serializable {

    public static FlashMessage success(String key, Object... args) {
        return new FlashMessage("success", key, args);
    }

    public static FlashMessage danger(String key, Object... args) {
        return new FlashMessage("danger", key, args);
    }

    public static FlashMessage warning(String key, Object... args) {
        return new FlashMessage("warning", key, args);
    }

    public static FlashMessage info(String key, Object... args) {
        return new FlashMessage("info", key, args);
    }
}
