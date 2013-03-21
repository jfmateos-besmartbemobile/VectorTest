package com.smartick.pojos;

public class ListUser {
	
	private int id;
	private String userName;
	private String userPassword;
	private String avatarUrl;
	
	public ListUser(){
	}

	public ListUser(int id, String userName, String userPassword, String avatarUrl) {
		this.id = id;
		this.userName = userName;
		this.userPassword = userPassword;
		this.avatarUrl = avatarUrl;
	}
	
	public ListUser(String userName, String userPassword, String avatarUrl) {
		this.userName = userName;
		this.userPassword = userPassword;
		this.avatarUrl = avatarUrl;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

}