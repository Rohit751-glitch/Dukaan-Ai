package com.chhotuai.app;

import android.Manifest;
import android.app.Activity;
import android.print.PrintManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final int MIC_REQUEST = 44;
    private static final String PREFS = "chhotu_demo";
    private static final String KEY_URL = "server_url";
    private WebView webView;
    private PermissionRequest pendingWebPermission;
    private SharedPreferences prefs;
    private boolean loadingRemote = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new NativeBridge(), "Android");
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean wantsAudio = false;
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) wantsAudio = true;
                    }
                    if (!wantsAudio) { request.deny(); return; }
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else {
                        pendingWebPermission = request;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
                    }
                });
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return false;
            }
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (loadingRemote) {
                    view.evaluateJavascript("(function(){window.__CHHOTU_ANDROID__=true;})();", null);
                }
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame() && loadingRemote) {
                    loadingRemote = false;
                    showConnector("Could not reach the Chhotu server. Wake the laptop, make sure Chhotu + Cloudflare tunnel are running, then paste the current HTTPS tunnel URL.");
                }
            }
        });

        String saved = prefs.getString(KEY_URL, "");
        if (saved != null && saved.startsWith("https://")) loadRemote(saved);
        else showConnector("");
    }

    private void loadRemote(String url) {
        loadingRemote = true;
        webView.loadUrl(url);
    }

    private void showConnector(String error) {
        loadingRemote = false;
        String saved = prefs.getString(KEY_URL, "");
        String safeSaved = html(saved == null ? "" : saved);
        String safeError = html(error == null ? "" : error);
        String page = "<!doctype html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1,viewport-fit=cover,user-scalable=no'>" +
          "<style>*{box-sizing:border-box}body{margin:0;font-family:system-ui,-apple-system,sans-serif;background:linear-gradient(160deg,#f3f0ff,#f8fbfa);color:#211f2b;min-height:100vh;display:grid;place-items:center;padding:28px}.card{width:min(560px,100%);background:#fff;border-radius:28px;padding:30px;box-shadow:0 24px 70px rgba(31,27,52,.12)}.brand{display:flex;align-items:center;gap:14px;margin-bottom:28px}.logo{width:58px;height:58px;border-radius:18px;background:#6f5df4;position:relative;box-shadow:0 10px 28px rgba(111,93,244,.25)}.logo:after{content:'';position:absolute;inset:13px;border-radius:12px;background:#fff}.spark{position:absolute;right:-4px;top:4px;color:#ffd65c;z-index:2}h2{margin:0;font-size:28px}.sub{color:#8c8996;font-size:12px;letter-spacing:.12em}.title{font-size:38px;font-weight:800;line-height:1.02;margin:20px 0 12px}.desc{color:#7f7c88;line-height:1.55;margin-bottom:24px}label{display:block;font-size:12px;font-weight:800;letter-spacing:.08em;color:#888592;margin:10px 0}input{width:100%;padding:17px;border:1px solid #ddd9e8;border-radius:16px;font-size:16px;outline:none}input:focus{border-color:#6f5df4;box-shadow:0 0 0 3px rgba(111,93,244,.1)}button{width:100%;margin-top:14px;padding:17px;border:0;border-radius:16px;background:#211f2b;color:white;font-size:17px;font-weight:800}.hint{margin-top:18px;padding:14px;background:#f7f5fc;border-radius:14px;color:#6f6b79;font-size:13px;line-height:1.5}.err{color:#bd4b5e;margin-top:12px;font-size:13px}.tiny{text-align:center;color:#aaa6b1;font-size:11px;margin-top:18px}</style></head><body><div class='card'><div class='brand'><div class='logo'><span class='spark'>◆</span></div><div><h2>Chhotu<span style='color:#6f5df4'>_AI</span></h2><div class='sub'>DEMO BRIDGE</div></div></div><div class='title'>Connect to your working Chhotu.</div><div class='desc'>This APK opens the exact laptop version that already passed your real voice, approval and database tests.</div><label>SECURE CHHOTU URL</label><input id='u' value='"+safeSaved+"' placeholder='https://xxxxx.trycloudflare.com'><button onclick='go()'>Connect Chhotu</button>" +
          (safeError.isEmpty()?"":"<div class='err'>"+safeError+"</div>") +
          "<div class='hint'><b>On laptop:</b><br>1. Keep <code>./RUN_CHHOTU.sh</code> running.<br>2. Run <code>cloudflared tunnel --url http://localhost:3000</code>.<br>3. Paste the HTTPS <code>trycloudflare.com</code> URL here once.</div><div class='tiny'>URL is stored only on this phone. No Gemini or database secret is stored in the APK.</div></div><script>function go(){var u=document.getElementById('u').value.trim();if(!/^https:\/\//i.test(u)){alert('Paste the HTTPS tunnel URL');return;}Android.connect(u)}</script></body></html>";
        webView.loadDataWithBaseURL("https://local.chhotu.ai/", page, "text/html", "UTF-8", "https://local.chhotu.ai/");
    }

    private String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == MIC_REQUEST && pendingWebPermission != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                pendingWebPermission.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            } else pendingWebPermission.deny();
            pendingWebPermission = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else if (loadingRemote) showConnector("");
        else super.onBackPressed();
    }

    public class NativeBridge {
        @JavascriptInterface public void connect(String rawUrl) {
            runOnUiThread(() -> {
                try {
                    Uri uri = Uri.parse(rawUrl.trim());
                    if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                        showConnector("Use the HTTPS URL printed by Cloudflare tunnel.");
                        return;
                    }
                    String clean = rawUrl.trim();
                    prefs.edit().putString(KEY_URL, clean).apply();
                    loadRemote(clean);
                } catch (Exception e) {
                    showConnector("Invalid URL.");
                }
            });
        }

        @JavascriptInterface public void resetConnection() {
            runOnUiThread(() -> {
                prefs.edit().remove(KEY_URL).apply();
                showConnector("");
            });
        }

        @JavascriptInterface public void printCurrent() {
            runOnUiThread(() -> {
                PrintManager pm = (PrintManager)getSystemService(Context.PRINT_SERVICE);
                pm.print("Chhotu_AI", webView.createPrintDocumentAdapter("Chhotu_AI"), null);
            });
        }
    }
}
