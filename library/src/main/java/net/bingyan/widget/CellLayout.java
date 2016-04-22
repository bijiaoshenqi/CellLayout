package net.bingyan.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class CellLayout extends ViewGroup {
    private int rowCount;       // 总共的行数
    private int columnCount;   // 总共的列数
    private int rowVisible;    // 一次最多显示的行数
    private int columnVisible; // 一次最多显示的列数

    /** measure遇到RelativeLayout时有bug  {@link #onMeasure(int, int)} */
    private int tmpMeasuredCount = 0;

    public CellLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyleAttr, defStyleRes);
        rowCount = array.getInteger(R.styleable.CellLayout_cell_row_count, 1);
        columnCount = array.getInteger(R.styleable.CellLayout_cell_column_count, 1);
        rowVisible = array.getInteger(R.styleable.CellLayout_cell_row_visible, 1);
        columnVisible = array.getInteger(R.styleable.CellLayout_cell_column_visible, 1);
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMeasured = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMeasured = MeasureSpec.getSize(heightMeasureSpec);
        final int rowVisible = Math.max(1, this.rowVisible);
        final int columnVisible = Math.max(1, this.columnVisible);
        final int rowCount = Math.max(rowVisible, this.rowCount);
        final int columnCount = Math.max(columnVisible, this.columnCount);

        final float cellHeight = (heightMeasured - getPaddingTop() - getPaddingBottom()) / rowVisible;
        /**
         * RelativeLayout的measure过程很特别
         * 首先 measure horizontal，将width确定
         * 再 measure child 一遍，完全确定
         * 后果就是如果按照下面的被注释的代码运行的话，会导致：
         * 首先 measure了一遍，得到width,此时可能超过了屏幕宽，再measure一遍，再此基础上再扩展一遍宽
         *
         * final float cellWidth = (widthMeasured - getPaddingLeft() - getPaddingRight()) / columnVisible;
         */
        ++tmpMeasuredCount;
        final float cellWidth;
        if (getParent() instanceof RelativeLayout && tmpMeasuredCount % 2 == 0) {
            cellWidth = (widthMeasured - getPaddingLeft() - getPaddingRight()) / columnCount;
        } else {
            cellWidth = (widthMeasured - getPaddingLeft() - getPaddingRight()) / columnVisible;
        }

        for (int i = 0; i < getChildCount(); ++i) {
            final View child = getChildAt(i);
            final LayoutParams params = (LayoutParams) child.getLayoutParams();
            final int childRowCount = Math.max(0, params.rowCount);
            final int childColumnCount = Math.max(0, params.columnCount);

            final int childWidth = (int) (cellWidth * childColumnCount - params.leftMargin - params.rightMargin);
            final int childHeight = (int) (cellHeight * childRowCount - params.topMargin - params.bottomMargin);

            child.measure(
                    MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
            );
        }

        setMeasuredDimension(
                getPaddingLeft() + (int) (cellWidth * columnCount) + getPaddingRight(),
                getPaddingTop() + (int) (cellHeight * rowCount) + getPaddingBottom()
        );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int rowVisible = Math.max(1, this.rowVisible);
        final int columnVisible = Math.max(1, this.columnVisible);
        final int rowCount = Math.max(rowVisible, this.rowCount);
        final int columnCount = Math.max(columnVisible, this.columnCount);
        final float cellWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / columnCount;
        final float cellHeight = (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / rowCount;

        for (int i = 0; i < getChildCount(); ++i) {
            final View child = getChildAt(i);
            final LayoutParams params = (LayoutParams) child.getLayoutParams();
            final int childRowCount = Math.max(0, params.rowCount);
            final int childColumnCount = Math.max(0, params.columnCount);

            child.layout(
                    getPaddingLeft() + (int) (cellWidth * params.column + params.leftMargin),
                    getPaddingTop() + (int) (cellHeight * params.row + params.topMargin),
                    getPaddingLeft() + (int) (cellWidth * params.column + cellWidth * childColumnCount - params.rightMargin),
                    getPaddingTop() + (int) (cellHeight * params.row + cellHeight * childRowCount - params.bottomMargin)
            );
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {
        private int row;            // 起始行，0 index
        private int column;        // 起始列，0 index
        private int rowCount;      // 占的行数
        private int columnCount;  // 占的列数

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CellLayout_LayoutParams);
            row = array.getInteger(R.styleable.CellLayout_LayoutParams_cell_row, 0);
            column = array.getInteger(R.styleable.CellLayout_LayoutParams_cell_column, 0);
            rowCount = array.getInteger(R.styleable.CellLayout_LayoutParams_cell_row_count, 1);
            columnCount = array.getInteger(R.styleable.CellLayout_LayoutParams_cell_column_count, 1);
            array.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);

            row = 0;
            column = 0;
            rowCount = 1;
            columnCount = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            LayoutParams params = (LayoutParams) source;

            this.row = params.row;
            this.column = params.column;
            this.rowCount = params.rowCount;
            this.columnCount = params.columnCount;
        }

    }

}
