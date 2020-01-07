package cs.hku.hk.moodlehelper.supports;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cs.hku.hk.moodlehelper.R;

/**
 * The class is for automatically load course data from remote website.
 * Instance the class inside an OnClickListener, and then call the execute method.
 * Destroy it after usage.
 */
public class WebExtension extends WebViewClient
{
    private Context rootContext;
    private WebView webView;
    private WebSettings settings;

    private String userName;
    private String userPIN;
    private ProgressDialog syncingDialog;
    private String jstr;

    /**
     * Constructor
     * @param rootContext the UI context in which the WebExtension is invoked
     * @param dialogView the view in which the Processing Dialog should show
     */
    public WebExtension(Context rootContext, View dialogView)
    {
        this.rootContext = rootContext;
        SharedPreferences sp = rootContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        userName = sp.getString("portalID", "");
        userPIN  = sp.getString("portalPIN","");
        loadJavaScript();

        webView = new WebView(rootContext);
        webView.setVisibility(View.GONE);
        settings = webView.getSettings();
        configWebView();

        syncingDialog = new ProgressDialog(dialogView, R.string.currently_sync);
    }

    /**
     * Configuration for the web view component to ensure the callbacks and JavaScript execution
     */
    private void configWebView()
    {
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(false);
        settings.setDefaultTextEncodingName("utf-8");

        //for the Client
        webView.setWebViewClient(this);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
    {
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    /**
     * The function listen to the WebView loading result and try to parse the
     * JSON string in the result so that they can be stored in SharedPreference.
     * @param view The WebView widget which it listens to
     * @param url The web page that is just loaded
     */
    @Override
    public void onPageFinished(WebView view, String url)
    {
        super.onPageFinished(view, url);
        syncingDialog.dismiss();
        //evaluate javascript segments
        webView.evaluateJavascript(jstr, new ValueCallback<String>()
        {
            @Override public void onReceiveValue(String value)
            {
                if(value.matches("\\[.*]"))
                {
                    syncingDialog.dismiss();
                    try
                    {
                        JSONArray courseArray = new JSONArray(value);
                        handleJSONArray(courseArray);
                    }
                    catch (JSONException e){/*skip*/}
                    destroy();
                }

                Log.d("JS", value);
            }
        });
    }

    /**
     * load JavaScript as jstr
     * @see <a href=https://blog.csdn.net/yyanjun/article/details/80353766>Reference</a> The article provides a clear guidance.
     */
    private void loadJavaScript()
    {
        try
        {
            InputStream in = rootContext.getAssets().open("sync.js");
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
        jstr += "switchURL(\""+userName+"\", \""+userPIN+"\"); ";
    }

    /**
     * Start the syncing
     */
    public void execute()
    {
        syncingDialog.show();
        webView.loadUrl("hkuportal.hku.hk/login.html");
    }

    /**
     * Destroy the WebView instance, delete the cache and remove it
     */
    private void destroy()
    {
        if (webView != null)
        {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();

            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
    }

    /**
     * Deal with JSON array to store them into shared preferences
     * @param array JSON array to be dealt with
     * @throws JSONException
     */
    private void handleJSONArray(JSONArray array) throws JSONException
    {
        if(array==null || array.length()==0) return;

        SharedPreferences spCourses = rootContext.getSharedPreferences("courses", Context.MODE_PRIVATE);
        SharedPreferences spNames = rootContext.getSharedPreferences("names", Context.MODE_PRIVATE);
        for(int i=0; i<array.length(); i++)
        {
            JSONObject courseItem = array.getJSONObject(i);
            String courseName = courseItem.getString("course_name");
            if(spCourses.getString(courseName, "").equals(""))
            {
                SharedPreferences.Editor editor = spCourses.edit();
                editor.putString(courseName, courseItem.getString("course_url"));
                editor.apply();

                editor = spNames.edit();
                editor.putString(courseName, "*"+courseItem.getString("course_title"));
            }
            //else: already existing in shared preferences
        }
    }
}
