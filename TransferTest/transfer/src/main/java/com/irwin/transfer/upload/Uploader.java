package com.irwin.transfer.upload;

import android.text.TextUtils;


import com.irwin.transfer.IProgressListener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Irwin on 2016/3/15.
 */
public class Uploader implements Status, MultipartConsts {

    private static final String TAG = "Uploader";
    private static final long PROGRESS_RATE = 1500;
    private UploadListener mListener;
    private int mStatus = Status.WAITING;
    private ArrayList<UploadParam> mParamList = new ArrayList<>();


    public Uploader() {
    }

    public Uploader setParam(UploadParam... param) {
        for (UploadParam item : param) {
            mParamList.add(item);
        }
        return this;
    }

    public Uploader setParam(Collection<UploadParam> collection) {
        for (UploadParam item : collection) {
            mParamList.add(item);
        }
        return this;
    }

    public List<UploadParam> getParam() {
        return mParamList;
    }

    public Uploader setListener(UploadListener listener) {
        mListener = listener;
        return this;
    }

    public String upload() {
        String response = null;
        try {
            if (canRun()) {
                updateState(UPLOADING);
                response = doUpload();
                updateState(FINISHED, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateState(ERROR, e);
        }
        return response;
    }

    public String getBoundaryPrefixed() {
        return BOUNDARY_PREFIX + mParamList.get(0).getBoundary();
    }


    protected String doUpload() throws Exception {
        if (mParamList.size() == 0) {
            throw new NullPointerException("No UploadParam specified.");
        }
        final UploadParam param = mParamList.get(0);
        HttpURLConnection connection = openConnection(param.getUrl());
        setRequestHeaders(connection, param);
        return doUpload(connection, mParamList);
    }

    protected HttpURLConnection openConnection(String dstUrl) throws IOException {
        URL url = new URL(dstUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(30 * 1000);
        connection.setConnectTimeout(15 * 1000);
        connection.setRequestMethod("POST");
      /* 允许Input、Output，不使用Cache */
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        return connection;
    }

    protected void setRequestHeaders(HttpURLConnection connection, UploadParam param) {
        //        Header from chrome.
//        Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
//        Content-Type:multipart/form-data; boundary=----WebKitFormBoundaryFBGY291WtAZQATKp
//        Origin:http://localhost:8080
//        Referer:http://localhost:8080/FileServer/
//        Upgrade-Insecure-Requests:1
//        User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36

        connection.setRequestProperty(HEADER_ACCEPT, MIME_TYPE_ALL);
        connection.setRequestProperty(HEADER_CONTENT_TYPE, String.format(CONTENT_TYPE_MULTIPART, CHARSET_UTF8, param.getBoundary()));
//            connection.setRequestProperty(HEADER_CONNECTION, "Keep-Alive");
//            connection.setRequestProperty("Charset", CHARSET_UTF8);
        Map<String, String> headers = param.getHeaders();
        if (headers != null && headers.size() > 0) {
            Set<String> keySet = headers.keySet();
            for (String key : keySet) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
    }

    protected String doUpload(HttpURLConnection connection, List<UploadParam> paramList) throws Exception {
        //Multipart form, see:
//        http://www.cnblogs.com/top5/archive/2012/02/12/2347751.html
//        http://blog.csdn.net/MSPinyin/article/details/6141638

        Iterator<UploadParam> iterator = paramList.iterator();
        UploadParam param;
        HashMap<UploadParam, byte[]> formMap = new HashMap<>();
        long totalSize = 0;
        byte[] headerBuf;
        while (iterator.hasNext()) {
            param = iterator.next();
            String fileKey = TextUtils.isEmpty(param.getFileKey()) ? "file" : param.getFileKey();
            String fileName = param.getFileName();
            String fileType = TextUtils.isEmpty(param.getContentType()) ? MIME_TYPE_ALL : param.getContentType();

            //    Content-Disposition: form-data; name="fileKey"; filename="bg_entry.png"
            //            Content-Type: image/png
            StringBuilder builder = new StringBuilder(buildParams(param.getParams()));
            builder.append(getBoundaryPrefixed())
                    .append(CRLF)
                    .append(String.format(HEADER_CONTENT_DISPOSITION + COLON_SPACE + FORM_DATA + SEMICOLON_SPACE + FILENAME, fileKey, fileName))
                    .append(CRLF)
                    .append(HEADER_CONTENT_TYPE).append(fileType)
                    .append(CRLF)
                    //Must jump to new line to indicate the beginning of data.
                    .append(CRLF);
            headerBuf = textToBytes(builder.toString());
            totalSize += headerBuf.length;
            formMap.put(param, headerBuf);
            totalSize += new File(param.getPath()).length();
        }
        //Must jump to new line to indicate the end of data.
        byte[] tailBuf = textToBytes(CRLF + getBoundaryPrefixed() + BOUNDARY_PREFIX + CRLF);
        totalSize += tailBuf.length;
        //Generally speaking,Files larger than 4M should use streaming mode.
        if (totalSize > 4 * 1024 * 1024) {
            //Avoid oom when post large file.Ether way is ok.
            connection.setChunkedStreamingMode(1024);
//                connection.setFixedLengthStreamingMode(totalSize);
        }
        connection.setRequestProperty(HEADER_CONTENT_LENGTH, String.valueOf(totalSize));
        connection.connect();


        DataOutputStream outs = null;
        BufferedReader ins = null;
        FileInputStream fouts = null;
        String response = null;
        iterator = paramList.iterator();
        long startTime = System.currentTimeMillis();
        long currentBytes = 0;
        int length = -1;
        long now = 0;
        try {
            outs = new DataOutputStream(connection.getOutputStream());
            //Send single file.
            while (iterator.hasNext()) {
                param = iterator.next();
                File file = new File(param.getPath());
                headerBuf = formMap.get(param);
                outs.write(headerBuf);
                currentBytes += headerBuf.length;
                fouts = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                while ((length = fouts.read(buffer)) != -1) {
                    if (length > 0) {
                        outs.write(buffer, 0, length);
                        currentBytes += length;
                        now = System.currentTimeMillis();
                        if (now - startTime >= PROGRESS_RATE) {
                            updateProgress(currentBytes, totalSize);
                            startTime = now;
                        }
                    }
                    if (!canRun()) {
                        throw new Exception("Upload cancelled");
                    }
                }
                fouts.close();
                fouts = null;
            }
            outs.write(tailBuf);
            outs.flush();
            updateProgress(totalSize, totalSize);

            //Response.
            if (connection.getResponseCode() != 200) {
                throw new IllegalStateException(String.format("Error upload response: code:%s  msg:%s", connection.getResponseCode(), connection.getResponseMessage()));
            }
            ins = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuffer b = new StringBuffer();
            while ((line = ins.readLine()) != null) {
                b.append(line);
                if (!canRun()) {
                    throw new Exception("Upload cancelled");
                }
            }

            response = b.toString();
            if (TextUtils.isEmpty(response)) {
                throw new NullPointerException("Null response: " + response);
            }
            outs.close();
            outs = null;
            ins.close();
            ins = null;
        } finally {
            if (fouts != null) {
                fouts.close();
                fouts = null;
            }
            if (outs != null) {
                outs.close();
                outs = null;
            }
            if (ins != null) {
                ins.close();
                ins = null;
            }
        }
        return response;
    }

    public static byte[] textToBytes(String text) throws UnsupportedEncodingException {
        return text.getBytes(CHARSET_UTF8);
    }

    public String buildParams(List<MultipartParam> params) {
        if (params == null || params.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int size = params.size();
        MultipartParam target;
        for (int i = 0; i < size; i++) {
            target = params.get(i);
            builder.append(getBoundaryPrefixed())
                    .append(CRLF)
                    .append(String.format(HEADER_CONTENT_DISPOSITION + COLON_SPACE + FORM_DATA, target.Key))
                    .append(CRLF)
                    .append(HEADER_CONTENT_TYPE + COLON_SPACE + target.ContentType)
                    .append(CRLF)
                    .append(CRLF)
                    .append(target.Value)
                    .append(CRLF);
        }
        return builder.toString();
    }

    public int getStatus() {
        return mStatus;
    }

    public boolean isRunning() {
        return mStatus == UPLOADING;
    }

    public void cancel() {
        updateState(CANCELED);
    }

    protected boolean canRun() {
        return (mStatus != CANCELED);
    }

    protected void updateState(int state) {
        updateState(state, null);
    }

    protected void updateState(int status, Object msg) {
        if (mStatus == status) {
            return;
        }
        if (mStatus == CANCELED) {
            return;
        }
        if (mListener != null) {
            mListener.onStatusChanged(status, msg);
        }
    }

    protected void updateProgress(long currentBytes, long totalBytes) {
        if (mListener != null) {
            mListener.onProgress(currentBytes, totalBytes);
        }
    }

    public void reset() {
        if (isRunning()) {
            return;
        }
        mParamList = null;
        mListener = null;
        mStatus = WAITING;
    }

    public interface UploadListener extends IProgressListener, Status {

        public void onStatusChanged(int status, Object msg);
    }

}
