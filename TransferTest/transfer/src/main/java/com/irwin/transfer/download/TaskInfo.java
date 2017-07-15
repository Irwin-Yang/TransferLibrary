package com.irwin.transfer.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.irwin.database.BaseColumns;


/**
 * Created by Irwin on 2016/3/9.
 */
public class TaskInfo extends Request {
    public interface Columns extends BaseColumns {

        /**
         * Source url.
         */
        public static final String URL = "url";

        /**
         * Full absolute path of downloaded file.
         */
        public static final String PATH = "path";

        /**
         * Source name.
         */
        public static final String NAME = "name";

        /**
         * Task status.
         */
        public static final String STATUS = "status";

        /**
         * Current downloaded bytes.
         */
        public static final String CURRENT_BYTES = "current_bytes";

        /**
         * Total bytes of source.
         */
        public static final String TOTAL_BYTES = "total_bytes";

        /**
         * Description of task.
         */
        public static final String DESCRIPTION = "description";

        /**
         * Time when the task was created.
         */
        public static final String CREATE_TIME = "create_time";

        /**
         * Time when status updated.
         */
        public static final String UPDATE_TIME = "update_time";
    }

    /**
     * Task id
     */
    public final long ID;


    /**
     * Task status.
     */
    public int Status;

    /**
     * Current downloaded bytes.
     */
    public long CurrentBytes;

    /**
     * Total bytes of source.
     */
    public long TotalBytes;

    /**
     * Time when the task was created.
     */
    public long CreateTime;

    /**
     * Time when status updated.
     */
    public long UpdateTime;

    public TaskInfo(long id, String url, String path,
                    String name, int status, String description) {
        this(id, url, path, name, status, 0, 0, description, 0, 0);
    }

    public TaskInfo(long id, String url, String path,
                    String name, int status, long currentBytes,
                    long totalBytes, String description, long createTime, long updateTime) {
        super(url, name, path, description);
        ID = id;
        Status = status;
        CurrentBytes = currentBytes;
        TotalBytes = totalBytes;
        CreateTime = createTime <= 0 ? System.currentTimeMillis() : createTime;
        UpdateTime = updateTime <= 0 ? CreateTime : updateTime;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "ID=" + ID +
                ", Status=" + Status +
                ", CurrentBytes=" + CurrentBytes +
                ", TotalBytes=" + TotalBytes +
                ", CreateTime=" + CreateTime +
                ", UpdateTime=" + UpdateTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.ID);
        dest.writeInt(this.Status);
        dest.writeLong(this.CurrentBytes);
        dest.writeLong(this.TotalBytes);
        dest.writeLong(this.CreateTime);
        dest.writeLong(this.UpdateTime);
    }

    protected TaskInfo(Parcel in) {
        super(in);
        this.ID = in.readLong();
        this.Status = in.readInt();
        this.CurrentBytes = in.readLong();
        this.TotalBytes = in.readLong();
        this.CreateTime = in.readLong();
        this.UpdateTime = in.readLong();
    }

    public static final Parcelable.Creator<TaskInfo> CREATOR = new Parcelable.Creator<TaskInfo>() {
        public TaskInfo createFromParcel(Parcel source) {
            return new TaskInfo(source);
        }

        public TaskInfo[] newArray(int size) {
            return new TaskInfo[size];
        }
    };
}
