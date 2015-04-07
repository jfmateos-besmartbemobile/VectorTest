package com.mobile.android.smartick.widgets.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UsersListAdapter extends ArrayAdapter<User> {
	private static final String URL_DEFAULT_AVATAR = "/images/avatares/login-default/s_azul_t.png";
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
                if (users != null && users[0] != null && users[0].getUsername() != null && users[0].getUsername() != ""){

                    JSONObject response = requestWebService(Constants.GET_AVATAR_IMAGE_SERVICE + users[0].getUsername());
                    if (response!=null){
                        String urlAvatar = response.getString("urlAvatar");
                        if (urlAvatar != null){
                            Log.d(Constants.USER_LIST_TAG,"requestdImg: " + urlAvatar);
                            urlAvatar = Constants.URL_CONTEXT + urlAvatar;
                            URL url = new URL(urlAvatar);
                            return BitmapFactory.decodeStream((InputStream) url.getContent());
                        }
                    }
                    return null;
                }
			}catch (IOException e) {
				try {
                    Log.d(Constants.USER_LIST_TAG,"Default avatar set");
					return BitmapFactory.decodeStream((InputStream) new URL(Constants.URL_CONTEXT + URL_DEFAULT_AVATAR).getContent());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} catch (Exception e) {
                Log.d(Constants.USER_LIST_TAG,"Error decoding image");
                return null;
            }

			return null;
	    }
	    
	    @Override
	    protected void onPostExecute(Bitmap result) {
            Log.d(Constants.USER_LIST_TAG,"results -> " + result);
            this.avatar.setImageBitmap(result);
	    }
	 }


    public static JSONObject requestWebService(String serviceUrl) {

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection)
                    urlToRequest.openConnection();

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
            }

            // create JSON object from content
            InputStream in = new BufferedInputStream(
                    urlConnection.getInputStream());
            return new JSONObject(getResponseText(in));

        } catch (MalformedURLException e) {
            // URL is invalid
        } catch (SocketTimeoutException e) {
            // data retrieval or connection timed out
        } catch (IOException e) {
            // could not read response body
            // (could not create input stream)
        } catch (JSONException e) {
            // response body is no valid JSON string
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }
}