package com.offerpilot.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 交换机/队列/绑定配置
 * <p>
 * 使用 Topic Exchange，支持按 routing key 通配符匹配。
 * 当前路由规则：
 *   - application.status.change → 投递状态变更事件
 * 后续可扩展：
 *   - application.interview.created → 面试创建事件
 *   - application.offer.sent → Offer 发出事件
 */
@Configuration
public class RabbitMQConfig {

    /** Topic 交换机名称 */
    public static final String EXCHANGE = "offer.application.exchange";

    /** 状态变更队列名称 */
    public static final String STATUS_QUEUE = "offer.application.status.queue";

    /** 状态变更路由键 */
    public static final String STATUS_ROUTING_KEY = "application.status.change";

    // ==================== 交换机 ====================

    @Bean
    public TopicExchange applicationExchange() {
        // durable=true：重启后交换机不丢失
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ==================== 队列 ====================

    @Bean
    public Queue statusChangeQueue() {
        // durable=true：重启后队列不丢失
        // exclusive=false：多个消费者可共享
        // autoDelete=false：没有消费者时不自动删除
        return new Queue(STATUS_QUEUE, true, false, false);
    }

    // ==================== 绑定 ====================

    @Bean
    public Binding statusChangeBinding() {
        return BindingBuilder.bind(statusChangeQueue())
                .to(applicationExchange())
                .with(STATUS_ROUTING_KEY);
    }

    // ==================== 消息序列化 ====================

    /**
     * Jackson JSON 消息转换器
     * <p>
     * 替代默认的 SimpleMessageConverter，使 RabbitMQ 支持任意 POJO 收发。
     * 发送时自动序列化为 JSON，接收时自动反序列化为目标类型。
     * 同时注册 JavaTimeModule 以正确处理 LocalDateTime 等 Java 8 时间类型。
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 配置 RabbitTemplate 使用 JSON 序列化
     * <p>
     * 如果只声明 messageConverter Bean，Spring Boot 会自动装配到 RabbitTemplate。
     * 显式声明是为了确保类型安全，也方便后续扩展（如添加自定义拦截器）。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
