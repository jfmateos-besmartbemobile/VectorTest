package com.mobile.android.smartick.widgets.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.Constants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsersListAdapter extends ArrayAdapter<User> {

    private TextView botonLoginText;
	private List<User> users = new ArrayList<User>();
	private LayoutInflater mInflater;
    private HashMap<String,String> userAvatarMap = new HashMap<>();
	private ImageLoader imageLoader;
    private DisplayImageOptions imageLoaderDisplayoptions;

	public UsersListAdapter(Context context, int textViewResourceId,
                            List<User> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.users = objects;

		//sets up imageLoader
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        imageLoaderDisplayoptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisk(true).build();
	}

	@Override
	public int getCount() {
		return users.size();
	}

	@Override
	public User getItem(int position) {
		return users.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		User user = getItem(position);

        if (user.getPerfil().equals(UserType.ALUMNO.toString())){
            convertView = mInflater.inflate(R.layout.student_login_cell, parent, false);
        }

        if (user.getPerfil().equals(UserType.TUTOR.toString())){
            convertView = mInflater.inflate(R.layout.tutor_login_cell, parent, false);
        }

        botonLoginText = (TextView) convertView.findViewById(R.id.userslist_botonloginText);
        Typeface tfDidact = Typeface.createFromAsset(getContext().getAssets(), "fonts/DidactGothic.ttf");
        botonLoginText.setTypeface(tfDidact);
        botonLoginText.setText(user.getUsername());

		//gets student avatar
		if (user.getPerfil().equals(UserType.ALUMNO.toString())){
			ImageView avatar = (ImageView) convertView.findViewById(R.id.userslist_avatar);

            //try to get avatarUrl from cache
            String avatarUrl = getAvatarUrlForUser(user.getUsername());
			new RetrieveAvatar(avatar,avatarUrl,user.getUsername()).execute();
		}

		return convertView;
	}

    private String getAvatarUrlForUser(String username){
        return userAvatarMap.get(username);
    }

    private void putAvatarUrlForUser(String avatarUrl, String username){
        userAvatarMap.put(username,avatarUrl);
    }

	private class RetrieveAvatar extends AsyncTask<String, Bitmap, String> {

		ImageView avatar;
        String avatarUrl;
        String username;

		public RetrieveAvatar(ImageView avatar,String avatarUrl,String username){
			this.avatar = avatar;
            this.avatarUrl = avatarUrl;
            this.username = username;
		}

	    protected String doInBackground(String... params) {
            if (avatarUrl!= null){
                //user avatar ulr already known
                Log.d(Constants.USER_LIST_TAG,"avatar url already known for user " + this.username);
                return avatarUrl;
            }else{
                //user avatar url unknown
                Log.d(Constants.USER_LIST_TAG,"retreiving avatar URL for user " + this.username);
                try {
                    if (this.username != null && this.username.length() > 0){
                        GetAvatarImageForUserResponse response = SmartickRestClient.get().getAvatarImageForUser(this.username);
                        if (response!= null){
                            return Constants.instance().getUrl_context() + response.getUrlAvatar();
                        }
                    }
                } catch (Exception e) {
                    Log.d(Constants.USER_LIST_TAG, "Error decoding image");
                }
            }
			return null;
	    }

	    @Override
	    protected void onPostExecute(String result) {
            Log.d(Constants.USER_LIST_TAG, "results -> " + result);
            this.avatar.setImageResource(R.drawable.s_default);

            if (result != null) {
                imageLoader.displayImage(result, this.avatar, imageLoaderDisplayoptions);

                //stores in cache
                putAvatarUrlForUser(result, this.username);
            }
	    }
	 }
}