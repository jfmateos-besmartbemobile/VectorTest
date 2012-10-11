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
        int width;
        try { 
        	display.getSize(size);
        	width = size.x;
        } catch (NoSuchMethodError e) { 
        	width = display.getWidth();
        }
        Double val = new Double(width)/new Double(1000);
        val = val * 100d;
        return val.intValue();
    }

}
