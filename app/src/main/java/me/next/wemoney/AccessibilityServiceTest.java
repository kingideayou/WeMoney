package me.next.wemoney;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Path;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

/**
 * Created by NeXT on 17/8/8.
 * see: https://developer.android.com/training/accessibility/service.html
 * see: https://developer.android.com/training/accessibility/service.html#events
 */

public class AccessibilityServiceTest extends AccessibilityService {

    private static final String TAG = AccessibilityService.class.getSimpleName();

    private static final String WECHAT_NOTIFICATION_TIP = "[微信红包]";//通知栏推送文案
    private static final String WECHAT_LUCKY_MONEY_GET = "领取红包";//聊天页红包描述

    private static final String ACTIVITY_HOME = "LauncherUI";
    private static final String ACTIVITY_CHAT_WITH = "En_5b8fbb1e"; //与他人聊天页，不常出现
    private static final String ACTIVITY_PACKAGE = "En_fba4b94f"; //打开红包页

    private static final String VIEW_ID_CHAT_TIME = "com.tencent.mm:id/aih";
    private static final String VIEW_ID_CHAT_CONTENT = "com.tencent.mm:id/aje";
    private static final String VIEW_ID_CHAT_USER_NAME = "com.tencent.mm:id/ajc";
    private static final String VIEW_ID_CHAT_OPEN_BUTTON = "com.tencent.mm:id/bp6";

    private HongBaoBean mHongBaoBean = new HongBaoBean();
    AccessibilityNodeInfo rootNodeInfo;

    private String currentActivityName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        getActivityName(accessibilityEvent);

        if (handleNotification(accessibilityEvent)) {
            return;
        }
        if (handleChatListLuckyMoney(accessibilityEvent)) {
            return;
        }

