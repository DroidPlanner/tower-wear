package com.o3dr.android.dp.wear.widgets.indicators;

/**
 * Created by fhuya on 1/8/15.
 */
public interface PageIndicator {

    public void setCurrentItem(int item);

    public void setPageCount(int count);

    public void onPageScrollStateChanged(int state);

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

    public void onPageSelected(int position);
}
