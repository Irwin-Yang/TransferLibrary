package com.irwin.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.RequiresApi;

import com.irwin.transfer.IProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Irwin on 2015/11/10.
 */
public class FileUtil {
    private FileUtil() {
   /* cannot be instantiated */
    }

    /**
     * Get cache directory for app.
     *
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static File getCacheDir(Context context) {
        if (hasSDCard()) {
            return context.getExternalCacheDir();
        }
        return context.getCacheDir();
    }

    /**
     * Tell if sdcard is existing.
     *
     * @return
     */
    public static boolean hasSDCard() {
        return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTABLE));
    }

    /**
     * Get root directory of external storage(SDCard).
     *
     * @return Directory or null if external storage is not available.
     */
    public static File getExternalDir() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

    /**
     * Get an available dir to store file, that means we will create directories for you if needed.This method will consider external storage
     * in first priority,then the internal storage, and always return a path.
     *
     * @param context
     * @param file    directory name.
     * @return
     */
    public static File getFileDir(Context context, String file) {
        File f = getExternalDir();
        f = f == null ? FileUtil.getCacheDir(context) : f;
        f = new File(f + File.separator + file);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    /**
     * Get an available dir to store cache files, that means we will create directories for you if needed.This method will consider external cache directory
     * in first priority,then the internal storage, and always return a path.
     *
     * @param context
     * @param file    directory name.
     * @return
     */
    public static File getCacheDir(Context context, String file) {
        File f = getCacheDir(context);
        f = new File(f + File.separator + file);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    /**
     * @param file
     * @return Size in kb.
     */
    public static long getStorageSize(File file) {
        StatFs sf = new StatFs(file.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
//        long availCount = sf.getAvailableBlocks();
        return (blockSize * blockCount / 1024);
    }

    /**
     * Get file size in bytes.
     *
     * @param file File or directory
     * @return
     */
    public static long getFileSize(File file) {
        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFileSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
    }


    /**
     * Delete file or folder
     *
     * @param file
     * @return true if success.
     */
    public static boolean deleteFile(File file) {
        if (file.isFile()) {
            return file.delete();
        } else if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }

            for (int i = 0; i < childFiles.length; i++) {
                if (!deleteFile(childFiles[i])) {
                    return false;
                }
            }
            return file.delete();
        }
        return false;
    }

    public void copyFile(File src, File dest, IProgressListener listener) throws IOException {
        FileInputStream ins = null;
        FileOutputStream outs = null;
        try {
            ins = new FileInputStream(src);
            outs = new FileOutputStream(dest);
            byte[] buffer = new byte[20 * 1024];
            int length = 0;
            long startTime = System.currentTimeMillis();
            long now = 0;
            long total = src.length();
            long currentBytes = 0;
            while ((length = ins.read(buffer)) != -1) {
                outs.write(buffer, 0, length);
                currentBytes += length;
                if (listener != null) {
                    now = System.currentTimeMillis();
                    if (now - startTime >= 2000) {
                        startTime = now;
                        listener.onProgress(currentBytes, total);
                    }
                }
            }
            outs.close();
            outs = null;
            ins.close();
            ins = null;
        } finally {
            if (outs != null) {
                outs.close();
            }
            if (ins != null) {
                ins.close();
            }
        }
    }
}
