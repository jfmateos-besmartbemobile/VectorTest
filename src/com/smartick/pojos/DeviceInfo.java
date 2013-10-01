package com.smartick.pojos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;

public class DeviceInfo{
	
	public static final String deviceInfoFileName = "SMARTICK_DEVICEINFO";

	public static String createDeviceInfoFile(Context context){	
		File f = getDeviceInfoFile(context);
		try {
				if (!f.exists()){
					writeDeviceInfoFile(f);
				}
			} catch (IOException e) {
				e.printStackTrace();
		}
		return f.getAbsolutePath();    
	}
	
	public static void writeDeviceInfoFile(File deviceInfoFile) throws IOException{
        FileOutputStream out = new FileOutputStream(deviceInfoFile);
        String info = ("OSver= " + System.getProperty("os.version")
        	  + System.getProperty("line.separator")
 			   + "Rel= " + android.os.Build.VERSION.RELEASE
 			  + System.getProperty("line.separator")
 			   + "Device= " + android.os.Build.DEVICE
 			  + System.getProperty("line.separator")
 			   + "Model= " + android.os.Build.MODEL
 			  + System.getProperty("line.separator")
 			   + "Product= " + android.os.Build.PRODUCT
 			  + System.getProperty("line.separator")
 			   + "Brand=" + android.os.Build.BRAND
 			  + System.getProperty("line.separator")
 			   + "Display= " + android.os.Build.DISPLAY
 			  + System.getProperty("line.separator")
 			   + "Hardware= " + android.os.Build.HARDWARE
 			  + System.getProperty("line.separator")
 			   + "Manufacturer= " + android.os.Build.MANUFACTURER
 			  + System.getProperty("line.separator")
 			   + "Serial= " + android.os.Build.SERIAL
 			  + System.getProperty("line.separator")
 			   + "User= " + android.os.Build.USER
 			  + System.getProperty("line.separator")
 			   + "Host=" + android.os.Build.HOST);
		out.write(info.getBytes());
		out.close();
	}
	
	public static String readDeviceInfoFile(File deviceInfoFile){
		RandomAccessFile f;
		byte[] bytes = null;
		try {
			f = new RandomAccessFile(deviceInfoFile, "r");
	        bytes = new byte[(int) f.length()];
	        f.readFully(bytes);
	        f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return new String(bytes);
	}
		
	public static File getDeviceInfoFile(Context context){
		if (context!= null){
			File devInfo = new File(context.getFilesDir(), deviceInfoFileName);
			return devInfo;
		}
		return null;
	}
	
}