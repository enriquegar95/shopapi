package com.shopapi.messaging;

import com.shopapi.IntegrationTestBase;
import com.shopapi.categoria.Categoria;
import com.shopapi.categoria.CategoriaRepository;
import com.shopapi.pedido.LineaPedidoRequestDTO;
import com.shopapi.pedido.PedidoRequestDTO;
import com.shopapi.pedido.PedidoService;
import com.shopapi.producto.Producto;
import com.shopapi.producto.ProductoRepository;
import com.shopapi.usuario.RolUsuario;
import com.shopapi.usuario.Usuario;
import com.shopapi.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(PedidoEventPublicationIntegrationTest.CapturaEventosListener.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PedidoEventPublicationIntegrationTest extends IntegrationTestBase {

    @Autowired private PedidoService pedidoService;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CapturaEventosListener capturaEventosListener;

    @Test
    void crearPedido_publicaEventoTrasElCommit() {
        Categoria categoria = categoriaRepository.save(Categoria.builder().nombre("Electronica Eventos").build());
        Producto producto = productoRepository.save(Producto.builder()
                .nombre("Teclado").precio(BigDecimal.TEN).stock(10).categoria(categoria).build());
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Cliente Test").email("evento@test.com")
                .password(passwordEncoder.encode("x")).rol(RolUsuario.CLIENTE).build());

        PedidoRequestDTO dto = new PedidoRequestDTO(usuario.getId(),
                List.of(new LineaPedidoRequestDTO(producto.getId(), 2)));

        pedidoService.crear(dto, "evento@test.com");

        await().atMost(ofSeconds(5)).untilAsserted(() ->
                assertThat(capturaEventosListener.getEventos())
                        .anySatisfy(evento -> assertThat(evento).isInstanceOf(PedidoCreadoEvent.class)));
    }

    @Component
    static class CapturaEventosListener {

        private final Jackson2JsonMessageConverter messageConverter;
        private final List<Object> eventos = new CopyOnWriteArrayList<>();

        CapturaEventosListener(Jackson2JsonMessageConverter messageConverter) {
            this.messageConverter = messageConverter;
        }

        @RabbitListener(bindings = @QueueBinding(
                value = @org.springframework.amqp.rabbit.annotation.Queue(value = "test.pedidos.captura", autoDelete = "true"),
                exchange = @Exchange(value = RabbitConfig.EXCHANGE, type = "topic"),
                key = "pedido.#"
        ))
        public void capturar(org.springframework.amqp.core.Message mensaje) {
            eventos.add(messageConverter.fromMessage(mensaje));
        }

        List<Object> getEventos() {
            return eventos;
        }
    }
}