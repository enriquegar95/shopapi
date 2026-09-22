package com.shopapi.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PedidoEventBridge {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCrearPedido(PedidoCreadoEvent evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "pedido.creado", evento);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarEstado(PedidoEstadoCambiadoEvent evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "pedido.estado.cambiado", evento);
    }
}