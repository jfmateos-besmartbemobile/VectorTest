package com.mobile.android.smartick.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobile.android.smartick.pojos.User;

import java.util.ArrayList;
import java.util.List;

public class UsersDBHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version (V2 incluimos campo perfil de usuario)
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "smartickUsers";

    // Userstable name
    private static final String TABLE_USERS = "users";

    // users Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AVATAR = "avatarUrl";
    private static final String KEY_PERFIL = "perfil";

    public static final int INVALID_USER_ID = -1;

    public UsersDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
        				+ KEY_ID + " INTEGER PRIMARY KEY,"
        				+ KEY_USERNAME + " TEXT,"
        				+ KEY_PASSWORD + " TEXT," 
        				+ KEY_AVATAR + " TEXT,"
                        + KEY_PERFIL + " TEXT"
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
    public void addUser(User user) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_USERNAME, user.getUsername());
	    values.put(KEY_PASSWORD, user.getPassword());
	    values.put(KEY_AVATAR, user.getUrlAvatar());
        values.put(KEY_PERFIL, user.getPerfil());
	 
	    // Inserting Row
	    db.insert(TABLE_USERS, null, values);
	    db.close(); // Closing database connection
	}
    
    //Getting user
    public User getUser(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
     
        Cursor cursor = db.query(TABLE_USERS, new String[] {
        		KEY_ID,
                KEY_USERNAME,
                KEY_PASSWORD,
                KEY_AVATAR,
                KEY_PERFIL
        		}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
     
        User user = new User(
        						cursor.getString(1), 
        						cursor.getString(2),
        						cursor.getString(3),
                                cursor.getString(4)
        						);
        // return listUser
        db.close();
        return user;
    }
    
    public User getUser(String username){
    	String query = "Select * FROM " + TABLE_USERS + " WHERE username" + " =  \"" + username + "\"";
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(query, null);
    	User user = new User();
		if (cursor.moveToFirst()) {
	        user = new User(
	        		cursor.getInt(0),
	        		cursor.getString(1), 
					cursor.getString(2),
					cursor.getString(3),
                    cursor.getString(4)
					);
			cursor.close();
		}
        db.close();
        return user;
    }
    
    
     //Getting all Users
	 public List<User> getAllUsers() {
	    List<User> userList = new ArrayList<User>();
	    
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_USERS;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            User user = new User();
	            user.setId(Integer.parseInt(cursor.getString(0)));
	            user.setUsername(cursor.getString(1));
	            user.setPassword(cursor.getString(2));
	            user.setUrlAvatar(cursor.getString(3));
                user.setPerfil(cursor.getString(4));

	            userList.add(user);
	        } while (cursor.moveToNext());
	    }
	 
	    db.close();
	    // return user list
	    return userList;
	}
    
    //Updates a User entry
	public int updateUser(User user) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_USERNAME, user.getUsername());
	    values.put(KEY_PASSWORD, user.getPassword());
	    values.put(KEY_AVATAR, user.getUrlAvatar());
        values.put(KEY_PERFIL, user.getPerfil());
	 
	    // updating row
	    return db.update(TABLE_USERS, values, KEY_ID + " = ?",
	            new String[] { String.valueOf(user.getId()) });
	}
	
	//Deletes a User entry
	public void deleteUser(User user) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    if (user.getId() != UsersDBHandler.INVALID_USER_ID){
		    db.delete(TABLE_USERS, KEY_ID + " = ?",
		            new String[] { String.valueOf(user.getId()) });
	    }
	    db.close();
	}

    //Deletes all Users
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, null, null);
        db.close();
    }

}


