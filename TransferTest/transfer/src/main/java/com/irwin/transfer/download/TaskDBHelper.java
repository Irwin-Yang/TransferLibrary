package com.irwin.transfer.download;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import com.irwin.database.AbstractDao;
import com.irwin.database.BaseDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Irwin on 2016/3/9.
 */
public class TaskDBHelper extends BaseDBHelper {
    public static final String DB_NAME = "download.db";
    public static final int DB_VERSION = 1;

    public TaskDBHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        this(context, factory, null);
    }

    public TaskDBHelper(Context context, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        super(context, DB_NAME, factory, DB_VERSION, errorHandler);
    }

    @Override
    public List<AbstractDao> getTables() {
        ArrayList<AbstractDao> list = new ArrayList<>(1);
        list.add(TaskDao.getInstance());
        return list;
    }

}
