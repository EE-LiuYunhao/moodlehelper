package cs.hku.hk.moodlehelper.graphic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URL;
import java.util.Objects;

import cs.hku.hk.moodlehelper.R;
import cs.hku.hk.moodlehelper.supports.CourseCardButtonAdapter;
import cs.hku.hk.moodlehelper.supports.CourseCardMover;
import cs.hku.hk.moodlehelper.supports.WebExtension;

public class MainFragment extends Fragment implements CourseCardButtonAdapter.ItemClickListener
{
    private RecyclerView coursesList;
    private CourseCardButtonAdapter mAdapter;
    private GridLayoutManager mManager;
    private ItemTouchHelper mItemTouchHelper;
    private NavItemListener setter;
    private View.OnClickListener menuOpenListener;

    void setSetter(NavItemListener setter)
    {
        this.setter = setter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //set the toolbar
        Toolbar toolbar = view.findViewById(R.id.mainActivityToolbar);
        AppCompatActivity parentActivity = (AppCompatActivity)getActivity();
        if(parentActivity!=null)
            parentActivity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        toolbar.setNavigationOnClickListener(menuOpenListener);

        if(mManager==null)
            mManager = new GridLayoutManager(getContext(),1);
        if(mAdapter==null)
            mAdapter = new CourseCardButtonAdapter(view.findViewById(R.id.root_main_view));
        mAdapter.setClickListener(this);
        coursesList = view.findViewById(R.id.main_courses_list);
        coursesList.setLayoutManager(mManager);
        coursesList.setAdapter(mAdapter);

        if(mItemTouchHelper==null)
            mItemTouchHelper= new ItemTouchHelper(new CourseCardMover(mAdapter));
        mItemTouchHelper.attachToRecyclerView(coursesList);

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
        SharedPreferences sp = Objects.requireNonNull(getContext()).getSharedPreferences("user", Context.MODE_PRIVATE);
        String uid = sp.getString("portalID","");
        String pin = sp.getString("portalPIN", "");
        Intent gotoWebView = new Intent(getContext(), MoodleContent.class);
        gotoWebView.putExtra("url",courseURL);
        gotoWebView.putExtra("courseName", name);
        gotoWebView.putExtra("uid", uid);
        gotoWebView.putExtra("pin", pin);
        if(WebExtension.isInternetConnected(getContext()) && !uid.equals("") && !pin.equals(""))
            startActivity(gotoWebView);
        else if(uid.equals("") || pin.equals(""))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.UID_PIN_problem)
                    .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            Window window = dialog.getWindow();
            Objects.requireNonNull(window).setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.dialog_anim);
            dialog.show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.network_problem)
                    .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });

            AlertDialog dialog = builder.create();
            Window window = dialog.getWindow();
            Objects.requireNonNull(window).setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.dialog_anim);
            dialog.show();
        }
    }

    @Override
    public void onDestroy()
    {
        coursesList.setAdapter(null);
        super.onDestroy();
    }

    RecyclerView getCoursesList()
    {
        return coursesList;
    }

    private void refreshView()
    {
        mAdapter.refreshCourseList();
        mAdapter.notifyDataSetChanged();
        if(setter!=null)
            setter.onMenuItemReselected(DrawerRoot.COURSES);
    }

    static MainFragment newInstance(NavItemListener setter)
    {
        final MainFragment f = new MainFragment();
        f.setSetter(setter);
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
