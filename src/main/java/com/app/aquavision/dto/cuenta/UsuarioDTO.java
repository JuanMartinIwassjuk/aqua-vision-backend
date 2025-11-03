package com.app.aquavision.dto.cuenta;

import java.time.LocalDateTime;


public class UsuarioDTO {
    private String nombre;
    private String apellido;
    private String nombreUsuario;
    private String correo;
    private boolean correoVerificado;
    private boolean cuentaActiva;
    private LocalDateTime ultimoLogin;

    public UsuarioDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public UsuarioDTO setNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getApellido() {
        return apellido;
    }

    public UsuarioDTO setApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public UsuarioDTO setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        return this;
    }

    public String getCorreo() {
        return correo;
    }

    public UsuarioDTO setCorreo(String correo) {
        this.correo = correo;
        return this;
    }

    public boolean isCorreoVerificado() {
        return correoVerificado;
    }

    public UsuarioDTO setCorreoVerificado(boolean correoVerificado) {
        this.correoVerificado = correoVerificado;
        return this;
    }

    public boolean isCuentaActiva() {
        return cuentaActiva;
    }

    public UsuarioDTO setCuentaActiva(boolean cuentaActiva) {
        this.cuentaActiva = cuentaActiva;
        return this;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public UsuarioDTO setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
        return this;
    }
}
