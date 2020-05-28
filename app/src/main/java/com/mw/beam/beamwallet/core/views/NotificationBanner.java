package com.mw.beam.beamwallet.core.views;

import android.app.Activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.text.Spannable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.view.GestureDetectorCompat;

import com.mw.beam.beamwallet.R;
import com.mw.beam.beamwallet.core.entities.NotificationItem;
import com.mw.beam.beamwallet.core.entities.NotificationType;
import com.mw.beam.beamwallet.screens.app_activity.AppActivity;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class NotificationBanner {

    private Context mContext;
    private Activity activity;
    private View popupView;
    private View rootView;
    private  boolean focusable;
    private  boolean asDropDown;
    private PopupWindow popupWindow;


    public static int SUCCESS = 1;
    public static int INFO = 2;
    public static int WARNING = 3;
    public static int ERROR = 4;
    public static int CUSTOM = 5;

    private boolean showBanner = false;
    private long duration = 7000;
    private Timer myTimer;

    private TextView titleMessage;
    private TextView detailMessage;
    private CardView cardView;
    private float initialX, initialY;
    private GestureDetector gestureDetector;
    private GestureDetectorCompat simpleGestureHandler;

    private AppCompatImageView iconView;

    private int layout;

    private String TAG = getClass().getName();

    private static NotificationBanner instance;

    public interface BannerListener {
        void onViewClickListener(String notificationId, String objectId, NotificationType type);
    }

    private String notificationId;
    private String objectId;
    private NotificationType type;

    private BannerListener listener;

    //Constructors
    public NotificationBanner(){

    }

    public static NotificationBanner make(View view, Activity activity, NotificationItem notification, BannerListener listener) {

        if(instance == null){
            instance = new NotificationBanner();
        }else {
            if(instance.showBanner){
                instance.dismissBanner();
            }
        }
        instance.rootView = view;
        instance.activity = activity;
        instance.layout =  R.layout.notification_banner;
        instance.setLayout(instance.layout);
        instance.setBannerTitle(notification.getName());
        instance.setIconView(notification.getIcon());
        instance.notificationId = notification.getNId();
        instance.objectId = notification.getPId();
        instance.type = notification.getType();
        instance.listener = listener;

        if(notification.getDetailSpannable()!=null) {
            instance.setBannerMessage(notification.getDetailSpannable());
        }

        if(notification.getType() == NotificationType.Address || notification.getType() == NotificationType.Version) {
            instance.iconView.setImageTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorPrimaryDark,null)));
        }
        else {
            instance.iconView.setImageTintList(null);
        }

        instance.setListener();

        return instance;
    }

    public static NotificationBanner make(View view, Activity activity, String message, Spannable detail, int icon, String notificationId, String objectId,
                                          NotificationType type, BannerListener listener) {

        if(instance == null){
            instance = new NotificationBanner();
        }else {
            if(instance.showBanner){
                instance.dismissBanner();
            }
        }
        instance.rootView = view;
        instance.activity = activity;
        instance.layout =  R.layout.notification_banner;
        instance.setLayout(instance.layout);
        instance.setBannerTitle(message);
        instance.setIconView(icon);
        instance.notificationId = notificationId;
        instance.objectId = objectId;
        instance.type = type;
        if(detail!=null) {
            instance.setBannerMessage(detail);
        }

        if(type == NotificationType.Address || type == NotificationType.Version) {
            instance.iconView.setImageTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorPrimaryDark,null)));
        }
        else {
            instance.iconView.setImageTintList(null);
        }
        instance.listener = listener;
        instance.setListener();

        return instance;
    }

    public static NotificationBanner getInstance(){
        if(instance == null){
            instance = new NotificationBanner();
        }
        return instance;
    }

    private void setIconView(Integer icon){
        iconView = popupView.findViewById(R.id.appCompatImageView);
        iconView.setImageResource(icon);
    }

    private void setBannerTitle(String text){
        titleMessage = popupView.findViewById(R.id.titleText);
        titleMessage.setText(text);
    }

    private void setBannerMessage(Spannable text){
        detailMessage = popupView.findViewById(R.id.detailText);
        detailMessage.setVisibility(View.VISIBLE);
        detailMessage.setText(text);
    }

    private void setListener() {
        cardView = popupView.findViewById(R.id.mainCard);
        cardView.setAlpha(1.0f);

        gestureDetector = new GestureDetector(activity.getBaseContext(), new OnSwipeListener(){
            @Override
            public boolean onSwipe(Direction direction) {
                 if (direction==Direction.up){
                    dismissBanner();
                }
                return true;
            }
        });

        simpleGestureHandler = new GestureDetectorCompat(activity.getBaseContext(), new SimpleGestureListener());

        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(simpleGestureHandler.onTouchEvent(event)) {
                    AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
                    animation1.setDuration(250);
                    cardView.startAnimation(animation1);
                    listener.onViewClickListener(notificationId, objectId, type);
                    dismissBanner();
                }
                else {
                    gestureDetector.onTouchEvent(event);
                }
                return true;
            }
        });
    }

    public void setLayout(int layout){
        if(activity != null){
            LayoutInflater inflater = (LayoutInflater)
                    activity.getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            popupView = inflater.inflate(layout, null);
        }
    }

    public void dismissBanner(){
        if(showBanner) {
            try{
                popupWindow.dismiss();
                showBanner = false;
                asDropDown = false;
                myTimer.cancel();
                myTimer = null;
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }
        }
    }

    public void show(){
        if(activity!=null && !showBanner){
            showBanner = true;

            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;

            popupWindow = new PopupWindow(popupView, width, height, focusable);
            popupWindow.setAnimationStyle(R.style.topAnimation);
            rootView.post(new Runnable() {
                public void run() {
                    popupWindow.showAtLocation(rootView, Gravity.TOP, 0, 0);
                }
            });

            Vibrator v = (Vibrator) AppActivity.self.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(400);

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(AppActivity.self.getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

            autoDismiss(instance.duration);
        }
    }

    private void autoDismiss(long duration){
        if(duration > 0){
            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    AppActivity.self.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissBanner();
                        }
                    });
                }

            }, duration, 1000);
        }
    }
}
