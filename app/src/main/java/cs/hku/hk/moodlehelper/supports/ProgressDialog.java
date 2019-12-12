/**
 * This is for showing a dialog to user when a background connection is in progress
 */
package cs.hku.hk.moodlehelper.supports;

import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import cs.hku.hk.moodlehelper.R;

public class ProgressDialog
{
    private AlertDialog alertDialog;
    private View rootView;

    /**
     * Constructor, to build a ProcessingDialog instance for further usage
     * @param rootView The view a processing dialog should be bound to
     */
    public ProgressDialog(View rootView)
    {
        this.rootView = rootView;
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
        builder.setView(View.inflate(this.rootView.getContext(), R.layout.progress_dialog,null));
        alertDialog = builder.create();
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
    }

    /**
     * Display the dialog in the context
     */
    public void show()
    {
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    /**
     * Dismiss the dialog
     */
    public void dismiss()
    {
        alertDialog.cancel();
    }

}
