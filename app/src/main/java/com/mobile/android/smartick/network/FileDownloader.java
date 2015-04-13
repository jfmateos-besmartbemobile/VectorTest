package com.mobile.android.smartick.network;

import android.util.Log;

import com.mobile.android.smartick.util.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by sbarrio on 13/04/15.
 */
public class FileDownloader {

        public static void download(String fileURL, File targetFile, String cookieString) {
            int count;
            try {
                URL urlDownload = new URL(fileURL);
                URLConnection conn = urlDownload.openConnection();
                if (cookieString != null){
                    conn.setRequestProperty("Cookie", cookieString);
                }
                conn.connect();

                int lengthOfFile = conn.getContentLength();

                Log.d(Constants.FILE_DL_TAG, "Downloading file with size: " + lengthOfFile);

                // download the file
                InputStream input = conn.getInputStream();
                OutputStream output = new FileOutputStream(targetFile);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
