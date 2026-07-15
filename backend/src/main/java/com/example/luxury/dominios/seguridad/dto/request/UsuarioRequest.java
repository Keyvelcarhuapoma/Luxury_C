package com.example.luxury.dominios.seguridad.dto.request;

import com.example.luxury.dominios.seguridad.enums.NombreRol;
import com.example.luxury.dominios.seguridad.enums.TipoDocumento;

public class UsuarioRequest {

    private String nombres;
    private String apellidos;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String correo;
    private String contrasena;
    private NombreRol rol;
    private Long sedeId;
    private boolean activo;

    public UsuarioRequest(String nombres, String apellidos, TipoDocumento tipoDocumento, String numeroDocumento,
            String telefono, String correo, String contrasena, NombreRol rol, Long sedeId, boolean activo) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.sedeId = sedeId;
        this.activo = activo;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
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

    public String getContrasena() {
        return contrasena;
    }

    public NombreRol getRol() {
        return rol;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public void setSedeId(Long sedeId) {
        this.sedeId = sedeId;
    }

    public boolean isActivo() {
        return activo;
    }
}
