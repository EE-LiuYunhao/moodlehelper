package cs.hku.hk.moodlehelper.supports;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import cs.hku.hk.moodlehelper.R;

/**
 * Adapter for the recycler view in the main activity
 */
public class CourseCardButtonAdapter extends CourseCardBaseAdapter implements CourseCardMover.CourseCardMoverAdapter
{

    private int cardWidth;

    private final static int MAJOR_COURSE = 1;
    private final static int MINOR_COURSE = 2;
    private final static int CC_COURSE    = 3;
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
        holder.courseName.setText(courses.get(position).courseName);
        holder.courseTitle.setText(courses.get(position).courseTitle);
        SharedPreferences sp = rootView.getContext().getSharedPreferences("PriorityCategory",Context.MODE_PRIVATE);
        int category = sp.getInt(courses.get(position).courseName, 0);
        category %= 10;
        switch(category)
        {
            case MAJOR_COURSE:
                ((CardView)holder.item).setCardBackgroundColor(rootView.getContext().getResources().getColor(R.color.major_courses, rootView.getContext().getTheme()));
                break;
            case MINOR_COURSE:
                ((CardView)holder.item).setCardBackgroundColor(rootView.getContext().getResources().getColor(R.color.minor_courses, rootView.getContext().getTheme()));
                break;
            case CC_COURSE:
                ((CardView)holder.item).setCardBackgroundColor(rootView.getContext().getResources().getColor(R.color.common_core_courses, rootView.getContext().getTheme()));
                break;
            case 0:
            default:
                break;
        }

        final TextView courseTitleCopy = holder.courseTitle;
        final TextView courseNameCopy  = holder.courseName;

        holder.courseName.post(new Runnable()
        {
            @Override
            public void run()
            {
                int totalMargin = ((ViewGroup.MarginLayoutParams)courseNameCopy.getLayoutParams()).leftMargin
                        + ((ViewGroup.MarginLayoutParams)courseNameCopy.getLayoutParams()).rightMargin
                        + ((ViewGroup.MarginLayoutParams)courseTitleCopy.getLayoutParams()).leftMargin
                        + ((ViewGroup.MarginLayoutParams)courseTitleCopy.getLayoutParams()).rightMargin;

                ViewGroup.LayoutParams titleLayout = courseTitleCopy.getLayoutParams();
                titleLayout.width = cardWidth - totalMargin - courseNameCopy.getWidth();
                courseTitleCopy.setLayoutParams(titleLayout);
            }
        });
    }

    /**
     * This class bind the onItemClick method defined in CourseCardBaseAdapter.ItemClickListener
     *   with the card view.
     */
    public class ViewHolder extends CourseCardBaseAdapter.ViewHolder implements View.OnClickListener
    {
        ImageButton btn;
        ImageButton setting;
        ImageButton delete;
        ImageButton category;

        ViewHolder(final View itemView)
        {
            super(itemView, R.id.courseTitle, R.id.courseId);

            itemView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    cardWidth = itemView.getWidth();
                }
            });
            btn = itemView.findViewById(R.id.course_button);
            btn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mClickListener != null) mClickListener.onItemClick(v, courses.get(getAdapterPosition()).courseName);
                }
            });

            setting = itemView.findViewById(R.id.course_button_setting);
            setting.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    CourseListManipulate.editCourseItem(v.getContext(),CourseCardButtonAdapter.this, courses.get(getAdapterPosition()).courseName)
                    .show();
                }
            });

            delete = itemView.findViewById(R.id.course_delete);
            delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    CourseListManipulate.deleteCourse(v.getContext(), CourseCardButtonAdapter.this, courses.get(getAdapterPosition()).courseName);
                }
            });

            category = itemView.findViewById(R.id.course_category);
            category.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                    View dialogView = View.inflate(rootView.getContext(), R.layout.edit_course_category, null);
                    builder.setView(dialogView);
                    final AlertDialog alertDialog = builder.create();

                    TextView [] categories = new TextView[4];
                    categories[0] = dialogView.findViewById(R.id.color_default);
                    categories[1] = dialogView.findViewById(R.id.color_major);
                    categories[2] = dialogView.findViewById(R.id.color_minor);
                    categories[3] = dialogView.findViewById(R.id.color_ccc);

                    for(int i=0; i<categories.length; i++)
                    {
                        if(categories[i]!=null)
                        {
                            final int i_copy = i;
                            categories[i].setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    CourseListManipulate.setCourseCategory(v.getContext(),CourseCardButtonAdapter.this,courses.get(getAdapterPosition()).courseName,i_copy);
                                    alertDialog.cancel();
                                }
                            });
                        }
                    }
                    Window window = alertDialog.getWindow();
                    Objects.requireNonNull(window).setWindowAnimations(R.style.dialog_anim);
                    window.setGravity(Gravity.CENTER);
                    alertDialog.show();
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
        SharedPreferences titles = rootView.getContext().getSharedPreferences("names", Context.MODE_PRIVATE);
        courses.clear();
        SharedPreferences priorityCategory = rootView.getContext().getSharedPreferences("PriorityCategory",Context.MODE_PRIVATE);

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
                    int priority = priorityCategory.getInt(entry.getKey(), 0);
                    priority /= 10;
                    courses.add(new Course(entry.getKey(), titles.getString(entry.getKey(),""),courseUrlStr, priority));
                }
                courses.sort(new Comparator<Course>()
                {
                    @Override
                    public int compare(Course o1, Course o2)
                    {
                        return o2.priority-o1.priority; //smaller priority => bottom
                    }
                });
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull CourseCardBaseAdapter.ViewHolder holder)
    {
        if(holder.item!=null)
            ((CardView)holder.item).setCardBackgroundColor(rootView.getContext().getResources().getColor(R.color.colorBackground, rootView.getContext().getTheme()));
        super.onViewRecycled(holder);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition)
    {
        Collections.swap(courses, fromPosition, toPosition);
        notifyItemMoved(fromPosition,toPosition);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        String [] courseNames = new String[courses.size()];
        ListIterator it = courses.listIterator();
        while(it.hasNext())
            courseNames[it.nextIndex()]=((Course)it.next()).courseName;
        CourseListManipulate.resetCoursePriority(rootView.getContext(), courseNames);
    }

}
