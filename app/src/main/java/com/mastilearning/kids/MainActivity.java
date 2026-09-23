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

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        web = new WebView(this);

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        web.setWebViewClient(new WebViewClient());

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setSpeechRate(0.78f);
                tts.setPitch(1.25f);

                if (!pendingText.isEmpty()) {
                    speakNow(pendingText, pendingLocale);
                }
            }
        });

        web.addJavascriptInterface(new TTSBridge(), "AndroidTTS");

        web.loadUrl("file:///android_asset/index.html");

        setContentView(web);
    }

    private void speakNow(String text, Locale locale) {
        int result = tts.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {

            if ("hi".equals(locale.getLanguage())) {
                tts.setLanguage(new Locale("hi", "IN"));
            } else {
                tts.setLanguage(Locale.US);
            }
        }

        tts.setSpeechRate(0.78f);
        tts.setPitch(1.25f);

        tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "masti-story"
        );
    }

    private class TTSBridge {

        @JavascriptInterface
        public void speak(String text, String lang) {
            pendingText = text;

            if (lang != null && lang.startsWith("hi")) {
                pendingLocale = new Locale("hi", "IN");
            } else {
                pendingLocale = Locale.US;
            }

            runOnUiThread(() -> {
                speakNow(pendingText, pendingLocale);
            });
        }

        @JavascriptInterface
        public void pause() {
            runOnUiThread(() -> {
                if (tts != null) {
                    tts.stop();
                }
            });
        }

        @JavascriptInterface
        public void resume() {
            runOnUiThread(() -> {
                if (!pendingText.isEmpty()) {
                    speakNow(pendingText, pendingLocale);
                }
            });
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(() -> {
                if (tts != null) {
                    tts.stop();
                }
                pendingText = "";
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (web != null) {
            web.destroy();
        }

        super.onDestroy();
    }
}
