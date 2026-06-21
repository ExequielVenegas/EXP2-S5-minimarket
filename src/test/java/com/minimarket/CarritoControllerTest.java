package com.minimarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.controller.CarritoController;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.service.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoController.class)
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Carrito carrito;

    @BeforeEach
    public void setUp() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche");
        producto.setPrecio(1200.0);
        producto.setStock(20);

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(2);
    }

    @Test
    @WithMockUser
    public void testListarCarrito_debeRetornar200() throws Exception {
        when(carritoService.findAll()).thenReturn(Arrays.asList(carrito));

        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidad").value(2));
    }

    @Test
    @WithMockUser
    public void testObtenerCarritoPorId_existente_debeRetornar200() throws Exception {
        when(carritoService.findById(1L)).thenReturn(carrito);

        mockMvc.perform(get("/api/carrito/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    @WithMockUser
    public void testObtenerCarritoPorId_inexistente_debeRetornar404() throws Exception {
        when(carritoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/carrito/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testAgregarProductoAlCarrito_debeRetornar200() throws Exception {
        when(carritoService.save(any(Carrito.class))).thenReturn(carrito);

        mockMvc.perform(post("/api/carrito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    @WithMockUser
    public void testActualizarCarrito_existente_debeRetornar200() throws Exception {
        when(carritoService.findById(1L)).thenReturn(carrito);
        when(carritoService.save(any(Carrito.class))).thenReturn(carrito);

        mockMvc.perform(put("/api/carrito/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testActualizarCarrito_inexistente_debeRetornar404() throws Exception {
        when(carritoService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/carrito/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testEliminarCarrito_existente_debeRetornar204() throws Exception {
        when(carritoService.findById(1L)).thenReturn(carrito);
        doNothing().when(carritoService).deleteById(1L);

        mockMvc.perform(delete("/api/carrito/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testEliminarCarrito_inexistente_debeRetornar404() throws Exception {
        when(carritoService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/carrito/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
