package com.irwin.transfer.download;


import android.os.AsyncTask;

/**
 * Created by Irwin on 2016/2/22.
 * A lite implementation for downloading file asynchronously.
 */
public class AsyncDownloader extends AsyncTask<String, Long, Void> {
    private Downloader mDownloader;

    public AsyncDownloader(String url, String path, DownloadListener downloadListener) {
        this(url, path, 0, 0, downloadListener);
    }

    public AsyncDownloader(String url, String path, long rangeStart, long rangeEnd, DownloadListener downloadListener) {
        Downloader downloader = new Downloader();
        mDownloader = downloader;
        downloader.setUrl(url);
        downloader.setPath(path);
        downloader.setRange(rangeStart, 0);
        downloader.setListener(downloadListener);
    }

    @Override
    protected Void doInBackground(String... params) {
        if (mDownloader != null) {
            mDownloader.download();
        }
        return null;
    }

    public AsyncDownloader setListener(DownloadListener listener) {
        if (mDownloader != null) {
            mDownloader.setListener(listener);
        }
        return this;
    }

}
