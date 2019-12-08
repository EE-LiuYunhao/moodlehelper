package cs.hku.hk.moodlehelper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import cs.hku.hk.moodlehelper.R;

public class MainActivity extends AppCompatActivity
{

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
                switch (item.getItemId())
                {
                    case R.id.action_settings:
                        Intent gotoSettings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(gotoSettings);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, R.string.invalid_button, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
