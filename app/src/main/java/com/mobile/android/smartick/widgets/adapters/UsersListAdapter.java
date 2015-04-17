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
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.network.GetAvatarImageForUserResponse;
import com.mobile.android.smartick.network.SmartickRestClient;
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

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UsersListAdapter extends ArrayAdapter<User> {
	private static final String URL_DEFAULT_AVATAR = "/images/avatares/login-default/s_azul_t.png";
	private Context context;
	private ImageView avatar;
	//private TextView userName;
    private Button botonLogin;
    private TextView botonLoginText;
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

        botonLogin = (Button) row.findViewById(R.id.userslist_botonlogin);
        botonLoginText = (TextView) row.findViewById(R.id.userslist_botonloginText);
        botonLoginText.setText(user.getUsername());

        ImageView avatar = (ImageView) row.findViewById(R.id.userslist_avatar);
        Log.d(Constants.USER_LIST_TAG,"retreiving avatar for user: " + user.getUsername());
        new RetrieveAvatar(avatar,user.getUsername()).execute();

		return row;
	}

	private class RetrieveAvatar extends AsyncTask<String, Bitmap, Bitmap> {

		ImageView avatar;
        String username;

		public RetrieveAvatar(ImageView avatar,String username){
			this.avatar = avatar;
            this.username = username;
		}

	    protected Bitmap doInBackground(String... params) {
			try {
                Log.d(Constants.USER_LIST_TAG,"doInBackground");
                if (this.username != null && this.username.length() > 0){
                    GetAvatarImageForUserResponse response = SmartickRestClient.get().getAvatarImageForUser(this.username);
                    if (response!= null){
                        String urlAvatar = Constants.URL_CONTEXT + response.getUrlAvatar();
                        URL url = new URL(urlAvatar);
                        Log.d(Constants.USER_LIST_TAG,"setting avatar to" + urlAvatar);
                        return BitmapFactory.decodeStream((InputStream) url.getContent());
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
                Log.d(Constants.USER_LIST_TAG, "Error decoding image");
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
}