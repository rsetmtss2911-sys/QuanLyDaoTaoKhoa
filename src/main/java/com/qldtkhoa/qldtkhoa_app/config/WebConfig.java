package com.qldtkhoa.qldtkhoa_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class WebConfig {

    /**
     * [TIMEZONE NOTE - GATE 3d]
     * Toàn bộ tầng Java luôn dùng múi giờ nghiệp vụ {@code Asia/Ho_Chi_Minh} (UTC+7) qua bean {@link Clock} này,
     * độc lập với múi giờ OS/JVM khi deploy lên container/cloud UTC.
     * Lưu ý: cần giữ đồng bộ với {@code @Today} trong {@code sql/Patch_v3_App_QLDTKhoa.sql}
     * (chuyển sang {@code AT TIME ZONE 'SE Asia Standard Time'} khi SQL Server chạy ở môi trường UTC).
     */
    @Bean
    public Clock systemClock(@Value("${app.academic.timezone:Asia/Ho_Chi_Minh}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
