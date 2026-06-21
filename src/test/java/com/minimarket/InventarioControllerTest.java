package com.minimarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.controller.InventarioController;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventarioController.class)
public class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Inventario inventario;

    @BeforeEach
    public void setUp() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche Entera");
        producto.setPrecio(1200.0);
        producto.setStock(50);

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(20);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());
    }

    @Test
    @WithMockUser
    public void testListarInventario_debeRetornar200() throws Exception {
        when(inventarioService.findAll()).thenReturn(Arrays.asList(inventario));

        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoMovimiento").value("Entrada"));
    }

    @Test
    @WithMockUser
    public void testObtenerMovimientoPorId_existente_debeRetornar200() throws Exception {
        when(inventarioService.findById(1L)).thenReturn(inventario);

        mockMvc.perform(get("/api/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(20));
    }

    @Test
    @WithMockUser
    public void testObtenerMovimientoPorId_inexistente_debeRetornar404() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/inventario/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testRegistrarMovimiento_debeRetornar200() throws Exception {
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(post("/api/inventario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoMovimiento").value("Entrada"));
    }

    @Test
    @WithMockUser
    public void testActualizarMovimiento_existente_debeRetornar200() throws Exception {
        when(inventarioService.findById(1L)).thenReturn(inventario);
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(put("/api/inventario/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testActualizarMovimiento_inexistente_debeRetornar404() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/inventario/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testEliminarMovimiento_existente_debeRetornar204() throws Exception {
        when(inventarioService.findById(1L)).thenReturn(inventario);
        doNothing().when(inventarioService).deleteById(1L);

        mockMvc.perform(delete("/api/inventario/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testEliminarMovimiento_inexistente_debeRetornar404() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/inventario/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
