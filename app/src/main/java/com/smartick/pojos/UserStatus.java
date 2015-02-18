package com.smartick.pojos;

/**
 * Estado de usuario (chequeo)
 * Created by gorgue on 18/02/2015.
 */
public class UserStatus {

    public enum Status {
        login_valid, login_invalid, password_invalid, no_active_subscription;
    };

    private String nombre;
    private String apellidos;
    private String password;
    private Status status;
    private String user;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