        handleChatDetailLuckyMoney(accessibilityEvent);
        openLuckyPackage(accessibilityEvent);
    }

    /**
     * 开红包
     */
    private void openLuckyPackage(final AccessibilityEvent accessibilityEvent) {
        Log.e(TAG, "Event Class Name : " + accessibilityEvent.getClassName().toString());
        Log.e(TAG, "Event Class Name currentActivityName : " + currentActivityName);
        Log.e(TAG, "Event Class Name eventType : " + accessibilityEvent.getEventType());
        if (accessibilityEvent.getEventType() != TYPE_WINDOW_STATE_CHANGED || !currentActivityName.contains(ACTIVITY_PACKAGE)) {
            return;
        }

        AccessibilityNodeInfo openButtonNodeInfo = findOpenButton(accessibilityEvent.getSource());
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float dpi = metrics.density;
        if (android.os.Build.VERSION.SDK_INT <= 23) {
            if (openButtonNodeInfo == null) {
                return;
            }
            openButtonNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            if (android.os.Build.VERSION.SDK_INT > 23) {
                Path path = new Path();
                if (640 == dpi) {
                    path.moveTo(720, 1575);
                } else {
                    path.moveTo(540, 1060);
                }
                GestureDescription.Builder builder = new GestureDescription.Builder();
                GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(path, 450, 50)).build();
                dispatchGesture(gestureDescription, new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                    }
                }, null);

            }
        }
    }

    /**
     * 获取拆红包页按钮
     * 该页面只有「開」按钮为 Button 类型
     * @param rootNodeInfo ParentNodeInfo
     * @return 「開」按钮的 NodeInfo
     */
    private AccessibilityNodeInfo findOpenButton(AccessibilityNodeInfo rootNodeInfo) {
        if (rootNodeInfo == null) {
            return null;
        }

        Log.e(TAG, "Event Class Name info : " + rootNodeInfo.toString());

        List<AccessibilityNodeInfo> accessibilityNodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(VIEW_ID_CHAT_OPEN_BUTTON);
        if (accessibilityNodeInfoList != null && accessibilityNodeInfoList.size() > 0) {
            return accessibilityNodeInfoList.get(0);
        }

        while (rootNodeInfo != null) {

            if ("android.widget.Button".equals(rootNodeInfo.getClassName())) {
                return rootNodeInfo;
            }

            int nodeChildCount = rootNodeInfo.getChildCount();
            for (int i = 0; i < nodeChildCount; i++) {
                AccessibilityNodeInfo childNodeInfo = rootNodeInfo.getChild(i);
                if (childNodeInfo != null && "android.widget.Button".equals(childNodeInfo.getClassName())) {
                    return childNodeInfo;
                }
            }
            rootNodeInfo = rootNodeInfo.getParent();
        }
        return null;
    }

    /**
     * 打开聊天列表中的红包
     */
    private void handleChatDetailLuckyMoney(AccessibilityEvent accessibilityEvent) {
        rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> accessibilityNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByText(WECHAT_LUCKY_MONEY_GET);
        if (accessibilityNodeInfos.isEmpty()) {
            return;
        }
        AccessibilityNodeInfo nodeToClick = accessibilityNodeInfos.get(0);
        if (nodeToClick == null) {
            return;
        }
        CharSequence description = nodeToClick.getContentDescription();
        CharSequence parentDescription = nodeToClick.getParent().getContentDescription();

        Log.d(TAG, "nodeDesc : " + nodeToClick.toString() + " - " + nodeToClick.getParent().toString());
        // 领取红包按钮没有点击事件，需要调用父控件的点击事件
        if (nodeToClick.getParent() != null) {
            nodeToClick.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 监听首页聊天列表，监听新红包
     * @return 有红包？
     */
    private boolean handleChatListLuckyMoney(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return false;
        }

        AccessibilityNodeInfo nodeInfo = accessibilityEvent.getSource();
        if (nodeInfo == null) {
            return false;
        }

        AccessibilityNodeInfo parentNodeInfo = nodeInfo.getParent();
        if (parentNodeInfo != null) {
            parentNodeInfo = parentNodeInfo.getParent();
        }
        if (parentNodeInfo == null) {
            return false;
        }
        List<AccessibilityNodeInfo> accessibilityNodeInfos = parentNodeInfo.findAccessibilityNodeInfosByViewId(VIEW_ID_CHAT_CONTENT);
//        List<AccessibilityNodeInfo> accessibilityNodeInfos = parentNodeInfo.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
        List<AccessibilityNodeInfo> nameAccessibilityNodeInfos = parentNodeInfo.findAccessibilityNodeInfosByViewId(VIEW_ID_CHAT_USER_NAME);
//        List<AccessibilityNodeInfo> nameAccessibilityNodeInfos = parentNodeInfo.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
        if (accessibilityNodeInfos != null && accessibilityNodeInfos.size() > 0) {
            AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfos.get(0);
            AccessibilityNodeInfo nameAccessibilityNodeInfo = nameAccessibilityNodeInfos.get(0);

            if (accessibilityNodeInfo == null) {
                return false;
            }

            String chatContent = accessibilityNodeInfo.getText().toString();
            String chatName = nameAccessibilityNodeInfo.getText().toString();

            Log.e(TAG, nodeInfo.getClassName() + " 找到了 ByViewId : " + accessibilityNodeInfo.getText().toString());

            if (chatContent.contains(":" + WECHAT_NOTIFICATION_TIP)) { //群聊
                if (!mHongBaoBean.getChatContent().equals(chatContent)) {
                    updateHongBaoBean(chatContent, chatName);
                    return toChatDetailPage(accessibilityNodeInfo);
                }
            } else if (chatContent.contains(WECHAT_NOTIFICATION_TIP)) { // 目前这种机制会错过同一个人连续发送多个红包
                if (!chatName.equals(mHongBaoBean.getChatName())) {
                    updateHongBaoBean(chatContent, chatName);
                    return toChatDetailPage(accessibilityNodeInfo);
                } else if (!chatContent.equals(mHongBaoBean.getChatContent())){
                    updateHongBaoBean(chatContent, chatName);
                    return toChatDetailPage(accessibilityNodeInfo);
                }
            }
        }
        return false;
    }

    private boolean toChatDetailPage(AccessibilityNodeInfo accessibilityNodeInfo) {
        while (accessibilityNodeInfo != null && !accessibilityNodeInfo.isClickable()) {
            accessibilityNodeInfo = accessibilityNodeInfo.getParent();
        }
        if (accessibilityNodeInfo != null && accessibilityNodeInfo.isClickable()) {
            accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        return true;
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
        if (accessibilityEvent.getEventType() != TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        currentActivityName = accessibilityEvent.getClassName().toString();
        Log.d(TAG, "currentActivityName : " + currentActivityName);
    }

    @Override
    public void onInterrupt() {

    }

    private void updateHongBaoBean(String chatContent, String chatName) {
        mHongBaoBean.setChatName(chatName);
        mHongBaoBean.setChatContent(chatContent);
    }

    public HongBaoBean getHongBaoBean() {
        return mHongBaoBean;
    }

    public void setHongBaoBean(HongBaoBean hongBaoBean) {
        mHongBaoBean = hongBaoBean;
    }

}
