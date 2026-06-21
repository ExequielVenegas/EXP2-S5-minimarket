package com.minimarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto producto;
    private Categoria categoria;

    @BeforeEach
    public void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Agua Mineral");
        producto.setPrecio(500.0);
        producto.setStock(10);
        producto.setCategoria(categoria);
    }

    @Test
    @WithMockUser
    public void testListarProductos_debeRetornar200ConLista() throws Exception {
        List<Producto> lista = Arrays.asList(producto);
        when(productoService.findAll()).thenReturn(lista);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Agua Mineral"));
    }

    @Test
    @WithMockUser
    public void testObtenerProductoPorId_existente_debeRetornar200() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Agua Mineral"))
                .andExpect(jsonPath("$.precio").value(500.0));
    }

    @Test
    @WithMockUser
    public void testObtenerProductoPorId_inexistente_debeRetornar404() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGuardarProducto_debeRetornar200() throws Exception {
        when(productoService.save(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Agua Mineral"));
    }

    @Test
    @WithMockUser
    public void testActualizarProducto_existente_debeRetornar200() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);
        when(productoService.save(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(put("/api/productos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testActualizarProducto_inexistente_debeRetornar404() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/productos/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testEliminarProducto_existente_debeRetornar204() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);
        doNothing().when(productoService).deleteById(1L);

        mockMvc.perform(delete("/api/productos/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testEliminarProducto_inexistente_debeRetornar404() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/productos/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
