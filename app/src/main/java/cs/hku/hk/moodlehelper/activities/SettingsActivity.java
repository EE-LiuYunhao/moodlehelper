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
import android.widget.EditText;
import android.widget.TextView;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.adapters.CourseCardBaseAdapter;

public class SettingsActivity extends AppCompatActivity implements CourseCardBaseAdapter.ItemClickListener
{
    RecyclerView coursesList;
    CourseCardBaseAdapter mAdapter;

    TextView mUID;
    TextView mPIN;

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

                                String courseNameStr = courseName.getText().toString();
                                String courseUrlStr = courseUrl.getText().toString();

                                SharedPreferences sharedPreferences = getSharedPreferences("courses", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(courseNameStr,courseUrlStr);
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

        if(!storedUID.equals(""))
        {
            mUID.setText(storedUID);
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
        final View addCourseView = View.inflate(view.getContext(), R.layout.edit_course, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setView(addCourseView);

        final EditText courseName = addCourseView.findViewById(R.id.edit_course_name);
        final EditText courseUrl = addCourseView.findViewById(R.id.edit_course_url);
        final SharedPreferences sharedPreferences = getSharedPreferences("courses", MODE_PRIVATE);
        final String urlStr = sharedPreferences.getString(name,getString(R.string.example_course_url));

        courseName.setText(name);
        courseName.setEnabled(false);
        courseUrl.setText(urlStr);

        builder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        String courseUrlStr = courseUrl.getText().toString();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(name,courseUrlStr);
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
                }).setNeutralButton(R.string.delete_course_setting,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(name);
                        editor.apply();
                        mAdapter.refreshCourseList();
                        mAdapter.notifyDataSetChanged();
                    }
                });
        AlertDialog addCourseDialog = builder.create();
        addCourseDialog.show();
    }
}
