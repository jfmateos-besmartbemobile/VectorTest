package com.smartick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class OfflineActivity extends Activity{

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline);
		View button = findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	retryConnect();
            }
        });
	}
	
	private void retryConnect(){
		Intent intent = new Intent(this, MainActivity.class);
    	startActivity(intent);
	}
}
