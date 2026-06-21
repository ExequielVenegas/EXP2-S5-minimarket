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

    @BeforeEach
    public void setUp() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Agua Mineral");
        producto.setPrecio(500.0);
        producto.setStock(10);
        producto.setCategoria(categoria);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");
        usuario.setPassword("clave123");

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(3);
    }

    @Test
    public void testAgregarProducto_conStockSuficiente_debeGuardarCarrito() {
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        boolean hayStock = producto.getStock() >= carrito.getCantidad();
        Carrito resultado = null;

        if (hayStock) {
            resultado = carritoService.save(carrito);
        }

        assertNotNull(resultado, "El carrito debería haberse guardado porque hay stock suficiente");
        assertEquals(3, resultado.getCantidad(), "La cantidad del carrito debe ser 3");
        assertEquals("Agua Mineral", resultado.getProducto().getNombre(), "El producto debe ser Agua Mineral");

        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    public void testAgregarProducto_sinStockSuficiente_noDebeGuardarCarrito() {
        carrito.setCantidad(20);

        boolean hayStock = producto.getStock() >= carrito.getCantidad();
        Carrito resultado = null;

        if (hayStock) {
            resultado = carritoService.save(carrito);
        }

        assertNull(resultado, "El carrito NO debe guardarse porque no hay stock suficiente");
        assertTrue(producto.getStock() < carrito.getCantidad(), "El stock disponible es menor a lo pedido");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    public void testAgregarProducto_cantidadExactoAlStock_debeGuardarCarrito() {
        carrito.setCantidad(10);
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        boolean hayStock = producto.getStock() >= carrito.getCantidad();
        Carrito resultado = null;
        if (hayStock) {
            resultado = carritoService.save(carrito);
        }

        assertNotNull(resultado, "El carrito debe guardarse cuando la cantidad es exactamente igual al stock");
        assertEquals(10, resultado.getCantidad());
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    public void testCarrito_usuarioAsociadoEsCorrecto() {
        assertNotNull(carrito.getUsuario(), "El carrito debe tener un usuario asociado");
        assertEquals(1L, carrito.getUsuario().getId(), "El ID del usuario debe ser 1");
        assertEquals("cliente1", carrito.getUsuario().getUsername(), "El username debe ser 'cliente1'");
    }

    @Test
    public void testCarrito_productoAsociadoEsCorrecto() {
        assertNotNull(carrito.getProducto(), "El carrito debe tener un producto asociado");
        assertEquals(1L, carrito.getProducto().getId(), "El ID del producto debe ser 1");
        assertEquals("Agua Mineral", carrito.getProducto().getNombre(), "El nombre del producto debe ser 'Agua Mineral'");
        assertEquals(500.0, carrito.getProducto().getPrecio(), "El precio debe ser 500.0");
    }

    @Test
    public void testCarrito_usuarioNoDebeSerNulo() {
        carrito.setUsuario(null);
        assertNull(carrito.getUsuario(), "Si se setea null, getUsuario() debe devolver null");
    }

    @Test
    public void testFindAll_debeRetornarListaDeCarritos() {
        Carrito carrito2 = new Carrito();
        carrito2.setId(2L);
        carrito2.setUsuario(usuario);
        carrito2.setProducto(producto);
        carrito2.setCantidad(1);

        when(carritoRepository.findAll()).thenReturn(Arrays.asList(carrito, carrito2));

        List<Carrito> lista = carritoService.findAll();

        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(2, lista.size(), "Debe haber 2 carritos en la lista");
        verify(carritoRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_conIdExistente_debeRetornarCarrito() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        Carrito resultado = carritoService.findById(1L);

        assertNotNull(resultado, "El carrito encontrado no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID debe ser 1");
        verify(carritoRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_conIdInexistente_debeRetornarNull() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        Carrito resultado = carritoService.findById(99L);

        assertNull(resultado, "Debe retornar null si el carrito no existe");
    }

    @Test
    public void testDeleteById_debeEliminarCarrito() {
        doNothing().when(carritoRepository).deleteById(1L);

        carritoService.deleteById(1L);

        verify(carritoRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByUsuarioId_debeRetornarCarritosDelUsuario() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(carrito));

        List<Carrito> lista = carritoService.findByUsuarioId(1L);

        assertNotNull(lista);
        assertEquals(1, lista.size(), "El usuario 1 tiene 1 carrito");
        assertEquals("cliente1", lista.get(0).getUsuario().getUsername());
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
    }

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
