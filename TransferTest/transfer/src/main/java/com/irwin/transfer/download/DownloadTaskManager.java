package com.irwin.transfer.download;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.irwin.database.IDBObserver;
import com.irwin.database.ObservableDao;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Irwin on 2016/3/9.
 * Download Manager
 */
public class DownloadTaskManager implements TaskInfo.Columns {

    private static final String TAG = "DownloadTaskManager";

    private static DownloadTaskManager INSTANCE;

    private static ArrayList<WeakReference<DownloadCallback>> mCallbackList = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private int mParallel = 3;

    private boolean mRepeatedly = true;

    private HashMap<Long, TaskDownloader> mTaskMap = new HashMap<>(mParallel);

    private TaskDBHelper mTaskDBHelper;

    public static synchronized DownloadTaskManager setup(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DownloadTaskManager(context);
        }
        return INSTANCE;
    }

    public static DownloadTaskManager getInstance() {
        return INSTANCE;
    }

    private DownloadTaskManager(Context context) {
        mTaskDBHelper = new TaskDBHelper(context, null);
        TaskDao.getInstance().registerObserver(mTaskObserver);
    }

    public DownloadTaskManager addCallback(DownloadCallback cb) {
        if (cb != null) {
            mCallbackList.add(new WeakReference<DownloadCallback>(cb));
        }
        return this;
    }

    public void removeCallback(DownloadCallback cb) {
        synchronized (mCallbackList) {
            Iterator<WeakReference<DownloadCallback>> iterator = mCallbackList.iterator();
            DownloadCallback item;
            while (iterator.hasNext()) {
                item = iterator.next().get();
                if (item == null) {
                    iterator.remove();
                    continue;
                }
                if (item == cb) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public DownloadTaskManager setParallel(int count) {
        if (count <= 0) {
            throw new InvalidParameterException("Parallel count must >0");
        }
        mParallel = count;
        return this;
    }

    public int getParallel() {
        return mParallel;
    }

    /**
     * @return
     */
    public boolean isDownloadRepeatedly() {
        return mRepeatedly;
    }

    /**
     * Set if re-download tasks has finished or been running.We take task with same url as
     * same task.
     *
     * @param repeatedly true if re-download
     */
    public void setDownloadRepeatedly(boolean repeatedly) {
        mRepeatedly = repeatedly;
    }

    long createTask(Request request) {
        TaskDao taskDao = TaskDao.getInstance();
        if (!mRepeatedly) {
            final TaskInfo local = getExisted(request);
            if (local != null) {
                boolean errOccurred = false;
                switch (local.Status) {
                    case Status.CANCELED:
                    case Status.ERROR:
                    case Status.PAUSED_MANUALLY:
                    case Status.PAUSED:
                        if (taskDao.updateStatus(local.ID, Status.WAITING, true) <= 0) {
                            Log.w("Info", "Fail to update status of download task: " + request);
                            errOccurred = true;
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyStatus(local.ID, Status.WAITING);
                                }
                            });
                        }
                        break;
                    default:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyStatus(local.ID, local.Status);
                            }
                        });
                        break;
                }
                if (!errOccurred) {
                    return local.ID;
                }
            }
        }
        ContentValues values = new ContentValues();
        values.put(URL, request.getUrl());
        values.put(PATH, request.getPath());
        values.put(NAME, request.getName());
        values.put(DESCRIPTION, request.getDescription());
        values.put(STATUS, Status.WAITING);
        long time = System.currentTimeMillis();
        values.put(CREATE_TIME, time);
        values.put(UPDATE_TIME, time);
        long id = taskDao.insert(values, null, null);
        if (id > 0) {
            taskDao.notifyDBChange(ObservableDao.INSERT, id, null, 1, null);
        }
        return id;
    }

    TaskInfo getExisted(Request request) {
        List<TaskInfo> list = TaskDao.getInstance().query(URL + "=? AND " + PATH + "=? ",
                new String[]{String.valueOf(request.Url), String.valueOf(request.getPath())}, 0, 1);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }


    public long enqueue(Request request) {
        if (!request.isValid()) {
            Log.e(TAG, "Request is not valid: " + request);
            return -1;
        }
        return createTask(request);
    }


    public boolean pause(long id) {
        boolean ret = false;
        TaskDownloader target = mTaskMap.get(Long.valueOf(id));
        if (target != null) {
            ret = target.pauseTask(true);
            return ret;
        }
        ret = TaskDao.getInstance().updateStatus(id, Status.WAITING, true) > 0;
        return ret;
    }

    public boolean resume(long id) {
        TaskInfo info = getInfo(id);
        if (info == null) {
            return false;
        }
        boolean ret = false;
        switch (info.Status) {
            case Status.CANCELED:
            case Status.ERROR:
            case Status.PAUSED_MANUALLY:
            case Status.PAUSED:
                ret = TaskDao.getInstance().updateStatus(id, Status.WAITING, true) > 0;
                break;
        }
        return ret;
    }

    public boolean remove(long id) {
        return remove(id, true);
    }

    public boolean remove(long id, boolean deleteFile) {
        boolean ret = false;
        TaskDownloader target = mTaskMap.get(Long.valueOf(id));
        if (target != null) {
            ret = target.stopTask();
        }
        TaskDao.getInstance().deleteTask(id, deleteFile);
        return ret;
    }

    public TaskInfo getInfo(long id) {
        return TaskDao.getInstance().queryByID(id);
    }

    private void checkTask() {
        synchronized (mTaskMap) {
            if (mTaskMap.size() >= mParallel) {
                return;
            }
            List<TaskInfo> waitings = TaskDao.getInstance().getWaitings();
            if (waitings == null || waitings.size() == 0) {
                Log.w(TAG, "No waiting tasks.");
                return;
            }
            int size = mParallel - mTaskMap.size();
            int waitingCount = waitings.size();
            for (int i = 0; i < size && i < waitingCount; i++) {
                final TaskInfo info = waitings.get(i);
                //Avoid download duplicately.
                if (mTaskMap.get(Long.valueOf(info.ID)) != null) {
                    i--;
                    waitings.remove(info);
                    waitingCount = waitings.size();
                    continue;
                }
                new TaskDownloader(info).start();
            }
        }
    }

    public void destroy() {
        synchronized (mTaskMap) {
            Iterator<TaskDownloader> iterator = mTaskMap.values().iterator();
            TaskDownloader downloader;
            while (iterator.hasNext()) {
                downloader = iterator.next();
                downloader.pauseTask(false);
            }
            mTaskMap.clear();
        }

        TaskDao.getInstance().unregisterObserver(mTaskObserver);
        if (mTaskDBHelper != null) {
            mTaskDBHelper.close();
            mTaskDBHelper = null;
        }
    }


    protected void removeRunning(Long id) {
        synchronized (mTaskMap) {
            mTaskMap.remove(Long.valueOf(id));
        }
        checkTask();
    }

    protected void notifyStatus(final long id, final int status) {
        synchronized (mCallbackList) {
            Iterator<WeakReference<DownloadCallback>> iterator = mCallbackList.iterator();
            DownloadCallback target;
            while (iterator.hasNext()) {
                target = iterator.next().get();
                if (target == null) {
                    iterator.remove();
                    continue;
                }
                target.onStatusChange(id, status);
            }
        }
    }

    protected void notifyProgress(final long id, final long currentBytes, final long totalBytes) {
        synchronized (mCallbackList) {
            Iterator<WeakReference<DownloadCallback>> iterator = mCallbackList.iterator();
            DownloadCallback target;
            while (iterator.hasNext()) {
                target = iterator.next().get();
                if (target == null) {
                    iterator.remove();
                    continue;
                }
                target.onProgress(id, currentBytes, totalBytes);
            }
        }
    }

    public  interface DownloadCallback {

         void onStatusChange(long id, int status);

         void onProgress(long id, long currentBytes, long totalBytes);
    }

    private final IDBObserver mTaskObserver = new IDBObserver() {
        @Override
        public void onChange(int type, Object id, Object entity, int count, Object tag) {
            switch (type) {
                case ObservableDao.INSERT:
                    notifyStatus((Long) id, Status.WAITING);
                    break;
                case ObservableDao.UPDATE:
                    //We filtered the status update by Tag in TaskDao.
                    if (TaskInfo.Columns.STATUS.equals(tag)) {
                        notifyStatus((Long) id, (Integer) entity);
                    }
                    break;
                case ObservableDao.DELETE:
                    break;
            }
            Log.i(TAG, "DB changed,check task");
            checkTask();
        }
    };

    private class TaskExecutor extends Thread {
        public final TaskInfo Task;

        public TaskExecutor(TaskInfo task) {
            Task = task;
        }
    }

    private class TaskDownloader extends TaskExecutor implements DownloadListener {

        public final Downloader Downloader;

        public TaskDownloader(TaskInfo task) {
            super(task);
            Downloader = new Downloader();
            Downloader.setUrl(task.getUrl()).setPath(task.getPath())
                    .setRange(task.CurrentBytes, task.TotalBytes).setListener(this);
            mTaskMap.put(Long.valueOf(task.ID), this);
        }

        public boolean pauseTask(boolean fromUser) {
            Downloader.pause(fromUser);
            return true;
        }

        public boolean stopTask() {
            Downloader.cancel();
            return true;
        }

        @Override
        public void run() {
            super.run();
            Downloader.download();
            Long id = Task.ID;
            if (mTaskMap.get(id) != null) {
                //Ensure to running next task.
                removeRunning(Task.ID);
            }
        }

        @Override
        public void onStatusChange(Downloader downloader, final int status) {
            Task.Status = status;
            //If task is running,No need to notify db change.
            boolean notify = status != DOWNLOADING && status != WAITING;
            if (notify) {
                //Remove it from task map to let other tasks go.
                removeRunning(Task.ID);
            }
            TaskDao.getInstance().updateStatus(Task.ID, status, notify);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyStatus(Task.ID, status);
                }
            });
        }

        @Override
        public void onProgress(final long currentBytes, final long totalBytes) {
            Task.CurrentBytes = currentBytes;
            Task.TotalBytes = totalBytes;
            TaskDao.getInstance().updateProgress(Task);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyProgress(Task.ID, currentBytes, totalBytes);
                }
            });
        }
    }


}
