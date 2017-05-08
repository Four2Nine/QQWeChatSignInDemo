package com.sealiu.qqwechatsignin;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import static com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN;
import static com.tencent.connect.common.Constants.PARAM_EXPIRES_IN;
import static com.tencent.connect.common.Constants.PARAM_OPEN_ID;


public class LoginActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final String SCOPE = "get_simple_userinfo";
    private static final int WECHAT_SIGNIN_PERM = 100;
    private IUiListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_qq).setOnClickListener(this);
        findViewById(R.id.sign_in_wechat).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_qq:
                loginQQ();
                break;
            case R.id.sign_in_wechat:
                if (checkWeChatSignInPermission()) {
                    loginWechat();
                } else {
                    ActivityCompat.requestPermissions(
                            LoginActivity.this,
                            new String[]{
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            WECHAT_SIGNIN_PERM
                    );
                }

            default:
                break;
        }
    }

    private void loginQQ() {
        if (!mTencent.isSessionValid()) {

            listener = new IUiListener() {
                @Override
                public void onComplete(Object o) {
                    if (o != null) {
                        JSONObject jsonObject = (JSONObject) o;

                        try {
                            String token = jsonObject.getString(PARAM_ACCESS_TOKEN);
                            String expires = jsonObject.getString(PARAM_EXPIRES_IN);
                            String openId = jsonObject.getString(PARAM_OPEN_ID);

                            sharedPref.edit().putString("access_token", token).apply();
                            sharedPref.edit().putString("open_id", openId).apply();
                            sharedPref.edit().putString("expires", expires).apply();
                            sharedPref.edit().putString("from", "qq").apply();

                            mTencent.setAccessToken(token, expires);
                            mTencent.setOpenId(openId);

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    Log.d(TAG, uiError.errorCode + ": " + uiError.errorMessage + "(" + uiError.errorDetail + ")");
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "canceled");
                }
            };

            mTencent.login(this, SCOPE, listener);
        }
    }

    private void loginWechat() {
        regToWx();

        // send oauth request
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        wxapi.sendReq(req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WECHAT_SIGNIN_PERM) {
            if (checkWeChatSignInPermission()) {
                loginWechat();
            }
        }
    }
}

