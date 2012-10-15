package com.smartick;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {
	
	@SuppressLint("NewApi")
	public static int getScale(WindowManager windowManager){
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        int width, height;
        try { 
        	display.getSize(size);
        	width = size.x;
        	height = size.y;
        }catch (NoSuchMethodError e) { 
        	width = display.getWidth();
        	height = display.getHeight();
        }
        Double val = new Double(height)/new Double(width);
        val = val * 110d;
        return val.intValue();
    }

}
