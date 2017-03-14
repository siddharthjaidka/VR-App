package com.evistek.vr.activity;

import com.evistek.vr.R;
import com.evistek.vr.model.UserInfo;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.LoginCallback;
import com.evistek.vr.net.callback.RegisterCallback;
import com.evistek.vr.net.callback.UserCallBack;
import com.evistek.vr.net.callback.UserNameCallBack;
import com.evistek.vr.net.json.JsonRespUser;
import com.evistek.vr.user.User;
import com.evistek.vr.utils.Utils;
import com.evistek.vr.user.ThirdPartyLogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends Activity {
    private final static String TENCENT_APP_ID = "1105529432";
    private final static String TENCENT_SCOPE = "all";
    private final static String TAG = "LoginV2Activity";
    private EditText mLogUserNameET;
    private EditText mLogPasswordEt;
    private Button mLogButton;
    private TextView mRegisterBt;
    private ImageView mBack;
    private static ThirdPartyLogin mThirdPartyLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        initLoginSlidingMenu();
    }

    private void initLoginSlidingMenu() {
        mLogUserNameET = (EditText) findViewById(R.id.EtLogUsername);
        mLogPasswordEt = (EditText) findViewById(R.id.EtLogPassword);
        mLogButton = (Button) findViewById(R.id.BtLogLogin);
        mRegisterBt = (TextView) findViewById(R.id.BtLogRegister);
        mBack = (ImageView) findViewById(R.id.v2_user_login_backbt);
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLogButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mLogUserNameET.getText().toString().equals("")) {
                    if (mLogPasswordEt.getText().toString().length() > 6) {
                        final String userName = mLogUserNameET.getText().toString();
                        final String password = mLogPasswordEt.getText().toString();
                        NetWorkService.login(userName, password, new LoginCallback() {

                            @Override
                            public void onResult(int code, String msg, JsonRespUser respLogin) {
                                if (code == 200) {
                                    getApplicationContext();
                                    Utils.saveValue(Utils.SHARED_USERNAME, userName);
                                    Utils.saveValue(Utils.SHARED_USERID, respLogin.getUserId());
                                    Utils.saveValue(Utils.SHARED_REGISTERTIME, respLogin.getRegisterTime());
                                    Utils.saveValue(Utils.SHARED_USERTYPE, respLogin.getType());
                                    Utils.saveValue(Utils.SHARED_USERLEVEL, respLogin.getLevel());
                                    Utils.saveValue(Utils.SHARED_SOURCE, respLogin.getSource());
                                    addLoginRecord(respLogin.getUserId());
                                    E3DApplication.getInstance().getUser().update();
                                    updateDeviceInfo();
                                    Toast.makeText(getApplicationContext(),
                                            getApplicationContext().getResources().getText(R.string.LoginSuccess),
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                } else if (code == 404) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                } else if (code == 401) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                } else if (Utils.isNetworkAvailable() == false) {
                                    Toast.makeText(getApplicationContext(), R.string.net_not_available, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getApplicationContext().getResources().getText(R.string.RegisterPasswordlimit),
                                Toast.LENGTH_SHORT).show();
                        mLogPasswordEt.setText("");
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            getApplicationContext().getResources().getText(R.string.RegisterUsernamelimit),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRegisterBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
//                startActivity(intent);
                AlertDialog mdialog = new  AlertDialog.Builder(LoginActivity.this)
                        .setTitle(R.string.downloadDeletetip)
                        .setMessage(R.string.log_warning)
                        .setPositiveButton(R.string.ok,null)
                        .show();
            }
        });

    }

    public void qqLogin(View view) {
        mThirdPartyLogin = new ThirdPartyLogin(LoginActivity.this, mHandler, TENCENT_APP_ID, TENCENT_SCOPE);
    }

    Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                final ThirdPartyLogin.User userInfo = mThirdPartyLogin.getUser();
                NetWorkService.register(userInfo.getOpenId(), userInfo.getAccessToken(),
                        userInfo.getNickName(), userInfo.getLocation(), userInfo.getSex(),
                        userInfo.getHeadImgurl(), userInfo.getSource(), new RegisterCallback() {
                            @Override
                            public void onResult(int code, String msg) {
                                if (code == 200) {
                                    NetWorkService.registerByThirdParty(userInfo.getOpenId(),
                                            userInfo.getAccessToken(), userInfo.getNickName(),
                                            userInfo.getLocation(), userInfo.getSex(),
                                            userInfo.getHeadImgurl(), userInfo.getSource(), new LoginCallback() {

                                                @Override
                                                public void onResult(int code, String msg, JsonRespUser respLogin) {
                                                    Utils.saveValue(Utils.SHARED_USERNAME, respLogin.getUserName());
                                                    Utils.saveValue(Utils.SHARED_NICKNAME, respLogin.getNickName());
                                                    Utils.saveValue(Utils.SHARED_USERID, respLogin.getUserId());
                                                    Utils.saveValue(Utils.SHARED_REGISTERTIME, respLogin.getRegisterTime());
                                                    Utils.saveValue(Utils.SHARED_USERTYPE, respLogin.getType());
                                                    Utils.saveValue(Utils.SHARED_USERLEVEL, respLogin.getLevel());
                                                    Utils.saveValue(Utils.SHARED_SOURCE, respLogin.getSource());
                                                    Utils.saveValue(Utils.SHARED_HEAD_IMGURL, respLogin.getHeadImgurl());
                                                    addLoginRecord(respLogin.getUserId());
                                                    E3DApplication.getInstance().getUser().update();
                                                    finish();
                                                }
                                            });
                                }
                                if (code == 406) {
                                    NetWorkService.getUserByUserName(userInfo.getOpenId(), new UserNameCallBack() {
                                        @Override
                                        public void onResult(int code, final JsonRespUser JsonResp) {
                                            if (code == 200) {
                                                UserInfo user = new UserInfo(userInfo.getOpenId(),
                                                        userInfo.getAccessToken(), userInfo.getNickName(),
                                                        userInfo.getLocation(), userInfo.getSex(),
                                                        userInfo.getHeadImgurl(), userInfo.getSource());
                                                        addLoginRecord(JsonResp.getUserId());
                                                        updateUserAndLogin(JsonResp, user);
                                            }
                                        }
                                    });
                                }
                            }
                        });
            }
            return true;
        }

    });

    public void addLoginRecord (int userId) {
        //上传登录记录的信息 1：login ; 0:logout
        NetWorkService.addLoginRecord(userId, 1, new UserCallBack() {

            @Override
            public void onResult(int code, String msg) {
                if (code == 200) {
                    Log.i(TAG, "add loginRecord successfully");
                } else if (code == 400) {
                    Log.i(TAG, "failed to add loginRecord");
                }
            }
        });
    }

    public void updateUserAndLogin(final JsonRespUser JsonResp, final UserInfo user) {
        Date register = null;
        try {
            register = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm").parse(JsonResp.getRegisterTime());
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        user.setUserId(JsonResp.getUserId());
        user.setRegisterTime(register);
        user.setTelephone(JsonResp.getTelephone());
        user.setEmail(JsonResp.getEmail());
        user.setType(JsonResp.getType());
        user.setLevel(JsonResp.getLevel());
        user.setVrDevice(E3DApplication.getInstance().getUser().vrDevice);
        // update UserInfo
        NetWorkService.updateUserInfo(user, new UserCallBack() {

            @Override
            public void onResult(int code, String msg) {
                if (code == 200) {
                    Utils.saveValue(Utils.SHARED_USERNAME, user.getUsername());
                    Utils.saveValue(Utils.SHARED_NICKNAME, user.getNickName());
                    Utils.saveValue(Utils.SHARED_USERID, user.getUserId());
                    Utils.saveValue(Utils.SHARED_REGISTERTIME, JsonResp.getRegisterTime());
                    Utils.saveValue(Utils.SHARED_USERTYPE, user.getType());
                    Utils.saveValue(Utils.SHARED_USERLEVEL, user.getLevel());
                    Utils.saveValue(Utils.SHARED_SOURCE, user.getSource());
                    Utils.saveValue(Utils.SHARED_HEAD_IMGURL, user.getHeadImgurl());
                    E3DApplication.getInstance().getUser().update();
                    finish();
                }
            }
        });
    }

    private void updateDeviceInfo() {
        User user = E3DApplication.getInstance().getUser();
        if (user.isLogin) {

            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(user.id);
            userInfo.setVrDevice(user.vrDevice);

            NetWorkService.updateUserInfo(userInfo, new UserCallBack() {
                @Override
                public void onResult(int code, String msg) {
                    if (code != 200) {
                        Log.e(TAG, "code: " + code + " msg: " + msg);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mThirdPartyLogin.onActivityResultData(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
