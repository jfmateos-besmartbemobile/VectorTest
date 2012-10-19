package com.smartick.pojos;

public class ListUser {
	
	private String userName;
	private String userPassword;
	private String avatarUrl;
	
	public ListUser() {
		// TODO Auto-generated constructor stub
	}
	
	public ListUser(String userName, String userPassword, String avatarUrl) {
		this.userName = userName;
		this.userPassword = userPassword;
		this.avatarUrl = avatarUrl;
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