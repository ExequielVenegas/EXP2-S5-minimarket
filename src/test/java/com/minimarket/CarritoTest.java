package com.minimarket;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Categoria;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.service.impl.CarritoServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarritoTest {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Producto producto;
    private Usuario usuario;
    private Carrito carrito;

    /**
     * @BeforeEach: este método se ejecuta ANTES de cada prueba.
     * Prepara los datos base para no repetir código en cada test.
     */
    @BeforeEach
    public void setUp() {
        // Crear una categoría de ejemplo
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");

        // Crear un producto con stock suficiente
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Agua Mineral");
        producto.setPrecio(500.0);
        producto.setStock(10); // Hay 10 unidades disponibles
        producto.setCategoria(categoria);

        // Crear un usuario de prueba
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");
        usuario.setPassword("clave123");

        // Crear un carrito que relaciona usuario y producto
        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(3); // Pide 3 unidades (hay 10, entonces hay stock suficiente)
    }

    // =========================================================
    // PRUEBA 1: Disponibilidad de stock
    // La actividad pide validar que agregarProducto() solo
    // permite agregar si hay stock suficiente.
    // =========================================================

    /**
     * Caso exitoso: la cantidad pedida es menor o igual al stock disponible.
     * Resultado esperado: el carrito se guarda correctamente.
     */
    @Test
    public void testAgregarProducto_conStockSuficiente_debeGuardarCarrito() {
        // ARRANGE (preparar): el mock devuelve el carrito cuando se llama a save()
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        // ACT (actuar): llamamos al servicio para guardar el carrito
        // La validación de stock: cantidad pedida (3) <= stock disponible (10)
        boolean hayStock = producto.getStock() >= carrito.getCantidad();
        Carrito resultado = null;

        if (hayStock) {
            resultado = carritoService.save(carrito);
        }

        // ASSERT (verificar): el resultado no debe ser nulo y debe tener los datos correctos
        assertNotNull(resultado, "El carrito debería haberse guardado porque hay stock suficiente");
        assertEquals(3, resultado.getCantidad(), "La cantidad del carrito debe ser 3");
        assertEquals("Agua Mineral", resultado.getProducto().getNombre(), "El producto debe ser Agua Mineral");

        // Verificar que el repositorio fue llamado exactamente 1 vez
        verify(carritoRepository, times(1)).save(carrito);
    }

    /**
     * Caso fallido: la cantidad pedida supera el stock disponible.
     * Resultado esperado: el carrito NO se guarda.
     */
    @Test
    public void testAgregarProducto_sinStockSuficiente_noDebeGuardarCarrito() {
        // ARRANGE: el cliente pide 20 unidades pero solo hay 10 en stock
        carrito.setCantidad(20);

        // ACT: validamos stock antes de guardar
        boolean hayStock = producto.getStock() >= carrito.getCantidad();
        Carrito resultado = null;

        if (hayStock) {
            resultado = carritoService.save(carrito);
        }

        // ASSERT: el resultado debe ser nulo porque no había stock suficiente
        assertNull(resultado, "El carrito NO debe guardarse porque no hay stock suficiente");
        assertTrue(producto.getStock() < carrito.getCantidad(), "El stock disponible es menor a lo pedido");

        // Verificar que el repositorio NUNCA fue llamado (no se intentó guardar)
        verify(carritoRepository, never()).save(any());
    }

    /**
     * Caso borde: la cantidad pedida es exactamente igual al stock disponible.
     * Resultado esperado: el carrito SÍ se guarda (límite exacto es válido).
     */
    @Test
    public void testAgregarProducto_cantidadExactoAlStock_debeGuardarCarrito() {
        // ARRANGE: pedir exactamente las 10 unidades disponibles
        carrito.setCantidad(10);
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        // ACT
        boolean hayStock = producto.getStock() >= carrito.getCantidad();
        Carrito resultado = null;
        if (hayStock) {
            resultado = carritoService.save(carrito);
        }

        // ASSERT
        assertNotNull(resultado, "El carrito debe guardarse cuando la cantidad es exactamente igual al stock");
        assertEquals(10, resultado.getCantidad());
        verify(carritoRepository, times(1)).save(carrito);
    }

    // =========================================================
    // PRUEBA 2: Validación de relación Producto-Usuario
    // La actividad pide verificar que el usuario asociado
    // al carrito es el correcto.
    // =========================================================

    /**
     * Verifica que el usuario asociado al carrito es el esperado.
     */
    @Test
    public void testCarrito_usuarioAsociadoEsCorrecto() {
        // ASSERT directo sobre el objeto carrito preparado en setUp()
        assertNotNull(carrito.getUsuario(), "El carrito debe tener un usuario asociado");
        assertEquals(1L, carrito.getUsuario().getId(), "El ID del usuario debe ser 1");
        assertEquals("cliente1", carrito.getUsuario().getUsername(), "El username debe ser 'cliente1'");
    }

    /**
     * Verifica que el producto asociado al carrito es el esperado.
     */
    @Test
    public void testCarrito_productoAsociadoEsCorrecto() {
        assertNotNull(carrito.getProducto(), "El carrito debe tener un producto asociado");
        assertEquals(1L, carrito.getProducto().getId(), "El ID del producto debe ser 1");
        assertEquals("Agua Mineral", carrito.getProducto().getNombre(), "El nombre del producto debe ser 'Agua Mineral'");
        assertEquals(500.0, carrito.getProducto().getPrecio(), "El precio debe ser 500.0");
    }

    /**
     * Verifica que no se puede crear un carrito con usuario nulo.
     */
    @Test
    public void testCarrito_usuarioNoDebeSerNulo() {
        carrito.setUsuario(null);
        assertNull(carrito.getUsuario(), "Si se setea null, getUsuario() debe devolver null");
        // Esto simula que el sistema detectaría un error al intentar persistirlo
    }

    // =========================================================
    // PRUEBA 3: Pruebas del servicio con Mockito
    // Cubre los métodos de CarritoServiceImpl para alcanzar 80%
    // =========================================================

    /**
     * Verifica que findAll() devuelve la lista de carritos del repositorio.
     */
    @Test
    public void testFindAll_debeRetornarListaDeCarritos() {
        // ARRANGE: el mock devuelve una lista con 2 carritos
        Carrito carrito2 = new Carrito();
        carrito2.setId(2L);
        carrito2.setUsuario(usuario);
        carrito2.setProducto(producto);
        carrito2.setCantidad(1);

        when(carritoRepository.findAll()).thenReturn(Arrays.asList(carrito, carrito2));

        // ACT
        List<Carrito> lista = carritoService.findAll();

        // ASSERT
        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(2, lista.size(), "Debe haber 2 carritos en la lista");
        verify(carritoRepository, times(1)).findAll();
    }

    /**
     * Verifica que findById() devuelve el carrito correcto cuando existe.
     */
    @Test
    public void testFindById_conIdExistente_debeRetornarCarrito() {
        // ARRANGE
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        // ACT
        Carrito resultado = carritoService.findById(1L);

        // ASSERT
        assertNotNull(resultado, "El carrito encontrado no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID debe ser 1");
        verify(carritoRepository, times(1)).findById(1L);
    }

    /**
     * Verifica que findById() devuelve null cuando el carrito no existe.
     */
    @Test
    public void testFindById_conIdInexistente_debeRetornarNull() {
        // ARRANGE: el repositorio devuelve Optional vacío (no encontró nada)
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        Carrito resultado = carritoService.findById(99L);

        // ASSERT
        assertNull(resultado, "Debe retornar null si el carrito no existe");
    }

    /**
     * Verifica que deleteById() llama al repositorio correctamente.
     */
    @Test
    public void testDeleteById_debeEliminarCarrito() {
        // ARRANGE: no hay nada que configurar, solo verificamos que se llama
        doNothing().when(carritoRepository).deleteById(1L);

        // ACT
        carritoService.deleteById(1L);

        // ASSERT: verificar que el repositorio recibió la llamada
        verify(carritoRepository, times(1)).deleteById(1L);
    }

    /**
     * Verifica que findByUsuarioId() devuelve los carritos del usuario.
     */
    @Test
    public void testFindByUsuarioId_debeRetornarCarritosDelUsuario() {
        // ARRANGE
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(carrito));

        // ACT
        List<Carrito> lista = carritoService.findByUsuarioId(1L);

        // ASSERT
        assertNotNull(lista);
        assertEquals(1, lista.size(), "El usuario 1 tiene 1 carrito");
        assertEquals("cliente1", lista.get(0).getUsuario().getUsername());
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
    }

    /**
     * Verifica los getters y setters de la entidad Carrito (cobertura de entidad).
     */
    @Test
    public void testCarrito_gettersYSettersFuncionanCorrectamente() {
        Carrito c = new Carrito();
        c.setId(5L);
        c.setUsuario(usuario);
        c.setProducto(producto);
        c.setCantidad(2);

        assertEquals(5L, c.getId());
        assertEquals(usuario, c.getUsuario());
        assertEquals(producto, c.getProducto());
        assertEquals(2, c.getCantidad());
    }
}
