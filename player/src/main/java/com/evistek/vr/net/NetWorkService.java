package com.evistek.vr.net;

import android.util.Log;

import com.evistek.vr.activity.E3DApplication;
import com.evistek.vr.model.Favorite;
import com.evistek.vr.model.LoginInfo;
import com.evistek.vr.model.PlayRecord;
import com.evistek.vr.model.UserInfo;
import com.evistek.vr.model.Video;
import com.evistek.vr.net.callback.ApplicationCallback;
import com.evistek.vr.net.callback.CategoryCallback;
import com.evistek.vr.net.callback.ContentCallBack;
import com.evistek.vr.net.callback.ContentCommentCallback;
import com.evistek.vr.net.callback.DeviceCallback;
import com.evistek.vr.net.callback.FavoriteCallback;
import com.evistek.vr.net.callback.ImageCallback;
import com.evistek.vr.net.callback.LoginCallback;
import com.evistek.vr.net.callback.PlayRecordCallback;
import com.evistek.vr.net.callback.RegisterCallback;
import com.evistek.vr.net.callback.SendCommentCallback;
import com.evistek.vr.net.callback.UserCallBack;
import com.evistek.vr.net.callback.UserCommentCallback;
import com.evistek.vr.net.callback.UserNameCallBack;
import com.evistek.vr.net.callback.UserNameListCallBack;
import com.evistek.vr.net.callback.VideoCallback;
import com.evistek.vr.net.callback.VrOnlineCallback;
import com.evistek.vr.net.json.JsonReqApplication;
import com.evistek.vr.net.json.JsonReqChallenge;
import com.evistek.vr.net.json.JsonReqContent;
import com.evistek.vr.net.json.JsonReqContentComment;
import com.evistek.vr.net.json.JsonReqDevice;
import com.evistek.vr.net.json.JsonReqDownload;
import com.evistek.vr.net.json.JsonReqList;
import com.evistek.vr.net.json.JsonReqLogin;
import com.evistek.vr.net.json.JsonReqPlayRecord;
import com.evistek.vr.net.json.JsonReqRegister;
import com.evistek.vr.net.json.JsonReqSendComment;
import com.evistek.vr.net.json.JsonReqUser;
import com.evistek.vr.net.json.JsonReqUserComment;
import com.evistek.vr.net.json.JsonReqUserName;
import com.evistek.vr.net.json.JsonReqUserResource;
import com.evistek.vr.net.json.JsonReqVrOnline;
import com.evistek.vr.net.json.JsonRespApplication;
import com.evistek.vr.net.json.JsonRespCategory;
import com.evistek.vr.net.json.JsonRespContent;
import com.evistek.vr.net.json.JsonRespContentComment;
import com.evistek.vr.net.json.JsonRespFavorite;
import com.evistek.vr.net.json.JsonRespImage;
import com.evistek.vr.net.json.JsonRespLogin;
import com.evistek.vr.net.json.JsonRespPlayRecord;
import com.evistek.vr.net.json.JsonRespUser;
import com.evistek.vr.net.json.JsonRespUserName;
import com.evistek.vr.net.json.JsonRespVideo;
import com.evistek.vr.net.json.JsonRespVrOnline;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

public class NetWorkService {
    //The Dateformat of Gson is the same with the server.
    private static Gson mGson = new GsonBuilder().setDateFormat("MMM dd, yyyy hh:mm:ss aa").create();
    private static final String TAG = "NetWorkService";

