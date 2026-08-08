package com.shopapi.producto;

import com.shopapi.categoria.Categoria;
import com.shopapi.categoria.CategoriaRepository;
import com.shopapi.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("Electronica").build();
    }

    @Test
    void crear_conCategoriaExistente_devuelveProductoCreado() {
        ProductoRequestDTO dto = new ProductoRequestDTO(
                "Teclado", BigDecimal.valueOf(59.99), 10, 1L);
        Producto productoGuardado = Producto.builder()
                .id(1L).nombre("Teclado").precio(BigDecimal.valueOf(59.99))
                .stock(10).categoria(categoria).build();

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        ProductoResponseDTO resultado = productoService.crear(dto);

        assertThat(resultado.nombre()).isEqualTo("Teclado");
        assertThat(resultado.categoriaNombre()).isEqualTo("Electronica");
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crear_conCategoriaInexistente_lanzaExcepcionYNoGuardaNada() {
        ProductoRequestDTO dto = new ProductoRequestDTO(
                "Teclado", BigDecimal.valueOf(59.99), 10, 99L);

        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crear(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria no encontrada");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_conIdInexistente_lanzaResourceNotFoundException() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}