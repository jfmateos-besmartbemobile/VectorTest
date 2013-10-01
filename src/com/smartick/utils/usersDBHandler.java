package com.smartick.utils;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartick.pojos.ListUser;

public class usersDBHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "smartickUsers";
 
    // Userstable name
    private static final String TABLE_USERS = "users";
 
    // users Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AVATAR = "avatarUrl";
    
    public static final int INVALID_USER_ID = -1;
 
    public usersDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
        				+ KEY_ID + " INTEGER PRIMARY KEY,"
        				+ KEY_USERNAME + " TEXT,"
        				+ KEY_PASSWORD + " TEXT," 
        				+ KEY_AVATAR + " TEXT"
        				+ ")";
        
        db.execSQL(CREATE_USERS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
 
        // Create tables again
        onCreate(db);
    }
    
    
    //-------------- CRUD -----------------------
    
    // Getting numUsers
    public int getUserCount() {
        String countQuery = "SELECT  * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
 
        // return count
        return cursor.getCount();
    }
    
    // Adding new user
    public void addUser(ListUser user) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_USERNAME, user.getUserName());
	    values.put(KEY_PASSWORD, user.getUserPassword());
	    values.put(KEY_AVATAR, user.getAvatarUrl());
	 
	    // Inserting Row
	    db.insert(TABLE_USERS, null, values);
	    db.close(); // Closing database connection
	}
    
    //Getting user
    public ListUser getUser(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
     
        Cursor cursor = db.query(TABLE_USERS, new String[] {
        		KEY_ID,
                KEY_USERNAME,
                KEY_PASSWORD,
                KEY_AVATAR
        		}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
     
        ListUser listUser = new ListUser(
        						cursor.getString(1), 
        						cursor.getString(2),
        						cursor.getString(3)
        						);
        // return listUser
        db.close();
        return listUser;
    }
    
    public ListUser getUser(String username){
    	String query = "Select * FROM " + TABLE_USERS + " WHERE username" + " =  \"" + username + "\"";
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(query, null);
    	ListUser listUser = new ListUser();
		if (cursor.moveToFirst()) {
	        listUser = new ListUser(
	        		cursor.getInt(0),
	        		cursor.getString(1), 
					cursor.getString(2),
					cursor.getString(3)
					);
			cursor.close();
		}
        db.close();
        return listUser;
    }
    
    
     //Getting all Users
	 public List<ListUser> getAllUsers() {
	    List<ListUser> userList = new ArrayList<ListUser>();
	    
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_USERS;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            ListUser listUser = new ListUser();
	            listUser.setId(Integer.parseInt(cursor.getString(0)));
	            listUser.setUserName(cursor.getString(1));
	            listUser.setUserPassword(cursor.getString(2));
	            listUser.setAvatarUrl(cursor.getString(3));

	            userList.add(listUser);
	        } while (cursor.moveToNext());
	    }
	 
	    db.close();
	    // return user list
	    return userList;
	}
    
    //Updates a User entry
	public int updateUser(ListUser listUser) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_USERNAME, listUser.getUserName());
	    values.put(KEY_PASSWORD, listUser.getUserPassword());
	    values.put(KEY_AVATAR, listUser.getAvatarUrl());
	 
	    // updating row
	    return db.update(TABLE_USERS, values, KEY_ID + " = ?",
	            new String[] { String.valueOf(listUser.getId()) });
	}
	
	//Deletes a User entry
	public void deleteUser(ListUser listUser) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    if (listUser.getId() != usersDBHandler.INVALID_USER_ID){
		    db.delete(TABLE_USERS, KEY_ID + " = ?",
		            new String[] { String.valueOf(listUser.getId()) });
	    }
	    db.close();
	}
	    
}


