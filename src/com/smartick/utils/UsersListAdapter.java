package com.smartick.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartick.activities.R;
import com.smartick.pojos.ListUser;

public class UsersListAdapter extends ArrayAdapter<ListUser> {
	private static final String URL_DEFAULT_AVATAR = "images/avatares/login-default/s_azul_t.png";
	private Context context;
	private ImageView avatar;
	private TextView userName;
	private Button delete;
	private usersDBHandler db;
	private List<ListUser> users = new ArrayList<ListUser>();

	public UsersListAdapter(Context context, int textViewResourceId,
			List<ListUser> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.users = objects;
	}

	public int getCount() {
		return this.users.size();
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.users_list, parent, false);
		}

		ListUser user = getItem(position);
		
		avatar = (ImageView) row.findViewById(R.id.avatar);
		
		userName = (TextView) row.findViewById(R.id.nameUser);
		
		userName.setText(user.getUserName());
		
		delete = (Button) row.findViewById(R.id.deleteButton);
		delete.setClickable(false);
		delete.setFocusable(false);
		
		delete.setTag(position);
		
		db = new usersDBHandler(this.context);
		
		delete.setOnClickListener(new OnClickListener() {

			//Listener para boton de borrado de entrada en lista
			public void onClick(View v) {
				//obtenemos posicion en listView
				int position = (Integer) v.getTag();
				TextView userName = (TextView) ((View )v.getParent()).findViewById(R.id.nameUser);
				String userNameToDelete = userName.getText().toString();
				
				//eliminamos elemento de la listview
				users.remove(position);
			    notifyDataSetChanged();
			    
			    //finalmente, eliminamos el elemento de la base de datos

			    //getAllUsers
			    List<ListUser> userList = db.getAllUsers();
			    
			    //obtener el id de mi user
			    int id = usersDBHandler.INVALID_USER_ID;
			    
			    for (int i=0;i<userList.size();i++){
			    	if (userList.get(i).getUserName().equals(userNameToDelete))
			    	{
			    		id = userList.get(i).getId();
			    		break;
			    	}
			    }
			    
			    //borrar usando listuser id
			    ListUser listuser = new ListUser();
			    listuser.setId(id);
			    db.deleteUser(listuser);
			}
			
		});

		
		//modificar sistema de recuperaci—n de avatares TO DO
//		public static final String URL_DEFAULT_AVATAR = "avatares/avatares/peq/8564.png";
		new RetreiveAvatar().execute(user);

		return row;
	}
	
	//TO DO - Cambiar para obtener avatar de DB
	
	private class RetreiveAvatar extends AsyncTask<ListUser, Bitmap, Bitmap> {
			
	    protected Bitmap doInBackground(ListUser... users) {
			try {
				return BitmapFactory.decodeStream((InputStream)new URL(Constants.URL_CONTEXT+users[0].getAvatarUrl()).getContent());
			} catch (IOException e) {
				try {
					return BitmapFactory.decodeStream((InputStream)new URL(Constants.URL_CONTEXT+URL_DEFAULT_AVATAR).getContent());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return null;
	    }
	    
	    @Override
	    protected void onPostExecute(Bitmap result) {
	    	avatar.setImageBitmap(result);
	    	super.onPostExecute(result);
	    }
	 }

}
