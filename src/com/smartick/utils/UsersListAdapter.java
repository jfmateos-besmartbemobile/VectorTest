package com.smartick.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartick.activities.R;
import com.smartick.pojos.ListUser;

public class UsersListAdapter extends ArrayAdapter<ListUser> {
	private static final String URL_CONTEXT = "http://192.168.1.148/";
	private Context context;
	private ImageView avatar;
	private TextView userName;
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
		new RetreiveAvatar().execute(user);

		return row;
	}
	
	private class RetreiveAvatar extends AsyncTask<ListUser, Bitmap, Bitmap> {
			
	    protected Bitmap doInBackground(ListUser... users) {
			try {
				Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(URL_CONTEXT+users[0].getAvatarUrl()).getContent());
				return bitmap;
			} catch (IOException e) {
				return null;
			}
	    }
	    
	    @Override
	    protected void onPostExecute(Bitmap result) {
	    	avatar.setImageBitmap(result);
	    	super.onPostExecute(result);
	    }
	 }

}
