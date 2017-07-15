package com.irwin.utils;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;


import com.irwin.app.DLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Irwin on 2015/12/23.
 */
public class DebugUtil {
    private static final String DEFAULT_TAG = "Info";
    static final String NEW_LINE = "\r\n";

    public static void showIntent(String tag, String prefix, Intent intent) {
        if (intent == null) {
            print(tag, prefix + NEW_LINE + " but null intent getted.");
            return;
        }
        StringBuilder builder = new StringBuilder(prefix == null ? "" : prefix).append(NEW_LINE);
        builder.append("Action:").append(intent.getAction()).append(NEW_LINE);
        Bundle bundle = intent.getExtras();
        Set<String> set = bundle.keySet();
        if (set != null && set.size() > 0) {
            Iterator<String> iterator = set.iterator();
            String key;
            while (iterator.hasNext()) {
                key = iterator.next();
                builder.append(key).append("=").append(bundle.get(key)).append(NEW_LINE);
            }
        }
        print(tag, builder.toString());
    }

    public static void showBundle(String tag, String prefix, Bundle bundle) {
        if (bundle == null) {
            print(tag, prefix + NEW_LINE + " but null bundle.");
            return;
        }
        StringBuilder builder = new StringBuilder(prefix == null ? "" : prefix).append(NEW_LINE);
        Set<String> set = bundle.keySet();
        if (set != null && set.size() > 0) {
            Iterator<String> iterator = set.iterator();
            String key;
            while (iterator.hasNext()) {
                key = iterator.next();
                builder.append(key).append("=").append(bundle.get(key)).append(NEW_LINE);
            }
        }
        print(tag, builder.toString());
    }

    public static void showCursor(String tag, String prefix, Cursor cursor) {


    }

    public static void showString(String tag, String prefix, String source) {
        int threshold = 1000;
        int times = source.length() / threshold;
        int rest = source.length() % threshold;
        int offset = 0;
        int end = 0;
        String msg = null;
        if (prefix != null) {
            DLog.i(tag, prefix);
        }
        for (int i = 0; i < times; i++) {
            end = offset + threshold;
            msg = source.substring(offset, end);
            offset = end;
            DLog.i(tag, msg);
        }
        if (rest > 0) {
            DLog.i(tag, source.substring(offset));
        }
    }

    public static void writeFile(String source, File dest,boolean append) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(dest,append);
            os.write(source.getBytes());
            os.flush();
            os.close();
            os = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(os!=null)
            {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    static void print(String tag, String msg) {
        DLog.i(tag == null ? DEFAULT_TAG : tag, msg);
    }


}
