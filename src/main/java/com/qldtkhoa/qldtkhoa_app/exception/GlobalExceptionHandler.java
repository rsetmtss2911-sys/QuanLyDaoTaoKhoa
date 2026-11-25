package com.qldtkhoa.qldtkhoa_app.exception;

import com.qldtkhoa.qldtkhoa_app.util.SqlServerErrors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.UUID;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, HttpServletRequest request, Model model) {
        log.warn("Không tìm thấy tài nguyên: key={}, args={}", ex.getMessageKey(), ex.getArgs());
        populateCommonAttributes(request, model);
        model.addAttribute("messageKey", ex.getMessageKey());
        model.addAttribute("messageArgs", ex.getArgs());
        model.addAttribute("errorMessage", resolveMessage(ex.getMessageKey(), ex.getArgs(), "Không tìm thấy dữ liệu yêu cầu."));
        return "error/404";
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request, Model model) {
        log.debug("Không tìm thấy tài nguyên tĩnh: {}", ex.getResourcePath());
        populateCommonAttributes(request, model);
        model.addAttribute("messageKey", "error.notFound");
        model.addAttribute("errorMessage", resolveMessage("error.notFound", null, "Không tìm thấy tài nguyên yêu cầu."));
        return "error/404";
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessRule(BusinessRuleException ex, HttpServletRequest request, Model model) {
        log.warn("Vi phạm quy tắc học vụ: key={}, args={}", ex.getMessageKey(), ex.getArgs());
        populateCommonAttributes(request, model);
        model.addAttribute("messageKey", ex.getMessageKey());
        model.addAttribute("messageArgs", ex.getArgs());
        model.addAttribute("errorMessage", resolveMessage(ex.getMessageKey(), ex.getArgs(), ex.getMessage()));
        return "error/400";
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(Exception ex, HttpServletRequest request, Model model) {
        log.warn("Dữ liệu đầu vào không hợp lệ: {}", ex.getMessage());
        populateCommonAttributes(request, model);
        model.addAttribute("messageKey", "common.invalidInput");
        model.addAttribute("errorMessage", resolveMessage("common.invalidInput", null, "Dữ liệu biểu mẫu không hợp lệ hoặc thiếu thông tin bắt buộc."));
        return "error/400";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        log.warn("Tham số yêu cầu không hợp lệ: {}", ex.getMessage());
        populateCommonAttributes(request, model);
        model.addAttribute("messageKey", "common.invalidInput");
        model.addAttribute("errorMessage", ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : "Dữ liệu yêu cầu không hợp lệ.");
        return "error/400";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicate(DuplicateResourceException ex, HttpServletRequest request, Model model) {
        log.warn("Tài nguyên đã tồn tại: key={}, args={}", ex.getMessageKey(), ex.getArgs());
        populateCommonAttributes(request, model);
        model.addAttribute("messageKey", ex.getMessageKey());
        model.addAttribute("messageArgs", ex.getArgs());
        model.addAttribute("errorMessage", resolveMessage(ex.getMessageKey(), ex.getArgs(), "Dữ liệu đã tồn tại trong hệ thống."));
        return "error/409";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request, Model model) {
        log.warn("Xung đột toàn vẹn dữ liệu: {}", SqlServerErrors.describe(ex));
        populateCommonAttributes(request, model);
        String messageKey = SqlServerErrors.translateToMessageKey(ex);
        model.addAttribute("messageKey", messageKey);
        model.addAttribute("errorMessage", resolveMessage(messageKey, null, "Xung đột toàn vẹn dữ liệu hoặc bản ghi đang được sử dụng ở phân hệ khác."));
        return "error/409";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex) {
        // Ném tiếp để Spring Security xử lý chuyển hướng hoặc AccessDeniedHandler
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception ex, HttpServletRequest request, Model model) {
        String errorRef = UUID.randomUUID().toString().substring(0, 8);
        log.error("Lỗi không mong muốn hệ thống [ref={}]: {}", errorRef, ex.getMessage(), ex);
        populateCommonAttributes(request, model);
        model.addAttribute("errorRef", errorRef);
        return "error/500";
    }

    private void populateCommonAttributes(HttpServletRequest request, Model model) {
        if (request != null && model != null) {
            model.addAttribute("currentPath", request.getRequestURI());
        }
    }

    private String resolveMessage(String key, Object[] args, String defaultMessage) {
        if (key == null) return defaultMessage;
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return messageSource.getMessage(key, args, defaultMessage, locale);
        } catch (Exception e) {
            return defaultMessage != null ? defaultMessage : key;
        }
    }
}
