package cs.hku.hk.moodlehelper.supports;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import cs.hku.hk.moodlehelper.R;


public class ProgressView extends View
{
    private int mDotCount = 5;// default
    private int mDotColor = 0xFFFF9966;// default
    private Paint mPaint;// painting brush for the canvas

    private int mMovingWidth;// width of the dot area
    private int mOriginalDistance;// maximum allowed distance of moving

    private int mDotRadius = 7; // radius in dp (temp)

    private int mCurrentDistance = 0; //current distance away from the nearest unmoved dot

    private ValueAnimator mAnimator; //Animator handler

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        mDotColor = ta.getColor(R.styleable.ProgressView_dot_color, mDotColor);
        mDotCount = ta.getInt(R.styleable.ProgressView_dot_count, mDotCount);
        mDotRadius = (int)ta.getDimension(R.styleable.ProgressView_dot_radius, mDotRadius);
        mOriginalDistance = (int)ta.getDimension(R.styleable.ProgressView_original_distance, mOriginalDistance);
        mMovingWidth = 2 * mDotCount * mDotRadius;

        ta.recycle();
        init();
    }


    public ProgressView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ProgressView(Context context)
    {
        this(context, null);
    }


    /**
     * Convert dp into px; prepare the painter; initialize the animator
     */
    private void init()
    {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.FILL_AND_STROKE);
        mPaint.setColor(mDotColor);

        initAnimator();
    }

    /**
     * Initialize the animator, to generate a linear variation for dot movement
     */
    private void initAnimator()

    {
        mAnimator = ValueAnimator.ofInt(-1*mOriginalDistance, mOriginalDistance);

        mAnimator.setDuration(780);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                mCurrentDistance = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        // Reset the parameters to avoid exceeding the view boarders
        int maxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        mMovingWidth = mMovingWidth > maxWidth-2*mOriginalDistance ? maxWidth-2*mOriginalDistance : mMovingWidth;

        if(mMovingWidth < mDotCount*2*mDotRadius)
            mDotRadius = (mMovingWidth/mDotCount)/2;
        if(mDotRadius > getHeight() - getPaddingTop() - getPaddingBottom())
            mDotRadius = getHeight() - getPaddingTop() - getPaddingBottom();
    }



    @Override
    protected void onDraw(Canvas canvas)
    {
        if(mDotCount % 2 == 0) //even number
        {
            for(int i=0; i<mDotCount-2; i+=2)
            {
                canvas.drawCircle((getWidth()>>1) - (i>>1) * mDotRadius * 2 - mDotRadius,
                                  getHeight()>>1,
                                     mDotRadius,
                                     mPaint);
                canvas.drawCircle((getWidth()>>1) + (i>>1) * mDotRadius * 2 + mDotRadius,
                                  getHeight()>>1,
                                     mDotRadius,
                                     mPaint);
            }
        }
        else
        {
            //middle one
            canvas.drawCircle(getWidth()>>1,
                              getHeight()>>1,
                                 mDotRadius,
                                 mPaint);
            for(int i=0; i<mDotCount-3; i+=2)
            {
                canvas.drawCircle((getWidth()>>1) - (i+2)*mDotRadius,
                                  getHeight()>>1,
                                     mDotRadius,
                                     mPaint);
                canvas.drawCircle((getWidth()>>1) + (i+2)*mDotRadius,
                                  getHeight()>>1,
                                     mDotRadius,
                                     mPaint);
            }
        }
        //the leftmost one
        canvas.drawCircle((getWidth()>>1) - (mDotCount-1)*mDotRadius + (mCurrentDistance<0?mCurrentDistance:0),
                          getHeight()>>1,
                             mDotRadius,
                             mPaint);
        //the rightmost one
        canvas.drawCircle((getWidth()>>1) + (mDotCount-1)*mDotRadius + (mCurrentDistance>0?mCurrentDistance:0),
                          getHeight()>>1,
                             mDotRadius,
                             mPaint);
    }

    @Override
    protected void onAttachedToWindow()

    {
        super.onAttachedToWindow();
        startAnimation();
    }

    /**
     * Instruct the dots starting movement
     */
    public void startAnimation()
    {
        mAnimator.start();
    }

    /**
     * Instruct the dots to end movement
     */
    public void stopAnimation()
    {
        mAnimator.end();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        stopAnimation();
    }

}
