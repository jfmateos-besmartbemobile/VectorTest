package com.smartick;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {
	private final static Double WIDTH_BODY = (double) 1000;
	
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
        Double val = new Double(width)/new Double(WIDTH_BODY);
        val = val * 100d;
        return val.intValue();
    }

}