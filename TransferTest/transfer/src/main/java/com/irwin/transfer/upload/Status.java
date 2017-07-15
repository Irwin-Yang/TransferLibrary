package com.irwin.transfer.upload;

/**
 * Created by Irwin on 2016/2/22.
 */
public interface Status {

    int CANCELED = -10;

    int ERROR = -1;

    int WAITING = 1;

    int UPLOADING = 2;

    int FINISHED = 3;
}
