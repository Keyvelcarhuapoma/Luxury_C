package com.example.luxury.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.luxury.api.dto.UsuarioApiResponse;
import com.example.luxury.dominios.seguridad.dto.request.UsuarioRequest;
import com.example.luxury.dominios.seguridad.enums.NombreRol;
import com.example.luxury.dominios.seguridad.enums.TipoDocumento;
import com.example.luxury.dominios.seguridad.models.Usuario;
import com.example.luxury.dominios.seguridad.repositories.UsuarioRepository;
import com.example.luxury.dominios.seguridad.services.GestionUsuarioService;
import com.example.luxury.dominios.seguridad.services.TokenService;
import com.example.luxury.dominios.sede.dto.SedeResponse;
import com.example.luxury.dominios.sede.service.SedeService;
import com.example.luxury.dominios.eventoacceso.service.EventoAccesoService;
import com.example.luxury.dominios.common.enums.TipoEventoAcceso;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final GestionUsuarioService gestionUsuarioService;
    private final TokenService tokenService;
    private final SedeService sedeService;
    private final EventoAccesoService eventoAccesoService;

    public ApiAuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
            GestionUsuarioService gestionUsuarioService, TokenService tokenService, SedeService sedeService,
            EventoAccesoService eventoAccesoService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.gestionUsuarioService = gestionUsuarioService;
        this.tokenService = tokenService;
        this.sedeService = sedeService;
        this.eventoAccesoService = eventoAccesoService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginApiRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.identificador(), request.contrasena()));
        } catch (Exception e) {
            Optional<Usuario> optUsuario = usuarioRepository.buscarPorIdentificador(request.identificador());
            eventoAccesoService.registrar(optUsuario.orElse(null), request.identificador(),
                    TipoEventoAcceso.LOGIN_FALLIDO, "Intento de inicio de sesion fallido por credenciales incorrectas.", "API / Angular");
            throw e;
        }

        Optional<Usuario> opt = usuarioRepository.buscarPorIdentificador(request.identificador());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        Usuario usuario = opt.get();

        eventoAccesoService.registrar(usuario, request.identificador(),
                TipoEventoAcceso.LOGIN_EXITOSO, "Inicio de sesion exitoso via API", "API / Angular");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", tokenService.generarToken(usuario));
        data.put("tipo", "Bearer");
        data.put("usuario", ApiMapper.usuarioDto(usuario));
        data.put("expiraEnSegundos", tokenService.obtenerExpiracionSegundos());
        return data;
    }

    @PostMapping("/registro")
    public UsuarioApiResponse registro(@Valid @RequestBody RegistroApiRequest request) {
        validarDocumento(request.tipoDocumento(), request.numeroDocumento());
        validarTelefono(request.telefono());
        validarCorreoLuxury(request.correo());

        UsuarioRequest usuarioRequest = new UsuarioRequest(
                request.nombres(),
                request.apellidos(),
                TipoDocumento.valueOf(request.tipoDocumento()),
                request.numeroDocumento(),
                request.telefono(),
                request.correo(),
                request.contrasena(),
                NombreRol.OPERADOR,
                sedePorDefecto(),
                true);
        return ApiMapper.usuarioDto(gestionUsuarioService.crear(usuarioRequest));
    }

    private Long sedePorDefecto() {
        List<SedeResponse> sedes = sedeService.listar();
        if (sedes.isEmpty()) {
            throw new IllegalArgumentException("No hay sedes activas para asignar al usuario.");
        }
        return sedes.get(0).getId();
    }

    private void validarDocumento(String tipoDocumento, String numeroDocumento) {
        if ("DNI".equals(tipoDocumento) && !numeroDocumento.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 digitos.");
        }
        if ("CE".equals(tipoDocumento) && !numeroDocumento.matches("[A-Z0-9]{9,12}")) {
            throw new IllegalArgumentException("El CE debe tener entre 9 y 12 letras o numeros.");
        }
    }

    private void validarTelefono(String telefono) {
        if (telefono == null || !telefono.matches("9\\d{8}")) {
            throw new IllegalArgumentException("El telefono debe empezar con 9 y tener 9 digitos.");
        }
    }

    private void validarCorreoLuxury(String correo) {
        if (correo == null || !correo.trim().toLowerCase().matches("[a-z0-9._%+-]+@luxury\\.com")) {
            throw new IllegalArgumentException("El correo debe pertenecer a @luxury.com.");
        }
    }

    public record LoginApiRequest(
            @NotBlank(message = "El identificador es obligatorio.") String identificador,
            @NotBlank(message = "La contrasena es obligatoria.") String contrasena) {
    }

    public record RegistroApiRequest(
            @NotBlank(message = "Los nombres son obligatorios.") String nombres,
            @NotBlank(message = "Los apellidos son obligatorios.") String apellidos,
            @NotBlank @Pattern(regexp = "DNI|CE|PASAPORTE", message = "Tipo de documento invalido.") String tipoDocumento,
            @NotBlank(message = "El numero de documento es obligatorio.")
            @Pattern(regexp = "[A-Z0-9]{8,12}", message = "Documento invalido.") String numeroDocumento,
            @NotBlank(message = "El telefono es obligatorio.")
            @Pattern(regexp = "9\\d{8}", message = "El telefono debe empezar con 9 y tener 9 digitos.") String telefono,
            @NotBlank(message = "El correo es obligatorio.")
            @Email(message = "El correo no es valido.")
            @Pattern(regexp = "[A-Za-z0-9._%+-]+@luxury\\.com", message = "El correo debe pertenecer a @luxury.com.") String correo,
            @NotBlank(message = "La contrasena es obligatoria.")
            @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres.") String contrasena) {
    }
}
