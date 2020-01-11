package cs.hku.hk.moodlehelper.supports;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The derived class from ItemTouchHelper.Callback for the implementation of move items in a RecyclerView
 */
public class CourseCardMover extends ItemTouchHelper.Callback
{
    private CourseCardMoverAdapter mAdapter;

    /**
     * Constructor
     * @param adapter The adapter for the RecyclerView where the ViewItems are in.
     *                The adapter has to implement CourseCardMover.CourseCardMoverAdapter
     */
    public CourseCardMover(CourseCardMoverAdapter adapter)
    {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
    {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags,0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
    {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
    {
        //not allowed
    }

    @Override
    public boolean isLongPressDragEnabled()
    {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled()
    {
        return false;
    }

    /**
     * The interface for the adapter to
     */
    interface CourseCardMoverAdapter
    {
        /**
         * Method being called when one items are going to change its position
         * @param fromPosition the original position
         * @param toPosition the target position
         */
        void onItemMove(int fromPosition, int toPosition);
    }
}
