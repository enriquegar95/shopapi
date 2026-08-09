package com.shopapi.pedido;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(
            @Valid @RequestBody PedidoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoResponseDTO creado = pedidoService.crear(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<Page<PedidoResponseDTO>> listar(
            Pageable pageable, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.listar(pageable, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Cambiar el estado de un pedido",
            description = "Las unicas transiciones validas son: PENDIENTE -> CONFIRMADO o CANCELADO; "
                    + "CONFIRMADO -> ENVIADO o CANCELADO; ENVIADO -> ENTREGADO. "
                    + "Cancelar un pedido repone automaticamente el stock de sus lineas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "409", description = "La transicion de estado solicitada no es valida")
    })
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambiarEstadoDTO dto) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, dto.estado()));
    }
}