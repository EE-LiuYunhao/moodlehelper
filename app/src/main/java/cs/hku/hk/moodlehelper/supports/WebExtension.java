package cs.hku.hk.moodlehelper.supports;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs.hku.hk.moodlehelper.R;

/**
 * The class is for automatically load course data from remote website.
 * Instance the class inside an OnClickListener, and then call the execute method.
 * Destroy it after usage.
 */
public class WebExtension extends WebViewClient
{
    private static final int WEBVIEW_TIME_OUT = 1;

    private Context rootContext;
    private RecyclerView updatedView;
    private WebView webView;
    private WebSettings settings;

    private String userName;
    private String userPIN;
    private ProgressDialog syncingDialog;
    private String jstr;

    private static class MyHandler extends android.os.Handler
    {
        private WebExtension extension;

        MyHandler(WebExtension extension)
        {
            this.extension = extension;
        }

        @Override
        public void handleMessage(@NonNull Message msg)
        {
            if(msg.what == WEBVIEW_TIME_OUT)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(extension.rootContext);
                builder.setTitle(R.string.load_failure)
                       .setMessage(R.string.network_problem)
                       .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });
                extension.destroy();
                builder.create().show();
            }
        }
    }
    private MyHandler myHandler;

    /**
     * Constructor
     * @param rootContext the UI context in which the WebExtension is invoked
     * @param updatedView the view to be updated
     */
    public WebExtension(Context rootContext, RecyclerView updatedView)
    {
        this.rootContext = rootContext;
        this.updatedView = updatedView;
        SharedPreferences sp = rootContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        userName = sp.getString("portalID", "");
        userPIN  = sp.getString("portalPIN","");
        loadJavaScript();

        webView = new WebView(rootContext);
        webView.setVisibility(View.GONE);
        settings = webView.getSettings();
        configWebView();

        syncingDialog = new ProgressDialog(updatedView, R.string.currently_sync);
        syncingDialog.setAutoDismiss(false);

        myHandler = new MyHandler(this);
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
        final Pattern pattern = Pattern.compile("\\[.*]");
        //evaluate javascript segments
        webView.evaluateJavascript(jstr, new ValueCallback<String>()
        {
            @Override public void onReceiveValue(String value)
            {
                Matcher m = pattern.matcher(value);
                if(m.find())
                {
                    syncingDialog.dismiss();
                    try
                    {
                        String sub = m.group().replace("\\\"","\"");
                        JSONArray courseArray = new JSONArray(sub);
                        handleJSONArray(courseArray);
                    }
                    catch (JSONException e){/*skip*/}
                    destroy();

                    ((CourseCardBaseAdapter) Objects.requireNonNull(updatedView.getAdapter())).refreshCourseList();
                    updatedView.getAdapter().notifyDataSetChanged();
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
        if(isInternetConnected(rootContext) && !userPIN.equals("") && !userName.equals(""))
        {
            syncingDialog.show();
            webView.loadUrl("https://hkuportal.hku.hk/login.html");
            final Thread timeCounter = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    long timeOfDraw = System.currentTimeMillis();
                    while(syncingDialog.isShowing())
                    {
                        if(System.currentTimeMillis()-timeOfDraw >= 20000)
                        {
                            myHandler.sendEmptyMessage(WEBVIEW_TIME_OUT);
                            break;
                        }
                    }
                }
            });
            timeCounter.start();
        }
        else if(userPIN.equals("") || userName.equals(""))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(rootContext);
            builder.setTitle(R.string.UID_PIN_problem)
                    .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            destroy();
            builder.create().show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(rootContext);
            builder.setTitle(R.string.load_failure)
                   .setMessage(R.string.network_problem)
                   .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                   {
                       @Override
                       public void onClick(DialogInterface dialog, int which)
                       {
                           dialog.cancel();
                       }
                   });
            destroy();
            builder.create().show();
        }
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
            webView.destroy();
            webView = null;
        }
    }

    /**
     * Deal with JSON array to store them into shared preferences
     * @param array JSON array to be dealt with
     * @throws JSONException when the parsed JSON array fails in retrieving data
     */
    private void handleJSONArray(JSONArray array) throws JSONException
    {
        if(array==null || array.length()==0) return;
        myHandler.removeCallbacksAndMessages(null);

        SharedPreferences spCourses = rootContext.getSharedPreferences("courses", Context.MODE_PRIVATE);
        SharedPreferences spNames = rootContext.getSharedPreferences("names", Context.MODE_PRIVATE);
        SharedPreferences spPriority = rootContext.getSharedPreferences("PriorityCategory", Context.MODE_PRIVATE);
        for(int i=0; i<array.length(); i++)
        {
            JSONObject courseItem = array.getJSONObject(i);
            String courseName = courseItem.getString("course_name");
            SharedPreferences.Editor editor = spCourses.edit();
            editor.putString(courseName, courseItem.getString("course_url"));
            editor.apply();

            editor = spNames.edit();
            editor.putString(courseName, "*"+courseItem.getString("course_title"));
            editor.apply();

            editor = spPriority.edit();
            int originalCategory = spPriority.getInt(courseName,0)%10;
            editor.putInt(courseName, i*10+originalCategory);
            editor.apply();
        }
    }

    public static boolean isInternetConnected(Context rootContext)
    {
        ConnectivityManager manager = (ConnectivityManager)rootContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager==null?null:manager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
