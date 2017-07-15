package com.irwin.transfer.download;


import android.text.TextUtils;


import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;

/**
 * Created by Irwin on 2016/2/22.
 */


public class Downloader implements Status {
    private static final int BUFFER_SIZE = 32 * 1024;

    private static long PROGRESS_RATE = 1500;

    private String mUrl = null;

    private String mPath = null;

    private long mStart = 0;

    private long mCurrentBytes = 0;

    private int mStatus = Status.WAITING;

    private long mSize = 0;

    private DownloadListener mListener;

    public static long getProgressRate() {
        return PROGRESS_RATE;
    }

    public static void setProgressRate(long progressRateInMS) {
        PROGRESS_RATE = progressRateInMS;
    }

    public Downloader setUrl(String url) {
        mUrl = url;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    public Downloader setPath(String path) {
        mPath = path;
        return this;
    }

    public String getPath() {
        return mPath;
    }

    public Downloader setRange(long start, long end) {
        if (start >= 0) {
            mStart = start;
        }
        mSize = end;
        return this;
    }

    public void cancel() {
        updateStatus(CANCELED);
    }

    public void pause(boolean fromUser) {
        updateStatus(fromUser ? PAUSED_MANUALLY : PAUSED);
    }

    private boolean canRun() {
        return (mStatus != CANCELED && mStatus != PAUSED_MANUALLY && mStatus != PAUSED);
    }

    public DownloadListener getListener() {
        return mListener;
    }

    public Downloader setListener(DownloadListener listener) {
        mListener = listener;
        return this;
    }

    public boolean isRunning() {
        return (mStatus >= DOWNLOADING && mStatus < FINISHED);
    }

    public int getStatus() {
        return mStatus;
    }

    public long getSize() {
        return mSize;
    }


    protected void updateStatus(int status) {
        if (mStatus == status) {
            return;
        }
        if (mStatus == PAUSED_MANUALLY || mStatus == PAUSED || mStatus == CANCELED) {
            return;
        }
        mStatus = status;
        if (mListener != null) {
            mListener.onStatusChange(this, status);
        }
    }

    public long getCurrentBytes() {
        return mCurrentBytes;
    }

    private void doDownload()
            throws Exception {
        String url = getUrl();
        String path = getPath();
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            throw new InvalidParameterException("Url or path is invalid");
        }
        long rangeStart = mStart;
        if (rangeStart > 0 && rangeStart >= mSize) {
            updateStatus(FINISHED);
            return;
        }
        updateStatus(DOWNLOADING);
        InputStream inStream = null;
        RandomAccessFile file = null;
        try {
            URL downUrl = new URL(url);
            HttpURLConnection http = (HttpURLConnection) downUrl
                    .openConnection();
            http.setConnectTimeout(10 * 1000);
            //Use Get method
            http.setRequestMethod("GET");
            //Accept all format of data.
            http.setRequestProperty("Accept", "*/*");
            http.setRequestProperty("Charset", "UTF-8");

            //Data block to download.
            http.setRequestProperty("Range", "bytes=" + rangeStart + "-");
            http.setRequestProperty("User-Agent", "Client");
            http.setRequestProperty("Connection", "Keep-Alive");
            http.connect();
            if (http.getResponseCode() != 200
                    && http.getResponseCode() != 206) {
                throw new IllegalAccessException("Invalid request: "
                        + http.getResponseCode());
            }
            long total = getContentLen(http);
            if (total <= 0) {
                throw new IllegalAccessException("Invalid content-length: "
                        + total);
            }
            if (mSize <= 0) {
                mSize = total;
            } else if (total > mSize) {
                throw new IllegalAccessException(String.format("Expected max size %d but get %d", mSize, total));
            }
            inStream = http.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            File tmp = new File(path + ".tmp");
            file = new RandomAccessFile(tmp, "rwd");
            file.setLength(mSize);
            file.seek(rangeStart);
            mCurrentBytes = rangeStart;
            int size = 0;
            long startTime = System.currentTimeMillis();
            long now = 0;
            while ((size = inStream.read(buffer)) != -1) {
                if (size > 0) {
                    file.write(buffer, 0, size);
                    mCurrentBytes += size;
                    now = System.currentTimeMillis();
                    if (now - startTime >= PROGRESS_RATE) {
                        updateProgress(mCurrentBytes, mSize);
                        startTime = now;
                    }
                }
                if (!canRun()) {
                    throw new Exception("Download task cancelled or paused");
                }
            }


            file.close();
            file = null;
            inStream.close();
            inStream = null;
            if (mSize == mCurrentBytes) {
                updateProgress(mCurrentBytes, mSize);
                // Rename
                File f = new File(path);
                tmp.renameTo(f);
            } else {
                throw new IllegalStateException("Download not finihsed.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e2) {
                    // TODO: handle exception
                    e2.printStackTrace();
                }
            }
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e2) {
                    // TODO: handle exception
                    e2.printStackTrace();
                }
            }
        }
    }

    public void download() {
        try {
            if (canRun()) {
                doDownload();
                updateStatus(FINISHED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(ERROR);
        }
    }

    private long getContentLen(HttpURLConnection conn) throws Exception {
        long len = conn.getContentLength();
        if (len <= 0) {
            try {
                len = Long.parseLong(conn.getHeaderField("Content-Length"));
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
        if (len <= 0) {
            //Try to calculate size from 'Range' field.
            String range = conn.getHeaderField("Range");
            if (range != null) {
                String[] array = range.replace("bytes=", "").split("-");
                if (array.length == 2) {
                    len = Long.parseLong(array[1]) - Long.parseLong(array[0]);
                }
            }
        }
        return len;
    }

    protected void updateProgress(long currentBytes, long totalBytes) {
        if (mListener != null) {
            mListener.onProgress(currentBytes, totalBytes);
        }
    }

}
