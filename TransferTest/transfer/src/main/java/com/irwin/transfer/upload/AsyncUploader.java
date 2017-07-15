package com.irwin.transfer.upload;

import android.os.AsyncTask;

/**
 * Created by IrwinX on 2016/7/24.
 */
public class AsyncUploader extends AsyncTask<Void, Void, String> implements Uploader.UploadListener {

    private Uploader.UploadListener mListener;

    private UploadParam mParam;

    public AsyncUploader setParam(UploadParam param) {
        mParam = param;
        return this;
    }

    public UploadParam getParam() {
        return mParam;
    }

    public AsyncUploader setListener(Uploader.UploadListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    protected String doInBackground(Void... params) {
        Uploader uploader = createUploader();
        uploader.setParam(mParam).setListener(this);
        return uploader.upload();
    }

    protected Uploader createUploader() {
        return new Uploader();
    }

    @Override
    public void onStatusChanged(int status, Object msg) {
        if (mListener != null) {
            mListener.onStatusChanged(status, msg);
        }
    }

    @Override
    public void onProgress(long currentProgress, long totalProgress) {
        if (mListener != null) {
            mListener.onProgress(currentProgress, totalProgress);
        }
    }

}
