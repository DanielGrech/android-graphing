package com.dgsd.android.graphing;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.LinkedList;
import java.util.List;

public class BarGraph extends View {
    private static final String TAG = BarGraph.class.getSimpleName();

    private static final Interpolator sInterpolator = new DecelerateInterpolator(1.2f);

    private Paint mPaint;
    private Paint mAxisLabelPaint;
    private Paint mAxisTitlePaint;
    private Paint mAxisLinePaint;

    private int mWidth;
    private int mHeight;

    private List<DataDimension> mData;

    private String[] mYValues;

    private float mYValueHeight;
    private Rect mTextHeightRect;

    private float mMaxXValueWidth;
    private float mMaxYValueWidth;

    private int mMinValue;
    private int mMaxValue;

    private float mXAxisLabelTopPadding;
    private float mYAxisLabelRightPadding;
    private float mYAxisTitlePadding;

    private float mYAxisTitleHeight;
    private float mBarPadding;
    private float mBorderWidth;
    private int mBarRoundingValue;
    private int mAnimationDuration;

    private RectF mRectF;

    private float mCurrentAnimValue;

    private String mYAxisTitle;

    private Path mPath;

    private LabelAdapter mLabelAdapter;

    public BarGraph(final Context context) {
        super(context);
        init(null);
    }

