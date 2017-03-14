package com.evistek.vr.net;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.evistek.vr.net.callback.DownloadCallback;
import com.evistek.vr.net.json.JsonReqDownload;
import com.evistek.vr.activity.E3DApplication;
import com.google.gson.Gson;

import android.util.Log;

public class DownloadManager {
//    private static final String TAG = "DownloadManager";
//    private static final String TEMP_FILE = "e3dgallerydownload.tmp";
//    private static final int UNVALID_ID = 0;
//    private HttpHandler mHttpHandler;
//    private String mDownloadDir;
//    private Gson mGson;
//    private static DownloadManager mInstance;
//
//    private DownloadManager() {
//        mHttpHandler = null;
//        mDownloadDir = E3DApplication.getInstance().getExternalCacheDir().getAbsolutePath() + "/images/";
//        mGson = new Gson();
//    }
//
//    public synchronized static DownloadManager getInstance() {
//        if (mInstance == null) {
//            mInstance = new DownloadManager();
//        }
//
//        return mInstance;
//    }
//
//    public int download(String url) {
//        download(url, null);
//        return 0;
//    }
//
//    public int download(String url, DownloadCallback callBack) {
//        String fileName = url.substring(url.lastIndexOf("/") + 1);
//        download(url, mDownloadDir + fileName, true, false, callBack);
//        return 0;
//    }
//
//    public int download(String url, String target, DownloadCallback callback) {
//        download(url, target, true, false, callback);
//        return 0;
//    }
//
//    public int download(final String url, String target, boolean autoResume, boolean autoRename,
//            final DownloadCallback callBack) {
//        final String actualFileName = target;
//
//        File file = new File(actualFileName);
//        if (file.exists()) {
//            callBack.onResult(200, actualFileName);
//        } else {
//            HttpUtils httpUtils = new HttpUtils();
//            mHttpHandler = httpUtils.download(url, mDownloadDir + TEMP_FILE, autoRename, autoRename,
//                new RequestCallBack<File>() {
//                    @Override
//                    public void onLoading(long total, long current, boolean isUploading) {
//                        callBack.onProgress((int) (current * 100 / total));
//                    }
//
//                    @Override
//                    public void onSuccess(ResponseInfo<File> arg0) {
//                        File targetFile = arg0.result;
//                        targetFile.renameTo(new File(actualFileName));
//                        callBack.onResult(200, actualFileName);
//
//                        updateDownloadCount(UNVALID_ID, url);
//                    }
//
//                    @Override
//                    public void onFailure(HttpException arg0, String arg1) {
//                        callBack.onResult(arg0.getExceptionCode(), arg1);
//                    }
//                });
//        }
//        return 0;
//    }
//
//    public int pause() {
//        if (mHttpHandler != null && mHttpHandler.supportPause() && !mHttpHandler.isPaused()) {
//            mHttpHandler.pause();
//            return 0;
//        }
//
//        return -1;
//    }
//
//    public int cancel() {
//        if (mHttpHandler != null && mHttpHandler.supportCancel() && !mHttpHandler.isCancelled()) {
//            mHttpHandler.cancel();
//            mHttpHandler = null;
//            return 0;
//        }
//
//        return -1;
//    }
//
//    public String getDownloadDir() {
//        return mDownloadDir;
//    }
//
//    private void updateDownloadCount(final int contentId, final String url) {
//        JsonReqDownload jsonReqDownload = new JsonReqDownload();
//        jsonReqDownload.setName("download");
//        jsonReqDownload.setContentId(contentId);
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
//                    Log.e(TAG, "Failed to update download count, URL: " + url
//                            + " errorCode: " + arg0.getExceptionCode()
//                            + " errorInfo: " + arg1);
//                }
//
//                @Override
//                public void onSuccess(ResponseInfo<String> arg0) {
//                    // TODO Auto-generated method stub
//                }
//
//            });
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}
