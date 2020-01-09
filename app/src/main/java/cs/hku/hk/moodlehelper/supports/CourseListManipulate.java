package cs.hku.hk.moodlehelper.supports;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;

import cs.hku.hk.moodlehelper.R;

public class CourseListManipulate
{
    /**
     * Pop up a dialog for user to edit the course info
     * @param root the current context
     * @param mAdapter the adapter for the list in which the course items are displayed
     * @param name the name (identifier) of the course to be modified
     * @return the dialog object. Remember to call .show() on it to display the dialog
     */
    static public AlertDialog editCourseItem(@NonNull final Context root, @Nullable final RecyclerView.Adapter<?> mAdapter, @NonNull final String name)
    {
        final View addCourseView = View.inflate(root, R.layout.edit_course, null);
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(root);
        builder.setView(addCourseView);

        final EditText courseName = addCourseView.findViewById(R.id.edit_course_name);
        final EditText courseUrl = addCourseView.findViewById(R.id.edit_course_url);
        final EditText courseTitle = addCourseView.findViewById(R.id.edit_course_title);

        final SharedPreferences courseUrls = root.getSharedPreferences("courses", Context.MODE_PRIVATE);
        final SharedPreferences courseTitles = root.getSharedPreferences("names", Context.MODE_PRIVATE);
        final String urlStr = courseUrls.getString(name, root.getString(R.string.example_course_url));
        final String titleStr = courseTitles.getString(name, root.getString(R.string.example_course_title));

        courseName.setText(name);
        courseName.setEnabled(false);
        courseUrl.setText(urlStr);
        courseTitle.setText(titleStr);

        builder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String courseUrlStr = courseUrl.getText().toString();
                        String courseTitleStr = courseTitle.getText().toString();

                        updateCourseList(courseUrls, courseTitles, mAdapter, courseUrlStr, courseTitleStr, name);

                    }
                }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                }).setNeutralButton(R.string.delete_course_setting,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteCourse(courseUrls, courseTitles, mAdapter, name);
                    }
                });

        return builder.create();
    }

    /**
     * Store the updated info into shared preferences
     * @param courseUrls shared preference storing course url
     * @param courseTitles shared preference storing course titles
     * @param mAdapter the adapter for the list in which the course items are displayed
     * @param newCourseUrl new course url
     * @param newCourseTitle new course title
     * @param name the name (identifier) of the course to be modified
     */
    static private void updateCourseList(@NonNull SharedPreferences courseUrls,
                                         @NonNull SharedPreferences courseTitles,
                                         @Nullable final RecyclerView.Adapter<?> mAdapter,
                                         @Nullable String newCourseUrl,
                                         @Nullable String newCourseTitle,
                                         @NonNull final String name)
    {
        SharedPreferences.Editor editor = courseUrls.edit();
        if(newCourseUrl!=null)
        {
            editor.putString(name,newCourseUrl);
            editor.apply();
        }

        editor = courseTitles.edit();
        if(newCourseTitle!=null)
        {
            editor.putString(name, newCourseTitle);
            editor.apply();
        }

        if(mAdapter instanceof CourseCardBaseAdapter)
            ((CourseCardBaseAdapter)mAdapter).refreshCourseList();
        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    /**
     * Remove the course info entry from the shared preferences
     * @param courseUrls shared preference storing course url
     * @param courseTitles shared preference storing course titles
     * @param mAdapter the adapter for the list in which the course items are displayed
     * @param name the name (identifier) of the course to be modified
     */
    static private void deleteCourse(@NonNull SharedPreferences courseUrls,
                                     @NonNull SharedPreferences courseTitles,
                                     @Nullable final RecyclerView.Adapter<?> mAdapter,
                                     @NonNull final String name)
    {
        SharedPreferences.Editor editor = courseUrls.edit();
        editor.remove(name);
        editor.apply();

        editor = courseTitles.edit();
        editor.remove(name);
        editor.apply();

        if(mAdapter instanceof CourseCardBaseAdapter)
            ((CourseCardBaseAdapter)mAdapter).refreshCourseList();
        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();
    }

    /**
     * Delete the course from the list
     * @param root the context where the list is displayed
     * @param mAdapter the adapter for the list in which the course items are displayed
     * @param courseName the name (identifier) of the course to be modified
     */
    static void deleteCourse(@NonNull final Context root,
                                    @Nullable final RecyclerView.Adapter<?> mAdapter,
                                    @NonNull final String courseName)
    {
        final SharedPreferences courseUrls = root.getSharedPreferences("courses", Context.MODE_PRIVATE);
        final SharedPreferences courseTitles = root.getSharedPreferences("names", Context.MODE_PRIVATE);

        deleteCourse(courseUrls, courseTitles, mAdapter, courseName);
    }
}
