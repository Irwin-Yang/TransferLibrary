package com.irwin.transfer.download;

import android.text.TextUtils;

import com.irwin.transfer.ICallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IrwinX on 2017/4/14.
 */

public class TextDownloader {
    private String mUrl;
    private ICallback<String> mCallback;

    public TextDownloader(String url) {
        mUrl = url;
    }

    public TextDownloader setCallback(ICallback<String> callback) {
        mCallback = callback;
        return this;
    }

    public void download() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mCallback.onSuccess(downloadSync());
                } catch (Exception e) {
                    mCallback.onFail(e);
                }
            }
        }.start();
    }


    public String downloadSync() throws IOException {
        HttpURLConnection connection = null;
        InputStream ins = null;
        String result = null;
        try {
            URL uri = new URL(mUrl);
            connection = (HttpURLConnection) uri.openConnection();
            //Add this to fix EOFException.
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Encoding", "");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = "";
                ins = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                result = response;
            }
            if (ins != null) {
                ins.close();
                ins = null;
            }
            connection.disconnect();
            connection = null;
        } finally {
            try {
                if (ins != null) {
                    ins.close();
                    ins = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }
        return result;
    }
}
