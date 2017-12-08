package cn.com.pyc.suizhi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.sz.help.KeyHelp;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.DeviceUtil;
import com.sz.mobilesdk.util.SZLog;

import java.util.ArrayList;

import cn.com.pyc.suizhi.SZMusicPlayActivity;
import cn.com.pyc.suizhi.common.MusicMode;
import cn.com.pyc.suizhi.help.MusicHelp;
import cn.com.pyc.suizhi.model.FileData;

/**
 * 悬浮图标服务,主要应用在音乐播放时候 <br/>
 * 桌面不显示，音乐界面不显示
 *
 * @author hudq
 */
public class MusicViewService extends BaseMusicService {

    private static final String TAG = "MusicViewService";

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;
    private FrameLayout mView;
    private ImageView mImageView;
    private int mTouchStartX;
    private int mTouchStartY;
    private int mInScreenX;
    private int mInScreenY;
    private boolean isAdd = false;

    private long endMillis;
    private long startMillis;

    //private String curFileId;
    private String myProId;
    private String productName;
    private String albumPic;
    private ArrayList<FileData> dataList;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        createParams();
        // 注册监听HOME键的广播
        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHomeKeyEventReceiver);
    }

    //待检验
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null
                    && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    // 表示按了home键,程序到了后台
                    removeView();
                } else if (TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {
                    // 表示长按home键,显示最近使用的程序列表
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopService();
            return super.onStartCommand(intent, flags, startId);//Service.START_STICKY;
        }

        if (intent.hasExtra(KeyHelp.MVS_NAME))
            productName = intent.getStringExtra(KeyHelp.MVS_NAME);
        if (intent.hasExtra(KeyHelp.MVS_MYPROID))
            myProId = intent.getStringExtra(KeyHelp.MVS_MYPROID);
        if (intent.hasExtra(KeyHelp.MVS_MYPROURL))
            albumPic = intent.getStringExtra(KeyHelp.MVS_MYPROURL);
//        if (intent.hasExtra(KeyHelp.MVS_FILEID))
//            curFileId = intent.getStringExtra(KeyHelp.MVS_FILEID);
        if (intent.hasExtra(KeyHelp.MVS_FILE_LIST)) {
            dataList = intent.getParcelableArrayListExtra(KeyHelp.MVS_FILE_LIST);
        }

        if (intent.hasExtra(KeyHelp.MVS_OPTION)) {
            int option = intent.getIntExtra(KeyHelp.MVS_OPTION, -1);
            SZLog.d("option = " + option);
            switchShow(option);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void switchShow(int option) {
        switch (option) {
            case MusicMode.Suspend.SHOW:
                addView();
                break;
            case MusicMode.Suspend.ROTATE:
                rotateView();
                break;
            case MusicMode.Suspend.HALT:
                haltView();
                break;
            case MusicMode.Suspend.REMOVE:
                removeView();
                break;
            case MusicMode.Suspend.END:
                stopService();
                break;
        }
    }

    private void addView() {
        if (mWindowManager != null) {
            if (!isAdd) {
                mWindowManager.addView(createView(), wmParams);
                isAdd = true;
            }
        }
    }

    private void removeView() {
        if (mImageView != null) {
            mImageView.clearAnimation();
        }
        if (mWindowManager != null)
            if (isAdd && mView != null) {
                mWindowManager.removeView(mView);
                isAdd = false;
            }
    }

    private void stopService() {
        removeView();
        stopSelf();
    }

    private void rotateView() {
        if (mImageView != null) {
            if (mImageView.getAnimation() == null) {
                Animation an = new RotateAnimation(0, 360,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                an.setInterpolator(new LinearInterpolator());// 不停顿
                an.setRepeatCount(Animation.INFINITE);// 重复次数
                an.setFillEnabled(true);
                an.setFillBefore(false);
                an.setFillAfter(true);// 停在最后
                an.setDuration(2800);
                mImageView.startAnimation(an);
            }
        }
    }

    private void haltView() {
        if (mImageView != null) {
            mImageView.clearAnimation();
        }
    }

    private void createParams() {
        // 获取WindowManager
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        Point outSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(outSize);
        int screenHeight = outSize.y;

        // 设置LayoutParams(全局变量）相关参数
        wmParams = new WindowManager.LayoutParams();

        if (DeviceUtil.hasKitkat()) {
            // 使用TYPE_TOAST这个不需要权限的type时, 悬浮窗正常显示, 但低版本不能接受触摸事件.
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            // 那么优先级比TYPE_SYSTEM_ALERT会降低一些, 即拉下通知栏不可见
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;

        // 设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;

        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.gravity = Gravity.LEFT | Gravity.TOP; // 调整悬浮窗口至左上角
        wmParams.x = 0;
        wmParams.y = screenHeight / 2;
        // 设置悬浮窗口长宽数据
        wmParams.width = CommonUtil.dip2px(this, 46);
        wmParams.height = CommonUtil.dip2px(this, 46);
    }

    private View createView() {
        mView = new FrameLayout(getApplicationContext());
        mImageView = new ImageView(getApplicationContext());
        mImageView.setScaleType(ScaleType.CENTER_CROP);
        mImageView.setImageResource(cn.com.pyc.pbb.R.drawable.ic_music_circle);
        mView.addView(mImageView);

        final int mStatusHeight = CommonUtil.getStatusHeight(this);
        mView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                mInScreenX = (int) event.getRawX();
                mInScreenY = (int) event.getRawY() - mStatusHeight;
                //DRMLog.i("mInScreenX= " + mInScreenX + "，yInScreen= "+ mInScreenY);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        startMillis = System.currentTimeMillis();
                        // 获取相对View的坐标，即以此View左上角为原点
                        mTouchStartX = (int) event.getX();
                        mTouchStartY = (int) event.getY();
                        //DRMLog.d("mTouchStartX= " + mTouchStartX + "，mTouchStartY= " +
                        // mTouchStartY);
                    }
                    break;
                    case MotionEvent.ACTION_MOVE: {
                        // 更新浮动窗口位置参数
                        wmParams.x = mInScreenX - mTouchStartX;
                        wmParams.y = mInScreenY - mTouchStartY;
                        mWindowManager.updateViewLayout(mView, wmParams);
                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        mTouchStartX = 0;
                        mTouchStartY = 0;
                        endMillis = System.currentTimeMillis();
                        if ((endMillis - startMillis) < 101) {
                            if (myProId == null) {
                                stopService();
                                return true;
                            }
                            Intent intent = new Intent(mContext, SZMusicPlayActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(KeyHelp.KEY_MYPRO_ID, myProId); //必传
                            intent.putExtra(KeyHelp.KEY_PRO_NAME, productName);
                            intent.putExtra(KeyHelp.KEY_PRO_URL, albumPic);
                            intent.putExtra(KeyHelp.KEY_FILE_ID, MusicHelp.getCurrentPlayId());
                            intent.putParcelableArrayListExtra(KeyHelp.KEY_FILE_LIST, dataList);
                            mContext.startActivity(intent);
                        }
                    }
                    break;
                }
                return true;
            }
        });
        return mView;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

}
