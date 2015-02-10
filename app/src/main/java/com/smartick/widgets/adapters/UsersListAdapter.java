package com.smartick.widgets.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.example.gorgue.myapplication.R;
import com.smartick.data.UsersDBHandler;
import com.smartick.pojos.User;
import com.smartick.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UsersListAdapter extends ArrayAdapter<User> {
	private static final String URL_DEFAULT_AVATAR = "images/avatares/login-default/s_azul_t.png";
	private Context context;
	private ImageView avatar;
	//private TextView userName;
    private Button botonLogin;
	//private Button delete;
	//private UsersDBHandler db;
	private List<User> users = new ArrayList<User>();

	public UsersListAdapter(Context context, int textViewResourceId,
                            List<User> objects) {
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

		User user = getItem(position);
		
		//userName = (TextView) row.findViewById(R.id.userslist_username);
		//userName.setText(user.getUsername());

        botonLogin = (Button) row.findViewById(R.id.userslist_botonlogin);
        botonLogin.setText(user.getUsername());
        // El action lo tenemos en el elemento del listview
//        botonLogin.setOnClickListener(new OnClickListener() {

            //Listener para boton de login
//            public void onClick(View v) {
//                Button botonLogin = (Button)v;
//                System.out.println("LOG ME IN!!");
//            }
//        });


/*
        db = new UsersDBHandler(this.context);
		delete = (Button) row.findViewById(R.id.userslist_delete);
		delete.setClickable(false);
		delete.setFocusable(false);
		delete.setTag(position);
		delete.setOnClickListener(new OnClickListener() {

			//Listener para boton de borrado de entrada en lista
			public void onClick(View v) {
				//obtenemos posicion en listView
				int position = (Integer) v.getTag();
				TextView userName = (TextView) ((View)v.getParent()).findViewById(R.id.userslist_username);
				String userNameToDelete = userName.getText().toString();
				
				//eliminamos elemento de la listview
				users.remove(position);
			    notifyDataSetChanged();
			    
			    //finalmente, eliminamos el elemento de la base de datos

			    //getAllUsers
			    List<ListUser> userList = db.getAllUsers();
			    
			    //obtener el id de mi user
			    int id = UsersDBHandler.INVALID_USER_ID;
			    
			    for (int i=0;i<userList.size();i++){
			    	if (userList.get(i).getUsername().equals(userNameToDelete))
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
*/

        ImageView avatar = (ImageView) row.findViewById(R.id.userslist_avatar);
        new RetrieveAvatar(avatar).execute(user);

		return row;
	}
	
	
	private class RetrieveAvatar extends AsyncTask<User, Bitmap, Bitmap> {
		
		ImageView avatar;
		
		public RetrieveAvatar(ImageView avatar){
			this.avatar = avatar;
		}
			
	    protected Bitmap doInBackground(User... users) {
			try {
                URL url = new URL(Constants.URL_CONTEXT + Constants.URL_PEQ_AVATAR + ((users[0].getUrlAvatar()!=null)?getAvatarName(users[0].getUrlAvatar()):URL_DEFAULT_AVATAR));
				return BitmapFactory.decodeStream((InputStream) url.getContent());
			} catch (IOException e) {
				try {
					return BitmapFactory.decodeStream((InputStream) new URL(Constants.URL_CONTEXT + URL_DEFAULT_AVATAR).getContent());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			return null;
	    }
	    
	    @Override
	    protected void onPostExecute(Bitmap result) {

            this.avatar.setImageBitmap(result);
	    }

        /**
         * A partir del path completo obtenemos el nombre
         * Devuelve null si no hay nom bre de fichero en el path
         * @param path path del avatar
         */
        private String getAvatarName(String path) {
            System.out.println("path del avatar:" + path);
            if (path.lastIndexOf('/') != -1) {
                return path.substring(path.lastIndexOf('/') + 1);
            } else {
                return null;
            }

        }
	 }

}
