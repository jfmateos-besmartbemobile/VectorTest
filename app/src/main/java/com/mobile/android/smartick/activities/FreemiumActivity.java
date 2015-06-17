package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.util.Constants;

import java.util.ArrayList;
import java.util.List;

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
    private TextView textChoose;
    private TextView textAvatarOk;
    private TextView textHowOld;
    private Button buttonOkAvatar;
    private Button buttonCancelAvatar;
    private Button buttonConfirmAge;
    private SeekBar avatarAgeSeekBar;
    private ImageView selectedAvatarPreview;
    private List<ImageView> avatarImageViews;
    private int windowHeight = 0;

    private int selectedAvatar = Constants.DEFAULT_FREEMIUM_AVATAR;
    private int selectedAge = Constants.DEFAULT_FREEMIUM_AGE;

    private boolean avatarsAnimating = false;
    private boolean avatarPreviewAnimating = false;
    private boolean choosingAge = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_freemium);

        //loads references to view elements
        loadAvatarImages();
        setAvatarListeners();
        loadTextViews();
        loadButtons();
        avatarAgeSeekBar = (SeekBar) findViewById(R.id.avatarAgeSeekBar);

        //configures seekbar values
        avatarAgeSeekBar.setProgress(Constants.DEFAULT_FREEMIUM_AGE);
        avatarAgeSeekBar.setMax(Constants.MAX_FREEMIUM_AGE - Constants.MIN_FREEMIUM_AGE);
        avatarAgeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedAge = Constants.MIN_FREEMIUM_AGE + progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //sets up window height
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        windowHeight = size.y;

        //hides preview and confirm dialog
        selectedAvatarPreview.setY(windowHeight);
        selectedAvatarPreview.setVisibility(View.GONE);
        textAvatarOk.setVisibility(View.GONE);
        textHowOld.setVisibility(View.GONE);
        buttonCancelAvatar.setVisibility(View.GONE);
        buttonOkAvatar.setVisibility(View.GONE);
        buttonConfirmAge.setVisibility(View.GONE);
        avatarAgeSeekBar.setVisibility(View.GONE);

        //sets up TextFonts
        Typeface tfBoogaloo = Typeface.createFromAsset(getAssets(), "fonts/Boogaloo-Regular.otf");
        ((TextView) findViewById(R.id.text_choose_avatar)).setTypeface(tfBoogaloo);
        ((TextView) findViewById(R.id.text_is_avatar_ok)).setTypeface(tfBoogaloo);
        ((TextView) findViewById(R.id.text_how_old)).setTypeface(tfBoogaloo);
        ((Button) findViewById(R.id.confirmAgeButton)).setTypeface(tfBoogaloo);
        ((Button) findViewById(R.id.confirmAgeButton)).setTypeface(tfBoogaloo);
    }

    private void loadAvatarImages(){
        avatar1 = (ImageView)findViewById(R.id.avatar1);
        avatar2 = (ImageView)findViewById(R.id.avatar2);
        avatar3 = (ImageView)findViewById(R.id.avatar3);
        avatar4 = (ImageView)findViewById(R.id.avatar4);
        avatar5 = (ImageView)findViewById(R.id.avatar5);
        avatar6 = (ImageView)findViewById(R.id.avatar6);
        avatar7 = (ImageView)findViewById(R.id.avatar7);
        avatar8 = (ImageView)findViewById(R.id.avatar8);
        avatar9 = (ImageView)findViewById(R.id.avatar9);

        avatarImageViews = new ArrayList<ImageView>();
        avatarImageViews.add(avatar1);
        avatarImageViews.add(avatar2);
        avatarImageViews.add(avatar3);
        avatarImageViews.add(avatar4);
        avatarImageViews.add(avatar5);
        avatarImageViews.add(avatar6);
        avatarImageViews.add(avatar7);
        avatarImageViews.add(avatar8);
        avatarImageViews.add(avatar9);

        selectedAvatarPreview = (ImageView)findViewById(R.id.avatarSelectedPreview);
    }

    private void loadTextViews(){
        textAvatarOk = (TextView) findViewById(R.id.text_is_avatar_ok);
        textChoose = (TextView) findViewById(R.id.text_choose_avatar);
        textHowOld = (TextView) findViewById(R.id.text_how_old);
    }

    private void loadButtons(){
        buttonCancelAvatar = (Button) findViewById(R.id.avatar_select_button_cancel);
        buttonOkAvatar = (Button) findViewById(R.id.avatar_select_button_ok);
        buttonConfirmAge = (Button) findViewById(R.id.confirmAgeButton);
    }

    private void setAvatarListeners(){
        int i = 1;
        for (ImageView a: avatarImageViews){
            a.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    avatarSelected(v);
                }
            });
            i++;
        }
    }

    private void avatarSelected(View v){
        Log.d(Constants.FREEMIUM_LOG_TAG,"Avatar selected " + v.getTag());
        int bigAvatarId;
        switch(Integer.parseInt((String)v.getTag())){
            case 1: bigAvatarId = R.drawable.avatar1_big;
                    break;
            case 2: bigAvatarId = R.drawable.avatar2_big;
                    break;
            case 3: bigAvatarId = R.drawable.avatar3_big;
                    break;
            case 4: bigAvatarId = R.drawable.avatar4_big;
                    break;
            case 5: bigAvatarId = R.drawable.avatar5_big;
                    break;
            case 6: bigAvatarId = R.drawable.avatar6_big;
                    break;
            case 7: bigAvatarId = R.drawable.avatar7_big;
                    break;
            case 8: bigAvatarId = R.drawable.avatar8_big;
                    break;
            case 9: bigAvatarId = R.drawable.avatar9_big;
                    break;
            default: bigAvatarId = R.drawable.avatar1_big;
        }

        //sets new image to avatar preview
        selectedAvatarPreview = (ImageView) findViewById(R.id.avatarSelectedPreview);
        selectedAvatarPreview.setImageResource(bigAvatarId);
        selectedAvatarPreview.invalidate();
        selectedAvatar = Integer.parseInt((String) v.getTag());
        showAvatars(false);
    }

    private void showAvatars(final boolean visible){

        final int deltaY;
        if (visible){
            deltaY = -windowHeight/2;
        }else{
            deltaY = windowHeight/2;
        }

        //moves every avatar back up
        for (ImageView i: avatarImageViews){
            final ImageView animatedAvatar = i;
            TranslateAnimation animation = new TranslateAnimation(0,0,0,deltaY);
            animation.setDuration(700);
            animation.setFillAfter(false);
            animation.setAnimationListener(new Animation.AnimationListener() {
                                               @Override
                                               public void onAnimationStart(Animation animation) {
                                               }

                                               @Override
                                               public void onAnimationRepeat(Animation animation) {
                                               }

                                               @Override
                                               public void onAnimationEnd(Animation animation) {
                                                   animatedAvatar.clearAnimation();

                                                   animatedAvatar.setY(animatedAvatar.getY() + deltaY);
//                                                   Log.d(Constants.FREEMIUM_LOG_TAG, "anim done for " + animatedAvatar.getTag() + " y:" + animatedAvatar.getY());
                                                   showAvatarPreview(!visible);
                                               }
                                           }
            );
            i.startAnimation(animation);
        }
    }

    private void showAvatarPreview(final boolean visible){

        if ((!visible && selectedAvatarPreview != null && selectedAvatarPreview.getVisibility() == View.GONE)
        || (visible && selectedAvatarPreview != null && selectedAvatarPreview.getVisibility() == View.VISIBLE)){
            //trying to hide when view is already invisible or show when it is already visible -> do nothing
            return;
        }

        final int deltaY;
        if (visible){
            deltaY = -windowHeight/2;
            selectedAvatarPreview.setVisibility(View.VISIBLE);
        }else{
            deltaY = windowHeight/2;
            selectedAvatarPreview.setVisibility(View.GONE);
            textAvatarOk.setVisibility(View.GONE);
            textChoose.setVisibility(View.VISIBLE);
            buttonCancelAvatar.setVisibility(View.GONE);
            buttonOkAvatar.setVisibility(View.GONE);
        }

        TranslateAnimation animation = new TranslateAnimation(0,0,0,deltaY);
        animation.setDuration(300);
        animation.setFillAfter(false);
        animation.setAnimationListener(new Animation.AnimationListener() {
                                           @Override
                                           public void onAnimationStart(Animation animation) {
                                           }

                                           @Override
                                           public void onAnimationRepeat(Animation animation) {
                                           }

                                           @Override
                                           public void onAnimationEnd(Animation animation) {
                                               selectedAvatarPreview.clearAnimation();
                                               selectedAvatarPreview.setY(selectedAvatarPreview.getY() + deltaY);

                                               //shows text
                                               if (visible) {
                                                   textAvatarOk.setVisibility(View.VISIBLE);
                                                   textChoose.setVisibility(View.GONE);
                                                   buttonCancelAvatar.setVisibility(View.VISIBLE);
                                                   buttonOkAvatar.setVisibility(View.VISIBLE);
                                               }
                                           }
                                       }
        );
        selectedAvatarPreview.startAnimation(animation);
    }

    /** Volver a la pantalla de inicio */
    private void irWelcome() {
        finish();
    }

    public void backButtonPressed(View view){
        if (choosingAge){
            showAvatarPreview(false);
            showAvatars(true);
            choosingAge = false;
            textHowOld.setVisibility(View.GONE);
            buttonConfirmAge.setVisibility(View.GONE);
            avatarAgeSeekBar.setVisibility(View.GONE);
        }else{
            irWelcome();
        }
    }

    public void skipButtonPressed(View view){
        Log.d(Constants.FREEMIUM_LOG_TAG, "Skip button pressed");
        showAvatarPreview(false);
        showAvatars(true);
    }

    public void cancelAvatarPressed(View view){
        showAvatarPreview(false);
        showAvatars(true);
    }

    public void okAvatarPressed(View view){
        choosingAge = true;
        buttonOkAvatar.setVisibility(View.GONE);
        buttonCancelAvatar.setVisibility(View.GONE);
        textAvatarOk.setVisibility(View.GONE);
        showAgeChooser();
    }

    private void showAgeChooser(){
        buttonConfirmAge.setVisibility(View.VISIBLE);
        textHowOld.setVisibility(View.VISIBLE);
        avatarAgeSeekBar.setVisibility(View.VISIBLE);
    }

    public void okAvatarAgePressed(View view){
        //reads freemium age from seekbar and starts freemium session
        selectedAge = avatarAgeSeekBar.getProgress();
        startFreemiumSession();
    }

    private void startFreemiumSession(){
        finish();

        Intent intent = new Intent(this, FreemiumMainActivity.class);
        intent.putExtra("selectedAvatar", selectedAvatar);
        intent.putExtra("selectedAge", selectedAge);
        startActivity(intent);
    }
}
