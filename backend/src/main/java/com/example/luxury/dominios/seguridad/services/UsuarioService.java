package com.example.luxury.dominios.seguridad.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.luxury.dominios.seguridad.models.Rol;
import com.example.luxury.dominios.seguridad.models.Usuario;
import com.example.luxury.dominios.seguridad.repositories.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identificador) {
        Optional<Usuario> opt = usuarioRepository.buscarPorIdentificador(identificador);
        if (!opt.isPresent()) {
            throw new UsernameNotFoundException("Usuario no encontrado.");
        }
        return construirUserDetails(opt.get());
    }

    public UserDetails loadUserById(Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (!opt.isPresent()) {
            throw new UsernameNotFoundException("Usuario no encontrado.");
        }
        return construirUserDetails(opt.get());
    }

    public Usuario obtenerUsuarioPorIdentificador(String identificador) {
        Optional<Usuario> opt = usuarioRepository.buscarPorIdentificador(identificador);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        return opt.get();
    }

    private UserDetails construirUserDetails(Usuario usuario) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        for (Rol rol : usuario.getRoles()) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre().name()));
        }

        return User.withUsername(usuario.getNumeroDocumento())
                .password(usuario.getContrasenaHash())
                .authorities(autoridades)
                .disabled(!usuario.isActivo())
                .build();
    }
}

