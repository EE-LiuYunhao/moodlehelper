package cs.hku.hk.moodlehelper.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Map;

import cs.hku.hk.moodlehelper.R;

/**
 * Adapter for the recycler view in the main activity
 */
public class CourseCardButtonAdapter extends CourseCardBaseAdapter
{
    /**
     * Constructor for the CourseCardBaseAdapter
     *
     * @param root The root view where the recycler view is located in
     */
    public CourseCardButtonAdapter(View root)
    {
        super(root);
    }

    // inflates the cell layout from xml when needed
    @Override
    public @NonNull
    ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = mInflater.inflate(R.layout.course_card, parent, false);
        return new ViewHolder(itemView);
    }

    // binds the data to the text view in each cell
    @Override
    public void onBindViewHolder(@NonNull CourseCardBaseAdapter.ViewHolder holder, int position)
    {
        String tempURLStr = courses.get(position).courseURL.toString();
        int idStart = tempURLStr.indexOf("id=");

        holder.courseName.setText(courses.get(position).courseName);
        holder.courseId.setText(tempURLStr.substring(idStart));
    }

    /**
     * This class bind the onItemClick method defined in CourseCardBaseAdapter.ItemClickListener
     *   with the card view.
     */
    public class ViewHolder extends CourseCardBaseAdapter.ViewHolder implements View.OnClickListener
    {
        Button btn;

        ViewHolder(View itemView)
        {
            super(itemView, R.id.courseTitle, R.id.courseId);

            btn = itemView.findViewById(R.id.course_button);
            btn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mClickListener != null) mClickListener.onItemClick(v, courses.get(getAdapterPosition()).courseName);
                }
            });
        }

        @Override
        public void onClick(View view)
        {
            //The card responses to no onClick behaviour
        }
    }

    @Override
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
                alertNoCourses();
            }
        }
        else
        {
            alertNoCourses();
        }
    }

    private void alertNoCourses()
    {
        Toast.makeText(rootView.getContext(), R.string.no_course, Toast.LENGTH_SHORT).show();
    }
}
