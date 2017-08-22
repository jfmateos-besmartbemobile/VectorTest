package com.mobile.android.smartick.cutomviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    Float textSize = a.getDimension(R.styleable.LeftImageButton_android_textSize, 30);
    a.recycle();

    setOrientation(LinearLayout.HORIZONTAL);
    setGravity(Gravity.CENTER_VERTICAL);
    setClickable(true);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.left_image_button, this, true);

    TextView tvLabel = (TextView) findViewById(R.id.tv_label);
    ImageView icon = (ImageView) findViewById(R.id.iv_leftIcon);

    Typeface tfDidact = Typeface.createFromAsset(context.getAssets(), "fonts/DidactGothic.ttf");
    tvLabel.setTypeface(tfDidact);
    tvLabel.setText(label);
    tvLabel.setTextColor(valueColor);
    tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

    icon.setImageDrawable(drawableImage);

  }
}
