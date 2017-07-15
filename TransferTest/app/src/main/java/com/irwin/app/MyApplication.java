package com.irwin.app;

import android.app.Application;

import com.irwin.transfer.download.DownloadTaskManager;

/**
 * Created by Irwin on 2016/3/14.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadTaskManager.setup(this);
    }

}
