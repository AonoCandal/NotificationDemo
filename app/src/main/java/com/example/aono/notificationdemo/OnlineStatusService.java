package com.example.aono.notificationdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;


import java.lang.reflect.Method;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;

public class OnlineStatusService extends Service {


    private static final String STATUS = "STATUS";
    private static final int STATUS_ONLINE = 1;
    private static final int STATUS_OFFLINE = 2;
    public static final String ACTION_CHANGE_STATUS = "action_change_status";
    public static final String ACTION_DELETE_NOTIFICATION = "action_delete_notification";
    public static final String ACTION_ERROR = "action_error";
    private int mNotificationId = 0x001;
    private NotificationCompat.Builder mBuilder;
    private boolean isOnline = false;
    public static boolean isForground = true;//是否是前台进程


    private OnStatusBroadcastReceiver receiver;
    private RemoteViews remoteViews;
    private Notification notification;

    public static void startStatusServiceOnline(Context context) {
        Intent intent = new Intent(context, OnlineStatusService.class);
        intent.putExtra(STATUS, STATUS_ONLINE);
        context.startService(intent);
    }

    public static void startStatusServiceOffline(Context context) {
        Intent intent = new Intent(context, OnlineStatusService.class);
        intent.putExtra(STATUS, STATUS_OFFLINE);
        context.startService(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        regFilter();
        setNotification();
    }

    /**
     *
     * 收起通知栏
     * @param context
     */
    public static void collapseStatusBar(Context context) {
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void setNotification(){
        remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N)
            remoteViews.setInt(R.id.tv_title, "setTextColor", NotificationUtils.getNotificationColor(this));

        // 设置播放
        if (isOnline) {
            Intent playorpause = new Intent();
            playorpause.setAction(ACTION_CHANGE_STATUS);
            PendingIntent intent_play = PendingIntent.getBroadcast(this, 2,playorpause, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.iv_change_online_status, intent_play);
        }else {
            Intent playorpause = new Intent();
            playorpause.setAction(ACTION_CHANGE_STATUS);
            PendingIntent intent_play = PendingIntent.getBroadcast(this, 6,playorpause, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.iv_change_online_status, intent_play);
        }
        /**删除*/
        Intent deleteIntent = new Intent();
        deleteIntent.setAction(ACTION_DELETE_NOTIFICATION);
        PendingIntent intent_delete = PendingIntent.getBroadcast(this, 3,deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.iv_delete_notification,intent_delete);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContent(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        notification = mBuilder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int status = intent.getIntExtra(STATUS, 0);
            if (status == STATUS_ONLINE) {
                isOnline = true;
            } else if (status == STATUS_OFFLINE) {
                isOnline = false;
            }
            changeOnlineStatus(isOnline);
        }
        return super.onStartCommand(intent, flags, startId);
    }
    /*
         * 注册广播notification
         */
    private void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHANGE_STATUS);
        filter.addAction(ACTION_DELETE_NOTIFICATION);
        filter.setPriority(SYSTEM_HIGH_PRIORITY);
        receiver = new OnStatusBroadcastReceiver();
        registerReceiver(receiver, filter); // 注册接收
    }

    /**
     * 改变上下线状态
     * @param isOnline
     */
    public void changeOnlineStatus(boolean isOnline) {
        this.isOnline = isOnline;
        if (isOnline){
//            if (!isForground)
//                ToastUtils.makeTextOnceShow(this,"您已上线");
            remoteViews.setTextViewText(R.id.tv_title,"账号在线");
            remoteViews.setTextViewText(R.id.tv_msg,"点击离线切换状态");
            remoteViews.setImageViewResource(R.id.iv_change_online_status,R.drawable.btn_offline);
            startForeground(mNotificationId,notification);
        }else {
//            if (!isForground)
//                ToastUtils.makeTextOnceShow(this,"您已离线");
            remoteViews.setTextViewText(R.id.tv_title,"账号离线");
            remoteViews.setTextViewText(R.id.tv_msg,"点击上线接单切换状态");
            remoteViews.setImageViewResource(R.id.iv_change_online_status,R.drawable.btn_online);
            startForeground(mNotificationId,notification);
        }
    }

    /**
     * 接收广播
     */
    public class OnStatusBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(ACTION_CHANGE_STATUS)) {
//                if (NetworkUtil.isNetWorkAvailable(context)) {
//                    isOnline = !isOnline;
//                    collapseStatusBar(context);
//                    changeOnlineStatus(isOnline);
//                    SyncService.changeOnlineStatus(context,isOnline);
//                }
//            }else if (intent.getAction().equals(ACTION_ERROR)){
//                //TODO:返回原来的状态
//                isOnline = !isOnline;
//                changeOnlineStatus(isOnline);
//            }else if (intent.getAction().equals(ACTION_DELETE_NOTIFICATION)){
//                collapseStatusBar(context);
//                stopForeground(true);
////                NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
////                notificationManager.cancel(mNotificationId);
//            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver); // 服务终止时解绑
        }
    }

}