    public BarGraph(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BarGraph(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(final AttributeSet attrs) {
        final Resources r = getResources();

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mAxisLabelPaint = new Paint();
        mAxisLabelPaint.setAntiAlias(true);
        mAxisLabelPaint.setColor(r.getColor(R.color.bar_graph_axis_label_color));
        mAxisLabelPaint.setTextSize(r.getDimensionPixelSize(R.dimen.bar_graph_axis_label_size));
        mAxisLabelPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        mAxisTitlePaint = new Paint(mAxisLabelPaint);
        mAxisTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);

        mAxisLinePaint = new Paint();
        mAxisLinePaint.setAntiAlias(true);
        mAxisLinePaint.setStrokeWidth(r.getDimension(R.dimen.bar_graph_axis_width));
        mAxisLinePaint.setStyle(Paint.Style.STROKE);
        mAxisLinePaint.setColor(r.getColor(R.color.bar_graph_axis_line_color));

        mTextHeightRect = new Rect();
        mRectF = new RectF();

        mAxisLabelPaint.getTextBounds("888", 0, 3, mTextHeightRect);
        mYValueHeight = mTextHeightRect.height();


        mXAxisLabelTopPadding = r.getDimensionPixelSize(R.dimen.bar_graph_x_axis_top_padding);
        mYAxisLabelRightPadding = r.getDimensionPixelSize(R.dimen.bar_graph_y_axis_right_padding);
        mYAxisTitlePadding = r.getDimensionPixelSize(R.dimen.bar_graph_y_axis_title_padding);
        mBorderWidth = r.getDimensionPixelSize(R.dimen.bar_graph_border_width);
        mBarRoundingValue = r.getInteger(R.integer.bar_graph_rounding);
        mBarPadding = r.getDimensionPixelSize(R.dimen.bar_graph_bar_padding);
        mAnimationDuration = r.getInteger(R.integer.bar_graph_animation_duration);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        calculateYValues();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final float yAxisX = mMaxYValueWidth + mYAxisLabelRightPadding +
                getPaddingLeft() + (mYAxisTitleHeight * 2) + mYAxisTitlePadding;
        final float xAxisY = mHeight - getPaddingBottom() - mMaxXValueWidth - mXAxisLabelTopPadding;

        //Y-axis title
        if(mYAxisTitleHeight > 0) {
            canvas.save();
            {

                mPath.moveTo(mYAxisTitleHeight, xAxisY);
                mPath.lineTo(mYAxisTitleHeight, getPaddingTop());
                canvas.drawTextOnPath(mYAxisTitle,
                        mPath,
                        (xAxisY - getPaddingTop()) / 2f - mAxisTitlePaint.measureText(mYAxisTitle) / 2f,
                        0,
                        mAxisTitlePaint);
            }
            canvas.restore();
        }

        //Y-axis
        canvas.drawLine(yAxisX,
                0,
                yAxisX,
                mHeight - getPaddingBottom() - mMaxXValueWidth - mXAxisLabelTopPadding,
                mAxisLinePaint);

        //X-axis
        canvas.drawLine(yAxisX,
                xAxisY,
                mWidth - getPaddingRight(),
                xAxisY,
                mAxisLinePaint);

        if (mData != null) {
            canvas.save();
            {
                canvas.translate(yAxisX, 0);

                final float totalWidth = mWidth - yAxisX - getPaddingRight() - mBarPadding;
                final float widthOfEachBar = totalWidth / mData.size();
                final float bottom = mHeight - mMaxXValueWidth - mXAxisLabelTopPadding
                        - (mAxisLinePaint.getStrokeWidth() / 2f) - getPaddingBottom();
                final float totalHeight = bottom;

                for (int i = 0 ; i < mData.size(); i++) {
                    //Draw the border
                    mRectF.bottom = bottom;
                    mRectF.left = (i * widthOfEachBar) + mBarPadding;
                    mRectF.right = mRectF.left + widthOfEachBar - mBarPadding;
                    mRectF.top = totalHeight * (1f - ((mData.get(i).getValue() * mCurrentAnimValue) / mMaxValue));

                    mPaint.setColor(mData.get(i).getBorderColor());
                    canvas.drawRoundRect(mRectF, mBarRoundingValue, mBarRoundingValue, mPaint);

                    //Draw the bar itself..
                    mRectF.left += mBorderWidth;
                    mRectF.right -= mBorderWidth;
                    mRectF.top += mBorderWidth;

                    mPaint.setColor(mData.get(i).getColor());
                    canvas.drawRoundRect(mRectF, mBarRoundingValue, mBarRoundingValue, mPaint);

                    //X-Axis Label
                    canvas.save();
                    {
                        final float x = (i * widthOfEachBar) + (widthOfEachBar / 2f);
                        final float y = mHeight - getPaddingBottom();
                        canvas.rotate(-45, x, y);
                        canvas.drawText(mData.get(i).getName(), x, y, mAxisLabelPaint);
                    }
                    canvas.restore();
                }

            }
            canvas.restore();
        }


        //Y-axis labels
        if (mYValues != null) {
            canvas.save();
            {
                //Translate canvas up above x-axis labels..
                canvas.translate((mYAxisTitleHeight * 2) + getPaddingLeft() + mYAxisTitlePadding, -mMaxXValueWidth);

                for (int i = 0; i < mYValues.length; i++) {
                    canvas.drawText(mYValues[i],
                            0,
                            mHeight - (i * ((mHeight - mMaxXValueWidth) / mYValues.length)) - (mYValueHeight / 2f) - mXAxisLabelTopPadding - getPaddingBottom(),
                            mAxisLabelPaint);
                }
            }
            canvas.restore();
        }

    }

    public void add(String name, int value, int color) {
        add(name, value, color, color);
    }

    public void add(String name, int value, int color, int borderColor) {
        if (mData == null)
            mData = new LinkedList<DataDimension>();

        mData.add(new DataDimension(name, value, color, borderColor));

        calculateYValues();
        calculateMaxXLabelLength();
    }

    public void clearData() {
        if(mData != null)
            mData.clear();

        mYValues = null;
    }

    private void calculateMaxXLabelLength() {
        int maxStrLen = Integer.MIN_VALUE;
        if (mData != null) {
            for (DataDimension d : mData) {
                if (d.getName() != null && d.getName().length() > maxStrLen) {
                    maxStrLen = d.getName().length();
                    mMaxXValueWidth = mAxisLabelPaint.measureText(d.getName());
                }
            }
        }
    }

    private void calculateYValues() {
        if (mData == null || mData.isEmpty()) {
            mYValues = null;
            return;
        }

        int maxValues = (int) ((mHeight - mMaxXValueWidth - mXAxisLabelTopPadding - getPaddingBottom()) / (mYValueHeight * 2f) - 1);

        if (maxValues <= 2) {
            return;
        }

        mYValues = new String[maxValues];
        mMinValue = 0; //We always want 0 to be the min (unless we have a negative value)/
        mMaxValue = Integer.MIN_VALUE;
        for (DataDimension data : mData) {
            if (data.getValue() < mMinValue)
                mMinValue = data.getValue();
            if (data.getValue() > mMaxValue)
                mMaxValue = data.getValue();
        }

        mMaxYValueWidth = mAxisLabelPaint.measureText(String.valueOf(mMaxValue));

        final int stepVal = (mMaxValue - mMinValue) / (maxValues - 1);
        for (int i = 0; i < maxValues; i++) {
            if (i == 0) {
                mYValues[i] = mLabelAdapter == null ?
                        String.valueOf(mMinValue) : mLabelAdapter.toLabel(mMinValue);
            } else if (i == maxValues - 1) {
                mYValues[i] = mLabelAdapter == null ?
                        String.valueOf(mMaxValue) : mLabelAdapter.toLabel(mMaxValue);
            } else {
                mYValues[i] = mLabelAdapter == null ?
                        String.valueOf(mMinValue + (i * stepVal)) : mLabelAdapter.toLabel(mMinValue + (i * stepVal));
            }
        }
    }

    public void setYAxisTitle(String label) {
        mYAxisTitle = label;
        if(TextUtils.isEmpty(mYAxisTitle)) {
            mYAxisTitleHeight = 0;
        }  else {
            mAxisTitlePaint.getTextBounds(label, 0, label.length(), mTextHeightRect);
            mYAxisTitleHeight = mTextHeightRect.height();
        }
    }

    public void animateGraph() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "CurrentAnimValue", 0.0f, 1.0f);
        anim.setInterpolator(sInterpolator);
        anim.setDuration(mAnimationDuration);
        anim.start();
    }

    public void setCurrentAnimValue(float val) {
        mCurrentAnimValue = val;
        invalidate();
    }

    public float getCurrentAnimValue() {
        return mCurrentAnimValue;
    }

    public void setLabelAdapter(final LabelAdapter adapter) {
        mLabelAdapter = adapter;
    }

    public static interface LabelAdapter {
        public String toLabel(int value);
    }

    public static class DataDimension {
        private String mName;
        private int mValue;
        private int mColor;
        private int mBorderColor;

        public DataDimension(final String name, final int value, final int color, final int borderColor) {
            mName = name;
            mValue = value;
            mColor = color;
            mBorderColor = borderColor;
        }

        public String getName() {
            return mName;
        }

        public void setName(final String name) {
            mName = name;
        }

        public int getValue() {
            return mValue;
        }

        public void setValue(final int value) {
            mValue = value;
        }

        public int getColor() {
            return mColor;
        }

        public void setColor(final int color) {
            mColor = color;
        }

        public int getBorderColor() {
            return mBorderColor;
        }

        public void setBorderColor(final int borderColor) {
            mBorderColor = borderColor;
        }
    }
}
