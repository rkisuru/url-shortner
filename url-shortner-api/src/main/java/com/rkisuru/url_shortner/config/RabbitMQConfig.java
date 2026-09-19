package com.rkisuru.url_shortner.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "url-shortener.clicks";
    public static final String QUEUE_NAME    = "click-events";
    public static final String ROUTING_KEY   = "click.recorded";

    @Bean
    public TopicExchange clickExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue clickQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding clickBinding(Queue clickQueue, TopicExchange clickExchange) {
        return BindingBuilder.bind(clickQueue).to(clickExchange).with(ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
