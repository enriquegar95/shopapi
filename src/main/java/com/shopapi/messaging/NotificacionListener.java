package com.shopapi.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionListener {

    private final Jackson2JsonMessageConverter messageConverter;

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFICACIONES)
    public void escucharEventoPedido(Message mensaje) {
        Object evento = messageConverter.fromMessage(mensaje);

        if (evento instanceof PedidoCreadoEvent creado) {
            log.info("[NOTIFICACION] Pedido {} creado para {} — total {}",
                    creado.pedidoId(), creado.usuarioEmail(), creado.total());
        } else if (evento instanceof PedidoEstadoCambiadoEvent cambiado) {
            log.info("[NOTIFICACION] Pedido {}: {} -> {} — avisando a {}",
                    cambiado.pedidoId(), cambiado.estadoAnterior(),
                    cambiado.estadoNuevo(), cambiado.usuarioEmail());
        } else {
            log.warn("[NOTIFICACION] Evento no reconocido: {}", evento.getClass().getName());
        }
    }
}