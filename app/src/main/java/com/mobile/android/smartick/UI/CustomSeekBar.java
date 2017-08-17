package com.mobile.android.smartick.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by sbarrio on 25/06/15.
 */

public class CustomSeekBar extends SeekBar {

  private static final int textMargin = 6;
  private static final int leftPlusRightTextMargins = textMargin + textMargin;
  private static final int maxFontSize = 160;
  private static final int minFontSize = 100;

  private Rect textBounds;

  protected String overlayText;
  protected Paint textPaint;


  public CustomSeekBar(Context context, AttributeSet attrs) {
    super(context, attrs);

    //Set up drawn text attributes here
    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    textPaint.setTextAlign(Paint.Align.LEFT);
    Typeface tfBoogaloo = Typeface.createFromAsset(getContext().getAssets(), "fonts/Boogaloo-Regular.otf");
    textPaint.setTypeface(tfBoogaloo);
    textPaint.setColor(Color.WHITE);
    textPaint.setStrokeWidth(10);
    textPaint.setTextSize(maxFontSize);
    textBounds = new Rect();
  }

  //This attempts to ensure that the text fits inside your SeekBar on a resize
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    setFontSmallEnoughToFit(w - leftPlusRightTextMargins);
  }

  //Finds the largest text size that will fit
  protected void setFontSmallEnoughToFit(int width) {
    int textSize = maxFontSize;
    textPaint.setTextSize(textSize);
    while ((textPaint.measureText("  ") > width) && (textSize > minFontSize)) {
      textSize--;
      textPaint.setTextSize(textSize);
    }
  }

  //Clients use this to change the displayed text
  public void setOverlayText(String text) {
    this.overlayText = text;
    invalidate();
  }

  //Draws the text onto the SeekBar
  @Override
  protected synchronized void onDraw(Canvas canvas) {
    //Draw everything else (i.e., the usual SeekBar) first
    super.onDraw(canvas);

    //No text, no problem
    if (overlayText == null || overlayText.length() == 0) return;

    canvas.save();

    textPaint.getTextBounds(overlayText, 0, overlayText.length(), textBounds);

    //Finally, just draw the text on top of the SeekBar
    float x = getThumb().getBounds().centerX() - 4 * textBounds.width() / 6;
    float y = getThumb().getBounds().centerY() + 3 * textBounds.height() / 6;
    canvas.drawText(overlayText, x, y, textPaint);

    canvas.restore();
  }
}

