package cs.hku.hk.moodlehelper.supports;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cs.hku.hk.moodlehelper.R;

/**
 * Adapter for the recycler view in the setting activity
 */
public class CourseCardBaseAdapter extends RecyclerView.Adapter<CourseCardBaseAdapter.ViewHolder>
{
    ItemClickListener mClickListener;
    LayoutInflater mInflater;
    View rootView;
    List<Course> courses;
    private int bottomMargin = 15;

    /**
     * Constructor for the CourseCardBaseAdapter
     *
     * @param root The root view where the recycler view is located in
     */
    public CourseCardBaseAdapter(View root)
    {
        mInflater = LayoutInflater.from(root.getContext());
        rootView = root;

        courses = new ArrayList<>();
    }

    // inflates the cell layout from xml when needed
    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = mInflater.inflate(R.layout.course_settings_card, parent, false);
        return new ViewHolder(itemView);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        String tempCourseName = courses.get(position).courseName;
        holder.courseName.setText(tempCourseName);
        holder.courseTitle.setText(courses.get(position).courseTitle);

        if(position==courses.size()-1)
        {
            ViewGroup.MarginLayoutParams itemMargin = (ViewGroup.MarginLayoutParams) holder.item.getLayoutParams();
            bottomMargin = itemMargin.bottomMargin;
            itemMargin.bottomMargin = 170;
            holder.item.setLayoutParams(itemMargin);
        }
    }

    @Override
    public int getItemCount()
    {
        return courses.size();
    }

    private void alertNoCourses()
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
     * This class bind the onItemClick method defined in CourseCardBaseAdapter.ItemClickListener
     *   with the card view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView courseName;
        TextView courseTitle;
        View item;

        /**
         * Constructor for the view holder, binding the view with variables
         *
         * @param itemView each card view
         */
        ViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            item = itemView;

            courseName = itemView.findViewById(R.id.course_setting_name);
            courseTitle = itemView.findViewById(R.id.course_setting_title);
        }

        /**
         * Constructor for the view holder, binding the view with variables,
         *   The views for the course name and course id are specified.
         *
         * @param itemView each card view
         * @param courseNameId the ResID to the view of course name within the card
         * @param courseIdId the ResID to the view of course id within the card
         */
        ViewHolder(View itemView, int courseNameId, int courseIdId)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            item = itemView;

            courseName = itemView.findViewById(courseNameId);
            courseTitle = itemView.findViewById(courseIdId);
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

    /**
     * Abstraction for each course. Converting course url string into URL
     */
    class Course
    {
        String courseName;
        URL courseURL;
        String courseTitle;
        int priority;

        Course(String courseName, String courseTitle, String urlStr)
        {
            this.priority = 0;
            this.courseName = courseName;
            this.courseTitle = courseTitle.equals("") || courseTitle.matches("[\t ]*")?rootView.getContext().getString(R.string.empty_course_title):courseTitle;
            try
            {
                this.courseURL = new URL(urlStr);
            }
            catch (MalformedURLException e)
            {
                Toast.makeText(rootView.getContext(),R.string.wrong_url,Toast.LENGTH_SHORT).show();
                mClickListener.onItemClick(rootView, courseName);
            }
        }

        Course(String courseName, String courseTitle, String urlStr, int priority)
        {
            this(courseName, courseTitle, urlStr);
            this.priority = priority;
        }
    }

    /**
     * Update the course list. The caller should also invoke the notify-data-set method explicitly.
     */
    public void refreshCourseList()
    {
        SharedPreferences sp = rootView.getContext().getSharedPreferences("courses", Context.MODE_PRIVATE);
        SharedPreferences titles = rootView.getContext().getSharedPreferences("names", Context.MODE_PRIVATE);
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
                    courses.add(new Course(entry.getKey(), titles.getString(entry.getKey(),""),courseUrlStr));
                }
            }
            else
            {
                alertNoCourses();
            }
        }
        else
        {
            alertNoCourses();
        }
    }

    public @Nullable URL getURLByCourseName(String courseName)
    {
        for(Course each : courses)
        {
            if(each.courseName.equals(courseName))
            {
                return each.courseURL;
            }
        }
        return null;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder)
    {
        if(holder.item!=null)
        {
            ViewGroup.MarginLayoutParams itemMargin = (ViewGroup.MarginLayoutParams) holder.item.getLayoutParams();
            itemMargin.bottomMargin = bottomMargin;
            holder.item.setLayoutParams(itemMargin);
        }
        super.onViewRecycled(holder);
    }
}
