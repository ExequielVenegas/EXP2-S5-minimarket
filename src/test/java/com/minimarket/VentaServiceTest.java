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

    private Usuario usuario;
    private Producto producto1;
    private Producto producto2;
    private Venta venta;

    @BeforeEach
    public void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("clienteAna");
        usuario.setPassword("clave123");
        usuario.setRoles(Set.of(new Rol("USER")));

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

        DetalleVenta detalle1 = new DetalleVenta();
        detalle1.setProducto(producto1);
        detalle1.setCantidad(2);
        detalle1.setPrecio(producto1.getPrecio());

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setProducto(producto2);
        detalle2.setCantidad(3);
        detalle2.setPrecio(producto2.getPrecio());

        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(List.of(detalle1, detalle2));
    }

    @Test
    public void testVentaTieneUsuarioAsociado() {

        Usuario usuarioDeVenta = venta.getUsuario();

        assertNotNull(usuarioDeVenta, "La venta debe tener un usuario asociado");
        assertEquals("clienteAna", usuarioDeVenta.getUsername());
    }

    @Test
    public void testProductosTienenStockSuficiente() {

        boolean stockSuficiente = venta.getDetalles().stream()
                .allMatch(detalle ->
                        detalle.getProducto().getStock() >= detalle.getCantidad()
                );

        assertTrue(stockSuficiente, "Todos los productos deben tener stock suficiente");
    }

    @Test
    public void testVentaConStockInsuficienteEsInvalida() {
        Producto productoSinStock = new Producto();
        productoSinStock.setNombre("Coca-Cola");
        productoSinStock.setPrecio(1200.0);
        productoSinStock.setStock(1);

        DetalleVenta detalleSinStock = new DetalleVenta();
        detalleSinStock.setProducto(productoSinStock);
        detalleSinStock.setCantidad(5);
        detalleSinStock.setPrecio(productoSinStock.getPrecio());

        Venta ventaInvalida = new Venta();
        ventaInvalida.setDetalles(List.of(detalleSinStock));

        boolean stockSuficiente = ventaInvalida.getDetalles().stream()
                .allMatch(detalle ->
                        detalle.getProducto().getStock() >= detalle.getCantidad()
                );

        assertFalse(stockSuficiente, "La venta no debe procesarse si no hay stock suficiente");
    }

    @Test
    public void testCalcularTotalVenta() {

        double total = venta.getDetalles().stream()
                .mapToDouble(detalle -> detalle.getPrecio() * detalle.getCantidad())
                .sum();

        assertEquals(5400.0, total, 0.01,
                "El total debe ser 2*1500 + 3*800 = 5400");
    }

    @Test
    public void testCalcularTotalVentaUnProducto() {
        DetalleVenta detalleUnico = new DetalleVenta();
        detalleUnico.setProducto(producto1);
        detalleUnico.setCantidad(4);
        detalleUnico.setPrecio(producto1.getPrecio());

        Venta ventaSimple = new Venta();
        ventaSimple.setDetalles(List.of(detalleUnico));

        double total = ventaSimple.getDetalles().stream()
                .mapToDouble(detalle -> detalle.getPrecio() * detalle.getCantidad())
                .sum();

        assertEquals(6000.0, total, 0.01, "El total debe ser 4 * 1500 = 6000");
    }

    @Test
    public void testBuscarVentasPorUsuario() {
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(venta));

        List<Venta> ventas = ventaService.findByUsuarioId(1L);

        assertNotNull(ventas);
        assertEquals(1, ventas.size());
        assertEquals("clienteAna", ventas.get(0).getUsuario().getUsername());

        verify(ventaRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    public void testUsuarioSinVentasRetornaListaVacia() {
        when(ventaRepository.findByUsuarioId(99L)).thenReturn(List.of());

        List<Venta> ventas = ventaService.findByUsuarioId(99L);

        assertNotNull(ventas);
        assertTrue(ventas.isEmpty(), "Un usuario sin ventas debe retornar lista vacía");
        verify(ventaRepository, times(1)).findByUsuarioId(99L);
    }

    @Test
    public void testGuardarVenta() {
        when(ventaRepository.save(venta)).thenReturn(venta);

        Venta ventaGuardada = ventaService.save(venta);

        assertNotNull(ventaGuardada);
        assertNotNull(ventaGuardada.getUsuario());
        assertNotNull(ventaGuardada.getFecha());
        verify(ventaRepository, times(1)).save(venta);
    }
}
