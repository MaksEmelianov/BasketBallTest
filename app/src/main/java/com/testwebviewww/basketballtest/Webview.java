package com.testwebviewww.basketballtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Objects;

public class Webview extends AppCompatActivity {

    Intent intent;
    WebView webview;
    WebSettings webSettings;
    NetworkInfo networkInfo;

    String URL;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String keyURL = "keyURL";
    String valueURL;
    String defaultURL = "https://google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getPreferences(Context.MODE_PRIVATE);

        String configURL = getURLFromGoogleService();
        System.out.println("configURL - " + configURL);
        startWebview(configURL, savedInstanceState);

//        if (preferences.contains(keyURL)) {
//            exeIfSavedURL(savedInstanceState);
//        } else {
//            URL = getURLFromGoogleService();
//            if (URL.equals("") || isEmulator() || isThereSim()) {
//                startMainActivity();
//            } else {
//                saveURL(URL);
//                startWebview(valueURL, savedInstanceState);
//            }
//        }
    }


    private void saveURL(String valueURL) {
        preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(keyURL, valueURL);
        editor.apply();
    }

    private boolean isThereSim() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }

    private void exeIfSavedURL(Bundle savedInstanceState) {
        if (isThereInternet(this)) {
            valueURL = preferences.getString(keyURL, defaultURL);
            startWebview(valueURL, savedInstanceState);
        } else {
            Intent intent = new Intent(this, TurnOnInternet.class);
            startActivity(intent);
        }
    }

    private void startMainActivity() {
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void startWebview(String URL, Bundle savedInstanceState) {
        webview = findViewById(R.id.webview);
        setSettingWebview();
        if (!Objects.isNull(savedInstanceState)) {
            webview.restoreState(savedInstanceState);
        } else {
            webview.loadUrl(URL);
        }
    }

    private void setSettingWebview() {
        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webview.setInitialScale(1);

        webview.setWebViewClient(new MyWebViewClient());
    }

    private boolean isThereInternet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = manager.getActiveNetworkInfo();
        return !Objects.isNull(networkInfo) && networkInfo.isConnectedOrConnecting();
    }

    private String getURLFromGoogleService() {
        FirebaseApp.getInstance();
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder().build();
        config.setConfigSettingsAsync(settings);
        String configURL = config.getString("url");
        config.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Webview.this, "Fetch and activate succeeded",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Webview.this, "Fetch failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        return configURL;
    }

    private boolean isEmulator() {
        if (BuildConfig.DEBUG) {
            return false;
        }
        String phoneModel = Build.MODEL;
        String buildProduct = Build.PRODUCT;
        String buildHardware = Build.HARDWARE;
        String brand = Build.BRAND;
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.MANUFACTURER.contains("Genymotion")
                || phoneModel.contains("google_sdk")
                || phoneModel.toLowerCase().contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || buildHardware.equals("goldfish")
                || brand.contains("google")
                || buildHardware.equals("vbox86")
                || buildProduct.equals("sdk")
                || buildProduct.equals("google_sdk")
                || buildProduct.equals("sdk_x86")
                || buildProduct.equals("vbox86p")
                || Build.BOARD.toLowerCase().contains("nox")
                || Build.BOOTLOADER.toLowerCase().contains("nox")
                || buildHardware.toLowerCase().contains("nox")
                || buildProduct.toLowerCase().contains("nox")
                || (brand.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || buildProduct.equals("google_sdk"));
    }

    private String loadURL() {
        return getPreferences(Context.MODE_PRIVATE).getString(keyURL, defaultURL);
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            CookieManager manager = CookieManager.getInstance();
            manager.setAcceptCookie(true);
            preferences = getPreferences(Context.MODE_PRIVATE);
            editor = preferences.edit();
            editor.putString(keyURL, view.getUrl());
            editor.apply();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        webview = findViewById(R.id.webview);
        webview.saveState(outState);
        super.onSaveInstanceState(outState);
    }
}