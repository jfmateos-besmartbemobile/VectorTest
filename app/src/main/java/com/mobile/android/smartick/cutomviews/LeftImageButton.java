package com.mobile.android.smartick.cutomviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mobile.android.smartick.R;

/**
 * Created by dsanchezc on 22/8/17.
 */

public class LeftImageButton extends LinearLayout {

  public LeftImageButton(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeftImageButton, 0, 0);
    Drawable drawableImage = a.getDrawable(R.styleable.LeftImageButton_image);
    String label = a.getString(R.styleable.LeftImageButton_label);
    int valueColor = a.getColor(R.styleable.LeftImageButton_android_textColor, getResources().getColor(R.color.BlueColor));
    final Float textSize = a.getDimension(R.styleable.LeftImageButton_android_textSize, 30);
    int scaleTypeIndex = a.getInt(R.styleable.LeftImageButton_android_scaleType, -1);
    Float imageHeight = a.getDimension(R.styleable.LeftImageButton_imageHeight, 0);
    Float imageWidth = a.getDimension(R.styleable.LeftImageButton_imageWidth, 0);
    a.recycle();

    setOrientation(LinearLayout.HORIZONTAL);
    setGravity(Gravity.CENTER_VERTICAL);
    setClickable(true);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.left_image_button, this, true);

    final TextView tvLabel = (TextView) findViewById(R.id.tv_label);
    final ImageView icon = (ImageView) findViewById(R.id.iv_leftIcon);

    Typeface tfDidact = Typeface.createFromAsset(context.getAssets(), "fonts/DidactGothic.ttf");
    tvLabel.setTypeface(tfDidact);
    tvLabel.setText(label);
    tvLabel.setTextColor(valueColor);
    tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

    if (imageHeight > 0 && imageWidth > 0) {
      ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
      layoutParams.width = Math.round(imageWidth);
      layoutParams.height = Math.round(imageHeight);
      icon.setLayoutParams(layoutParams);
    }

    if (scaleTypeIndex > -1) {
      ImageView.ScaleType[] types = ImageView.ScaleType.values();
      ImageView.ScaleType scaleType = types[scaleTypeIndex];
      icon.setScaleType(scaleType);
    }
    icon.setImageDrawable(drawableImage);

    final float displacementX = textSize / 80;
    final float displacementY = textSize / 40;

    this.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        Log.d("TAG", "l> " + event);
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            icon.animate().translationXBy(displacementX).translationYBy(displacementY).setDuration(2);
            tvLabel.animate().translationXBy(displacementX).translationYBy(displacementY).setDuration(2);
            return false;
          case MotionEvent.ACTION_OUTSIDE:
          case MotionEvent.ACTION_UP:
            icon.animate().translationXBy(-displacementX).translationYBy(-displacementY).setDuration(2);
            tvLabel.animate().translationXBy(-displacementX).translationYBy(-displacementY).setDuration(2);
            return false;
        }
        return false;
      }
    });
  }
}
