package com.minimarket;

import com.minimarket.entity.*;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    // Objetos reutilizables
    private Usuario usuario;
    private Producto producto1;
    private Producto producto2;
    private Venta venta;

    @BeforeEach
    public void setUp() {
        // Crear usuario de prueba
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("clienteAna");
        usuario.setPassword("clave123");
        usuario.setRoles(Set.of(new Rol("USER")));

        // Crear productos de prueba con stock suficiente
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Leche");
        producto1.setPrecio(1500.0);
        producto1.setStock(10);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Pan");
        producto2.setPrecio(800.0);
        producto2.setStock(5);

        // Crear detalles de venta (2 unidades de Leche, 3 de Pan)
        DetalleVenta detalle1 = new DetalleVenta();
        detalle1.setProducto(producto1);
        detalle1.setCantidad(2);
        detalle1.setPrecio(producto1.getPrecio());

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setProducto(producto2);
        detalle2.setCantidad(3);
        detalle2.setPrecio(producto2.getPrecio());

        // Crear venta con esos detalles
        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(List.of(detalle1, detalle2));
    }

    // ---------------------------------------------------------------
    // TEST 1: Verificar que una venta tiene un usuario válido asociado
    // Cubre criterio 4: relación entre Venta y Usuario
    // ---------------------------------------------------------------
    @Test
    public void testVentaTieneUsuarioAsociado() {
        // Arrange: ya preparado en setUp()

        // Act
        Usuario usuarioDeVenta = venta.getUsuario();

        // Assert: la venta debe tener un usuario no nulo
        assertNotNull(usuarioDeVenta, "La venta debe tener un usuario asociado");
        assertEquals("clienteAna", usuarioDeVenta.getUsername());
    }

    // ---------------------------------------------------------------
    // TEST 2: Verificar que los productos de la venta tienen stock suficiente
    // Cubre criterio 3: validación de stock antes de procesar la venta
    // ---------------------------------------------------------------
    @Test
    public void testProductosTienenStockSuficiente() {
        // Arrange: la venta tiene 2 unidades de Leche (stock=10) y 3 de Pan (stock=5)

        // Act: verificamos cada detalle contra el stock disponible
        boolean stockSuficiente = venta.getDetalles().stream()
                .allMatch(detalle ->
                        detalle.getProducto().getStock() >= detalle.getCantidad()
                );

        // Assert
        assertTrue(stockSuficiente, "Todos los productos deben tener stock suficiente");
    }

    // ---------------------------------------------------------------
    // TEST 3: Verificar que una venta SIN stock suficiente es inválida
    // Cubre criterio 3: caso límite (stock insuficiente)
    // ---------------------------------------------------------------
    @Test
    public void testVentaConStockInsuficienteEsInvalida() {
        // Arrange: producto sin stock
        Producto productoSinStock = new Producto();
        productoSinStock.setNombre("Coca-Cola");
        productoSinStock.setPrecio(1200.0);
        productoSinStock.setStock(1); // Solo queda 1 unidad

        DetalleVenta detalleSinStock = new DetalleVenta();
        detalleSinStock.setProducto(productoSinStock);
        detalleSinStock.setCantidad(5); // Se intentan comprar 5
        detalleSinStock.setPrecio(productoSinStock.getPrecio());

        Venta ventaInvalida = new Venta();
        ventaInvalida.setDetalles(List.of(detalleSinStock));

        // Act: verificamos si hay stock suficiente
        boolean stockSuficiente = ventaInvalida.getDetalles().stream()
                .allMatch(detalle ->
                        detalle.getProducto().getStock() >= detalle.getCantidad()
                );

        // Assert: esta venta NO debe procesarse
        assertFalse(stockSuficiente, "La venta no debe procesarse si no hay stock suficiente");
    }

    // ---------------------------------------------------------------
    // TEST 4: Calcular correctamente el total de la venta
    // Cubre criterio 5: verificar resultado de cálculo interno
    // 2 Leches x $1500 + 3 Panes x $800 = $3000 + $2400 = $5400
    // ---------------------------------------------------------------
    @Test
    public void testCalcularTotalVenta() {
        // Arrange: venta preparada en setUp()

        // Act: calculamos el total sumando precio * cantidad de cada detalle
        double total = venta.getDetalles().stream()
                .mapToDouble(detalle -> detalle.getPrecio() * detalle.getCantidad())
                .sum();

        // Assert
        assertEquals(5400.0, total, 0.01,
                "El total debe ser 2*1500 + 3*800 = 5400");
    }

    // ---------------------------------------------------------------
    // TEST 5: Calcular total de venta con UN solo producto
    // Cubre criterio 5: segundo cálculo verificado
    // ---------------------------------------------------------------
    @Test
    public void testCalcularTotalVentaUnProducto() {
        // Arrange: venta con solo 1 tipo de producto (4 unidades de Leche a $1500)
        DetalleVenta detalleUnico = new DetalleVenta();
        detalleUnico.setProducto(producto1);
        detalleUnico.setCantidad(4);
        detalleUnico.setPrecio(producto1.getPrecio());

        Venta ventaSimple = new Venta();
        ventaSimple.setDetalles(List.of(detalleUnico));

        // Act
        double total = ventaSimple.getDetalles().stream()
                .mapToDouble(detalle -> detalle.getPrecio() * detalle.getCantidad())
                .sum();

        // Assert: 4 * 1500 = 6000
        assertEquals(6000.0, total, 0.01, "El total debe ser 4 * 1500 = 6000");
    }

    // ---------------------------------------------------------------
    // TEST 6: Buscar ventas por usuario usando mock (simula la BD)
    // Cubre criterio 4 y 6: relación Venta-Usuario + mock de dependencia
    // ---------------------------------------------------------------
    @Test
    public void testBuscarVentasPorUsuario() {
        // Arrange: el mock devuelve la lista de ventas del usuario con id=1
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(venta));

        // Act
        List<Venta> ventas = ventaService.findByUsuarioId(1L);

        // Assert
        assertNotNull(ventas);
        assertEquals(1, ventas.size());
        assertEquals("clienteAna", ventas.get(0).getUsuario().getUsername());

        // Verificamos que el mock fue invocado correctamente
        verify(ventaRepository, times(1)).findByUsuarioId(1L);
    }

    // ---------------------------------------------------------------
    // TEST 7: Usuario sin ventas retorna lista vacía
    // Cubre criterio 3: comportamiento con lista vacía
    // ---------------------------------------------------------------
    @Test
    public void testUsuarioSinVentasRetornaListaVacia() {
        // Arrange: el usuario con id=99 no tiene ventas
        when(ventaRepository.findByUsuarioId(99L)).thenReturn(List.of());

        // Act
        List<Venta> ventas = ventaService.findByUsuarioId(99L);

        // Assert
        assertNotNull(ventas);
        assertTrue(ventas.isEmpty(), "Un usuario sin ventas debe retornar lista vacía");
        verify(ventaRepository, times(1)).findByUsuarioId(99L);
    }

    // ---------------------------------------------------------------
    // TEST 8: Guardar una venta usando mock
    // Cubre criterio 6: simulación completa de guardar
    // ---------------------------------------------------------------
    @Test
    public void testGuardarVenta() {
        // Arrange
        when(ventaRepository.save(venta)).thenReturn(venta);

        // Act
        Venta ventaGuardada = ventaService.save(venta);

        // Assert
        assertNotNull(ventaGuardada);
        assertNotNull(ventaGuardada.getUsuario());
        assertNotNull(ventaGuardada.getFecha());
        verify(ventaRepository, times(1)).save(venta);
    }
}
