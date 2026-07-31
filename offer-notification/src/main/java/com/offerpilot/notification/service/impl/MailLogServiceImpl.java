package com.offerpilot.notification.service.impl;

import com.offerpilot.notification.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 邮件发送的日志模拟实现
 * <p>
 * 不在本地配置真实 SMTP（需要邮箱授权码且依赖外网），
 * 用日志打印模拟"已发送邮件"，接口契约与真实实现一致，不影响业务闭环演示。
 * <p>
 * 生产环境切换真实邮件：
 * 1. Nacos offer-notification.yaml 配置 spring.mail.host / port / username / password
 * 2. 新建 MailSmtpServiceImpl implements MailService，注入 JavaMailSender
 */
@Slf4j
@Service
public class MailLogServiceImpl implements MailService {

    @Override
    public void send(Long userId, String subject, String content) {
        log.info("📧 [邮件通知] 发送给用户#{}，主题：{}，正文：{}", userId, subject, content);
    }
}
