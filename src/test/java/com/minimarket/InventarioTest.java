package com.minimarket;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.impl.InventarioServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventarioTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Producto producto;
    private Inventario inventario;

    @BeforeEach
    public void setUp() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche Entera");
        producto.setPrecio(1200.0);
        producto.setStock(50);
        producto.setCategoria(categoria);

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(20);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());
    }

    @Test
    public void testInventario_tipoMovimientoNoDebeSerNulo() {
        assertNotNull(inventario.getTipoMovimiento(),
            "El tipo de movimiento no debe ser nulo");
    }

    @Test
    public void testInventario_tipoMovimientoNoDebeEstarVacio() {
        assertFalse(inventario.getTipoMovimiento().isEmpty(),
            "El tipo de movimiento no debe estar vacío");
    }

    @Test
    public void testInventario_tipoMovimientoDebeSerEntradaOSalida() {
        String tipo = inventario.getTipoMovimiento();
        boolean esValido = tipo.equals("Entrada") || tipo.equals("Salida");
        assertTrue(esValido,
            "El tipo de movimiento debe ser 'Entrada' o 'Salida', pero fue: " + tipo);
    }

    @Test
    public void testInventario_cantidadNoDebeSerNula() {
        assertNotNull(inventario.getCantidad(),
            "La cantidad no debe ser nula");
    }

    @Test
    public void testInventario_cantidadDebeSerMayorACero() {
        assertTrue(inventario.getCantidad() > 0,
            "La cantidad debe ser mayor a 0, fue: " + inventario.getCantidad());
    }

    @Test
    public void testInventario_fechaMovimientoNoDebeSerNula() {
        assertNotNull(inventario.getFechaMovimiento(),
            "La fecha del movimiento no debe ser nula");
    }

    @Test
    public void testInventario_movimientoTipoSalida_esValido() {
        inventario.setTipoMovimiento("Salida");
        inventario.setCantidad(5);

        assertNotNull(inventario.getTipoMovimiento());
        assertFalse(inventario.getTipoMovimiento().isEmpty());
        assertEquals("Salida", inventario.getTipoMovimiento());
        assertTrue(inventario.getCantidad() > 0);
    }

    @Test
    public void testInventario_productoAsociadoNoDebeSerNulo() {
        assertNotNull(inventario.getProducto(),
            "El inventario debe tener un producto asociado");
    }

    @Test
    public void testInventario_productoAsociadoTieneIdCorrecto() {
        assertEquals(1L, inventario.getProducto().getId(),
            "El producto del inventario debe tener ID 1");
    }

    @Test
    public void testInventario_productoAsociadoTieneNombreCorrecto() {
        assertEquals("Leche Entera", inventario.getProducto().getNombre(),
            "El nombre del producto debe ser 'Leche Entera'");
    }

    @Test
    public void testInventario_productoAsociadoTieneDatosValidos() {
        Producto p = inventario.getProducto();

        assertNotNull(p.getPrecio(), "El precio del producto no debe ser nulo");
        assertTrue(p.getPrecio() > 0, "El precio debe ser positivo");
        assertNotNull(p.getStock(), "El stock del producto no debe ser nulo");
        assertTrue(p.getStock() >= 0, "El stock no puede ser negativo");
    }

    @Test
    public void testInventario_cambiarProductoAsociado_funcionaCorrectamente() {
        Producto otroProducto = new Producto();
        otroProducto.setId(2L);
        otroProducto.setNombre("Yogur Natural");
        otroProducto.setPrecio(800.0);
        otroProducto.setStock(30);

        inventario.setProducto(otroProducto);

        assertEquals(2L, inventario.getProducto().getId(),
            "Ahora el inventario debe apuntar al producto con ID 2");
        assertEquals("Yogur Natural", inventario.getProducto().getNombre());
    }

    @Test
    public void testSave_debeGuardarInventario() {
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.save(inventario);

        assertNotNull(resultado, "El inventario guardado no debe ser nulo");
        assertEquals("Entrada", resultado.getTipoMovimiento());
        assertEquals(20, resultado.getCantidad());
        verify(inventarioRepository, times(1)).save(inventario);
    }

    @Test
    public void testFindAll_debeRetornarListaDeInventarios() {
        Inventario inventario2 = new Inventario();
        inventario2.setId(2L);
        inventario2.setProducto(producto);
        inventario2.setCantidad(5);
        inventario2.setTipoMovimiento("Salida");
        inventario2.setFechaMovimiento(new Date());

        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventario, inventario2));

        List<Inventario> lista = inventarioService.findAll();

        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(2, lista.size(), "Deben existir 2 movimientos de inventario");
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_conIdExistente_debeRetornarInventario() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        Inventario resultado = inventarioService.findById(1L);

        assertNotNull(resultado, "El inventario no debe ser nulo");
        assertEquals(1L, resultado.getId());
        verify(inventarioRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_conIdInexistente_debeRetornarNull() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        Inventario resultado = inventarioService.findById(99L);

        assertNull(resultado, "Debe retornar null si no existe el inventario");
    }

    @Test
    public void testDeleteById_debeEliminarInventario() {
        doNothing().when(inventarioRepository).deleteById(1L);

        inventarioService.deleteById(1L);

        verify(inventarioRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByProductoId_debeRetornarMovimientosDelProducto() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Arrays.asList(inventario));

        List<Inventario> lista = inventarioService.findByProductoId(1L);

        assertNotNull(lista);
        assertEquals(1, lista.size(), "El producto 1 tiene 1 movimiento en el inventario");
        assertEquals("Leche Entera", lista.get(0).getProducto().getNombre());
        verify(inventarioRepository, times(1)).findByProductoId(1L);
    }

    @Test
    public void testInventario_gettersYSettersFuncionanCorrectamente() {
        Date fecha = new Date();
        Inventario inv = new Inventario();
        inv.setId(9L);
        inv.setProducto(producto);
        inv.setCantidad(15);
        inv.setTipoMovimiento("Entrada");
        inv.setFechaMovimiento(fecha);

        assertEquals(9L, inv.getId());
        assertEquals(producto, inv.getProducto());
        assertEquals(15, inv.getCantidad());
        assertEquals("Entrada", inv.getTipoMovimiento());
        assertEquals(fecha, inv.getFechaMovimiento());
    }
}
