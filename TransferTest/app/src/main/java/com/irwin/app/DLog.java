package com.irwin.app;

import android.util.Log;

/**
 * Created by Irwin on 2015/12/16.
 * //TODO Add log level .
 */
public class DLog {

    private static LogHandler mLogHandler;

    private static int mLevel = Log.VERBOSE;

    private DLog() {
    }

    public static void setLevel(int level) {
        mLevel = level;
    }


    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        return handleLog(Log.VERBOSE, tag, msg, null);
    }

    /**
     * Send a {@link Log#VERBOSE} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        return handleLog(Log.VERBOSE, tag, msg, tr);
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        return handleLog(Log.DEBUG, tag, msg, null);
    }

    /**
     * Send a {@link Log#DEBUG} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        return handleLog(Log.DEBUG, tag, msg, tr);
    }

    /**
     * Send an {@link Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        return handleLog(Log.INFO, tag, msg, null);
    }

    /**
     * Send a {@link Log#INFO} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        return handleLog(Log.INFO, tag, msg, tr);
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        return handleLog(Log.WARN, tag, msg, null);
    }

    /**
     * Send a {@link Log#WARN} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        return handleLog(Log.WARN, tag, msg, tr);
    }

    /*
     * Send a {@link Log#WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
        return handleLog(Log.WARN, tag, null, tr);
    }

    /**
     * Send an {@link Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        return handleLog(Log.ERROR, tag, msg, null);
    }

    /**
     * Send a {@link Log#ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        return handleLog(Log.ERROR, tag, msg, tr);
    }

    public static int handleLog(int level, String tag, String message, Throwable throwable) {
        if (level < mLevel) {
            return -1;
        }
        final LogHandler handler = mLogHandler == null ? DEFAULTHANDLER : mLogHandler;
        return handler.handle(level, tag, message, throwable);
    }

    public interface LogHandler {
        public int handle(int level, String tag, String message, Throwable throwable);
    }

    private static final LogHandler DEFAULTHANDLER = new LogHandler() {
        @Override
        public int handle(int level, String tag, String message, Throwable throwable) {
            switch (level) {
                case Log.VERBOSE:
                    Log.v(tag, message, throwable);
                    break;
                case Log.DEBUG:
                    Log.d(tag, message, throwable);
                    break;
                case Log.INFO:
                    Log.i(tag, message, throwable);
                    break;
                case Log.WARN:
                    Log.w(tag, message, throwable);
                    break;
                case Log.ERROR:
                    Log.e(tag, message, throwable);
                    break;
                case Log.ASSERT:

                    break;
                default:
                    Log.w(tag, "Unknown log level: " + level + "  message: " + message);
                    break;
            }
            return 0;
        }
    };
}
