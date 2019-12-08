package cs.hku.hk.moodlehelper.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cs.hku.hk.moodlehelper.R;

public class CourseSettingCardAdapter extends RecyclerView.Adapter<CourseSettingCardAdapter.ViewHolder>
{
    private ItemClickListener mClickListener;
    private LayoutInflater mInflater;
    private View rootView;
    private List<Course> courses;

    public CourseSettingCardAdapter(View root)
    {
        mInflater = LayoutInflater.from(root.getContext());
        rootView = root;
        SharedPreferences sp = root.getContext().getSharedPreferences("courses", Context.MODE_PRIVATE);

        courses = new ArrayList<>();
    }

    // inflates the cell layout from xml when needed
    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = mInflater.inflate(R.layout.course_settings_card, parent, false);
        return new ViewHolder(itemView);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        TextView courseName = rootView.findViewById(R.id.course_setting_name);
        TextView courseId = rootView.findViewById(R.id.course_setting_id);

        String tempURLStr = courses.get(position).courseURL.toString();
        int idStart = tempURLStr.indexOf("id=");

        courseName.setText(courses.get(position).courseName);
        courseId.setText(tempURLStr.substring(idStart));
    }

    @Override
    public int getItemCount()
    {
        return courses.size();
    }

    private void aleartNoCourses()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
        builder.setTitle(R.string.no_course);
        builder.setNeutralButton(R.string.confirm,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    /**
     * This class bind the onItemClick method defined in CourseSettingCardAdapter.ItemClickListener
     *   with the card view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null) mClickListener.onItemClick(view, courses.get(getAdapterPosition()).courseName);
        }
    }

    /**
     * Allow the caller class to set on click listener for all the items altogether via implementing
     * an ItemClickListener interface.
     * @param itemClickListener the object where the ItemClickListener is implemented and the onItem-
     *                          Click function is set.
     */
    public void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    /**
     * The interface via which an onClick listener can be implemented for the item in the list.
     */
    public interface ItemClickListener
    {
        /**
         * A holder for the function to override the super.onItemClick() for each item.
         * @param view The clicked view object
         * @param name The name of the selected item
         */
        void onItemClick(View view, String name);
    }

    private class Course
    {
        String courseName;
        URL courseURL;

        Course(String courseName, String urlStr)
        {
            this.courseName = courseName;
            try
            {
                this.courseURL = new URL(urlStr);
            }
            catch (MalformedURLException e)
            {
                mClickListener.onItemClick(rootView, courseName);
            }
        }
    }

    public void refreshCourseList()
    {
        SharedPreferences sp = rootView.getContext().getSharedPreferences("courses", Context.MODE_PRIVATE);
        courses.clear();

        if(sp != null)
        {
            Map<String, ?> spCourses = sp.getAll();
            if(spCourses!=null && spCourses.size()!=0)
            {
                for(Map.Entry<String, ?> entry : spCourses.entrySet())
                {
                    String courseUrlStr;
                    try
                    {
                        courseUrlStr = (String)entry.getValue();
                    }
                    catch(ClassCastException e)
                    {
                        continue;
                    }
                    courses.add(new Course(entry.getKey(),courseUrlStr));
                }
            }
            else
            {
                aleartNoCourses();
            }
        }
        else
        {
            aleartNoCourses();
        }
    }
}
