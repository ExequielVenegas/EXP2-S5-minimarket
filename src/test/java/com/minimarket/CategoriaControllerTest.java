package com.minimarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.controller.CategoriaController;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
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

@WebMvcTest(CategoriaController.class)
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Categoria categoria;

    @BeforeEach
    public void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");
    }

    @Test
    @WithMockUser
    public void testListarCategorias_debeRetornar200() throws Exception {
        when(categoriaService.findAll()).thenReturn(Arrays.asList(categoria));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Lácteos"));
    }

    @Test
    @WithMockUser
    public void testObtenerCategoriaPorId_existente_debeRetornar200() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoria);

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lácteos"));
    }

    @Test
    @WithMockUser
    public void testObtenerCategoriaPorId_inexistente_debeRetornar404() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGuardarCategoria_debeRetornar200() throws Exception {
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoria);

        mockMvc.perform(post("/api/categorias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lácteos"));
    }

    @Test
    @WithMockUser
    public void testActualizarCategoria_existente_debeRetornar200() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoria);
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoria);

        mockMvc.perform(put("/api/categorias/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testActualizarCategoria_inexistente_debeRetornar404() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/categorias/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testEliminarCategoria_existente_debeRetornar204() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoria);
        doNothing().when(categoriaService).deleteById(1L);

        mockMvc.perform(delete("/api/categorias/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testEliminarCategoria_inexistente_debeRetornar404() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/categorias/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
