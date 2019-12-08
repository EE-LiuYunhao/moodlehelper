package cs.hku.hk.moodlehelper.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.adapters.CourseSettingCardAdapter;

public class SettingsActivity extends AppCompatActivity implements CourseSettingCardAdapter.ItemClickListener
{
    RecyclerView coursesList;
    CourseSettingCardAdapter mAdapter;

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
        mAdapter = new CourseSettingCardAdapter(findViewById(R.id.root_setting_view));
        mAdapter.setClickListener(this);
        coursesList = findViewById(R.id.courses_list);
        coursesList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mAdapter.refreshCourseList();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, String name)
    {
        Toast.makeText(this,"TODO", Toast.LENGTH_SHORT).show();
        //TODO: jump to editing
    }
}
