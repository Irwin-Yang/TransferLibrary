package com.irwin.transfer.upload;

import android.text.TextUtils;

import com.irwin.transfer.MD5Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IrwinX on 2016/7/24.
 */
public class UploadParam implements MultipartConsts {
    private String mUrl;
    private String mBoundary;
    private String mFileKey;
    private String mFileName;
    private String mPath;
    private String mFileType;
    private HashMap<String, String> mHeaders;
    private List<MultipartParam> mParams;

    public static String generateBoundary(String key) {
        return BOUNDARY_PREFIX + BOUNDARY_PREFIX + key;
    }

    public static String getNameFromPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return path;
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public UploadParam(String url) {
        mUrl = url;
    }


    public String getUrl() {
        return mUrl;
    }

    public String getBoundary() {
        return (TextUtils.isEmpty(mBoundary) ? generateBoundary(MD5Util.getMD5("AndroidMultipart")) : mBoundary);
    }

    public UploadParam setBoundary(String boundary) {
        if (!boundary.startsWith(BOUNDARY_PREFIX + BOUNDARY_PREFIX)) {
            boundary = generateBoundary(boundary);
        }
        this.mBoundary = boundary;
        return this;
    }


    public String getFileName() {
        if (TextUtils.isEmpty(mFileName)) {
            mFileName = getNameFromPath(mPath);
        }
        return mFileName;
    }

    public UploadParam setFileName(String name) {
        mFileName = name;
        return this;
    }

    public String getPath() {
        return mPath;
    }

    public UploadParam setPath(String path) {
        mPath = path;
        return this;
    }

    public String getContentType() {
        return mFileType;
    }

    public UploadParam setContentType(String contentType) {
        mFileType = contentType;
        return this;
    }

    public String getFileKey() {
        return mFileKey;
    }

    public UploadParam setFileKey(String fileKey) {
        this.mFileKey = fileKey;
        return this;
    }

    public HashMap<String, String> getHeaders() {
        return mHeaders;
    }

    public UploadParam addHeader(String key, String value) {
        if (mHeaders == null) {
            mHeaders = new HashMap<>();
        }
        mHeaders.put(key, value);
        return this;
    }

    public UploadParam addHeaders(HashMap<String, String> headers) {
        if (mHeaders == null) {
            mHeaders = new HashMap<>();
        }
        mHeaders.putAll(headers);
        return this;
    }

    public UploadParam addParams(MultipartParam param) {
        if (mParams == null) {
            mParams = new ArrayList<>();
        }
        mParams.add(param);
        return this;
    }

    public UploadParam addParams(Collection<MultipartParam> collection) {
        if (mParams == null) {
            mParams = new ArrayList<>();
        }
        mParams.addAll(collection);
        return this;
    }

    public List<MultipartParam> getParams() {
        return mParams;
    }


}
