package com.irwin.transfer.upload;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IrwinX on 2016/7/24.
 */
public class AsyncUploader extends AsyncTask<Void, Void, String> implements Uploader.UploadListener {

    private Uploader.UploadListener mListener;

    private Object mParam;

    public AsyncUploader setParam(UploadParam... param) {
        mParam = param;
        return this;
    }

    public AsyncUploader setParam(List<UploadParam> paramList) {
        mParam = paramList;
        return this;
    }

    public Object getParam() {
        return mParam;
    }

    public AsyncUploader setListener(Uploader.UploadListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    protected String doInBackground(Void... params) {
        Uploader uploader = createUploader();
        uploader.setListener(this);
        if (mParam == null) {
            throw new NullPointerException("Null upload params.");
        }
        if (mParam instanceof Collection) {
            uploader.setParam((Collection) mParam);
        } else if (mParam instanceof UploadParam[]) {
            uploader.setParam((UploadParam[]) mParam);
        } else if (mParam instanceof UploadParam) {
            uploader.setParam((UploadParam) mParam);
        } else {
            throw new IllegalArgumentException("Unsupported  param: " + (mParam));
        }
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
