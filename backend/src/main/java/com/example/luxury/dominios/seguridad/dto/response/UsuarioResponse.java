package com.example.luxury.dominios.seguridad.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.luxury.dominios.seguridad.enums.TipoDocumento;
import com.example.luxury.dominios.seguridad.models.Rol;
import com.example.luxury.dominios.seguridad.models.Usuario;

public class UsuarioResponse {

    private Long id;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private Long sedeId;
    private String sedeNombre;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String correo;
    private boolean activo;
    private String estado;
    private String roles;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    public UsuarioResponse(Long id, String nombres, String apellidos, String nombreCompleto,
            Long sedeId, String sedeNombre, TipoDocumento tipoDocumento, String numeroDocumento, String telefono,
            String correo, boolean activo, String estado, String roles, LocalDateTime fechaRegistro,
            LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.nombreCompleto = nombreCompleto;
        this.sedeId = sedeId;
        this.sedeNombre = sedeNombre;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = activo;
        this.estado = estado;
        this.roles = roles;
        this.fechaRegistro = fechaRegistro;
        this.fechaActualizacion = fechaActualizacion;
    }

    public static UsuarioResponse from(Usuario usuario) {
        List<String> listaRoles = new ArrayList<>();
        for (Rol rol : usuario.getRoles()) {
            listaRoles.add(rol.getNombre().name());
        }
        Collections.sort(listaRoles);
        String roles = String.join(", ", listaRoles);

        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
        String estado = usuario.isActivo() ? "ACTIVO" : "INACTIVO";
        Long sedeId = usuario.getSede() == null ? null : usuario.getSede().getId();
        String sedeNombre = usuario.getSede() == null ? "Todas las sedes" : usuario.getSede().getNombre();
        return new UsuarioResponse(usuario.getId(), usuario.getNombres(), usuario.getApellidos(), nombreCompleto,
                sedeId, sedeNombre, usuario.getTipoDocumento(), usuario.getNumeroDocumento(), usuario.getTelefono(),
                usuario.getCorreo(), usuario.isActivo(), estado, roles, usuario.getFechaRegistro(),
                usuario.getFechaActualizacion());
    }

    public Long getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getEstado() {
        return estado;
    }

    public String getRoles() {
        return roles;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
