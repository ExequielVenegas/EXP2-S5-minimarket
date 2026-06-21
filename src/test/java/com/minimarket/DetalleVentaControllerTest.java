package com.minimarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.controller.DetalleVentaController;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.entity.Usuario;
import com.minimarket.service.DetalleVentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DetalleVentaController.class)
public class DetalleVentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetalleVentaService detalleVentaService;

    @Autowired
    private ObjectMapper objectMapper;

    private DetalleVenta detalleVenta;

    @BeforeEach
    public void setUp() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero1");

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Pan Integral");
        producto.setPrecio(800.0);
        producto.setStock(30);

        detalleVenta = new DetalleVenta();
        detalleVenta.setId(1L);
        detalleVenta.setVenta(venta);
        detalleVenta.setProducto(producto);
        detalleVenta.setCantidad(2);
        detalleVenta.setPrecio(800.0);
    }

    @Test
    @WithMockUser
    public void testListarDetalleVentas_debeRetornar200() throws Exception {
        when(detalleVentaService.findAll()).thenReturn(Arrays.asList(detalleVenta));

        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidad").value(2));
    }

    @Test
    @WithMockUser
    public void testObtenerDetalleVentaPorId_existente_debeRetornar200() throws Exception {
        when(detalleVentaService.findById(1L)).thenReturn(detalleVenta);

        mockMvc.perform(get("/api/detalle-ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(800.0));
    }

    @Test
    @WithMockUser
    public void testObtenerDetalleVentaPorId_inexistente_debeRetornar404() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/detalle-ventas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGuardarDetalleVenta_debeRetornar200() throws Exception {
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(detalleVenta);

        mockMvc.perform(post("/api/detalle-ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detalleVenta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    @WithMockUser
    public void testActualizarDetalleVenta_existente_debeRetornar200() throws Exception {
        when(detalleVentaService.findById(1L)).thenReturn(detalleVenta);
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(detalleVenta);

        mockMvc.perform(put("/api/detalle-ventas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detalleVenta)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testActualizarDetalleVenta_inexistente_debeRetornar404() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/detalle-ventas/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detalleVenta)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testEliminarDetalleVenta_existente_debeRetornar204() throws Exception {
        when(detalleVentaService.findById(1L)).thenReturn(detalleVenta);
        doNothing().when(detalleVentaService).deleteById(1L);

        mockMvc.perform(delete("/api/detalle-ventas/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testEliminarDetalleVenta_inexistente_debeRetornar404() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/detalle-ventas/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
