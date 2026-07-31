package com.shopapi.pedido;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Page<Pedido> findByUsuarioId(Long usuarioId, Pageable pageable);
}