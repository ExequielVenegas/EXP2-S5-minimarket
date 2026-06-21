package com.minimarket;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;

    @BeforeEach
    public void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero1");
        usuario.setPassword("pass123");
    }

    @Test
    public void testFindByUsername_existente_debeRetornarUsuario() {
        when(usuarioRepository.findByUsername("cajero1"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.findByUsername("cajero1");

        assertTrue(resultado.isPresent(), "Debe encontrar el usuario");
        assertEquals("cajero1", resultado.get().getUsername());
        assertEquals(1L, resultado.get().getId());
        verify(usuarioRepository, times(1)).findByUsername("cajero1");
    }

    @Test
    public void testFindByUsername_inexistente_debeRetornarOptionalVacio() {
        when(usuarioRepository.findByUsername("fantasma"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.findByUsername("fantasma");

        assertFalse(resultado.isPresent(), "No debe encontrar el usuario");
        verify(usuarioRepository, times(1)).findByUsername("fantasma");
    }
}
