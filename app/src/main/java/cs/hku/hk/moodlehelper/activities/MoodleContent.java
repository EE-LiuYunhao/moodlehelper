package cs.hku.hk.moodlehelper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

import cs.hku.hk.moodlehelper.R;

public class MoodleContent extends AppCompatActivity
{
    private String courseName;
    private URL courseURL;
    private String uid;
    private String pin;
    private WebView webView;
    private WebViewClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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

        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        //TODO: include JS code here
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDefaultTextEncodingName("utf-8");
        webView.canGoForward();
        webView.canGoBack();
        webView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                ProgressBar bar = findViewById(R.id.progressbar);
                if(newProgress<100)
                {
                    bar.setProgress(newProgress);
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //DEBUG only
        String builder = "DEBUG USERNAME:[" + uid + "] PASSWORD:[" + pin + ']' +
                '\n' + courseURL;
        Toast.makeText(this,"DEBUG: "+ builder,Toast.LENGTH_SHORT).show();
        webView.loadUrl(courseURL.toString());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK && webView.canGoBack())
        {
            //TODO: if current URL == income, cannot go back
            webView.goBack();
        }
        return super.onKeyDown(keyCode, event);
    }
}
