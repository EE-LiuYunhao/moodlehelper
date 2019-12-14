package cs.hku.hk.moodlehelper.supports;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;

import cs.hku.hk.moodlehelper.R;

public class MoodleDownloadListener implements DownloadListener
{
    private Context root;
    private View rootView;
    private AppCompatActivity appActivity;
    private ProgressDialog progressDialog;
    private DownloadCompleteReceiver receiver;
    private LongSparseArray<String> fileCollection;

    static final private int REQUEST_WRITE_PERMISSION=1;
    static final private int REQUEST_READ_PERMISSION=2;

    public MoodleDownloadListener(View rootView, AppCompatActivity appActivity)
    {
        this.rootView = rootView;
        root = rootView.getContext();
        this.appActivity = appActivity;

        receiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        root.registerReceiver(receiver, intentFilter);
        fileCollection = new LongSparseArray<>();
    }

    public void unregisterReceiver()
    {
        try
        {
            root.unregisterReceiver(receiver);
        }
        catch(IllegalArgumentException e)
        {
            Log.e("Error caught", "Cannot unregister the receiver:\n");
            e.printStackTrace();
        }
    }


    @Override
    public void onDownloadStart(String url,
                                String userAgent,
                                String contentDisposition,
                                String mimetype,
                                long contentLength)
    {
        progressDialog = new ProgressDialog(rootView);
        progressDialog.show();

        //check for permission
        int permission = ActivityCompat.checkSelfPermission(appActivity.getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            boolean is=ActivityCompat.shouldShowRequestPermissionRationale(appActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(is)
            {
                appActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
            else
            {
                progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(root);
                builder.setTitle(R.string.no_download_permission)
                        .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
        }
        else
            downloadBySystem(url,contentDisposition,mimetype);
    }

    /**
     * Use the system DownloadManager to assist downloading
     *
     * @param url the URL of the resources to be downloaded
     * @param contentDisposition Content-Disposition HTTP header or {@code null}
     * @param mimeType Mime-type of the content
     * @see <a href="https://www.jianshu.com/p/6e38e1ef203a>Reference</a> The article provides a clear guidance.
     */
    private void downloadBySystem(String url, String contentDisposition, String mimeType)
    {
        //specifying the download URL
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        String cookies = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("cookie",cookies);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDescription(root.getString(R.string.download_prompt));

        //config for downloading network
        request.setAllowedOverMetered(true);
        request.setVisibleInDownloadsUi(true);
        request.setAllowedOverRoaming(true);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);

        String fileName  = URLUtil.guessFileName(url, contentDisposition, mimeType);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/moodle/"+fileName);

        // request the service
        final DownloadManager downloadManager = (DownloadManager)root.getSystemService(Context.DOWNLOAD_SERVICE);

        // add a new download request in queue
        assert downloadManager != null;
        long downloadId = downloadManager.enqueue(request);
        Log.d("downloadId:{}", downloadId+"");
        fileCollection.append(downloadId,fileName);
    }

    private class DownloadCompleteReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent != null)
            {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction()))
                {
                    progressDialog.dismiss();

                    long [] pattern = {100,100};
                    Vibrator vibrator = (Vibrator)root.getSystemService(Context.VIBRATOR_SERVICE);
                    assert vibrator != null;
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern,-1));

                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/moodle/";
                    filePath += fileCollection.get(downloadId);

                    //Dynamic request for permission
                    int permission = ActivityCompat.checkSelfPermission(appActivity.getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED)
                    {
                        boolean is=ActivityCompat.shouldShowRequestPermissionRationale(appActivity,Manifest.permission.READ_EXTERNAL_STORAGE);
                        if(is)
                        {
                            appActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
                        }
                    }

                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    {
                        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                        assert downloadManager != null;
                        String type = downloadManager.getMimeTypeForDownloadedFile(downloadId);
                        Intent openFile = openDownloadedFile(type, filePath);
                        context.startActivity(openFile);
                    }
                }
            }
        }
    }

    private Intent openDownloadedFile(String type, String filePath)
    {
        File downloadFile = new File(filePath);
        Uri uriFile = FileProvider.getUriForFile(root, "cs.hku.hk", downloadFile);

        Intent openFile = new Intent("android.intent.action.VIEW");
        openFile.addCategory("android.intent.category.DEFAULT");
        openFile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openFile.setDataAndType(uriFile, type);
        openFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return openFile;
    }
}
