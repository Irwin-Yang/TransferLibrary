package com.irwin.transfertest;

import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.irwin.app.DLog;
import com.irwin.transfer.IProgressListener;
import com.irwin.transfer.download.DownloadTaskManager;
import com.irwin.transfer.download.Request;
import com.irwin.transfer.download.Status;
import com.irwin.transfer.upload.AsyncUploader;
import com.irwin.transfer.upload.MultipartParam;
import com.irwin.transfer.upload.UploadParam;
import com.irwin.transfer.upload.Uploader;
import com.irwin.utils.FileUtil;
import com.irwin.utils.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements DownloadTaskManager.DownloadCallback {

    private static final String TAG = "DownloadTest";
    private ListView mLV_Download;
    private HashMap<Long, DownloadInfo> mMap = new HashMap<>();
    private DownloadAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLV_Download = (ListView) findViewById(R.id.ListView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                upload();
//                unZip();
                uploadAsync();
            }
        });
        initDownloadManager();
        initList();
    }

    void unZip() {
        final File file = new File(Environment.getExternalStorageDirectory(), "Tile.zip");
        final File path = new File(Environment.getExternalStorageDirectory(), "/Tiles/");
        if (path.exists()) {
            FileUtil.deleteFile(path);
        } else {
            path.mkdirs();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ZipUtil.unZipFileLite(file.getAbsolutePath(), path.getAbsolutePath(), new IProgressListener() {
                        @Override
                        public void onProgress(long currentProgress, long totalProgress) {
                            DLog.i(TAG, "Progress: " + (currentProgress * 100 / totalProgress));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void uploadAsync() {
        String url = "http://172.16.168.12";
        UploadParam param = new UploadParam(url);
        param.setContentType(Uploader.MIME_TYPE_STREAM)
                .setFileKey("fileKey")
                .setPath(new File(Environment.getExternalStorageDirectory(), "scs.jpeg").getAbsolutePath())
                .addParams(new MultipartParam("name", "scs.jpeg"))
                .addParams(new MultipartParam("type", "phootball_match_data"));
        AsyncUploader uploader = new AsyncUploader().setParam(param);
        uploader.setListener(new Uploader.UploadListener() {
            @Override
            public void onStatusChanged(int status, Object msg) {
                switch (status) {
                    case ERROR:
                        DLog.i(TAG, "Upload error");
                        if (msg instanceof Throwable) {
                            ((Throwable) msg).printStackTrace();
                        }
                        break;
                    case FINISHED:
                        DLog.i(TAG, "Upload finished");
                        if (msg instanceof String) {
                            DLog.i(TAG, "Response: " + msg);
                        }
                        break;
                }
            }

            @Override
            public void onProgress(long currentProgress, long totalProgress) {
                DLog.i(TAG, String.format("Currentbytes: %d, total: %d", currentProgress, totalProgress));
            }
        });
        uploader.execute();
    }

    void upload() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String url = "http://pay.southindustry.com/phootball-web/file/upload";
                UploadParam param = new UploadParam(url);
                param.setContentType(Uploader.MIME_TYPE_STREAM)
                        .setFileKey("fileKey")
                        .setPath(new File(Environment.getExternalStorageDirectory(), "scs.jpeg").getAbsolutePath())
                        .addParams(new MultipartParam("name", "scs.jpeg"))
                        .addParams(new MultipartParam("type", "phootball_match_data"));
                Uploader uploader = new Uploader().setParam(param);
                uploader.setListener(new Uploader.UploadListener() {
                    @Override
                    public void onStatusChanged(int status, Object msg) {
                        switch (status) {
                            case ERROR:
                                DLog.i(TAG, "Upload error");
                                if (msg instanceof Throwable) {
                                    ((Throwable) msg).printStackTrace();
                                }
                                break;
                            case FINISHED:
                                DLog.i(TAG, "Upload finished");
                                if (msg instanceof String) {
                                    DLog.i(TAG, "Response: " + msg);
                                }
                                break;
                        }
                    }

                    @Override
                    public void onProgress(long currentProgress, long totalProgress) {
                        DLog.i(TAG, String.format("Currentbytes: %d, total: %d", currentProgress, totalProgress));
                    }
                });
                uploader.upload();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    void initDownloadManager() {
        DownloadTaskManager.setup(this).addCallback(this).setParallel(2).setDownloadRepeatedly(false);
    }

    void initList() {
        String[] array = {"http://182.140.231.63/apk.r1.market.hiapk.com/data/upload/apkres/2017/7_3/9/com.phootball_092808.apk?wsiphost=local",
                "http://apk.r1.market.hiapk.com/data/upload/apkres/2017/7_10/11/me.ele_111208.apk",
                "http://images.cnblogs.com/cnblogs_com/oxgen/576959/o_%E8%8A%82%E7%9B%AE%E4%BB%93%E5%BA%934.jpg",
                "http://118.123.97.62/apk.r1.market.hiapk.com/data/upload/apkres/2017/7_7/14/com.jingdong.app.mall_024308.apk?wsiphost=local",
                "http://images.cnblogs.com/cnblogs_com/oxgen/576959/o_c4.png",
                "http://118.123.97.62/apk.r1.market.hiapk.com/data/upload/apkres/2017/6_28/17/com.Qunar_053417.apk?wsiphost=local",
        };
        mAdapter = new DownloadAdapter();
        DownloadInfo info;
        ArrayList<DownloadInfo> list = new ArrayList<>(array.length);
        String name;
        for (String url : array) {
            name = Request.getNameFromUrl(url);
            info = new DownloadInfo(url);
            info.setPath(new File(FileUtil.getFileDir(this, "DownloadFile"), name)
                    .getAbsolutePath());
            list.add(info);
        }
        mAdapter.addAll(list);
        mLV_Download.setAdapter(mAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadTaskManager.getInstance().destroy();
    }

    @Override
    public void onStatusChange(long id, int status) {
        DownloadInfo target = mMap.get(Long.valueOf(id));
        if (target != null) {
            target.setStatus(status);
        }
    }

    @Override
    public void onProgress(long id, long currentBytes, long totalBytes) {
        DownloadInfo target = mMap.get(Long.valueOf(id));
        if (target != null) {
            target.setProgress(currentBytes, totalBytes);
        }
    }

    private class DownloadAdapter extends BaseAdapter {

        private ArrayList<DownloadInfo> mList = new ArrayList<>();

        public void add(DownloadInfo info) {
            mList.add(info);
            notifyDataSetChanged();
        }

        public void addAll(Collection<DownloadInfo> collection) {
            mList.addAll(collection);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_downloadlist, null);
                holder = new ViewHolder(convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.bindData(mList.get(position));
            return convertView;
        }

        public class ViewHolder implements View.OnClickListener {
            TextView mTV_Name;
            ProgressBar mProgressBar;
            Button mButton;
            Button mDelButton;
            DownloadInfo mData;

            public ViewHolder(View v) {
                v.setTag(this);
                mTV_Name = (TextView) v.findViewById(R.id.TV_Name);
                mProgressBar = (ProgressBar) v.findViewById(R.id.ProgressBar);
                mButton = (Button) v.findViewById(R.id.BTN_Action);
                mDelButton = (Button) v.findViewById(R.id.BTN_Del);
                mButton.setOnClickListener(this);
                mDelButton.setOnClickListener(this);
            }

            public void bindData(DownloadInfo data) {
                data.ViewHolder = this;
                mData = data;
                mTV_Name.setText(data.getName());
                setProgress(data.CurrentBytes, data.TotalBytes);
                setStatus(data.Status);
            }

            public ViewHolder setProgress(long currentBytes, long totalBytes) {
                mProgressBar.setProgress(totalBytes == 0 ? 0 : (int) (currentBytes * 100 / totalBytes));
                return this;
            }

            public ViewHolder setStatus(int status) {
                String action = "Download";
                switch (status) {
                    case Status.WAITING:
                        action = "Waiting";
                        break;
                    case Status.ERROR:
                        action = "Error";
                        break;
                    case Status.PAUSED:
                    case Status.PAUSED_MANUALLY:
                        action = "Paused";
                        break;
                    case Status.DOWNLOADING:
                        action = "...";
                        break;
                    case Status.FINISHED:
                        action = "Finished";
                        mMap.remove(Long.valueOf(mData.getId()));
                        break;
                }
                mButton.setText(action);
                return this;
            }

            @Override
            public void onClick(View v) {
                final DownloadInfo data = mData;
                if (data == null) {
                    return;
                }
                switch (v.getId()) {
                    case R.id.BTN_Action: {
                        switch (data.Status) {
                            case Status.WAITING:
                            case Status.DOWNLOADING:
                                DownloadTaskManager.getInstance().pause(data.getId());
                                break;
                            case Status.FINISHED:
                                break;
                            case Status.ERROR:
                            case Status.PAUSED:
                            case Status.PAUSED_MANUALLY:
                                if (DownloadTaskManager.getInstance().resume(data.getId())) {
                                    break;
                                }
                            default:
                                long id = DownloadTaskManager.getInstance().enqueue(data);
                                DLog.w(TAG, "Enqueue task :" + id);
                                if (id > 0) {
                                    data.setId(id);
                                    mMap.put(Long.valueOf(id), data);
                                } else {
                                    setStatus(Status.ERROR);
                                }
                                break;
                        }
                    }
                    break;
                    case R.id.BTN_Del:
                        DownloadTaskManager.getInstance().remove(data.getId(), false);
                        data.setStatus(Status.CANCELED);
                        break;
                }
            }
        }
    }

    private class DownloadInfo extends Request {
        public DownloadAdapter.ViewHolder ViewHolder;
        private long mID;
        public int Status;

        public long CurrentBytes;

        public long TotalBytes;

        public DownloadInfo(String url) {
            super(url);
        }

        public DownloadInfo setId(long id) {
            mID = id;
            return this;
        }

        public long getId() {
            return mID;
        }

        public DownloadInfo setStatus(int status) {
            Status = status;
            if (ViewHolder != null) {
                ViewHolder.setStatus(status);
            }
            return this;
        }

        public DownloadInfo setProgress(long currentBytes, long totalBytes) {
            CurrentBytes = currentBytes;
            TotalBytes = totalBytes;
            if (ViewHolder != null) {
                ViewHolder.setProgress(currentBytes, totalBytes);
            }
            return this;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.mID);
            dest.writeInt(this.Status);
            dest.writeLong(this.CurrentBytes);
            dest.writeLong(this.TotalBytes);
        }

        protected DownloadInfo(Parcel in) {
            super(in);
            this.ViewHolder = in.readParcelable(DownloadAdapter.ViewHolder.class.getClassLoader());
            this.mID = in.readLong();
            this.Status = in.readInt();
            this.CurrentBytes = in.readLong();
            this.TotalBytes = in.readLong();
        }


    }
}
