package cs.hku.hk.moodlehelper.graphic;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * The class config for the animator for recycler view.
 * Targeting at delete & move animation
 */
class FlyAnimator extends SimpleItemAnimator
{
    private List<RecyclerView.ViewHolder> removeHolders = new ArrayList<>();  //buffer list for viewHolders to be removed
    private List<RecyclerView.ViewHolder> removeAnimators = new ArrayList<>();
    private List<RecyclerView.ViewHolder> moveHolders = new ArrayList<>();    //buffer list for viewHolders to be moved
    private List<RecyclerView.ViewHolder> moveAnimators = new ArrayList<>();

    /**
     * Append a given holder to the buffer list for deleting
     * @param holder the view holder to be removed
     * @return true
     */
    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder)
    {
        removeHolders.add(holder);
        return true;
    }

    @Override //dummy
    public boolean animateAdd(RecyclerView.ViewHolder holder){return false;}

    /**
     * Append a given view holder to the buffer list for moving
     * @param holder the view holder to be moved
     * @param fromX original X coordinate
     * @param fromY original Y coordinate
     * @param toX   target X coordinate
     * @param toY   target Y coordinate
     * @return true
     */
    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY)
    {
        holder.itemView.setTranslationY(fromY - toY);
        moveHolders.add(holder);
        return true;
    }

    @Override //dummy
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop)
    {
        return false;
    }

    /**
     * When the animator is running, conduct the moving / deleting
     */
    @Override
    public void runPendingAnimations()
    {
        if(!removeHolders.isEmpty())
        {
            for(RecyclerView.ViewHolder holder : removeHolders)
            {
                remove(holder);
            }
            removeHolders.clear();
        }
        if(!moveHolders.isEmpty())
        {
            for(RecyclerView.ViewHolder holder : moveHolders)
            {
                move(holder);
            }
            moveHolders.clear();
        }
    }

    @Override //dummy
    public void endAnimation(@NonNull RecyclerView.ViewHolder item) {}
    @Override //dummy
    public void endAnimations() {}

    /**
     * Determine whether the animator should stop
     * @return True if the animator should be kept running
     */
    @Override
    public boolean isRunning()
    {
        return !(removeHolders.isEmpty() && removeAnimators.isEmpty() && moveHolders.isEmpty() && moveAnimators.isEmpty());
    }

    /**
     * Literally remove a view holder by translating it out of the visible region
     * @param holder the view holder to be translated out
     */
    private void remove(final RecyclerView.ViewHolder holder)
    {
        //This one use a TranslateAnimator
        //The translateAnimator can be attached to view holder to realize the animation
        removeAnimators.add(holder);
        TranslateAnimation animation = new TranslateAnimation(0, holder.itemView.getWidth(), 0, 0);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                dispatchRemoveStarting(holder); // super class method, to invoke the animation effect
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                removeAnimators.remove(holder);
                dispatchRemoveFinished(holder);
                if(!isRunning())
                {
                    dispatchAnimationsFinished(); // once this is the last for animation, dispatch all
                    //a method of the RecyclerView.ItemAnimator
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        holder.itemView.startAnimation(animation);
    }

    /**
     * Literally remove a view holder by translating it upward
     * @param holder the view holder to be translated up
     */
    private void move(final RecyclerView.ViewHolder holder)
    {
        //Instead of using a TranslateAnimator
        //The method use an ObjectAnimator, which automatically call setXXX method of the given object
        //Here the holder.itemView.setTranslationY() will be automatically invoked
        moveAnimators.add(holder);
        ObjectAnimator animator = ObjectAnimator.ofFloat(holder.itemView,
                "translationY", holder.itemView.getTranslationY(), 0);
        animator.setDuration(500);
        animator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationStart(android.animation.Animator animation)
            {
                dispatchMoveStarting(holder);
            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation)
            {
                dispatchMoveFinished(holder);
                moveAnimators.remove(holder);
                if(!isRunning()) dispatchAnimationsFinished();
            }
        });
        animator.start();
    }

}