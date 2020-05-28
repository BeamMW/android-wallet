package com.mw.beam.beamwallet.core.views;

import android.view.GestureDetector;
import android.view.MotionEvent;

class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener
{
    public boolean onSingleTapUp(MotionEvent e)
    {
        //single tap returns true so we know to register it over a swipe
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event)
    {

    }

    @Override
    public boolean onDown(MotionEvent event)
    {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
    {

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY)
    {
        return false;
    }
}
