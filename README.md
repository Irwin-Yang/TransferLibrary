# TransferLibrary
An library for file downloading/uploading in a reliable and simple way on Android.

## Feature

1.Support multi-task downloading and parallel controlling.
</br>
2.Support breakpoint continue downloading.
</br>
3.Support subscribing status/progress changing events of downloading.
</br>
4.Multpart form uploading.
</br>
</br>
![](https://github.com/Zeal27/TransferLibrary/blob/master/Pics/ezgif-1-9f2a149d38.gif?raw=true)


## How to use
#### 1.Download or clone sample code. import submodule `transfer` into your project.

#### 2.Setup `DownloadTaskManager` and enqueue downloading request.

```Java
     DownloadTaskManager downloadTaskManager = DownloadTaskManager.setup(context).addCallback(context).setParallel(2).setDownloadRepeatedly(false);
        //Create and config your download request.
        Request request = new Request(url);
        //Enqueue task.
        long id = downloadTaskManager.getInstance().enqueue(request);
        downloadTaskManager.addCallback(new DownloadTaskManager.DownloadCallback() {
            @Override
            public void onStatusChange(long id, int status) {
                //Do something on downloading status changed.
            }

            @Override
            public void onProgress(long id, long currentBytes, long totalBytes) {
                //Update ui.
            }
        });
        //Pause downloading
        downloadTaskManager.pause(id);
        //Resume downloading
        downloadTaskManager.resume(id);
        //Resume all task waiting or paused.
        downloadTaskManager.resume();
        //Stop or delete a task.
        downloadTaskManager.remove(id);
		
 ```
 
 </br>
 #### 3.To upload, use code below:
 
 ```Java
      //Create and config your upload param.
        UploadParam param = new UploadParam(url);
        AsyncUploader uploader = new AsyncUploader().setParam(param);
        uploader.setListener(new Uploader.UploadListener() {
            @Override
            public void onStatusChanged(int status, Object msg) {
                //Do something on upload status changed.
            }

            @Override
            public void onProgress(long currentProgress, long totalProgress) {
                //Update ui.
            }
        });
        uploader.execute();
 ```
 
 #### Enjoy it and any advice will be appreciated:D
  
  </br>
  Email:zhpngyang52@gmail.com
