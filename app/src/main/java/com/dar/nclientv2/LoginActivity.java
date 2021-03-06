package com.dar.nclientv2;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dar.nclientv2.settings.Global;
import com.dar.nclientv2.settings.Login;
import com.dar.nclientv2.utility.LogUtility;
import com.dar.nclientv2.utility.Utility;

import java.util.Calendar;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    CookieWaiter waiter;
    WebView webView;
    public TextView invalid;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.initActivity(this);
        setContentView(R.layout.activity_login);
        final Toolbar toolbar=findViewById(R.id.toolbar);
        webView=findViewById(R.id.webView);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_activity_login);
        assert getSupportActionBar()!=null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(Global.getUserAgent());
        webView.loadUrl(Utility.getBaseUrl()+"login/");
        waiter=new CookieWaiter();
        waiter.start();
    }

    @Override
    protected void onDestroy() {
        if(waiter!=null&&waiter.isAlive())
            waiter.interrupt();
        super.onDestroy();
    }

    class CookieWaiter extends Thread{
        @Override
        public void run() {
            CookieManager manager=CookieManager.getInstance();
            String cookies="";
            while (cookies==null||!cookies.contains("sessionid")) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {

                }
                if(isInterrupted())return;
                cookies=manager.getCookie(Utility.getBaseUrl());
            }
            /*LogUtility.d("Cookie string: "+cookies);
            String session= fetchCookie("sessionid",cookies);
            String cloudflare= fetchCookie("__cfduid",cookies);
            applyCookie("sessionid",session);
            applyCookie("__cfduid",cloudflare);
            runOnUiThread(LoginActivity.this::finish);*/
            Login.hasLogged(webView);
            runOnUiThread(LoginActivity.this::finish);
        }

        private void applyCookie(String name,String session) {
            HttpUrl url=HttpUrl.parse(Utility.getBaseUrl());
            Calendar expire=Calendar.getInstance();
            expire.add(Calendar.DAY_OF_MONTH,14);
            if(url==null)return;
            List<Cookie>cookies=Global.getClient().cookieJar().loadForRequest(url);
            Cookie newCookie=new Cookie.Builder()
                    .value(session)
                    .httpOnly()
                    .path("/")
                    .expiresAt(expire.getTimeInMillis())
                    .name(name)
                    .domain(Utility.getHost())
                    .build();
            LogUtility.d("Created cookie: "+newCookie);
            cookies.add(newCookie);
            Global.getClient().cookieJar().saveFromResponse(url,cookies);
        }

        String fetchCookie(String cookieName, String cookies){
            int start=cookies.indexOf(cookieName);
            start=cookies.indexOf('=',start)+1;
            int end=cookies.indexOf(';',start);
            return cookies.substring(start,end==-1?cookies.length()-1:end);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)finish();
        return super.onOptionsItemSelected(item);
    }
}

