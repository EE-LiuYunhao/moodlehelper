package cs.hku.hk.moodlehelper.graphic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.supports.WebExtension;

public class DrawerRoot extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavItemListener
{
    final private int CURRENT_FRAGMENT_MAIN = 1;
    final private int CURRENT_FRAGMENT_SETTING = 2;

    private DrawerLayout mDrawer;
    private NavigationView mNavigate;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private MainFragment mainFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_root);

        mDrawer = findViewById(R.id.drawer_layout);
        mNavigate = findViewById(R.id.navigation);

        mNavigate.setCheckedItem(R.id.menu_item_courses);
        mNavigate.setNavigationItemSelectedListener(this);

        View headerView = mNavigate.getHeaderView(0);
        ImageView headerImage = headerView.findViewById(R.id.header_image);
        FragmentToSetting switchListener = new FragmentToSetting();
        headerImage.setOnClickListener(switchListener);
        TextView headerInitial = headerView.findViewById(R.id.header_initial);
        headerInitial.setOnClickListener(switchListener);
        TextView headerText = headerView.findViewById(R.id.header_text);
        headerText.setOnClickListener(switchListener);

        fragmentManager = getSupportFragmentManager();
        mainFragment = MainFragment.newInstance(this);
        settingsFragment = SettingsFragment.newInstance(this);
        int prevFragment = savedInstanceState==null?0:savedInstanceState.getInt("currentFragment");
        if(prevFragment == CURRENT_FRAGMENT_MAIN)
        {
            currentFragment = mainFragment;
        }
        else if(prevFragment == CURRENT_FRAGMENT_SETTING)
        {
            currentFragment = settingsFragment;
        }
        else
        {
            currentFragment = mainFragment;
        }
        fragmentManager.beginTransaction().replace(R.id.main_fragment, currentFragment).commit();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.menu_item_courses:
                switchFragment(mainFragment);
                break;
            case R.id.menu_item_grade:
                openGradeMenu();
                break;
            case R.id.menu_item_profile:
                switchFragment(settingsFragment);
                break;
            case R.id.menu_item_sync:
                if(currentFragment!=settingsFragment)
                {
                    SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                    String userName = sp.getString("portalID", "");
                    String userPIN = sp.getString("portalPIN", "");
                    WebExtension extension = new WebExtension(DrawerRoot.this, mainFragment.getCoursesList(), userName, userPIN);
                    extension.execute();
                }
                else
                {
                    mDrawer.closeDrawers();
                    settingsFragment.externalSyncRequest(settingsFragment.getView());
                }
                break;
            case R.id.menu_item_clear_all:
                //TODO: call clean (not implemented)
            default:
                break;
        }
        mDrawer.closeDrawers();
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String uid = sp.getString("portalID","");

        View headerView = mNavigate.getHeaderView(0);
        TextView headerInitial = headerView.findViewById(R.id.header_initial);
        TextView headerText = headerView.findViewById(R.id.header_text);

        headerText.setText(uid);
        char [] init = new char[2];
        init[0] = Character.toUpperCase(uid.charAt(0));
        init[1] = uid.charAt(1);
        headerInitial.setText(new String(init));

        mainFragment.setSetter(this);
        settingsFragment.setSetter(this);

        if(currentFragment instanceof MainFragment)
            mNavigate.setCheckedItem(R.id.menu_item_courses);
        else if(currentFragment instanceof  SettingsFragment)
            mNavigate.setCheckedItem(R.id.menu_item_profile);
    }

    private class FragmentToSetting implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switchFragment(settingsFragment);
            mDrawer.closeDrawers();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(android.R.id.home == item.getItemId())
        {
            mDrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchFragment(Fragment targetFragment)
    {
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if(!targetFragment.isAdded())//first added
        {
            if(currentFragment !=null)
            {
                ft.hide(currentFragment);
            }
            ft.add(R.id.main_fragment,targetFragment,targetFragment.getClass().getName());
        }
        else //this fragment is added already => currentFragment cannot be null
        {
            if(currentFragment !=targetFragment)
                ft.hide(currentFragment).show(targetFragment);
            //else: do nothing
        }
        currentFragment = targetFragment;
        ft.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        if(currentFragment instanceof MainFragment)
            outState.putInt("currentFragment", CURRENT_FRAGMENT_MAIN);
        else if(currentFragment instanceof SettingsFragment)
            outState.putInt("currentFragment", CURRENT_FRAGMENT_SETTING);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMenuItemReselected(int itemId)
    {
        if(itemId!=GRADE && itemId!=COURSES && itemId!=PROFILE && itemId!=NONE)
            return;
        if(itemId!=NONE)
            mNavigate.setCheckedItem(itemId);
        else
            for(int i=0; i<mNavigate.getMenu().size(); i++)
                mNavigate.getMenu().getItem(i).setChecked(false);
    }

    @Override
    public void configActionBar()
    {
        mDrawer.openDrawer(GravityCompat.START);
    }

    private void openGradeMenu()
    {
        URL gradeURL;
        try
        {
            gradeURL = new URL("https://moodle.hku.hk/");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return;
        }
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String uid = sp.getString("portalID","");
        String pin = sp.getString("portalPIN", "");
        Intent gotoWebView = new Intent(this, MoodleContent.class);
        gotoWebView.putExtra("url", gradeURL);
        gotoWebView.putExtra("courseName", "MyGrade");
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
}
