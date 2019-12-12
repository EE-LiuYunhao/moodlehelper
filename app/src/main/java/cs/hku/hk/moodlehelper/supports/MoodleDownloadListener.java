package cs.hku.hk.moodlehelper.supports;

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
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.widget.Toast;

import cs.hku.hk.moodlehelper.R;

public class MoodleDownloadListener implements DownloadListener
{
    private Context root;
    private View rootView;
    private ProgressDialog progressDialog;
    private DownloadCompleteReceiver receiver;
    private LongSparseArray<String> fileCollection;

    public MoodleDownloadListener(View rootView)
    {
        this.rootView = rootView;
        root = rootView.getContext();

        receiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        root.registerReceiver(receiver, intentFilter);
        fileCollection = new LongSparseArray<>();
    }

    public void unregisterReceiver()
    {
        root.unregisterReceiver(receiver);
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
        PackageManager pm = root.getPackageManager();
        boolean permission_writeStorage = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", root.getPackageName()));
        if(!permission_writeStorage)
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

                    Toast.makeText(context, context.getString(R.string.file_save_to)+filePath, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
