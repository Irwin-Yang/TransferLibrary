package com.irwin.transfer.download;

/**
 * Created by Irwin on 2016/2/22.
 */
public interface DownloadListener extends Status{
    /**
     * Called on downloading status changed.
     * @param downloader
     * @param status
     */
    public void onStatusChange(Downloader downloader, int status);

    /**
     * Called back on downloading progress changed.
     * @param currentBytes
     * @param totalBytes
     */
    public void onProgress(long currentBytes, long totalBytes);
}
