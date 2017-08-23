package me.next.wemoney;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by NeXT on 17/8/8.
 * see: https://developer.android.com/training/accessibility/service.html
 * see: https://developer.android.com/training/accessibility/service.html#events
 */

public class AccessibilityServiceTest extends AccessibilityService {

    private static final String TAG = AccessibilityService.class.getSimpleName();

    private static final String WECHAT_NOTIFICATION_TIP = "[微信红包]";//通知栏推送文案

    private static final String ACTIVITY_HOME = "LauncherUI";
    private static final String ACTIVITY_CHAT_WITH = "En_5b8fbb1e"; //与他人聊天页，不常出现
    private static final String ACTIVITY_PACKAGE = "En_fba4b94f"; //打开红包页

    private String currentActivityName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        getActivityName(accessibilityEvent);

        if (handleNotification(accessibilityEvent)) {
            return;
        }

    }

    /**
     * 监听通知栏变化，如果包含 [微信红包] 字样，则点击该条通知
     * 注意：
     *  1.如果微信处于前台，并且当前页面为聊天列表页面，则不会显示通知
     *  2.如果微信处于前台，并且当前页面为与 xx 聊天页，不会接收到 {@link AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED} 类型的事件
     */
    private boolean handleNotification(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            return false;
        }
        String notificationTitle = accessibilityEvent.getText().toString();
        if (!notificationTitle.contains(WECHAT_NOTIFICATION_TIP)) {
            return false;
        }
        Parcelable parcelable = accessibilityEvent.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            try {
                notification.contentIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private void getActivityName(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        currentActivityName = accessibilityEvent.getClassName().toString();
        Log.d(TAG, "currentActivityName : " + currentActivityName);
    }

    @Override
    public void onInterrupt() {

    }
}
