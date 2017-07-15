package com.irwin.transfer.download;

/**
 * Created by Irwin on 2016/2/22.
 */
public interface Status {

    int CANCELED = -10;

    int ERROR = -1;

    int WAITING = 1;

    /**
     * Paused by system automatically
     */
     int PAUSED = 2;

    /**
     * Paused by user manually
     */
    int PAUSED_MANUALLY = 3;

    int DOWNLOADING = 4;

    int FINISHED = 5;
}
