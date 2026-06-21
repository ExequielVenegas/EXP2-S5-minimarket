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

    // Mock del repositorio: objeto falso para no usar base de datos real
    @Mock
    private InventarioRepository inventarioRepository;

    // El servicio real, con el mock inyectado dentro
    @InjectMocks
    private InventarioServiceImpl inventarioService;

    // Datos reutilizables entre pruebas
    private Producto producto;
    private Inventario inventario;

    /**
     * @BeforeEach: se ejecuta antes de CADA prueba.
     * Crea objetos base para no repetir código.
     */
    @BeforeEach
    public void setUp() {
        // Crear categoría
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");

        // Crear producto asociado al inventario
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche Entera");
        producto.setPrecio(1200.0);
        producto.setStock(50);
        producto.setCategoria(categoria);

        // Crear registro de inventario (movimiento de entrada)
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(20);
        inventario.setTipoMovimiento("Entrada"); // Valores posibles: "Entrada" o "Salida"
        inventario.setFechaMovimiento(new Date());
    }

    // =========================================================
    // PRUEBA 1: Información del Movimiento
    // La actividad pide validar que tipoMovimiento y cantidad
    // no sean nulos ni vacíos.
    // =========================================================

    /**
     * Verifica que tipoMovimiento no sea nulo.
     */
    @Test
    public void testInventario_tipoMovimientoNoDebeSerNulo() {
        assertNotNull(inventario.getTipoMovimiento(),
            "El tipo de movimiento no debe ser nulo");
    }

    /**
     * Verifica que tipoMovimiento no sea una cadena vacía.
     */
    @Test
    public void testInventario_tipoMovimientoNoDebeEstarVacio() {
        assertFalse(inventario.getTipoMovimiento().isEmpty(),
            "El tipo de movimiento no debe estar vacío");
    }

    /**
     * Verifica que tipoMovimiento sea un valor válido: "Entrada" o "Salida".
     */
    @Test
    public void testInventario_tipoMovimientoDebeSerEntradaOSalida() {
        String tipo = inventario.getTipoMovimiento();
        boolean esValido = tipo.equals("Entrada") || tipo.equals("Salida");
        assertTrue(esValido,
            "El tipo de movimiento debe ser 'Entrada' o 'Salida', pero fue: " + tipo);
    }

    /**
     * Verifica que la cantidad no sea nula.
     */
    @Test
    public void testInventario_cantidadNoDebeSerNula() {
        assertNotNull(inventario.getCantidad(),
            "La cantidad no debe ser nula");
    }

    /**
     * Verifica que la cantidad sea mayor a cero (un movimiento de 0 unidades no tiene sentido).
     */
    @Test
    public void testInventario_cantidadDebeSerMayorACero() {
        assertTrue(inventario.getCantidad() > 0,
            "La cantidad debe ser mayor a 0, fue: " + inventario.getCantidad());
    }

    /**
     * Verifica que la fecha del movimiento no sea nula.
     */
    @Test
    public void testInventario_fechaMovimientoNoDebeSerNula() {
        assertNotNull(inventario.getFechaMovimiento(),
            "La fecha del movimiento no debe ser nula");
    }

    /**
     * Caso de movimiento tipo "Salida": verifica que también es válido.
     */
    @Test
    public void testInventario_movimientoTipoSalida_esValido() {
        inventario.setTipoMovimiento("Salida");
        inventario.setCantidad(5);

        assertNotNull(inventario.getTipoMovimiento());
        assertFalse(inventario.getTipoMovimiento().isEmpty());
        assertEquals("Salida", inventario.getTipoMovimiento());
        assertTrue(inventario.getCantidad() > 0);
    }

    // =========================================================
    // PRUEBA 2: Relación Producto-Inventario
    // La actividad pide validar que el producto asociado
    // al inventario sea el correcto.
    // =========================================================

    /**
     * Verifica que el inventario tiene un producto asociado (no nulo).
     */
    @Test
    public void testInventario_productoAsociadoNoDebeSerNulo() {
        assertNotNull(inventario.getProducto(),
            "El inventario debe tener un producto asociado");
    }

    /**
     * Verifica que el producto asociado tiene el ID correcto.
     */
    @Test
    public void testInventario_productoAsociadoTieneIdCorrecto() {
        assertEquals(1L, inventario.getProducto().getId(),
            "El producto del inventario debe tener ID 1");
    }

    /**
     * Verifica que el producto asociado tiene el nombre correcto.
     */
    @Test
    public void testInventario_productoAsociadoTieneNombreCorrecto() {
        assertEquals("Leche Entera", inventario.getProducto().getNombre(),
            "El nombre del producto debe ser 'Leche Entera'");
    }

    /**
     * Verifica que el producto del inventario tiene precio y stock válidos.
     */
    @Test
    public void testInventario_productoAsociadoTieneDatosValidos() {
        Producto p = inventario.getProducto();

        assertNotNull(p.getPrecio(), "El precio del producto no debe ser nulo");
        assertTrue(p.getPrecio() > 0, "El precio debe ser positivo");
        assertNotNull(p.getStock(), "El stock del producto no debe ser nulo");
        assertTrue(p.getStock() >= 0, "El stock no puede ser negativo");
    }

    /**
     * Verifica que se puede reasignar un producto diferente al inventario.
     */
    @Test
    public void testInventario_cambiarProductoAsociado_funcionaCorrectamente() {
        // Crear un segundo producto
        Producto otroProducto = new Producto();
        otroProducto.setId(2L);
        otroProducto.setNombre("Yogur Natural");
        otroProducto.setPrecio(800.0);
        otroProducto.setStock(30);

        // Reasignar
        inventario.setProducto(otroProducto);

        // Verificar
        assertEquals(2L, inventario.getProducto().getId(),
            "Ahora el inventario debe apuntar al producto con ID 2");
        assertEquals("Yogur Natural", inventario.getProducto().getNombre());
    }

    // =========================================================
    // PRUEBA 3: Comportamiento del servicio con Mockito
    // Cubre los métodos de InventarioServiceImpl
    // =========================================================

    /**
     * Verifica que save() guarda un inventario correctamente.
     */
    @Test
    public void testSave_debeGuardarInventario() {
        // ARRANGE
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        // ACT
        Inventario resultado = inventarioService.save(inventario);

        // ASSERT
        assertNotNull(resultado, "El inventario guardado no debe ser nulo");
        assertEquals("Entrada", resultado.getTipoMovimiento());
        assertEquals(20, resultado.getCantidad());
        verify(inventarioRepository, times(1)).save(inventario);
    }

    /**
     * Verifica que findAll() retorna todos los registros de inventario.
     */
    @Test
    public void testFindAll_debeRetornarListaDeInventarios() {
        // ARRANGE: crear un segundo movimiento
        Inventario inventario2 = new Inventario();
        inventario2.setId(2L);
        inventario2.setProducto(producto);
        inventario2.setCantidad(5);
        inventario2.setTipoMovimiento("Salida");
        inventario2.setFechaMovimiento(new Date());

        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventario, inventario2));

        // ACT
        List<Inventario> lista = inventarioService.findAll();

        // ASSERT
        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(2, lista.size(), "Deben existir 2 movimientos de inventario");
        verify(inventarioRepository, times(1)).findAll();
    }

    /**
     * Verifica que findById() retorna el inventario correcto cuando existe.
     */
    @Test
    public void testFindById_conIdExistente_debeRetornarInventario() {
        // ARRANGE
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // ACT
        Inventario resultado = inventarioService.findById(1L);

        // ASSERT
        assertNotNull(resultado, "El inventario no debe ser nulo");
        assertEquals(1L, resultado.getId());
        verify(inventarioRepository, times(1)).findById(1L);
    }

    /**
     * Verifica que findById() retorna null si el inventario no existe.
     */
    @Test
    public void testFindById_conIdInexistente_debeRetornarNull() {
        // ARRANGE
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        Inventario resultado = inventarioService.findById(99L);

        // ASSERT
        assertNull(resultado, "Debe retornar null si no existe el inventario");
    }

    /**
     * Verifica que deleteById() llama al repositorio.
     */
    @Test
    public void testDeleteById_debeEliminarInventario() {
        // ARRANGE
        doNothing().when(inventarioRepository).deleteById(1L);

        // ACT
        inventarioService.deleteById(1L);

        // ASSERT
        verify(inventarioRepository, times(1)).deleteById(1L);
    }

    /**
     * Verifica que findByProductoId() retorna movimientos del producto correcto.
     */
    @Test
    public void testFindByProductoId_debeRetornarMovimientosDelProducto() {
        // ARRANGE
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Arrays.asList(inventario));

        // ACT
        List<Inventario> lista = inventarioService.findByProductoId(1L);

        // ASSERT
        assertNotNull(lista);
        assertEquals(1, lista.size(), "El producto 1 tiene 1 movimiento en el inventario");
        assertEquals("Leche Entera", lista.get(0).getProducto().getNombre());
        verify(inventarioRepository, times(1)).findByProductoId(1L);
    }

    /**
     * Cubre los getters y setters de la entidad Inventario.
     */
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
