package com.offerpilot.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.offerpilot.api.mq.MqConstants.EXCHANGE;
import static com.offerpilot.api.mq.MqConstants.STATUS_QUEUE;
import static com.offerpilot.api.mq.MqConstants.STATUS_ROUTING_KEY;

/**
 * RabbitMQ 配置（offer-notification 消费者侧）
 * <p>
 * 声明与 offer-application 相同的交换机 / 队列 / 绑定。
 * RabbitMQ 中资源声明是幂等的：两个服务各自声明同名资源不会冲突。
 * <p>
 * 必须配置 JSON 消息转换器，否则无法把队列中的 JSON 消息
 * 反序列化为 {@link com.offerpilot.api.event.ApplicationEvent}。
 */
@Configuration
@EnableRabbit
public class NotificationMqConfig {

    // ==================== 交换机 ====================

    @Bean
    public TopicExchange applicationExchange() {
        // durable=true：重启后交换机不丢失
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ==================== 队列 ====================

    @Bean
    public Queue statusChangeQueue() {
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

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