    /**
     * 用户注册
     *
     * @param userName
     * @param password
     * @param callback
     */
    public static void register(final String userName, final String password, final RegisterCallback callback) {
        JsonReqRegister reqJson = new JsonReqRegister();
        reqJson.setName("register");
        reqJson.setUserName(userName);

        OkHttpWrapper.getInstance().postAsync(Config.URL_REGISTER, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespLogin>() {
                    @Override
                    public void onResponse(JsonRespLogin response) {
                            if (response.getCode() == 200) {
                                callback.onResult(response.getCode(), "注册成功.");
                                String challenge = response.getChallenge();
                                Log.e(TAG, "challenge: " + challenge);

                                registerChallenge(userName, password, challenge);
                            } else {
                                callback.onResult(response.getCode(), userName + " 已存在.");
                                Log.e(TAG, "error code: " + response.getCode());
                            }
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "login error: " + code + " msg: " + msg);
                    }
                });
    }

    /**
     * 用户登录
     *
     * @param userName
     * @param passWord
     * @param callback
     */
    public static void login(final String userName, final String passWord, final LoginCallback callback) {
        JsonReqLogin reqJson = new JsonReqLogin();
        reqJson.setName("login");
        reqJson.setUserName(userName);

        OkHttpWrapper.getInstance().postAsync(Config.URL_LOGIN, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespLogin>() {
                    @Override
                    public void onResponse(JsonRespLogin response) {
                        if (response.getCode() == 200) {
                            String mChallenge = response.getChallenge();
                            loginChalleng(userName, passWord, mChallenge,
                                    new LoginCallback() {
                                        @Override
                                        public void onResult(int code, String msg, JsonRespUser respLogin) {
                                            if (code == 200) {
                                                callback.onResult(respLogin.getCode(), "登陆成功.", respLogin);
                                            } else if (code == 401) {
                                                callback.onResult(respLogin.getCode(), "用户名和密码不匹配.", null);
                                            } else {
                                                callback.onResult(code, "please check  your net.", null);
                                            }
                                        }
                                    });

                        } else if (response.getCode() == 404) {
                            callback.onResult(response.getCode(), "您输入的用户名不存在.", null);
                        }
                    }

                    @Override
                    public void onFailure(int code, String msg) {

                    }
                });
    }

    /**
     * 获取某个类型的Category
     *
     * @param pageNo
     * @param pageSize
     * @param contentType
     * @param callback
     */
    public static void getCateGoryList(int pageNo, int pageSize, String contentType, final CategoryCallback callback) {
        JsonReqList reqJson = new JsonReqList();
        reqJson.setName("list");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setContentType(contentType);
        reqJson.setDetail(false);
        reqJson.setCategoryId(0);
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST,
                mGson.toJson(reqJson), new OkHttpWrapper.RequestCallBack<JsonRespCategory>() {

                    @Override
                    public void onResponse(JsonRespCategory response) {
                        Log.e("ZWX", "onResponse2: "
                                + " pageNo: " + response.getPageNo()
                                + " pageSize: " + response.getPageSize()
                                + " resultSize: " + response.getResults().size());
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取某个类型的和VR相关的category
     */
    public static void getVRCategoryList(String contentType, final CategoryCallback callback) {
        OkHttpWrapper.getInstance().postAsync(Config.URL_VRLIST, mGson.toJson(contentType),
                new OkHttpWrapper.RequestCallBack<JsonRespCategory>() {
                    @Override
                    public void onResponse(JsonRespCategory response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取某个类型的和VR无关的category
     */
    public static void getCategoryListExceptVR(String contentType, final CategoryCallback callback) {
        OkHttpWrapper.getInstance().postAsync(Config.URL_EXCEPTVRLIST, mGson.toJson(contentType),
                new OkHttpWrapper.RequestCallBack<JsonRespCategory>() {
                    @Override
                    public void onResponse(JsonRespCategory response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取 Image List
     *
     * @param pageNo
     * @param pageSize
     * @param contentType
     * @param categoryId
     * @param callback
     */
    public static void getImageList(int pageNo, int pageSize, String contentType, int categoryId,
                                    final ImageCallback callback) {
        JsonReqList reqJson = new JsonReqList();
        reqJson.setName("list");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setContentType("image");
        reqJson.setDetail(true);
        reqJson.setCategoryId(categoryId);
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespImage>() {
                    @Override
                    public void onResponse(JsonRespImage response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取Recommend video List
     *
     * @param pageNo
     * @param pageSize
     */
    public static void getRecommendVideoList(int pageNo, int pageSize, final VideoCallback callback) {
        JsonReqList reqJson = new JsonReqList();
        reqJson.setName("recommendVideoList");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_RECOMMEND, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespVideo>() {
                    @Override
                    public void onResponse(JsonRespVideo response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取userName List
     */
    public static void getUserNameList(ArrayList<Integer> userIdList, final UserNameListCallBack callback) {
        JsonReqUserName reqJson = new JsonReqUserName();
        reqJson.setName("userNameList");
        reqJson.setUserIdList(userIdList);

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_USERNAME, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespUserName>() {
                    @Override
                    public void onResponse(JsonRespUserName response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getUserByUserName(String userName, final UserNameCallBack callback) {

        OkHttpWrapper.getInstance().postAsync(Config.URL_USERNAME, mGson.toJson(userName),
                new OkHttpWrapper.RequestCallBack<JsonRespUser>() {
                    @Override
                    public void onResponse(JsonRespUser response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void updateUserInfo(UserInfo userInfo, final UserCallBack callback) {

        OkHttpWrapper.getInstance().postAsync(Config.URL_UPDATEUSER, mGson.toJson(userInfo),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onResult(200, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
    * 添加用户登录信息
     * * */
    public static void addLoginRecord (int userId,int status, final UserCallBack callback) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUserId(userId);
        loginInfo.setStatus(status);

        OkHttpWrapper.getInstance().postAsync(Config.URL_ADD_LOGIN_RECORD, mGson.toJson(loginInfo),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onResult(200, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getContentList(ArrayList<Integer> contentIdList, final ContentCallBack callback) {
        JsonReqContent reqJson = new JsonReqContent();
        reqJson.setName("contentList");
        reqJson.setContentIdList(contentIdList);

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_CONTENT, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespContent>() {
                    @Override
                    public void onResponse(JsonRespContent response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getVideoById(int id, final VideoCallback callback) {

        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_VIDEO_BY_ID, mGson.toJson(id),
                new OkHttpWrapper.RequestCallBack<Video>() {
                    @Override
                    public void onResponse(Video response) {
                        //Log.e("ZWX", "getVideoById onResponse: " + response.getCode());
                        if (response != null) {
                            JsonRespVideo jsonRespVideo = new JsonRespVideo();
                            ArrayList<Video> videoArrayList = new ArrayList<Video>();
                            videoArrayList.add(response);
                            jsonRespVideo.setResults(videoArrayList);
                            callback.onResult(200, jsonRespVideo);
                        }

                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e("ZWX", "getVideoById onFailure: " + code);
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取 Video List
     *
     * @param pageNo
     * @param pageSize
     * @param contentType
     * @param categoryId
     * @param callback
     */
    public static void getVideoList(int pageNo, int pageSize, String contentType, int categoryId,
                                    final VideoCallback callback) {
        JsonReqList reqJson = new JsonReqList();
        reqJson.setName("list");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setContentType("video");
        reqJson.setDetail(true);
        reqJson.setCategoryId(categoryId);
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespVideo>() {
                    @Override
                    public void onResponse(JsonRespVideo response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getApplicationList(int pageNo, int pageSize, final ApplicationCallback callback) {
        JsonReqApplication jsonReq = new JsonReqApplication();
        jsonReq.setName("getApplication");
        jsonReq.setPageNo(pageNo);
        jsonReq.setPageSize(pageSize);
        jsonReq.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_APPLICATION, mGson.toJson(jsonReq),
                new OkHttpWrapper.RequestCallBack<JsonRespApplication>() {
                    @Override
                    public void onResponse(JsonRespApplication response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 添加播放历史
     * @param playRecord
     * @param callback
     */
    public static void addPlayRecord(PlayRecord playRecord, final UserCallBack callback) {

        OkHttpWrapper.getInstance().postAsync(Config.URL_ADD_PLAY_RECORD, mGson.toJson(playRecord),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onResult(200, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 更新播放记录的播放时长
     *
     * @param
     */
    public static void updatePlayRecordDuration(PlayRecord playRecord, final UserCallBack callback) {

        OkHttpWrapper.getInstance().postAsync(Config.URL_UPDATE_DURATION, mGson.toJson(playRecord),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onResult(200, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 根据用户ID获取播放记录
     *
     */
    public static void getPlayRecordsByUserId(int userId, final PlayRecordCallback callback) {
        JsonReqPlayRecord jsonReq = new JsonReqPlayRecord();
        jsonReq.setUserId(userId);
        jsonReq.setClient( E3DApplication.getInstance().getPackageName());

        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_PLAY_RECORD, mGson.toJson(jsonReq),
                new OkHttpWrapper.RequestCallBack<JsonRespPlayRecord>() {
                    @Override
                    public void onResponse(JsonRespPlayRecord response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                }
        );
    }

    /**
     *  删除播放记录
     * @param mList
     * @param callBack
     */
    public static void deletePlayRecords(List<PlayRecord> mList, final PlayRecordCallback callBack){

        OkHttpWrapper.getInstance().postAsync(Config.URL_DELETE_PLAY_RECORD, mGson.toJson(mList),
                new OkHttpWrapper.RequestCallBack<JsonRespPlayRecord>() {
                    @Override
                    public void onResponse(JsonRespPlayRecord response) {
                        callBack.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    public static void addFavorite(Favorite favorite, final FavoriteCallback callBack) {

        OkHttpWrapper.getInstance().postAsync(Config.URL_ADD_FAVORITE, mGson.toJson(favorite),
                new OkHttpWrapper.RequestCallBack<JsonRespFavorite>() {
                    @Override
                    public void onResponse(JsonRespFavorite response) {
                        callBack.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 根据用户ID获取收藏记录
     *
     * @param userId
     * @param callback
     */
    public static void getFavoriteByUserId(int userId, final FavoriteCallback callback) {
        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_FAVORITE, mGson.toJson(userId),
                new OkHttpWrapper.RequestCallBack<JsonRespFavorite>() {
                    @Override
                    public void onResponse(JsonRespFavorite response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void deleteFavorite(Favorite favorite, final FavoriteCallback callBack) {

        OkHttpWrapper.getInstance().postAsync(Config.URL_DELETE_FAVORITE, mGson.toJson(favorite),
                new OkHttpWrapper.RequestCallBack<JsonRespFavorite>() {
                    @Override
                    public void onResponse(JsonRespFavorite response) {
                        callBack.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 删除收藏记录
     * @param mList
     * @param callBack
     */
    public static void deleteFavorite(List<Favorite> mList, final FavoriteCallback callBack){

        OkHttpWrapper.getInstance().postAsync(Config.URL_DELETE_FAVORITES, mGson.toJson(mList),
                new OkHttpWrapper.RequestCallBack<JsonRespFavorite>() {
                    @Override
                    public void onResponse(JsonRespFavorite response) {
                        callBack.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 上传设备统计信息
     *
     * @param DeviceId
     * @param DeviceModel
     * @param System
     * @param Location
     * @param callback
     */
    public static void uploadDeviceInfo(String DeviceId, String DeviceModel, String System,
                                        String Location, final DeviceCallback callback) {
        JsonReqDevice reqJson = new JsonReqDevice();
        reqJson.setName("device");
        reqJson.setDeviceId(DeviceId);
        reqJson.setDeviceModel(DeviceModel);
        reqJson.setSystem(System);
        reqJson.setLocation(Location);
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_DEVICE, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onResult(200, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 获取一个内容的评论
     *
     * @param pageNo
     * @param pageSize
     * @param Id
     * @param Url
     * @param callback
     */
    public static void getContentComment(int pageNo, int pageSize, int Id, String Url,
                                         final ContentCommentCallback callback) {
        JsonReqContentComment reqJson = new JsonReqContentComment();
        reqJson.setName("getContentComment");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setId(Id);
        reqJson.setUrl(Url);
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_CONTENT_COMMENT, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespContentComment>() {

                    @Override
                    public void onResponse(JsonRespContentComment response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取一个用户的评论
     *
     * @param pageNo
     * @param pageSize
     * @param UserId
     * @param callback
     */
    public static void getUserComment(int pageNo, int pageSize, int UserId, final UserCommentCallback callback) {
        JsonReqUserComment reqJson = new JsonReqUserComment();
        reqJson.setName("getUserComment");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setUserId(UserId);
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_USER_COMMENT, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespContentComment>() {

                    @Override
                    public void onResponse(JsonRespContentComment response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取一个用户上传的图片
     *
     * @param pageNo
     * @param pageSize
     * @param userId
     * @param callback
     */
    public static void getUserImage(int pageNo, int pageSize, int userId, final ImageCallback callback) {
        JsonReqUserResource reqJson = new JsonReqUserResource();
        reqJson.setName("listByUser");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setUserId(userId);
        reqJson.setType("image");
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_BY_USER, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespImage>() {

                    @Override
                    public void onResponse(JsonRespImage response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取一个用户上传的视频
     *
     * @param pageNo
     * @param pageSize
     * @param userId
     * @param callback
     */
    public static void getUserVideo(int pageNo, int pageSize, int userId, final VideoCallback callback) {
        JsonReqUserResource reqJson = new JsonReqUserResource();
        reqJson.setName("listByUser");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setUserId(userId);
        reqJson.setType("video");
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_BY_USER, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespVideo>() {

                    @Override
                    public void onResponse(JsonRespVideo response) {
                        callback.onResult(response.getCode(), response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 发表评论
     *
     * @param userId
     * @param contentId
     * @param comment
     * @param callback
     */
    public static void sendComment(int userId, int contentId, String comment, final SendCommentCallback callback) {
        JsonReqSendComment reqJson = new JsonReqSendComment();
        reqJson.setName("sendComment");
        reqJson.setUserId(userId);
        reqJson.setContentId(contentId);
        reqJson.setComment(comment);
        reqJson.setVersion("1.0");

        OkHttpWrapper.getInstance().postAsync(Config.URL_SEND_COMMENT, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<String>() {

                    @Override
                    public void onResponse(String response) {
                        callback.onResult(200, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    private static int loginChalleng(String userName, String password, String challenge,
                                     final LoginCallback callback) {
        AuthServiceImpl authService = new AuthServiceImpl();

        JsonReqChallenge jsonReq = new JsonReqChallenge();
        jsonReq.setName("challenge");
        jsonReq.setUserName(userName);

        // 使用密码对challenge字符串进行加密
        SecretKey key = authService.generateKey(password);
        String encryptedChallenge = authService.encryptToBase64String(challenge, key);
        jsonReq.setCiphertext(encryptedChallenge);

        OkHttpWrapper.getInstance().postAsync(Config.URL_LOGIN_CHALLENGE, mGson.toJson(jsonReq),
                new OkHttpWrapper.RequestCallBack<JsonRespUser>() {

                    @Override
                    public void onResponse(JsonRespUser response) {
                        callback.onResult(response.getCode(), "", response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, msg, null);
                    }
                });

        return 0;
    }

    private static int registerChallenge(String userName, String password, String challenge) {
        AuthServiceImpl authService = new AuthServiceImpl();

        JsonReqChallenge jsonReq = new JsonReqChallenge();
        jsonReq.setName("challenge");
        jsonReq.setUserName(userName);
        // 使用challenge字符串对密码进行加密
        SecretKey key = authService.generateKey(challenge);
        String encryptedPassword = authService.encryptToBase64String(password, key);
        Log.e(TAG, encryptedPassword + " :encryptedPassword");
        jsonReq.setCiphertext(encryptedPassword);

        OkHttpWrapper.getInstance().postAsync(Config.URL_REGISTER_CHALLENGE, mGson.toJson(jsonReq),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {

                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e("ZWX", "onFailure code: " + code + " msg: " + msg);
                    }
                });

        return 0;
    }

    public static void register(final String userName, final String access_token, final String nickName, final String location, final String sex,
                                final String headImgurl, final String source, final RegisterCallback callback) {
        JsonReqRegister reqJson = new JsonReqRegister();
        reqJson.setName("register");
        reqJson.setUserName(userName);

        OkHttpWrapper.getInstance().postAsync(Config.URL_REGISTER, mGson.toJson(reqJson),
                new OkHttpWrapper.RequestCallBack<JsonRespLogin>() {
                    @Override
                    public void onResponse(JsonRespLogin response) {
                        if (response.getCode() == 200) {
                            callback.onResult(response.getCode(), "娉ㄥ唽鎴愬姛.");
                        } else {
                            callback.onResult(response.getCode(), userName + " 宸插瓨鍦?");
                            Log.e(TAG, "error code: " + response.getCode());
                        }
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "login error, code: " + code + " msg: " + msg);
                    }
                });
    }

    public static void registerByThirdParty(String userName, String access_token, String nickName, String location, String sex,
                                            String headImgurl, String source, final LoginCallback callback) {
        JsonReqUser jsonReq = new JsonReqUser();
        jsonReq.setName("challenge");
        jsonReq.setUserName(userName);
        jsonReq.setPwd(access_token);
        jsonReq.setNickName(nickName);
        jsonReq.setLocation(location);
        jsonReq.setSex(sex);
        jsonReq.setHeadImgurl(headImgurl);
        jsonReq.setSource(source);

        OkHttpWrapper.getInstance().postAsync(Config.URL_REGISTER, mGson.toJson(jsonReq),
                new OkHttpWrapper.RequestCallBack<JsonRespUser>() {
                    @Override
                    public void onResponse(JsonRespUser response) {
                        callback.onResult(response.getCode(), "success", response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {

                    }
                });
    }

    public static void updateDownloadCount(final String url, int id) {
        JsonReqDownload jsonReqDownload = new JsonReqDownload();
        jsonReqDownload.setName("download");
        jsonReqDownload.setContentId(id);
        jsonReqDownload.setUrl(url);

        OkHttpWrapper.getInstance().postAsync(Config.URL_APP_DOWNLOAD, mGson.toJson(jsonReqDownload),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {

                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "Failed to update download count, URL: " + url + " errorCode: " + code
                                + " errorInfo: " + msg);
                    }
                });
    }

    public static void updatePlayCount(final String url) {
        JsonReqDownload jsonReqDownload = new JsonReqDownload();
        jsonReqDownload.setName("download");
        jsonReqDownload.setContentId(0);
        jsonReqDownload.setUrl(url);

        OkHttpWrapper.getInstance().postAsync(Config.URL_DOWNLOAD, mGson.toJson(jsonReqDownload),
                new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(String response) {

                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "Failed to update download count, URL: " + url +
                                " errorCode: " + code +
                                " errorInfo: " + msg);
                    }
                });
    }

    public static void getVrOnline (int pageNo, int pageSize, int categoryId, final VrOnlineCallback callback) {
        JsonReqVrOnline reqJson = new JsonReqVrOnline();
        reqJson.setName("getVrOnline");
        reqJson.setCategoryId(categoryId);
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setVersion("1.0");
        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_AUDITED_VRONLINE, mGson.toJson(reqJson), new OkHttpWrapper.RequestCallBack<JsonRespVrOnline>() {
            @Override
            public void onResponse(JsonRespVrOnline response) {
                callback.onResult(response.getCode(), response);
            }

            @Override
            public void onFailure(int code, String msg) {
                callback.onResult(code, null);
            }
        });
    }

    public static void updateVrOnlinePlaycount (int id) {
        OkHttpWrapper.getInstance().postAsync(Config.URL_UPDATE_VRONLINE_PLAY_COUNT, mGson.toJson(id), new OkHttpWrapper.RequestCallBack<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "Successfully update playCount of vrOnline!");
            }

            @Override
            public void onFailure(int code, String msg) {
                Log.e(TAG, "Failed to update play count of vrOnline!");
            }
        });
    }
}
