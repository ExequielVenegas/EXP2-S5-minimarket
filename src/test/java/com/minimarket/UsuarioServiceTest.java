package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioAdmin;
    private Usuario usuarioNormal;

    @BeforeEach
    public void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setUsername("adminUser");
        usuarioAdmin.setPassword("password123");
        usuarioAdmin.setRoles(Set.of(new Rol("ADMIN")));

        usuarioNormal = new Usuario();
        usuarioNormal.setId(2L);
        usuarioNormal.setUsername("clienteJuan");
        usuarioNormal.setPassword("clave456");
        usuarioNormal.setRoles(Set.of(new Rol("USER")));
    }

    @Test
    public void testUsuarioTieneDatosCompletos() {

        String username = usuarioAdmin.getUsername();
        String password = usuarioAdmin.getPassword();
        Set<Rol> roles = usuarioAdmin.getRoles();

        assertNotNull(username, "El username no debe ser nulo");
        assertNotNull(password, "La contraseña no debe ser nula");
        assertNotNull(roles, "Los roles no deben ser nulos");
        assertFalse(username.isBlank(), "El username no debe estar vacío");
        assertFalse(password.isBlank(), "La contraseña no debe estar vacía");
        assertFalse(roles.isEmpty(), "El usuario debe tener al menos un rol");
    }

    @Test
    public void testUsuarioSinUsernameEsInvalido() {
        Usuario usuarioIncompleto = new Usuario();
        usuarioIncompleto.setPassword("clave123");
        usuarioIncompleto.setRoles(Set.of(new Rol("USER")));

        String username = usuarioIncompleto.getUsername();

        assertNull(username, "Un usuario sin username no es válido");
    }

    @Test
    public void testSoloAdminTieneRolAdministrador() {

        boolean adminTieneRolAdmin = usuarioAdmin.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        boolean normalTieneRolAdmin = usuarioNormal.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        assertTrue(adminTieneRolAdmin, "El usuario admin SÍ debe tener rol ADMIN");
        assertFalse(normalTieneRolAdmin, "El usuario normal NO debe tener rol ADMIN");
    }

    @Test
    public void testBuscarUsuarioPorUsername() {
        when(usuarioRepository.findByUsername("adminUser"))
                .thenReturn(Optional.of(usuarioAdmin));

        Optional<Usuario> resultado = usuarioService.findByUsername("adminUser");

        assertTrue(resultado.isPresent(), "Debe encontrar al usuario");
        assertEquals("adminUser", resultado.get().getUsername());

        verify(usuarioRepository, times(1)).findByUsername("adminUser");
    }

    @Test
    public void testBuscarUsuarioInexistenteRetornaVacio() {
        when(usuarioRepository.findByUsername("noExiste"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.findByUsername("noExiste");

        assertFalse(resultado.isPresent(), "No debe encontrar un usuario que no existe");
        verify(usuarioRepository, times(1)).findByUsername("noExiste");
    }

    @Test
    public void testGuardarUsuario() {
        when(usuarioRepository.save(usuarioNormal)).thenReturn(usuarioNormal);

        Usuario guardado = usuarioService.save(usuarioNormal);

        assertNotNull(guardado);
        assertEquals("clienteJuan", guardado.getUsername());
        verify(usuarioRepository, times(1)).save(usuarioNormal);
    }

    @Test
    public void testListarTodosLosUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioAdmin, usuarioNormal));

        List<Usuario> usuarios = usuarioService.findAll();

        assertNotNull(usuarios);
        assertEquals(2, usuarios.size());
        verify(usuarioRepository, times(1)).findAll();
    }
}
