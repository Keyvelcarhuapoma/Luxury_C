package com.example.luxury.dominios.seguridad.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.luxury.dominios.common.exception.ResourceNotFoundException;
import com.example.luxury.dominios.sede.dto.SedeResponse;
import com.example.luxury.dominios.sede.model.Sede;
import com.example.luxury.dominios.sede.service.SedeService;
import com.example.luxury.dominios.seguridad.dto.request.UsuarioRequest;
import com.example.luxury.dominios.seguridad.dto.response.UsuarioResponse;
import com.example.luxury.dominios.seguridad.enums.NombreRol;
import com.example.luxury.dominios.seguridad.enums.TipoDocumento;
import com.example.luxury.dominios.seguridad.models.Rol;
import com.example.luxury.dominios.seguridad.models.Usuario;
import com.example.luxury.dominios.seguridad.repositories.RolRepository;
import com.example.luxury.dominios.seguridad.repositories.UsuarioRepository;

@Service
public class GestionUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final SedeService sedeService;

    public GestionUsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder, SedeService sedeService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.sedeService = sedeService;
    }

    public List<UsuarioResponse> listar(String estado) {
        List<UsuarioResponse> resultado = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findAll()) {
            if (filtrarPorEstado(usuario, estado)) {
                resultado.add(UsuarioResponse.from(usuario));
            }
        }
        return resultado;
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        validar(request, null, true);

        Usuario usuario = new Usuario();
        aplicarDatos(usuario, request);
        usuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        usuario.setRoles(new HashSet<>(List.of(obtenerRol(request.getRol()))));
        usuario.setActivo(request.isActivo());

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        validar(request, id, false);

        aplicarDatos(usuario, request);
        if (request.getContrasena() != null && !request.getContrasena().isBlank()) {
            usuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        }
        usuario.getRoles().clear();
        usuario.getRoles().add(obtenerRol(request.getRol()));
        usuario.setActivo(request.isActivo());

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(!usuario.isActivo());
        usuarioRepository.save(usuario);
    }

    public Usuario buscar(Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (!opt.isPresent()) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return opt.get();
    }

    private boolean filtrarPorEstado(Usuario usuario, String estado) {
        if (estado == null || estado.isBlank()) {
            return true;
        }
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            return usuario.isActivo();
        }
        if ("INACTIVO".equalsIgnoreCase(estado)) {
            return !usuario.isActivo();
        }
        return true;
    }

    private void aplicarDatos(Usuario usuario, UsuarioRequest request) {
        usuario.setNombres(request.getNombres().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setTipoDocumento(request.getTipoDocumento());
        usuario.setNumeroDocumento(request.getNumeroDocumento().trim());
        usuario.setTelefono(request.getTelefono().trim());
        usuario.setCorreo(request.getCorreo().trim().toLowerCase());
        usuario.setSede(request.getRol() == NombreRol.ADMIN ? null : sedeService.buscar(request.getSedeId()));
    }

    private Rol obtenerRol(NombreRol nombreRol) {
        Optional<Rol> opt = rolRepository.findByNombre(nombreRol);
        if (opt.isPresent()) {
            return opt.get();
        }
        return rolRepository.save(new Rol(nombreRol));
    }

    private void validar(UsuarioRequest request, Long usuarioIdActual, boolean requiereContrasena) {
        if (requiereContrasena && (request.getContrasena() == null || request.getContrasena().isBlank())) {
            throw new IllegalArgumentException("La contrasena es obligatoria.");
        }
        if (request.getRol() != NombreRol.ADMIN && request.getSedeId() == null) {
            List<SedeResponse> sedes = sedeService.listar();
            if (!sedes.isEmpty()) {
                request.setSedeId(sedes.get(0).getId());
            }
            if (request.getSedeId() == null) {
                throw new IllegalArgumentException("La sede asignada es obligatoria para usuarios no administradores.");
            }
        }
        if (!request.getTelefono().trim().matches("9\\d{8}|\\d{9}")) {
            throw new IllegalArgumentException("El telefono debe tener 9 digitos numericos.");
        }
        if (request.getTipoDocumento() == TipoDocumento.DNI
                && !request.getNumeroDocumento().trim().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 digitos numericos.");
        }
        if (request.getTipoDocumento() == TipoDocumento.CE && request.getNumeroDocumento().trim().length() < 9) {
            throw new IllegalArgumentException("El Carne de Extranjeria (CE) debe tener al menos 9 caracteres.");
        }

        String numeroDocumento = request.getNumeroDocumento().trim();
        String telefono = request.getTelefono().trim();
        String correo = request.getCorreo().trim().toLowerCase();
        String nombres = request.getNombres().trim();
        String apellidos = request.getApellidos().trim();

        boolean duplicado = false;
        for (Usuario usuario : usuarioRepository.findAll()) {
            if (usuarioIdActual == null || !usuario.getId().equals(usuarioIdActual)) {
                if (usuario.getNumeroDocumento().equals(numeroDocumento)
                        || usuario.getTelefono().equals(telefono)
                        || usuario.getCorreo().equalsIgnoreCase(correo)
                        || (usuario.getNombres().trim().equalsIgnoreCase(nombres)
                                && usuario.getApellidos().trim().equalsIgnoreCase(apellidos))) {
                    duplicado = true;
                    break;
                }
            }
        }

        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con esos datos.");
        }
    }
}
