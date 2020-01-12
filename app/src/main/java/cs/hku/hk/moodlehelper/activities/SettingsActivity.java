package cs.hku.hk.moodlehelper.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.supports.CourseCardBaseAdapter;
import cs.hku.hk.moodlehelper.supports.CourseListManipulate;
import cs.hku.hk.moodlehelper.supports.WebExtension;

public class SettingsActivity extends AppCompatActivity implements CourseCardBaseAdapter.ItemClickListener
{
    RecyclerView coursesList;
    CourseCardBaseAdapter mAdapter;

    TextView mUID;
    TextView mPIN;
    Button syncBtn;

    WebExtension extension;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //set the toolbar
        Toolbar toolbar = findViewById(R.id.settingsActivityToolBar);
        toolbar.setLogo(R.drawable.ic_settings);
        setSupportActionBar(toolbar);
        //toolbar navigate
        toolbar.setNavigationIcon(R.drawable.ic_navigate_before_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d("ActivityDebug","Navigation on click event");
                Intent returnHome = new Intent();
                setResult(RESULT_OK, returnHome);
                finish();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
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

                                SharedPreferences sharedPreferences = getSharedPreferences("courses", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(courseNameStr,courseUrlStr);
                                editor.apply();

                                sharedPreferences = getSharedPreferences("names", MODE_PRIVATE);
                                editor = sharedPreferences.edit();
                                editor.putString(courseNameStr,courseTitleStr);
                                editor.apply();

                                sharedPreferences = getSharedPreferences("PriorityCategory", MODE_PRIVATE);
                                editor = sharedPreferences.edit();
                                editor.putInt(courseNameStr, 0);
                                editor.apply();

                                mAdapter.refreshCourseList();
                                mAdapter.notifyDataSetChanged();
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
                addCourseDialog.show();
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        mAdapter = new CourseCardBaseAdapter(findViewById(R.id.root_setting_view));
        mAdapter.setClickListener(this);
        coursesList = findViewById(R.id.courses_list);
        coursesList.setLayoutManager(layoutManager);
        coursesList.setAdapter(mAdapter);

        syncBtn = findViewById(R.id.sync_button);
        syncBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String userName = mUID.getText().toString();
                String userPIN = mPIN.getText().toString();
                if(userPIN.equals(""))
                {
                    SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                    userPIN = sp.getString("portalPIN", "");
                }
                extension = new WebExtension(SettingsActivity.this, coursesList, userName, userPIN);
                extension.execute();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mAdapter.refreshCourseList();
        mAdapter.notifyDataSetChanged();

        mUID = findViewById(R.id.uid_value);
        mPIN = findViewById(R.id.pin_value);

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
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

    @Override
    protected void onPause()
    {
        super.onPause();
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

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("portalID", portalIDStr);
        editor.putString("portalPIN", portalPINStr);
        editor.apply();
    }

    @Override
    public void onItemClick(View view, final String name)
    {
        CourseListManipulate.editCourseItem(view.getContext(), mAdapter, name).show();
    }
}
