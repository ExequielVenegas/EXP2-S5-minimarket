package com.minimarket;

import com.minimarket.entity.*;
import com.minimarket.repository.*;
import com.minimarket.service.impl.*;
import com.minimarket.security.model.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ServiciosTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private DetalleVentaRepository detalleVentaRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private RolRepository rolRepository;

    @InjectMocks private ProductoServiceImpl productoService;
    @InjectMocks private CategoriaServiceImpl categoriaService;
    @InjectMocks private DetalleVentaServiceImpl detalleVentaService;
    @InjectMocks private VentaServiceImpl ventaService;
    @InjectMocks private RolServiceImpl rolService;

    private Categoria categoria;
    private Producto producto;
    private Usuario usuario;
    private Venta venta;
    private DetalleVenta detalleVenta;
    private Rol rol;

    @BeforeEach
    public void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Jugo de Naranja");
        producto.setPrecio(900.0);
        producto.setStock(15);
        producto.setCategoria(categoria);

        rol = new Rol("ROLE_USER");
        rol.setId(1L);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("vendedor1");
        usuario.setPassword("pass123");
        usuario.setRoles(new HashSet<>(Arrays.asList(rol)));

        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());

        detalleVenta = new DetalleVenta();
        detalleVenta.setId(1L);
        detalleVenta.setVenta(venta);
        detalleVenta.setProducto(producto);
        detalleVenta.setCantidad(3);
        detalleVenta.setPrecio(900.0);
    }

    @Test
    public void testProductoService_findAll() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto));
        List<Producto> lista = productoService.findAll();
        assertEquals(1, lista.size());
        assertEquals("Jugo de Naranja", lista.get(0).getNombre());
        verify(productoRepository).findAll();
    }

    @Test
    public void testProductoService_findById_existente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        Producto resultado = productoService.findById(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void testProductoService_findById_inexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(productoService.findById(99L));
    }

    @Test
    public void testProductoService_save() {
        when(productoRepository.save(producto)).thenReturn(producto);
        Producto resultado = productoService.save(producto);
        assertNotNull(resultado);
        assertEquals("Jugo de Naranja", resultado.getNombre());
    }

    @Test
    public void testProductoService_deleteById() {
        doNothing().when(productoRepository).deleteById(1L);
        productoService.deleteById(1L);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    public void testProductoService_findByCategoriaId() {
        when(productoRepository.findByCategoriaId(1L)).thenReturn(Arrays.asList(producto));
        List<Producto> lista = productoService.findByCategoriaId(1L);
        assertEquals(1, lista.size());
        verify(productoRepository).findByCategoriaId(1L);
    }

    @Test
    public void testCategoriaService_findAll() {
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoria));
        List<Categoria> lista = categoriaService.findAll();
        assertEquals(1, lista.size());
        assertEquals("Bebidas", lista.get(0).getNombre());
    }

    @Test
    public void testCategoriaService_findById_existente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        assertNotNull(categoriaService.findById(1L));
    }

    @Test
    public void testCategoriaService_findById_inexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(categoriaService.findById(99L));
    }

    @Test
    public void testCategoriaService_save() {
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        assertEquals("Bebidas", categoriaService.save(categoria).getNombre());
    }

    @Test
    public void testCategoriaService_deleteById() {
        doNothing().when(categoriaRepository).deleteById(1L);
        categoriaService.deleteById(1L);
        verify(categoriaRepository).deleteById(1L);
    }

    @Test
    public void testDetalleVentaService_findAll() {
        when(detalleVentaRepository.findAll()).thenReturn(Arrays.asList(detalleVenta));
        List<DetalleVenta> lista = detalleVentaService.findAll();
        assertEquals(1, lista.size());
    }

    @Test
    public void testDetalleVentaService_findById_existente() {
        when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(detalleVenta));
        assertNotNull(detalleVentaService.findById(1L));
    }

    @Test
    public void testDetalleVentaService_findById_inexistente() {
        when(detalleVentaRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(detalleVentaService.findById(99L));
    }

    @Test
    public void testDetalleVentaService_save() {
        when(detalleVentaRepository.save(detalleVenta)).thenReturn(detalleVenta);
        DetalleVenta resultado = detalleVentaService.save(detalleVenta);
        assertEquals(3, resultado.getCantidad());
        assertEquals(900.0, resultado.getPrecio());
    }

    @Test
    public void testDetalleVentaService_deleteById() {
        doNothing().when(detalleVentaRepository).deleteById(1L);
        detalleVentaService.deleteById(1L);
        verify(detalleVentaRepository).deleteById(1L);
    }

    @Test
    public void testDetalleVentaService_findByVentaId() {
        when(detalleVentaRepository.findByVentaId(1L)).thenReturn(Arrays.asList(detalleVenta));
        List<DetalleVenta> lista = detalleVentaService.findByVentaId(1L);
        assertEquals(1, lista.size());
    }

    @Test
    public void testVentaService_findAll() {
        when(ventaRepository.findAll()).thenReturn(Arrays.asList(venta));
        assertEquals(1, ventaService.findAll().size());
    }

    @Test
    public void testVentaService_findById_existente() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        assertNotNull(ventaService.findById(1L));
    }

    @Test
    public void testVentaService_findById_inexistente() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(ventaService.findById(99L));
    }

    @Test
    public void testVentaService_save() {
        when(ventaRepository.save(venta)).thenReturn(venta);
        assertEquals(1L, ventaService.save(venta).getId());
    }

    @Test
    public void testVentaService_findByUsuarioId() {
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(venta));
        assertEquals(1, ventaService.findByUsuarioId(1L).size());
    }

    @Test
    public void testRolService_findByNombre_existente() {
        when(rolRepository.findByNombre("ROLE_USER")).thenReturn(Optional.of(rol));
        Optional<Rol> resultado = rolService.findByNombre("ROLE_USER");
        assertTrue(resultado.isPresent());
        assertEquals("ROLE_USER", resultado.get().getNombre());
    }

    @Test
    public void testRolService_findByNombre_inexistente() {
        when(rolRepository.findByNombre("ROLE_GHOST")).thenReturn(Optional.empty());
        assertFalse(rolService.findByNombre("ROLE_GHOST").isPresent());
    }

    @Test
    public void testRol_gettersYSetters() {
        Set<Usuario> usuarios = new HashSet<>();
        usuarios.add(usuario);

        Rol r = new Rol("ROLE_ADMIN");
        r.setId(2L);
        r.setUsuarios(usuarios);

        assertEquals(2L, r.getId());
        assertEquals("ROLE_ADMIN", r.getNombre());
        assertEquals(1, r.getUsuarios().size());

        r.setNombre("ROLE_MOD");
        assertEquals("ROLE_MOD", r.getNombre());
    }

    @Test
    public void testRol_constructorCompleto() {
        Set<Usuario> usuarios = new HashSet<>();
        Rol r = new Rol(5L, "ROLE_ADMIN", usuarios);
        assertEquals(5L, r.getId());
        assertEquals("ROLE_ADMIN", r.getNombre());
        assertNotNull(r.getUsuarios());
    }

    @Test
    public void testCategoria_gettersYSetters() {
        Categoria c = new Categoria();
        c.setId(3L);
        c.setNombre("Snacks");
        c.setProductos(Arrays.asList(producto));

        assertEquals(3L, c.getId());
        assertEquals("Snacks", c.getNombre());
        assertEquals(1, c.getProductos().size());
    }

    @Test
    public void testDetalleVenta_gettersYSetters() {
        DetalleVenta dv = new DetalleVenta();
        dv.setId(7L);
        dv.setVenta(venta);
        dv.setProducto(producto);
        dv.setCantidad(5);
        dv.setPrecio(1500.0);

        assertEquals(7L, dv.getId());
        assertEquals(venta, dv.getVenta());
        assertEquals(producto, dv.getProducto());
        assertEquals(5, dv.getCantidad());
        assertEquals(1500.0, dv.getPrecio());
    }

    @Test
    public void testVenta_gettersYSetters() {
        Date fecha = new Date();
        List<DetalleVenta> detalles = Arrays.asList(detalleVenta);

        Venta v = new Venta();
        v.setId(2L);
        v.setUsuario(usuario);
        v.setFecha(fecha);
        v.setDetalles(detalles);

        assertEquals(2L, v.getId());
        assertEquals(usuario, v.getUsuario());
        assertEquals(fecha, v.getFecha());
        assertEquals(1, v.getDetalles().size());
    }

    @Test
    public void testCustomUserDetails_gettersDeDatosDeUsuario() {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        assertEquals("vendedor1", userDetails.getUsername());
        assertEquals("pass123", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    public void testCustomUserDetails_getAuthorities_devuelveRolesDelUsuario() {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        assertNotNull(userDetails.getAuthorities());
        assertFalse(userDetails.getAuthorities().isEmpty());
        assertEquals("ROLE_USER",
            userDetails.getAuthorities().iterator().next().getAuthority());
    }
}
