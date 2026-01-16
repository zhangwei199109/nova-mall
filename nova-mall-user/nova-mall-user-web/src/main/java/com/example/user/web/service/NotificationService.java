package com.example.user.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 短信/邮件发送抽象（当前为 mock，打印日志）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Value("${notify.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notify.mail.enabled:false}")
    private boolean mailEnabled;

    public void sendSms(String mobile, String template, String code) {
        if (!smsEnabled) {
            log.info("[MOCK SMS] to={} template={} code={}", mobile, template, code);
            return;
        }
        // TODO: 集成实际短信通道
        log.info("[SMS] to={} template={} code={}", mobile, template, code);
    }

    public void sendMail(String email, String subject, String content) {
        if (!mailEnabled) {
            log.info("[MOCK MAIL] to={} subject={} content={}", email, subject, content);
            return;
        }
        // TODO: 集成实际邮件通道
        log.info("[MAIL] to={} subject={} content={}", email, subject, content);
    }
}




