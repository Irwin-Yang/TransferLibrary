package com.irwin.transfer.download;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;


/**
 * Created by Irwin on 2016/3/9.
 */
public class Request implements Parcelable {

    protected final String Url;

    private String mName;

    private String mPath;

    private String mDescription;

    public Request(String url) {
        this(url, getNameFromUrl(url), null, null);
    }

    public Request(String url, String name, String path, String description) {
        Url = url;
        mName = name;
        mPath = path;
        mDescription = description;
    }

    public static String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public String getUrl() {
        return Url;
    }

    public String getName() {
        return mName;
    }

    public Request setName(String name) {
        mName = name;
        return this;
    }

    public String getPath() {
        return mPath;
    }

    public Request setPath(String path) {
        mPath = path;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Request setDescription(String description) {
        mDescription = description;
        return this;
    }

    public boolean isValid() {
        return (!TextUtils.isEmpty(Url) && !TextUtils.isEmpty(mPath));
    }

    @Override
    public String toString() {
        return "Request{" +
                "Url='" + Url + '\'' +
                ", mName='" + mName + '\'' +
                ", mPath='" + mPath + '\'' +
                ", mDescription='" + mDescription + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Url);
        dest.writeString(this.mName);
        dest.writeString(this.mPath);
        dest.writeString(this.mDescription);
    }

    protected Request(Parcel in) {
        this.Url = in.readString();
        this.mName = in.readString();
        this.mPath = in.readString();
        this.mDescription = in.readString();
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        public Request createFromParcel(Parcel source) {
            return new Request(source);
        }

        public Request[] newArray(int size) {
            return new Request[size];
        }
    };
}
