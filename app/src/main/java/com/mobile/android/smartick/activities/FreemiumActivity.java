package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.mobile.android.smartick.R;

/**
 * Created by sbarrio on 18/05/15.
 */
public class FreemiumActivity extends Activity {


    private ImageView avatar1;
    private ImageView avatar2;
    private ImageView avatar3;
    private ImageView avatar4;
    private ImageView avatar5;
    private ImageView avatar6;
    private ImageView avatar7;
    private ImageView avatar8;
    private ImageView avatar9;
    private ImageView selectedAvatarPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freemium);
        loadAvatarImages();
    }

    public void loadAvatarImages(){
        avatar1 = new ImageView(this.getApplicationContext());
        avatar1.setImageResource(R.drawable.avatar1);
        avatar2 = new ImageView(this.getApplicationContext());
        avatar2.setImageResource(R.drawable.avatar2);
        avatar3 = new ImageView(this.getApplicationContext());
        avatar3.setImageResource(R.drawable.avatar3);
        avatar4 = new ImageView(this.getApplicationContext());
        avatar4.setImageResource(R.drawable.avatar4);
        avatar5 = new ImageView(this.getApplicationContext());
        avatar5.setImageResource(R.drawable.avatar5);
        avatar6 = new ImageView(this.getApplicationContext());
        avatar6.setImageResource(R.drawable.avatar6);
        avatar7 = new ImageView(this.getApplicationContext());
        avatar7.setImageResource(R.drawable.avatar7);
        avatar8 = new ImageView(this.getApplicationContext());
        avatar8.setImageResource(R.drawable.avatar8);
        avatar9 = new ImageView(this.getApplicationContext());
        avatar9.setImageResource(R.drawable.avatar9);
    }

    /** Volver a la pantalla de inicio */
    public void irWelcome(View view) {
        finish();
    }
}
