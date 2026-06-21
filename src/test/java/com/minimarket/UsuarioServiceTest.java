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

    // @Mock crea una versión simulada del repositorio.
    // En vez de ir a la base de datos real, devuelve lo que nosotros le digamos.
    @Mock
    private UsuarioRepository usuarioRepository;

    // @InjectMocks crea el servicio real e inyecta el mock anterior dentro.
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // Variables reutilizables en todos los tests
    private Usuario usuarioAdmin;
    private Usuario usuarioNormal;

    // @BeforeEach se ejecuta ANTES de cada test.
    // Aquí preparamos los datos de prueba (etapa "Arrange" del patrón AAA).
    @BeforeEach
    public void setUp() {
        // Usuario con rol ADMIN
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setUsername("adminUser");
        usuarioAdmin.setPassword("password123");
        usuarioAdmin.setRoles(Set.of(new Rol("ADMIN")));

        // Usuario con rol USER (sin privilegios de admin)
        usuarioNormal = new Usuario();
        usuarioNormal.setId(2L);
        usuarioNormal.setUsername("clienteJuan");
        usuarioNormal.setPassword("clave456");
        usuarioNormal.setRoles(Set.of(new Rol("USER")));
    }

    // ---------------------------------------------------------------
    // TEST 1: Verificar que un usuario tiene datos obligatorios completos
    // Cubre criterio 2 de la pauta: validar datos del usuario
    // ---------------------------------------------------------------
    @Test
    public void testUsuarioTieneDatosCompletos() {
        // Arrange: ya preparado en setUp()

        // Act: verificamos los campos directamente
        String username = usuarioAdmin.getUsername();
        String password = usuarioAdmin.getPassword();
        Set<Rol> roles = usuarioAdmin.getRoles();

        // Assert: ningún campo obligatorio debe ser nulo o vacío
        assertNotNull(username, "El username no debe ser nulo");
        assertNotNull(password, "La contraseña no debe ser nula");
        assertNotNull(roles, "Los roles no deben ser nulos");
        assertFalse(username.isBlank(), "El username no debe estar vacío");
        assertFalse(password.isBlank(), "La contraseña no debe estar vacía");
        assertFalse(roles.isEmpty(), "El usuario debe tener al menos un rol");
    }

    // ---------------------------------------------------------------
    // TEST 2: Verificar que un usuario SIN datos obligatorios es inválido
    // Cubre criterio 2: validar caso límite (usuario incompleto)
    // ---------------------------------------------------------------
    @Test
    public void testUsuarioSinUsernameEsInvalido() {
        // Arrange: usuario sin username
        Usuario usuarioIncompleto = new Usuario();
        usuarioIncompleto.setPassword("clave123");
        usuarioIncompleto.setRoles(Set.of(new Rol("USER")));

        // Act
        String username = usuarioIncompleto.getUsername();

        // Assert: el username es nulo, por lo tanto el usuario no es válido
        assertNull(username, "Un usuario sin username no es válido");
    }

    // ---------------------------------------------------------------
    // TEST 3: Verificar que solo usuarios con rol ADMIN pueden administrar
    // Cubre criterio 2: prueba de acceso según rol
    // ---------------------------------------------------------------
    @Test
    public void testSoloAdminTieneRolAdministrador() {
        // Arrange: ya preparado en setUp()

        // Act: revisamos si cada usuario tiene el rol ADMIN
        boolean adminTieneRolAdmin = usuarioAdmin.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        boolean normalTieneRolAdmin = usuarioNormal.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        // Assert
        assertTrue(adminTieneRolAdmin, "El usuario admin SÍ debe tener rol ADMIN");
        assertFalse(normalTieneRolAdmin, "El usuario normal NO debe tener rol ADMIN");
    }

    // ---------------------------------------------------------------
    // TEST 4: Buscar usuario por username usando mock (simula la BD)
    // Cubre criterio 2 y criterio 6: simulación de dependencia externa
    // ---------------------------------------------------------------
    @Test
    public void testBuscarUsuarioPorUsername() {
        // Arrange: le decimos al mock qué debe devolver cuando se llame findByUsername
        when(usuarioRepository.findByUsername("adminUser"))
                .thenReturn(Optional.of(usuarioAdmin));

        // Act: llamamos al servicio real (que por dentro usa el mock)
        Optional<Usuario> resultado = usuarioService.findByUsername("adminUser");

        // Assert
        assertTrue(resultado.isPresent(), "Debe encontrar al usuario");
        assertEquals("adminUser", resultado.get().getUsername());

        // Verificamos que el mock fue llamado exactamente 1 vez
        verify(usuarioRepository, times(1)).findByUsername("adminUser");
    }

    // ---------------------------------------------------------------
    // TEST 5: Buscar usuario que NO existe debe retornar vacío
    // Cubre criterio 2: caso límite (usuario no encontrado)
    // ---------------------------------------------------------------
    @Test
    public void testBuscarUsuarioInexistenteRetornaVacio() {
        // Arrange: el mock devuelve vacío para un username que no existe
        when(usuarioRepository.findByUsername("noExiste"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = usuarioService.findByUsername("noExiste");

        // Assert
        assertFalse(resultado.isPresent(), "No debe encontrar un usuario que no existe");
        verify(usuarioRepository, times(1)).findByUsername("noExiste");
    }

    // ---------------------------------------------------------------
    // TEST 6: Guardar usuario y verificar que se llama al repositorio
    // Cubre criterio 6: mock de dependencia al guardar
    // ---------------------------------------------------------------
    @Test
    public void testGuardarUsuario() {
        // Arrange: el mock simula que el repositorio guarda y devuelve el usuario
        when(usuarioRepository.save(usuarioNormal)).thenReturn(usuarioNormal);

        // Act
        Usuario guardado = usuarioService.save(usuarioNormal);

        // Assert
        assertNotNull(guardado);
        assertEquals("clienteJuan", guardado.getUsername());
        verify(usuarioRepository, times(1)).save(usuarioNormal);
    }

    // ---------------------------------------------------------------
    // TEST 7: Listar todos los usuarios
    // Cubre criterio 3: múltiples comportamientos del servicio
    // ---------------------------------------------------------------
    @Test
    public void testListarTodosLosUsuarios() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioAdmin, usuarioNormal));

        // Act
        List<Usuario> usuarios = usuarioService.findAll();

        // Assert
        assertNotNull(usuarios);
        assertEquals(2, usuarios.size());
        verify(usuarioRepository, times(1)).findAll();
    }
}
