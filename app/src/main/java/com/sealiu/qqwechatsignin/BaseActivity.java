package com.sealiu.qqwechatsignin;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by liuyang
 * on 2017/5/4.
 */

public class BaseActivity extends AppCompatActivity {
    // TODO: 2017/5/8 填写appid
    static final String APP_ID_WECHAT = "wxd477edab60670232";
    IWXAPI wxapi;

    Tencent mTencent;
    SharedPreferences sharedPref;


    public void regToWx() {
        wxapi = WXAPIFactory.createWXAPI(this, APP_ID_WECHAT, true);
        if (wxapi.isWXAppInstalled()) {
            wxapi.registerApp(APP_ID_WECHAT);
        } else {
            Toast.makeText(this, getString(R.string.wechat_un_install), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getSharedPreferences("health", Context.MODE_PRIVATE);
        mTencent = Tencent.createInstance("1106067843", this.getApplicationContext());
    }

    public static String doInBackground(String params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean checkWeChatSignInPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
