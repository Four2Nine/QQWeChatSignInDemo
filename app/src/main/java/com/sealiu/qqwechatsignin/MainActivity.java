package com.sealiu.qqwechatsignin;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sealiu.qqwechatsignin.bean.QUserInfo;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    String accessToken, openId;

    private TextView mTextMessage, mTextName, mTextGender, mTextLocation;
    private ImageView mImageHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accessToken = sharedPref.getString("access_token", "");
        openId = sharedPref.getString("open_id", "");

        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        mTextName = (TextView) findViewById(R.id.name);
        mTextGender = (TextView) findViewById(R.id.gender);
        mTextLocation = (TextView) findViewById(R.id.location);
        mImageHead = (ImageView) findViewById(R.id.avatar);;

        if (accessToken.equals("") || openId.equals("")) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            new GetQQInfo().execute("https://graph.qq.com/user/get_simple_userinfo?"+
                    "access_token="+ accessToken + "&openid="+ openId + "&appid=1106067843");
        }
    }

    private class GetQQInfo extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return BaseActivity.doInBackground(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null && !s.equals("")) {
                QUserInfo qUserInfo =  new Gson().fromJson(s, QUserInfo.class);

                if (qUserInfo != null) {
                    setQUserInfo(qUserInfo);
                }
            }
        }
    }

    private void setQUserInfo(QUserInfo q) {

        if (q.getRet() != 0) {
            mTextMessage.setText(q.getMsg());
        }
        mTextName.setText(q.getNickname());
        mTextGender.setText(q.getGender());
        mTextLocation.setText(q.getProvince() + " " + q.getCity());

        if (!q.getFigureurl_qq_2().equals("")) {
            Glide.with(this).load(q.getFigureurl_qq_2()).into(mImageHead);
        } else if (!q.getFigureurl_qq_1().equals("")){
            Glide.with(this).load(q.getFigureurl_qq_1()).into(mImageHead);
        }
        Log.d(TAG, q.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit) {
            if (mTencent != null) {
                mTencent.logout(this);
            }

            sharedPref.edit().putString("access_token", "").apply();
            sharedPref.edit().putString("open_id", "").apply();
            sharedPref.edit().putString("expires", "").apply();
            sharedPref.edit().putString("from", "").apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
