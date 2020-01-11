package cs.hku.hk.moodlehelper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URL;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.supports.CourseCardButtonAdapter;
import cs.hku.hk.moodlehelper.supports.CourseCardMover;
import cs.hku.hk.moodlehelper.supports.WebExtension;

public class MainActivity extends AppCompatActivity implements CourseCardButtonAdapter.ItemClickListener
{
    RecyclerView coursesList;
    CourseCardButtonAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the toolbar
        Toolbar toolbar = findViewById(R.id.mainActivityToolbar);
        toolbar.setLogo(R.drawable.ic_main_activity_logo);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (item.getItemId() == R.id.action_settings)
                {
                    Intent gotoSettings = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(gotoSettings);
                }
                else
                {
                    Toast.makeText(MainActivity.this, R.string.invalid_button, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        mAdapter = new CourseCardButtonAdapter(findViewById(R.id.root_main_view));
        mAdapter.setClickListener(this);
        coursesList = findViewById(R.id.main_courses_list);
        coursesList.setLayoutManager(layoutManager);
        coursesList.setAdapter(mAdapter);

        ItemTouchHelper helper = new ItemTouchHelper(new CourseCardMover(mAdapter));
        helper.attachToRecyclerView(coursesList);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mAdapter.refreshCourseList();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(View view, String name)
    {
        URL courseURL = mAdapter.getURLByCourseName(name);
        if(courseURL==null)
        {
            //DO Nothing
            return;
        }
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String uid = sp.getString("portalID","");
        String pin = sp.getString("portalPIN", "");
        Intent gotoWebView = new Intent(MainActivity.this, MoodleContent.class);
        gotoWebView.putExtra("url",courseURL);
        gotoWebView.putExtra("courseName", name);
        gotoWebView.putExtra("uid", uid);
        gotoWebView.putExtra("pin", pin);
        if(WebExtension.isInternetConnected(this) && !uid.equals("") && !pin.equals(""))
            startActivity(gotoWebView);
        else if(uid.equals("") || pin.equals(""))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.UID_PIN_problem)
                    .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.network_problem)
                    .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    protected void onDestroy()
    {
        coursesList.setAdapter(null);
        super.onDestroy();
    }
}
