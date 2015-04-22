package com.mobile.android.smartick.network;

/**
 * Created by sbarrio on 22/04/15.
 */
public class RegisterTutorResponse {

    private String tutorMail;
    private String password;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String installationId;
    private String device;
    private String version;
    private String osVersion;
    private String status;


    private RegisterTutorResponse(){}

    public String getTutorMail() {
        return tutorMail;
    }

    public void setTutorMail(String tutorMail) {
        this.tutorMail = tutorMail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
