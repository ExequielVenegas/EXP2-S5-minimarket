package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.model.CustomUserDetails;
import com.minimarket.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    public void setUp() {
        Rol rol = new Rol("ROLE_USER");
        rol.setId(1L);

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero1");
        usuario.setPassword("pass_encriptado_123");
        usuario.setRoles(roles);
    }

    @Test
    public void testLoadUserByUsername_usuarioExiste_debeRetornarUserDetails() {
        when(usuarioRepository.findByUsername("cajero1"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado = customUserDetailsService.loadUserByUsername("cajero1");

        assertNotNull(resultado, "El UserDetails no debe ser nulo");
        assertInstanceOf(CustomUserDetails.class, resultado,
                "Debe ser una instancia de CustomUserDetails");
        assertEquals("cajero1", resultado.getUsername(),
                "El username debe coincidir");
        assertEquals("pass_encriptado_123", resultado.getPassword(),
                "La contraseña debe coincidir");

        assertFalse(resultado.getAuthorities().isEmpty(),
                "Debe tener al menos un rol");
        assertEquals("ROLE_USER",
                resultado.getAuthorities().iterator().next().getAuthority(),
                "El rol debe ser ROLE_USER");

        verify(usuarioRepository, times(1)).findByUsername("cajero1");
    }

    @Test
    public void testLoadUserByUsername_usuarioNoExiste_debeLanzarExcepcion() {
        when(usuarioRepository.findByUsername("desconocido"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException excepcion = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("desconocido"),
                "Debe lanzar UsernameNotFoundException si el usuario no existe"
        );

        assertTrue(excepcion.getMessage().contains("desconocido"),
                "El mensaje de error debe mencionar el username buscado");

        verify(usuarioRepository, times(1)).findByUsername("desconocido");
    }
}
