package com.irwin.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Irwin on 2015/11/11.
 * Class provides monitoring of network connectivity state which will work rely on {@link NetworkReceiver}.You can use it in two ways:
 * <p/>1)Register receiver {@link NetworkReceiver} in manifest with action "android.net.conn.CONNECTIVITY_CHANGE"
 * <p/>2)Register receiver by call register() on application started, and unregister() on application terminated.
 */
public class NetworkMonitor {

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_BLUETOOTH = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_WIFI = 3;
    public static final int TYPE_ETHERNET = 4;

    private static ArrayList<NetworkListener> mListeners = new ArrayList<NetworkListener>();

    private static int mType = TYPE_UNKNOWN;

    private static NetworkReceiver mReceiver;

    /**
     * Register to system.
     *
     * @param context
     */
    public static synchronized void register(Context context) {
        if (mReceiver != null) {
            throw new IllegalStateException("Network receiver leaked.");
        }
        mReceiver = new NetworkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mReceiver, intentFilter);
    }

    /**
     * Unregister from system.
     *
     * @param context
     */
    public static synchronized void unregister(Context context) {
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    /**
     * Check network
     *
     * @param context
     * @return
     */
    public static int checkNetwork(Context context) {
        mType = getRealTypeInner(context, false);
        return mType;
    }

    /**
     * Tell if device is connected to network.
     *
     * @return true if connected.
     */
    public static boolean isConnected() {
        return mType > TYPE_NONE;
    }

    /**
     * Tell if bluetooth connected.
     *
     * @return
     */
    public static boolean isBluetoothConnected() {
        return mType == TYPE_BLUETOOTH;
    }

    public static void notifyChanged(int oldType, int newType) {
        synchronized (mListeners) {
            Iterator<NetworkListener> iterator = mListeners.iterator();
            NetworkListener l = null;
            while (iterator.hasNext()) {
                l = iterator.next();
                l.onNetworkChange(oldType, newType);
            }

        }
    }

    /**
     * Add network listener. {@link NetworkListener}
     *
     * @param l
     */
    public static void addListener(NetworkListener l) {
        mListeners.add(l);
    }

    public static void removeListener(NetworkListener l) {
        synchronized (mListeners) {
            Iterator<NetworkListener> iterator = mListeners.iterator();
            NetworkListener temp = null;
            while (iterator.hasNext()) {
                temp = iterator.next();
                if (temp == l) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * Get network type.
     *
     * @return
     */
    public static int getNetworkType() {
        return mType;
    }


    /**
     * Get network type in real time.
     *
     * @param context
     * @return
     * @see #TYPE_NONE ,#TYPE_UNKNOWN,#TYPE_WIFI etc.
     */
    protected static int getRealType(Context context) {
        return getRealTypeInner(context, true);
    }

    /**
     * Get network type in real time.
     *
     * @param context
     * @param update  Whether update internal type.
     * @return
     * @see #TYPE_NONE ,#TYPE_UNKNOWN,#TYPE_WIFI etc.
     */
    protected static int getRealTypeInner(Context context, boolean update) {
        int netType = TYPE_NONE;
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return netType;
        }
        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                netType = TYPE_WIFI;
                break;
            case ConnectivityManager.TYPE_MOBILE:
                netType = TYPE_MOBILE;
                break;
            case ConnectivityManager.TYPE_ETHERNET:
                netType = TYPE_ETHERNET;
                break;
            case ConnectivityManager.TYPE_BLUETOOTH:
                netType = TYPE_BLUETOOTH;
                break;
        }
        if (update) {
            mType = netType;
        }
        return netType;
    }


    /**
     * Tell if Wifi connection.
     */
    public static boolean isWifi() {
        return mType == TYPE_WIFI;
    }

    /**
     * Tell if Mobile connection.
     *
     * @return
     */
    public static boolean isMobile() {
        return mType == TYPE_MOBILE;
    }

    /**
     * 得到当前网速
     *
     * @param context 上下文
     * @return
     */
    public static long getTotalRxBytes(Context context) {
        return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    public interface NetworkListener {
        /**
         * @param oldType
         * @param newType
         */
        void onNetworkChange(int oldType, int newType);
    }

    /**
     * android.net.conn.CONNECTIVITY_CHANGE is required to register receiver.
     */
    public static class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = getRealTypeInner(context, false);
            final int oldType = mType;
            if (type != oldType) {
                mType = type;
                notifyChanged(oldType, type);
            }
        }
    }

}
