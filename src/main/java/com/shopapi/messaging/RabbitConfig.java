package com.shopapi.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "shopapi.pedidos";
    public static final String QUEUE_NOTIFICACIONES = "notificaciones.pedidos";
    public static final String DLX = "shopapi.pedidos.dlx";
    public static final String DLQ = "notificaciones.pedidos.dlq";

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange pedidosDlx() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICACIONES)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding notificacionesBinding(Queue notificacionesQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(notificacionesQueue).to(pedidosExchange).with("pedido.#");
    }

    @Bean
    public Binding dlqBinding(Queue notificacionesDlq, DirectExchange pedidosDlx) {
        return BindingBuilder.bind(notificacionesDlq).to(pedidosDlx).with(QUEUE_NOTIFICACIONES);
    }

    // TODO: Jackson2JsonMessageConverter esta deprecada desde Spring AMQP 4.0
    // en favor de JacksonJsonMessageConverter (basada en Jackson 3). Se mantiene por ahora
    // por el mismo motivo que en RedisConfig: el reemplazo es demasiado reciente.
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.shopapi.messaging");

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(new ObjectMapper());
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }


}