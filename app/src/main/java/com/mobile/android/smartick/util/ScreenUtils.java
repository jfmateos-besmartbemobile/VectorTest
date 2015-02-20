package com.mobile.android.smartick.util;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {
	private final static Double WIDTH_BODY = 1000d;
	private final static Double HEIGHT_BODY = 700d;
	
	@SuppressLint({ "NewApi", "UseValueOf" })
	public static int getScale(WindowManager windowManager, String pathUrl){
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        int width, height;
        try { 
        	display.getSize(size);
        	width = size.x;
        	height = size.y;
        }catch (NoSuchMethodError e) {
        	/*Para SDKs antiguas*/
        	width = display.getWidth();
        	height = display.getHeight();
        }
        Double val = 0d;
        if(isProblemasPath(pathUrl)){
        	val = new Double(height)/HEIGHT_BODY;
        }else{
            val = new Double(width)/WIDTH_BODY;
        }
        val = val * 100d;
        return val.intValue();
    }
	
	private static boolean isProblemasPath(String pathUrl){
		return pathUrl == null ? false : pathUrl.contains("/presentacionProblema.html");
	}
	
}