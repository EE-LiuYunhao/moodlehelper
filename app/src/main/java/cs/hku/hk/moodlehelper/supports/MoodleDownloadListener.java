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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

import cs.hku.hk.moodlehelper.R;

public class MoodleDownloadListener implements DownloadListener
{
    private Context root;
    private View rootView;
    private ProgressDialog progressDialog;
    public MoodleDownloadListener(View rootView)
    {
        this.rootView = rootView;
        root = rootView.getContext();

        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        root.registerReceiver(receiver, intentFilter);
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
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    assert downloadManager != null;
                    String type = downloadManager.getMimeTypeForDownloadedFile(downloadId);

                    if (TextUtils.isEmpty(type))
                    {
                        type = "*/*";
                    }

                    Uri uri = downloadManager.getUriForDownloadedFile(downloadId);
                    if (uri != null)
                    {
                        Intent handlerIntent = new Intent(Intent.ACTION_VIEW);
                        handlerIntent.setDataAndType(uri, type);
                        context.startActivity(handlerIntent);
                    }
                }
            }
        }
    }
}
