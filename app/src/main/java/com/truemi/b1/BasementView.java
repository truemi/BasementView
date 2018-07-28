package com.truemi.b1;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class BasementView extends LinearLayout {

    private   int            height;
    private   RelativeLayout basementLayout;
    private   RelativeLayout currentLayout;
    private   float          y;
    protected Scroller       scroller;
    private   LayoutParams   layoutParams;
    private   boolean        b1Show;
    private   double         v11;
    private boolean moving  = false;
    private float   damping = 0.5f;//阻尼系数
    private boolean isAbsorb;
    private boolean isTop;
    private  int duration =400;//滚动时间

    public BasementView(Context context) {
        this(context, null);
    }

    public BasementView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public BasementView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        basementLayout = new RelativeLayout(context);//负一楼layout
        currentLayout = new RelativeLayout(context);//当前layout

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                测量当前view的高度,通过margintop隐藏负一楼layout
                height = getHeight();
                layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                layoutParams.setMargins(0, -height, 0, 0);
                basementLayout.setLayoutParams(layoutParams);
                basementLayout.setEnabled(true);
                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                currentLayout.setLayoutParams(layoutParams1);
                //添加到当前view中
                addView(basementLayout);
                addView(currentLayout);
            }
        });
        //new出滚动器实例,设置动画插值器
        scroller = new Scroller(context, new DecelerateInterpolator());
    }

    /**
     * 触摸事件处理
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isAbsorb = false;
                y = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float v = ev.getY() - y;
                if (!b1Show) {
                    if (isTop&&v>0) {
                        isAbsorb = true;
                    } else {
                        isAbsorb = false;
                    }
                }else{
                    isAbsorb = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return isAbsorb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isAbsorb = true;
                v11 = 0;
                if (Math.abs(layoutParams.topMargin) >= height - 3) {
//                    B1未出现
                    b1Show = false;
                } else if (layoutParams.topMargin >= -2) {
//                    B1完全出现
                    b1Show = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                v11 = Math.ceil((double) (event.getY() - y)) * damping;

                if (!b1Show && layoutParams.topMargin < 0 && v11 > 0) {
                        layoutParams.setMargins(0, (int) v11 - height, 0, 0);
                        basementLayout.setLayoutParams(layoutParams);

                } else if (b1Show && v11 < 0) {
                        layoutParams.setMargins(0, (int) v11, 0, 0);
                        basementLayout.setLayoutParams(layoutParams);

                }
                break;
            case MotionEvent.ACTION_UP:
                if (!b1Show && layoutParams.topMargin < 0 && v11 > 0) {
                    if (Math.abs(v11 - height) >= height * 0.75) {
                        scrollView(0, (int) -v11, 200);
                    }

                    if (Math.abs(v11 - height) <= height * 0.75) {
                        scrollView(0, (int) Math.abs(v11 - height), 400);
                    }

                } else if (b1Show && v11 < 0) {

                    if (Math.abs(v11) <= height * 0.25) {
                        scrollView((int) 0, (int) (-v11), 200);
                    }
                    if (Math.abs(v11) >= height * 0.25) {
                        scrollView((int) 0, (int) -(height + v11), 400);
                    }
                }
                break;

        }
        return isAbsorb;

    }

    /**
     * 通过滚动器让view动起来
     * @param startY 相对开始位置(相对于view当前位置)
     * @param endY    相对结束位置
     * @param duration 滚动时间
     */
    public void scrollView(int startY, int endY, int duration) {
        scroller.startScroll(0, startY, 0, endY, duration);
        postInvalidate();//必须
    }

    /**
     * 回调起始位置与终止位置变化值(start --> start+end)的区间变化,精度为1
     */
    @Override
    public void computeScroll() {
        if (Math.abs(layoutParams.topMargin) >= height - 3) {
//                    B1未出现
            b1Show = false;
        } else if (layoutParams.topMargin >= -3) {
//                    B1完全出现
            b1Show = true;
        }
        moving = scroller.computeScrollOffset();
        if (scroller.computeScrollOffset()) {
            int currY = scroller.getCurrY();
            if (v11 > 0) {
                if (Math.abs(currY)+3>=Math.abs(scroller.getFinalY())) {
                    if (currY>-10){
                        layoutParams.setMargins(0, (int) 0, 0, 0);
                    }else{
                        layoutParams.setMargins(0, (int) - height, 0, 0);
                    }
                }else {
                    layoutParams.setMargins(0, (int) (currY - Math.abs(v11 - height)), 0, 0);
                }
            } else if (v11 < 0) {
                if (Math.abs(currY)+3>=Math.abs(scroller.getFinalY())) {
                    if (currY>-10){
                        layoutParams.setMargins(0, (int) 0, 0, 0);
                    }else{
                        layoutParams.setMargins(0, (int) - height, 0, 0);
                    }
                }else {
                    layoutParams.setMargins(0, (int) (currY + v11), 0, 0);
                }
            }else{
                    layoutParams.setMargins(0, currY - height, 0, 0);
            }
            basementLayout.setLayoutParams(layoutParams);
        }
        postInvalidate();//必须刷新view

    }

    /**
     * 获取负一楼View
     * @return
     */
    public View getBasementLayout() {
        return basementLayout;
    }

    /**
     * 获取主界面view
     * @return
     */
    public View getCurrentLayout() {
        return currentLayout;
    }

    /**
     * 设置负一楼view内容
     * @param view
     */
    public void setBasementLayout(View view) {
        basementLayout.addView(view);
    }

    /**
     * 设置主界面view内容
     * @param view
     */
    public void setCurrentLayout(View view) {
        currentLayout.addView(view);
    }

    /**
     * 滚动到负一楼
     */
    public void toB1(){
        v11  =0;
        if (!b1Show){
            scrollView(0,  height, duration);
        }
    }
    /**
     * 滚动到主界面
     */
    public void toF1(){
        v11  =0;
        if (b1Show){
            scrollView(height,-height, duration);
        }
    }

    /**
     * 设置滚动时间,单位为ms
     * @param duration
     */
    public void  setDuration(int duration){
        this.duration =duration;
    }

    /**
     * 是否打开负一楼
     * @return
     */
    public boolean  isOpenB1(){
      return b1Show;
    }

    /**
     * 绑定滚动控件
     * @param scrollView
     */
    public void  bindScrollAbleView(View scrollView){
        try {
            if (scrollView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) scrollView;
                absListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        isTop = firstVisibleItem == 0?true:false;
                    }
                });

            }
            }catch (Exception e){
            if (ViewCompat.canScrollVertically(scrollView, 1)||ViewCompat.canScrollVertically(scrollView, -1)){
                isTop = ViewCompat.canScrollVertically(scrollView, 1)?false:true;
            }
        }
    }
}
