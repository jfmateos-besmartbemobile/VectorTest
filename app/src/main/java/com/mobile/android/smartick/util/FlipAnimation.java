package com.mobile.android.smartick.util;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by dsanchezc on 17/8/17.
 */

public class FlipAnimation extends Animation {

  private Camera camera;

  private View fromView;
  private View toView;
  private View fromShadow;
  private View toShadow;

  private float centerX;
  private float centerY;

  private boolean forward = true;

  public FlipAnimation(View fromView, View toView, @Nullable View fromShadow, @Nullable View toShadow) {
    this.fromView = fromView;
    this.toView = toView;
    this.fromShadow = fromShadow;
    this.toShadow = toShadow;

    setDuration(600);
    setFillAfter(false);
    setInterpolator(new AccelerateDecelerateInterpolator());
  }

  public void reverse() {
    forward = false;
    View switchView = toView;
    toView = fromView;
    fromView = switchView;
  }

  @Override
  public void initialize(int width, int height, int parentWidth, int parentHeight) {
    super.initialize(width, height, parentWidth, parentHeight);
    centerX = width / 2f;
    centerY = height / 2f;
    camera = new Camera();
  }

  @Override
  protected void applyTransformation(float interpolatedTime, Transformation t) {
    final double radians = Math.PI * interpolatedTime;
    float degrees = (float) (180.0 * radians / Math.PI);

    if (interpolatedTime >= 0.5f) {
      degrees -= 180.f;
      fromView.setVisibility(View.GONE);
      toView.setVisibility(View.VISIBLE);
    }

    if (forward)
      degrees = -degrees;
    if (fromShadow != null && toShadow != null) {
      fromShadow.setAlpha(Math.abs(degrees) / 90f);
      toShadow.setAlpha(Math.abs(degrees) / 90f);
    }

    final Matrix matrix = t.getMatrix();
    camera.save();
    camera.getMatrix(matrix);
    camera.rotateY(degrees);
    camera.getMatrix(matrix);
    camera.restore();
    matrix.preScale(1 - Math.abs(degrees) / 90f, 1);
    matrix.preTranslate(-centerX, -centerY);
    matrix.postTranslate(centerX, centerY);
  }
}