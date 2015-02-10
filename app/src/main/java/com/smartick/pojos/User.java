package com.smartick.pojos;

/**
 * Usuario de Smartick, tanto alumnos como tutores
 */
public class User {
	
	private int id;
	private String username;
	private String password;
	private String urlAvatar;
    private String perfil;
	
	public User(){
	}

	public User(int id, String userName, String userPassword, String avatarUrl, String perfil) {
		this.id = id;
		this.username = userName;
		this.password = userPassword;
		this.urlAvatar = avatarUrl;
        this.perfil = perfil;
	}
	
	public User(String userName, String userPassword, String avatarUrl, String perfil) {
		this.username = userName;
		this.password = userPassword;
		this.urlAvatar = avatarUrl;
        this.perfil = perfil;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getUsername() {

        return username;
	}
	public void setUsername(String userName) {

        this.username = userName;
	}
	public String getPassword() {

        return password;
	}
	public void setPassword(String userPassword) {

        this.password = userPassword;
	}
	public String getUrlAvatar() {

        return urlAvatar;
	}
	public void setUrlAvatar(String avatarUrl) {

        this.urlAvatar = avatarUrl;
	}
    public String getPerfil() {
        return this.perfil;
    }
    public void setPerfil(String newPerfil) {
        this.perfil = newPerfil;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        User otherUser = (User) obj;
        // TODO: revisar equals con id
        return //this.id == guest.id ||
                this.username.equals(otherUser.getUsername());
    }

    @Override
    public int hashCode() {
        // TODO
        //return id;
        return username.hashCode();
    }
}