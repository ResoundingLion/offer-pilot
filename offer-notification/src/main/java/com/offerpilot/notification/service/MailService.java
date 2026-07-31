package com.offerpilot.notification.service;

/**
 * 邮件通知服务接口
 * <p>
 * 当前实现为日志模拟（{@link com.offerpilot.notification.service.impl.MailLogServiceImpl}），
 * 接口契约与真实实现一致——生产环境接入真实邮件时，新建实现类注入 JavaMailSender 即可切换。
 */
public interface MailService {

    /**
     * 发送一封邮件
     *
     * @param userId  接收者用户 ID
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    void send(Long userId, String subject, String content);
}
