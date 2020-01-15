package cs.hku.hk.moodlehelper.graphic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.supports.CourseCardBaseAdapter;
import cs.hku.hk.moodlehelper.supports.CourseListManipulate;
import cs.hku.hk.moodlehelper.supports.WebExtension;

public class SettingsFragment extends Fragment implements CourseCardBaseAdapter.ItemClickListener
{
    private RecyclerView coursesList;
    private CourseCardBaseAdapter mAdapter;
    private GridLayoutManager mManager;

    private TextView mUID;
    private TextView mPIN;

    private WebExtension extension;
    private NavItemListener setter;

    private View.OnClickListener fabOnClickListener;
    private View.OnClickListener syncOnClickListener;
    private View.OnClickListener menuOpenListener;


    void setSetter(NavItemListener setter)
    {
        this.setter = setter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        //set the toolbar
        Toolbar toolbar = view.findViewById(R.id.settingsActivityToolBar);
        AppCompatActivity parentActivity = (AppCompatActivity)getActivity();
        if(parentActivity!=null)
            parentActivity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        toolbar.setNavigationOnClickListener(menuOpenListener);


        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(fabOnClickListener);

        coursesList = view.findViewById(R.id.courses_list);
        // check adapter and if not set -> set it
        if(mAdapter==null)
            mAdapter = new CourseCardBaseAdapter(view.findViewById(R.id.root_setting_view));
        mAdapter.setClickListener(this);
        coursesList.setAdapter(mAdapter);
        // check layout manager and if not set -> set it
        if(mManager==null)
        {
            mManager = new GridLayoutManager(Objects.requireNonNull(getContext()),1);
            coursesList.setLayoutManager(mManager);
        }

        Button syncBtn = view.findViewById(R.id.sync_button);
        syncBtn.setOnClickListener(syncOnClickListener);

        mUID = view.findViewById(R.id.uid_value);
        mPIN = view.findViewById(R.id.pin_value);

        refreshView();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if(!hidden)//fragment comes to front
        {
            refreshView();
        }
        else //fragment back to stack
        {
            storeStates();
        }
    }

    @Override
    public void onItemClick(View view, final String name)
    {
        CourseListManipulate.editCourseItem(view.getContext(), mAdapter, name).show();
    }

    private void refreshView()
    {
        mAdapter.refreshCourseList();
        mAdapter.notifyDataSetChanged();
        setter.onMenuItemReselected(DrawerRoot.PROFILE);

        SharedPreferences sp = Objects.requireNonNull(getContext()).getSharedPreferences("user", Context.MODE_PRIVATE);
        String storedUID = sp.getString("portalID", "");
        String storedPIN = sp.getString("portalPIN", "");

        if(!storedUID.equals(""))
        {
            mUID.setText(storedUID);
        }
        if(!storedPIN.equals(""))
        {
            mPIN.setHint(R.string.initial_pin_unchanged);
        }
    }

    private void storeStates()
    {
        if(mUID==null || mPIN==null)
        {
            return;
        }

        String portalIDStr = mUID.getText().toString();
        String portalPINStr = mPIN.getText().toString();
        if(portalIDStr.equals("") || portalPINStr.equals(""))
        {
            return;
        }

        SharedPreferences sp = Objects.requireNonNull(getContext()).getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("portalID", portalIDStr);
        editor.putString("portalPIN", portalPINStr);
        editor.apply();
    }

    void externalSyncRequest(View v)
    {
        if(!isHidden())
            syncOnClickListener.onClick(v);
    }

    static SettingsFragment newInstance(NavItemListener setter)
    {
        final SettingsFragment f = new SettingsFragment();
        f.setSetter(setter);

        f.fabOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final View addCourseView = View.inflate(view.getContext(), R.layout.edit_course, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(addCourseView);
                builder.setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                final EditText courseName = addCourseView.findViewById(R.id.edit_course_name);
                                final EditText courseUrl = addCourseView.findViewById(R.id.edit_course_url);
                                final EditText courseTitle = addCourseView.findViewById(R.id.edit_course_title);

                                String courseNameStr = courseName.getText().toString();
                                String courseUrlStr = courseUrl.getText().toString();
                                String courseTitleStr = courseTitle.getText().toString();

                                Context currentContext = f.getContext();

                                if(currentContext != null)
                                {
                                    SharedPreferences sharedPreferences = currentContext.getSharedPreferences("courses", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(courseNameStr,courseUrlStr);
                                    editor.apply();

                                    sharedPreferences = currentContext.getSharedPreferences("names", Context.MODE_PRIVATE);
                                    editor = sharedPreferences.edit();
                                    editor.putString(courseNameStr,courseTitleStr);
                                    editor.apply();

                                    sharedPreferences = currentContext.getSharedPreferences("PriorityCategory", Context.MODE_PRIVATE);
                                    editor = sharedPreferences.edit();
                                    editor.putInt(courseNameStr, 0);
                                    editor.apply();

                                    f.mAdapter.refreshCourseList();
                                    f.mAdapter.notifyDataSetChanged();
                                }
                            }
                        }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });
                AlertDialog addCourseDialog = builder.create();
                Window window = addCourseDialog.getWindow();
                Objects.requireNonNull(window).setGravity(Gravity.CENTER);
                window.setWindowAnimations(R.style.dialog_anim);
                addCourseDialog.show();
            }
        };
        f.syncOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String userName = f.mUID.getText().toString();
                String userPIN = f.mPIN.getText().toString();
                if(userPIN.equals(""))
                {
                    SharedPreferences sp = Objects.requireNonNull(f.getContext()).getSharedPreferences("user", Context.MODE_PRIVATE);
                    userPIN = sp.getString("portalPIN", "");
                }
                f.extension = new WebExtension(Objects.requireNonNull(f.getContext()), f.coursesList, userName, userPIN);
                f.extension.execute();
            }
        };
        f.menuOpenListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(f.setter!=null)
                    f.setter.configActionBar();
            }
        };

        return f;
    }
}
