package cs.hku.hk.moodlehelper.supports;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import java.util.Objects;

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
        final SharedPreferences coursePC = root.getSharedPreferences("PriorityCategory", Context.MODE_PRIVATE);
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
                        if(mAdapter instanceof CourseCardBaseAdapter)
                            deleteCourse(courseUrls, courseTitles, coursePC, mAdapter, name, ((CourseCardBaseAdapter)mAdapter).getCoursePosition(name));
                        else
                            deleteCourse(courseUrls, courseTitles, coursePC, mAdapter, name, -1);
                    }
                });

        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        Objects.requireNonNull(window).setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.dialog_anim);
        return dialog;
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
     * @param coursePC shared preference storing course priorities & categories
     * @param mAdapter the adapter for the list in which the course items are displayed
     * @param name the name (identifier) of the course to be modified
     */
    static private void deleteCourse(@NonNull SharedPreferences courseUrls,
                                     @NonNull SharedPreferences courseTitles,
                                     @NonNull SharedPreferences coursePC,
                                     @Nullable final RecyclerView.Adapter<?> mAdapter,
                                     @NonNull final String name,
                                     final int position)
    {
        SharedPreferences.Editor editor = courseUrls.edit();
        editor.remove(name);
        editor.apply();

        editor = courseTitles.edit();
        editor.remove(name);
        editor.apply();

        editor = coursePC.edit();
        int originalC = coursePC.getInt(name,0)%10;
        editor.putInt(name,originalC); //clear the P part;
        editor.apply();

        if(mAdapter instanceof CourseCardBaseAdapter && position >= 0)
        {
            ((CourseCardBaseAdapter)mAdapter).removeFromCourseList(position);
        }

        if(mAdapter!=null && position >= 0)
        {
            mAdapter.notifyItemRemoved(position);
        }
    }

    /**
     * Delete the course from the list
     * @param root the context where the list is displayed
     * @param mAdapter the adapter for the list in which the course items are displayed
     * @param courseName the name (identifier) of the course to be deleted
     */
    static void deleteCourse(@NonNull final Context root,
                             @Nullable final RecyclerView.Adapter<?> mAdapter,
                             @NonNull final String courseName)
    {
        final SharedPreferences courseUrls = root.getSharedPreferences("courses", Context.MODE_PRIVATE);
        final SharedPreferences courseTitles = root.getSharedPreferences("names", Context.MODE_PRIVATE);
        final SharedPreferences coursePC = root.getSharedPreferences("PriorityCategory", Context.MODE_PRIVATE);

        if(mAdapter instanceof CourseCardBaseAdapter)
            deleteCourse(courseUrls, courseTitles, coursePC, mAdapter, courseName, ((CourseCardBaseAdapter)mAdapter).getCoursePosition(courseName));
        else
            deleteCourse(courseUrls, courseTitles, coursePC, mAdapter, courseName, -1);
    }

    /**
     * Store the course category
     * @param root the context where the list is displayed
     * @param mAdapter the adapter for the list in which the course items are displayed
     * @param courseName the adapter for the list in which the course items are displayed
     * @param category the category (an integer in [0,9]) of the course to be modified
     */
    static void setCourseCategory(@NonNull final Context root,
                                  @Nullable final RecyclerView.Adapter<?> mAdapter,
                                  @NonNull final String courseName,
                                  final int category)
    {
        final SharedPreferences sp = root.getSharedPreferences("PriorityCategory",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        int originalCategory = sp.getInt(courseName, 0);
        originalCategory -= originalCategory % 10; //only keep the priority part
        originalCategory += category;

        editor.putInt(courseName, originalCategory);
        editor.apply();

        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    /**
     * Store the course priority into shared preference
     * @param sp the share preference to store the priority
     * @param courseName the name (identifier) of the course to be modified
     * @param priority the priority of the course
     */
    static private void setCoursePriority(@NonNull final SharedPreferences sp,
                                          @NonNull final String courseName,
                                          final int priority)
    {
        SharedPreferences.Editor editor = sp.edit();

        int originalPriority = sp.getInt(courseName, 0);
        originalPriority %= 10; //only keep the category part
        originalPriority += 10*priority;

        editor.putInt(courseName, originalPriority);
        editor.apply();
    }

    static void resetCoursePriority(@NonNull final Context root,
                                    @Nullable final String [] names)
    {
        if(names==null) return;
        final SharedPreferences sp = root.getSharedPreferences("PriorityCategory",Context.MODE_PRIVATE);
        for(int i=0; i<names.length; i++)
        {
            setCoursePriority(sp, names[i], names.length-i);
        }
    }
}
