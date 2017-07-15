package com.irwin.transfer.download;

import android.content.ContentValues;
import android.database.Cursor;


import com.irwin.database.ObservableDao;
import com.irwin.database.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Irwin on 2016/3/9.
 */
public class TaskDao extends ObservableDao<TaskInfo, Long> implements TaskInfo.Columns {

    private static final TaskDao INSTANCE = new TaskDao();

    public static TaskDao getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Property> getPropertyList() {
        ArrayList<Property> list = new ArrayList<>();
        int index = 0;
        list.add(new Property(ID, Property.TYPE_INTEGER, index++).setPrimaryKey(true).setAutoIncrement(true));
        list.add(new Property(URL, Property.TYPE_TEXT, index++));
        list.add(new Property(PATH, Property.TYPE_TEXT, index++));
        list.add(new Property(NAME, Property.TYPE_TEXT, index++));
        list.add(new Property(STATUS, Property.TYPE_INTEGER, index++));
        list.add(new Property(CURRENT_BYTES, Property.TYPE_INTEGER, index++));
        list.add(new Property(TOTAL_BYTES, Property.TYPE_INTEGER, index++));
        list.add(new Property(DESCRIPTION, Property.TYPE_TEXT, index++));
        list.add(new Property(CREATE_TIME, Property.TYPE_INTEGER, index++));
        list.add(new Property(UPDATE_TIME, Property.TYPE_INTEGER, index++));
        return list;
    }

    @Override
    public String getTableName() {
        return "Task";
    }

    @Override
    public TaskInfo convert(Cursor cursor) {
        int index = 0;
        long id = cursor.getLong(index++);
        String url = cursor.getString(index++);
        String path = cursor.getString(index++);
        String name = cursor.getString(index++);
        int status = cursor.getInt(index++);
        long currentBytes = cursor.getLong(index++);
        long totalBytes = cursor.getLong(index++);
        String desc = cursor.getString(index++);
        long createTime = cursor.getLong(index++);
        long updateTime = cursor.getLong(index++);
        TaskInfo ret = new TaskInfo(id, url,
                path, name, status, currentBytes, totalBytes, desc, createTime, updateTime);
        return ret;
    }

    @Override
    public ContentValues convertValues(TaskInfo data) {
        ContentValues values = new ContentValues();
        values.put(ID, data.ID);
        values.put(URL, data.Url);
        values.put(PATH, data.getPath());
        values.put(NAME, data.getName());
        values.put(STATUS, data.Status);
        values.put(CURRENT_BYTES, data.CurrentBytes);
        values.put(TOTAL_BYTES, data.TotalBytes);
        values.put(DESCRIPTION, data.getDescription());
        values.put(CREATE_TIME, data.CreateTime);
        values.put(UPDATE_TIME, data.UpdateTime);
        return values;
    }

    /**
     * Build status of task that was interrupted accidentally.
     *
     * @return
     */
    public int buildStatus() {
        ContentValues values = new ContentValues(1);
        values.put(STATUS, Status.PAUSED);
        return update(values, STATUS + "=?", new String[]{String.valueOf(Status.DOWNLOADING)}, null, null);
    }

    public int updateStatus(Long id, int status, boolean notify) {
        ContentValues values = new ContentValues(2);
        values.put(STATUS, status);
        values.put(UPDATE_TIME, System.currentTimeMillis());
        int ret = updateByID(id, values, null, null);
        if (ret > 0) {
            notifyDBChange(UPDATE, id, status, 1, STATUS);
        }
        return ret;
    }

    public int updateProgress(TaskInfo task) {
        ContentValues values = new ContentValues(3);
        values.put(CURRENT_BYTES, task.CurrentBytes);
        values.put(TOTAL_BYTES, task.TotalBytes);
        values.put(UPDATE_TIME, System.currentTimeMillis());
        //Won't notify.
        return updateByID(task.ID, values, null, null);
    }

    public int deleteTask(long id, boolean deleteFile) {
        if (deleteFile) {
            TaskInfo target = queryByID(id);
            if (target == null) {
                return -1;
            }
            File file = new File(target.getPath());
            if (file.exists()) {
                file.delete();
            } else {
                new File(target.getPath() + ".tmp").delete();
            }
        }
        return deleteByID(id);
    }

    public List<TaskInfo> getWaitings() {
        return query(true, STATUS + " BETWEEN ? AND ?",
                new String[]{String.valueOf(Status.WAITING), String.valueOf(Status.PAUSED)},
                null, null, UPDATE_TIME + " ASC", null, null);
    }

    public List<TaskInfo> getTaskByStatus(int status) {
        return query(true, STATUS + "=?",
                new String[]{String.valueOf(status)},
                null, null, UPDATE_TIME + " ASC", null, null);
    }

}
