package cs.hku.hk.moodlehelper.supports;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;


import cs.hku.hk.moodlehelper.R;

/**
 * This is for showing a dialog to user when a background connection is in progress
 */
public class ProgressDialog
{
    private static final int DIALOG_TIME_OUT=1;

    private AlertDialog alertDialog;
    private View rootView;
    private long timeOfDraw;

    private ProgressView mProgressView;

    private MyHandler myHandler;

    private boolean autoDismiss = true;

    private static class MyHandler extends android.os.Handler
    {
        private ProgressDialog rootDialog;

        MyHandler(ProgressDialog rootDialog)
        {
            this.rootDialog = rootDialog;
        }

        @Override
        public void handleMessage(@NonNull Message msg)
        {
            if(msg.what == DIALOG_TIME_OUT)
                rootDialog.dismiss();
        }
    }


    /**
     * Constructor, to build a ProcessingDialog instance for further usage
     * @param rootView The view a processing dialog should be bound to
     */
    ProgressDialog(View rootView)
    {
        this.rootView = rootView;
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
        View dialogView = View.inflate(this.rootView.getContext(), R.layout.progress_dialog, null);
        builder.setView(dialogView);
        alertDialog = builder.create();


        mProgressView = dialogView.findViewById(R.id.progress_view_widget);
        myHandler = new MyHandler(this);
    }

    /**
     * Constructor, to build a ProcessingDialog instance with customized title
     * @param rootView The view a processing dialog should be bound to
     * @param resId The customized string title
     */
    public ProgressDialog(View rootView, int resId)
    {
        this.rootView = rootView;
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
        View dialogView = View.inflate(this.rootView.getContext(), R.layout.progress_dialog, null);
        TextView textView = dialogView.findViewById(R.id.download_progress_text);
        textView.setText(resId);
        builder.setView(dialogView);
        alertDialog = builder.create();

        mProgressView = dialogView.findViewById(R.id.progress_view_widget);
        myHandler = new MyHandler(this);
    }

    /**
     * Display the dialog in the context
     */
    void show()
    {
        alertDialog.setCancelable(false);
        alertDialog.show();
        mProgressView.startAnimation();

        timeOfDraw = System.currentTimeMillis();

        final Thread timeCounter = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(true)
                {
                    if(System.currentTimeMillis()-timeOfDraw >= 6000)
                        break;
                }

                if(autoDismiss)
                    myHandler.sendEmptyMessage(DIALOG_TIME_OUT);
            }
        });
        timeCounter.start();
    }

    /**
     * Dismiss the dialog
     */
    void dismiss()
    {
        mProgressView.stopAnimation();

        if(alertDialog.isShowing())
            alertDialog.cancel();
    }

    /**
     * Set whether the view can be dismissed automatically
     * @param autoDismiss determine whether dismissed automatically
     */
    public void setAutoDismiss(boolean autoDismiss)
    {
        this.autoDismiss = autoDismiss;
    }
}
