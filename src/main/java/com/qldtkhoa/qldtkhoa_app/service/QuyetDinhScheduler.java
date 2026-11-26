package com.qldtkhoa.qldtkhoa_app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Lịch chạy nền đồng bộ quyết định học vụ đến ngày hiệu lực (GATE 3d).
 * <p>
 * [TIMEZONE NOTE - GATE 3d] Cron sử dụng {@code zone = "${app.academic.timezone:Asia/Ho_Chi_Minh}"}
 * để luôn kích hoạt lúc 00:05 giờ Việt Nam (UTC+7) kể cả khi JVM/container chạy múi giờ UTC.
 * Kết hợp {@link ApplicationReadyEvent} để đồng bộ bù nếu máy chủ tắt qua đêm.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class QuyetDinhScheduler {

    private final QuyetDinhSyncService quyetDinhSyncService;

    @Scheduled(cron = "${app.academic.decision-sync-cron:0 5 0 * * *}",
               zone = "${app.academic.timezone:Asia/Ho_Chi_Minh}")
    public void runDailySync() {
        int synced = quyetDinhSyncService.syncDueDecisions();
        if (synced > 0) {
            log.info("Đã đồng bộ {} hồ sơ sinh viên theo quyết định học vụ đến hạn", synced);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        try {
            int synced = quyetDinhSyncService.syncDueDecisions();
            if (synced > 0) {
                log.info("Đã đồng bộ bù {} hồ sơ sinh viên khi khởi động ứng dụng", synced);
            }
        } catch (Exception ex) {
            log.warn("Bỏ qua đồng bộ quyết định lúc khởi động: {}", ex.getMessage());
        }
    }
}
