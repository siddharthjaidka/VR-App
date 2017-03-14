package com.evistek.vr.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.evistek.vr.R;
import com.evistek.vr.activity.E3DApplication;
import com.evistek.vr.net.Config;
import com.evistek.vr.utils.Utils;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private static final String mTaskFile = Config.DISK_CACHE_PATH + "DownloadTask";

    private static final int MSG_NEXT = 1;
    private static final int MSG_PAUSE = 2;
    private static final int MSG_CANCEL = 3;
    private static final int MSG_COMPLETE = 4;

    private static final boolean AUTO_RESUME = true;
    private static final boolean AUTO_RENAME = false;

    public static final int STATUS_DOWNLOADING = 0;
    public static final int STATUS_COMPLETED = 1;

    private Context mContext;
    private DownloadServiceBinder mBinder = new DownloadServiceBinder();
    //private HttpUtils mHttpUtils;
    //private HttpHandler<File> mHttpHandler;
    private Callback.Cancelable mCancelable;
    private String mDownloadDir;
    private String mTempFile;
    private List<Task> mTasks;
    private int mIndex;
    private int mStatus = STATUS_COMPLETED;
    private Gson mGson;
    private long mLastTime;
    private long mLastBytes;

    private Set<OnStartListener> mOnStartListeners = new HashSet<OnStartListener>();
    private Set<OnProgressListener> mOnProgressListeners = new HashSet<OnProgressListener>();
    private Set<OnSuccessListener> mOnSuccessListeners = new HashSet<OnSuccessListener>();
    private Set<OnFailureListener> mOnFailureListeners = new HashSet<OnFailureListener>();
    private Set<OnCompleteListener> mOnCompleteListeners = new HashSet<OnCompleteListener>();

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private String mNotificationTitle;
    private PendingIntent mNotificationIntent;
    private String mNotificationContent;
    private int mNotifyID = 1;

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEXT:
                    if (mIndex < mTasks.size()) {
                        String url = mTasks.get(mIndex).getUrl();
                        download(url);
                    } else {
                        sendMessage(MSG_COMPLETE);
                    }
                    break;
                case MSG_PAUSE:
                    // TODO
                    break;
                case MSG_CANCEL:
                    // TODO
                    break;
                case MSG_COMPLETE:
                    if (mStatus != STATUS_COMPLETED) {
                        mStatus = STATUS_COMPLETED;
                        notifyComplete();
                        if (getTotalNumber() == 0) {
                            removeDownloadNotify();
                        }
                        //Toast.makeText(mContext, R.string.download_finish, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
       // mHttpUtils = new HttpUtils();
        //mHttpHandler = null;
        mDownloadDir = E3DApplication.getInstance().getExternalCacheDir().getAbsolutePath() + "/app/";
        mGson = new Gson();

        mIndex = -1;
        mTasks = readTask();
        for (int i = 0; i < mTasks.size(); i++) {
            if (mTasks.get(i).getStatus() == Task.STATUS_PAUSE) {
                mIndex = i;
                break;
            }
        }

        if (mIndex == -1) {
            mIndex = mTasks.size();
        }

        //initNotification();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        mNotificationManager.cancel(mNotifyID);
        saveTask();
        super.onDestroy();
    }

    public void addTask(String url) {
        Task task = new Task();
        task.setUrl(url);
        task.setCoverUrl(url);
        task.setName(url.substring(url.lastIndexOf("/") + 1));
        task.setStatus(Task.STATUS_INIT);
        task.setProgress(0);

        addTask(task);
    }

    public void addTask(Task task) {
        for (Task t : mTasks) {
            if (t.getUrl().equals(task.getUrl())) {
                Log.e(TAG, "addTask fail, already exists.");
                return;
            }
        }

        mTasks.add(task);
        if (mStatus == STATUS_COMPLETED) {
            mStatus = STATUS_DOWNLOADING;

            sendMessage(MSG_NEXT);
            notifyStart();
        }

        //Toast.makeText(mContext, R.string.download_add_task, Toast.LENGTH_SHORT).show();
        //notifyTaskInfo();
    }

    public List<Task> getTasks() {
        return mTasks;
    }

    public int getCurrentTask() {
        return mIndex;
    }

    public int getTaskStatus(int position) {
        int status = mTasks.get(position).getStatus();
        return status;
    }

    public void resumeTask() {
        int position = getCurrentTask();
        Task task = mTasks.get(position);
        task.setStatus(Task.STATUS_START);
        mStatus = STATUS_DOWNLOADING;
        notifyStart();
        //notifyTaskInfo();
        sendMessage(MSG_NEXT);
    }

    public void cancelCurrentTask() {
        int position = getCurrentTask();
        if (mTasks.size() != 0 && mTasks.size() != position) {
            Task task = mTasks.get(position);
            task.setStatus(Task.STATUS_PAUSE);
//            if (mHttpHandler != null) {
//                mHttpHandler.cancel();
//            }

            if (mCancelable != null) {
                mCancelable.cancel();
            }
        }
    }

    public void removeTask(int position) {
        Task task = mTasks.get(position);
        mTasks.remove(task);

        if (position < getCurrentTask()) {
//            if (mHttpHandler != null) {
//                mHttpHandler.cancel();
//            }

            if (mCancelable != null) {
                mCancelable.cancel();
            }

            --mIndex;
            sendMessage(MSG_NEXT);
            notifyStart();
        } else if (position == getCurrentTask()) {
//            if (mHttpHandler != null) {
//                mHttpHandler.cancel();
//            }

            if (mCancelable != null) {
                mCancelable.cancel();
            }

            if(mTempFile != null) {
                File file = new File(mTempFile);
                if (file.exists()) {
                    file.delete();
                }
            }

            notifyStart();
            sendMessage(MSG_NEXT);
        } else {
            // position > getCurrentTask()
            // do nothing
        }

        if (mTasks.size() != 0) {
            //notifyTaskInfo();
        } else {
            removeDownloadNotify();
        }
    }

    public void cancelTask(int position) {
        if (position > mIndex) {
            Task task = mTasks.get(position);
            task.setStatus(Task.STATUS_CANCEL);
        }
    }

    public void cancelAll() {
//        if (mHttpHandler != null) {
//            mHttpHandler.cancel();
//        }

        if (mCancelable != null) {
            mCancelable.cancel();
        }

        if (mTempFile != null) {
            File file = new File(mTempFile);
            if (file.exists()) {
                file.delete();
            }
        }
        mTasks.clear();
        mIndex = 0;
        sendMessage(MSG_COMPLETE);
    }

    public void setOnStartListener(OnStartListener listener) {
        mOnStartListeners.add(listener);
    }

    public void removeOnStartListener(OnStartListener listener) {
        if (mOnStartListeners.contains(listener)) {
            mOnStartListeners.remove(listener);
        }
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mOnProgressListeners.add(listener);
    }

    public void removeOnProgressListener(OnProgressListener listener) {
        if (mOnProgressListeners.contains(listener)) {
            mOnProgressListeners.remove(listener);
        }
    }

    public void setOnSuccessListener(OnSuccessListener listener) {
        mOnSuccessListeners.add(listener);
    }

    public void removeOnSuccessListener(OnSuccessListener listener) {
        if (mOnSuccessListeners.contains(listener)) {
            mOnSuccessListeners.remove(listener);
        }
    }

    public void setOnFailureListener(OnFailureListener listener) {
        mOnFailureListeners.add(listener);
    }

    public void removeOnFailureListener(OnFailureListener listener) {
        if (mOnFailureListeners.contains(listener)) {
            mOnFailureListeners.remove(listener);
        }
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        mOnCompleteListeners.add(listener);
    }

    public void removeOnCompleteListener(OnCompleteListener listener) {
        if (mOnCompleteListeners.contains(listener)) {
            mOnCompleteListeners.remove(listener);
        }
    }

    public class DownloadServiceBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    public interface OnStartListener {
        void onStart();
    }

    public interface OnProgressListener {
        void onProgress(int index, String url, int progress);
    }

    public interface OnSuccessListener {
        void onSuccess(String url, String filePath);
    }

    public interface OnFailureListener {
        void onFailure(int code, String msg);
    }

    public interface OnCompleteListener {
        void onComplete();
    }

    private void sendMessage(int what) {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = what;
            mHandler.sendMessage(message);
        }
    }

    private int download(final String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        final String savedFilePath = mDownloadDir + fileName;
        mTempFile = mDownloadDir + fileName + ".temp";

        File file = new File(savedFilePath);
        if (file.exists()) {
            int progress = 100;

            notifyProgress(mIndex, url, progress);
            mTasks.get(mIndex).setProgress(progress);
            notifySuccess(url, savedFilePath);
            mTasks.get(mIndex).setStatus(Task.STATUS_COMPLETE);

            ++mIndex;
            sendMessage(MSG_NEXT);

        } else {
            //RequestParams params = new RequestParams();
            //params.setPriority(Priority.BG_LOW);
            mLastBytes = 0;
            mLastTime = 0;

            mCancelable = download(url, mTempFile, new Callback.ProgressCallback<File>() {
                @Override
                public void onWaiting() {

                }

                @Override
                public void onStarted() {

                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {
                    int progress = (int) (current * 100 / total);
                    notifyProgress(mIndex, url, progress);
                    mTasks.get(mIndex).setProgress(progress);
                    mTasks.get(mIndex).setStatus(Task.STATUS_START);

                    long now = System.currentTimeMillis();
                    if (mLastTime != 0 && (now - mLastTime) != 0) {
                        int v = (int) ((current - mLastBytes) / (now - mLastTime)); // KB/s
                        mTasks.get(mIndex).setRate(v);
                    }

                    mLastTime = now;
                    mLastBytes = current;
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    //notifyFailure(arg0.getExceptionCode(), arg1);
                }

                @Override
                public void onFinished() {

                }

                @Override
                public void onSuccess(File result) {
                    File targetFile = result;
                    targetFile.renameTo(new File(savedFilePath));

                    mTasks.get(mIndex).setStatus(Task.STATUS_COMPLETE);
                    mTasks.get(mIndex).setProgress(100);

                    mTasks.remove(mIndex);
                    mIndex--;

                    notifySuccess(url, savedFilePath);

                    ++mIndex;
                    sendMessage(MSG_NEXT);

                    //notifyTaskInfo();
                    updateDownloadCount(url);
                }
            });

//            mHttpHandler = mHttpUtils.download(url, mTempFile, params, AUTO_RESUME, AUTO_RENAME,
//                    new RequestCallBack<File>() {
//
//                        @Override
//                        public void onLoading(long total, long current, boolean isUploading) {
//                            int progress = (int) (current * 100 / total);
//                            notifyProgress(mIndex, url, progress);
//                            mTasks.get(mIndex).setProgress(progress);
//                            mTasks.get(mIndex).setStatus(Task.STATUS_START);
//
//                            long now = System.currentTimeMillis();
//                            if (mLastTime != 0 && (now - mLastTime) != 0) {
//                                int v = (int) ((current - mLastBytes) / (now - mLastTime)); // KB/s
//                                mTasks.get(mIndex).setRate(v);
//                            }
//
//                            mLastTime = now;
//                            mLastBytes = current;
//
//                        }
//
//                        @Override
//                        public void onSuccess(ResponseInfo<File> arg0) {
//                            File targetFile = arg0.result;
//                            targetFile.renameTo(new File(savedFilePath));
//
//                            mTasks.get(mIndex).setStatus(Task.STATUS_COMPLETE);
//                            mTasks.get(mIndex).setProgress(100);
//
//                            mTasks.remove(mIndex);
//                            mIndex--;
//
//                            notifySuccess(url, savedFilePath);
//
//                            ++mIndex;
//                            sendMessage(MSG_NEXT);
//
//                            //notifyTaskInfo();
//                            updateDownloadCount(url);
//                        }
//
//                        @Override
//                        public void onFailure(HttpException arg0, String arg1) {
//                            notifyFailure(arg0.getExceptionCode(), arg1);
//                        }
//                    });
        }
        return 0;
    }

    private void updateDownloadCount(final String url) {
//        JsonReqDownload jsonReqDownload = new JsonReqDownload();
//        jsonReqDownload.setName("download");
//        jsonReqDownload.setContentId(0);
//        jsonReqDownload.setUrl(url);
//
//        RequestParams params = new RequestParams("UTF-8");
//        params.setHeader("Content-Type", "application/json");
//        try {
//            params.setBodyEntity(new StringEntity(mGson.toJson(jsonReqDownload), "UTF-8"));
//            new HttpUtils().send(HttpMethod.POST, Config.URL_DOWNLOAD, params, new RequestCallBack<String>() {
//
//                @Override
//                public void onFailure(HttpException arg0, String arg1) {
//                    Log.e(TAG, "Failed to update download count, URL: " + url + " errorCode: " + arg0.getExceptionCode()
//                            + " errorInfo: " + arg1);
//                }
//
//                @Override
//                public void onSuccess(ResponseInfo<String> arg0) {
//                }
//
//            });
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    public void saveTask() {
        Utils.writeObjectToFile(mTasks, mTaskFile);
    }

    public int getStatus() {
        return mStatus;
    }

    public String getDownloadDir() {
        return mDownloadDir;
    }

    private List<Task> readTask() {
        List<Task> tasks = (ArrayList<Task>) Utils.readObjectFromFile(mTaskFile);
        if (tasks != null) {
            return tasks;
        }

        return new ArrayList<Task>();
    }

    private void notifyTaskInfo() {
        mNotificationContent = String.format(getResources().getString(R.string.download_notification),
                getCompletedTaskNumber(), getTotalNumber());

        mNotificationBuilder.setContentText(mNotificationContent);
        mNotificationManager.notify(mNotifyID, mNotificationBuilder.build());
    }

    public void removeDownloadNotify() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(mNotifyID);
        }
    }

    private int getCompletedTaskNumber() {
        int i = 0;
        for (Task t : mTasks) {
            if (t.getStatus() == Task.STATUS_COMPLETE) {
                ++i;
            }
        }

        return i;
    }

    private int getTotalNumber() {
        int i = 0;
        for (Task t : mTasks) {
            if (t.getStatus() != Task.STATUS_CANCEL) {
                ++i;
            }
        }

        return i;
    }

    private void notifyStart() {
        for (OnStartListener l : mOnStartListeners) {
            if (l != null) {
                l.onStart();
            }
        }
    }

    private void notifyProgress(int index, String url, int progress) {
        for (OnProgressListener l : mOnProgressListeners) {
            if (l != null) {
                l.onProgress(index, url, progress);
            }
        }
    }

    private void notifySuccess(String url, String filePath) {
        for (OnSuccessListener l : mOnSuccessListeners) {
            if (l != null) {
                l.onSuccess(url, filePath);
            }
        }
    }

    private void notifyFailure(int code, String msg) {
        for (OnFailureListener l : mOnFailureListeners) {
            if (l != null) {
                l.onFailure(code, msg);
            }
        }
    }

    private void notifyComplete() {
        for (OnCompleteListener l : mOnCompleteListeners) {
            if (l != null) {
                l.onComplete();
            }
        }
    }

    private <T>Callback.Cancelable download(String uri, String savePath, Callback.CommonCallback<T> callback) {
        RequestParams requestParams = new RequestParams(uri);
        requestParams.setAutoRename(AUTO_RENAME);
        requestParams.setAutoResume(AUTO_RESUME);
        requestParams.setSaveFilePath(savePath);

        Callback.Cancelable cancelable = x.http().get(requestParams, callback);
        return cancelable;
    }
}
