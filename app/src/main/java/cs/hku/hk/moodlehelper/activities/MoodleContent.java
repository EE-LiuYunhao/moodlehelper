package cs.hku.hk.moodlehelper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.supports.MoodleDownloadListener;

public class MoodleContent extends AppCompatActivity
{
    private URL courseURL;
    private String uid;
    private String pin;
    private WebView webView;
    private ProgressBar bar;
    MoodleDownloadListener mListener;

    private String jstr;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        String courseName;

        setContentView(R.layout.activity_moodlecontent);

        Intent intent = getIntent();
        courseName=intent.getStringExtra("courseName");
        courseURL=(URL)intent.getSerializableExtra("url");
        uid=intent.getStringExtra("uid");
        pin=intent.getStringExtra("pin");

        //set the toolbar
        Toolbar toolbar = findViewById(R.id.settingsActivityToolBar);
        toolbar.setLogo(R.drawable.ic_main_activity_logo);
        TextView titleName = findViewById(R.id.courseName);
        titleName.setText(courseName);
        setSupportActionBar(toolbar);
        //toolbar navigate
        toolbar.setNavigationIcon(R.drawable.ic_navigate_before_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d("ActivityDebug","Navigation on click event");
                Intent returnHome = new Intent();
                setResult(RESULT_OK, returnHome);
                finish();
            }
        });

        bar = findViewById(R.id.progressbar);
        bar.setMax(100);
        bar.setVisibility(View.VISIBLE);

        loadJavaScript();

        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDefaultTextEncodingName("utf-8");
        webView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                if(newProgress<100)
                {
                    bar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                bar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                bar.setVisibility(View.GONE);
                //evaluate javascript segments
                webView.evaluateJavascript(jstr, new ValueCallback<String>()
                {
                    @Override public void onReceiveValue(String value)
                    {
                        Log.d("JS", value);
                        if(value.equals("\"Success at clearViewElements()\""))
                        {
                            webView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        mListener = new MoodleDownloadListener(webView,this);
        webView.setDownloadListener(mListener);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        webView.loadUrl(courseURL.toString());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        webView.pauseTimers();
        mListener.unregisterReceiver();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        webView.resumeTimers();
    }

    @Override
    protected void onDestroy()
    {
        if (webView != null)
        {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();

            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK && webView.canGoBack())
        {
            webView.goBack();
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * load JavaScript as jstr
     * @see <a href=https://blog.csdn.net/yyanjun/article/details/80353766>Reference</a> The article provides a clear guidance.
     */
    private void loadJavaScript()
    {
        try
        {
            InputStream in = getAssets().open("assist.js");
            byte[] buff = new byte[1024];
            ByteArrayOutputStream fromFile = new ByteArrayOutputStream();
            do
            {
                int numRead = in.read(buff);
                if (numRead <= 0)
                {
                    break;
                }
                fromFile.write(buff, 0, numRead);
            } while (true);
            jstr = fromFile.toString();
            in.close();
            fromFile.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        jstr += "switchURL(\""+uid+"\", \""+pin+"\"); ";
    }
}
