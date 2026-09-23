package com.mastilearning.kids;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView web;
    private TextToSpeech tts;
    private String pendingText = "";
    private Locale pendingLocale = Locale.US;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true); s.setAllowContentAccess(true);
        web.setWebViewClient(new WebViewClient());
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setSpeechRate(0.78f); tts.setPitch(1.25f);
                if (!pendingText.isEmpty()) speakNow(pendingText, pendingLocale);
            }
        });
        web.addJavascriptInterface(new TTSBridge(), "AndroidTTS");
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }

    private void speakNow(String text, Locale locale) {
        int r = tts.setLanguage(locale);
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(locale.getLanguage().equals("hi") ? new Locale("hi","IN") : Locale.US);
        }
        tts.setSpeechRate(0.78f); tts.setPitch(1.25f);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "masti-story");
    }

    private class TTSBridge {
        @JavascriptInterface public void speak(String text, String lang) {
            pendingText = text; pendingLocale = lang.startsWith("hi") ? new Locale("hi","IN") : new Locale("en","IN");
            runOnUiThread(() -> speakNow(pendingText, pendingLocale));
        }
        @JavascriptInterface public void pause() { runOnUiThread(() -> { if(tts!=null) tts.stop(); }); }
        @JavascriptInterface public void resume() { runOnUiThread(() -> { if(!pendingText.isEmpty()) speakNow(pendingText,pendingLocale); }); }
        @JavascriptInterface public void stop() { runOnUiThread(() -> { if(tts!=null) tts.stop(); pendingText=""; }); }
    }

    @Override protected void onDestroy() { if(tts!=null){tts.stop();tts.shutdown();} if(web!=null)web.destroy(); super.onDestroy(); }
    @Override public void onBackPressed() { if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
