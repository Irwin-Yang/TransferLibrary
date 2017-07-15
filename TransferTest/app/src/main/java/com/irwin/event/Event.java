package com.irwin.event;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Irwin on 2015/12/28.
 */
public class Event implements Parcelable {
    /**
     * Event type.
     */
    public final int Type;

    private Bundle mExtra;

    private boolean mIsOrdered = true;

    private boolean mIsCancel = false;

    public Event(int eventType) {
        this(eventType, true, null);
    }

    public Event(int eventType, boolean isOrdered) {
        this(eventType, isOrdered, null);
    }

    public Event(int eventType, Bundle extra) {
        this(eventType, true, extra);
    }

    public Event(int eventType, boolean isOrdered, Bundle extra) {
        Type = eventType;
        mIsOrdered = isOrdered;
        mExtra = extra;
    }

    public Event setExtra(Bundle extra) {
        mExtra = extra;
        return this;
    }

    public Bundle getExtra() {
        return mExtra;
    }

    void setOrdered() {
        mIsOrdered = true;
    }

    public boolean isOrdered() {
        return mIsOrdered;
    }

    public boolean isCancel() {
        return mIsCancel;
    }

    public void setCancel() {
        if (mIsOrdered) {
            mIsCancel = true;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Type);
        dest.writeBundle(mExtra);
    }

    protected Event(Parcel in) {
        this.Type = in.readInt();
        mExtra = in.readBundle();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
