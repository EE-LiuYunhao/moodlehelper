package cs.hku.hk.moodlehelper.graphic;

import java.io.Serializable;

import cs.hku.hk.moodlehelper.R;

public interface NavItemListener
{
    int GRADE = R.id.menu_item_grade;
    int COURSES = R.id.menu_item_courses;
    int PROFILE = R.id.menu_item_profile;
    int NONE = 0;
    void onMenuItemReselected(int itemId);
    void configActionBar();
    void resetHeader(String name);
}
