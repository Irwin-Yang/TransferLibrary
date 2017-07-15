package com.irwin.transfer;

/**
 * Created by Irwin on 2015/12/4.
 * Common progress listener for background job.
 */
public interface IProgressListener {

    /**
     * Called on progress changed.
     *
     * @param currentProgress
     * @param totalProgress
     */
    public void onProgress(long currentProgress, long totalProgress);

}
