package me.next.wemoney;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

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

    private static final String VIEW_ID_CHAT_TIME = "com.tencent.mm:id/aih";
    private static final String VIEW_ID_CHAT_CONTENT = "com.tencent.mm:id/aii";
    private static final String VIEW_ID_CHAT_USER_NAME = "com.tencent.mm:id/aig";

    private HongBaoBean mHongBaoBean = new HongBaoBean();

    private String currentActivityName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        getActivityName(accessibilityEvent);

        if (handleNotification(accessibilityEvent)) {
            return;
        }
        handleChatListLuckyMoney(accessibilityEvent);

    }

    boolean haveNewLuckyPackage = false;

    private void handleChatListLuckyMoney(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }
        AccessibilityNodeInfo nodeInfo = accessibilityEvent.getSource();
        if (nodeInfo == null) {
            return;
        }

        AccessibilityNodeInfo parentNodeInfo = nodeInfo.getParent();
        if (parentNodeInfo != null) {
            parentNodeInfo = parentNodeInfo.getParent();
        }
        if (parentNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> accessibilityNodeInfos = parentNodeInfo.findAccessibilityNodeInfosByViewId(VIEW_ID_CHAT_CONTENT);
        List<AccessibilityNodeInfo> nameAccessibilityNodeInfos = parentNodeInfo.findAccessibilityNodeInfosByViewId(VIEW_ID_CHAT_USER_NAME);
        if (accessibilityNodeInfos != null && accessibilityNodeInfos.size() > 0) {
            AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfos.get(0);
            AccessibilityNodeInfo nameAccessibilityNodeInfo = nameAccessibilityNodeInfos.get(0);

            String chatContent = accessibilityNodeInfo.getText().toString();
            String chatName = nameAccessibilityNodeInfo.getText().toString();

            Log.e(TAG, nodeInfo.getClassName() + " 找到了 ByViewId : " + accessibilityNodeInfo.getText().toString());

            if (chatContent.contains(":" + WECHAT_NOTIFICATION_TIP)) { //群聊
                if (!mHongBaoBean.getChatContent().equals(chatContent)) {
                    updateHongBaoBean(chatContent, chatName);
                    toChatDetailPage(accessibilityNodeInfo);
                }
            } else if (chatContent.contains(WECHAT_NOTIFICATION_TIP)) { // 目前这种机制会错过同一个人连续发送多个红包
                if (!chatName.equals(mHongBaoBean.getChatName())) {
                    updateHongBaoBean(chatContent, chatName);
                    haveNewLuckyPackage = true;
                    toChatDetailPage(accessibilityNodeInfo);
                }
            }
        }

//        while (nodeInfo != null && !"android.widget.FrameLayout".equals(nodeInfo.getClassName())) {
////            int nodeChildCount = nodeInfo.getChildCount();
////            getChildViewText(nodeInfo, nodeChildCount);
//
//            List<AccessibilityNodeInfo> accessibilityNodeInfos = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aii");
//            if (accessibilityNodeInfos != null && accessibilityNodeInfos.size() > 0) {
//                for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos) {
//                    if (accessibilityNodeInfo.getText().toString().contains(WECHAT_NOTIFICATION_TIP)) {
//                        Log.e(TAG, nodeInfo.getClassName() + " 找到了 ByViewId : " + accessibilityNodeInfo.getText().toString());
//
//                        haveNewLuckyPackage = true;
//                        while (accessibilityNodeInfo != null && !accessibilityNodeInfo.isClickable()) {
//                            accessibilityNodeInfo = accessibilityNodeInfo.getParent();
//                        }
//                        if (accessibilityNodeInfo != null && accessibilityNodeInfo.isClickable()) {
////                                        accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        }
//                        break;
//                    }
//                }
//            } else {
//                Log.e(TAG, "没找到 ByViewId");
//            }
//            nodeInfo = nodeInfo.getParent();
//        }
    }

    private void toChatDetailPage(AccessibilityNodeInfo accessibilityNodeInfo) {
        while (accessibilityNodeInfo != null && !accessibilityNodeInfo.isClickable()) {
            accessibilityNodeInfo = accessibilityNodeInfo.getParent();
        }
        if (accessibilityNodeInfo != null && accessibilityNodeInfo.isClickable()) {
            accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void getChildViewText(AccessibilityNodeInfo nodeInfo, int nodeChildCount) {
        if (nodeChildCount != 0) {
            for (int i = 0; i < nodeChildCount; i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (childNodeInfo != null) {
//                    if ("android.widget.ListView".equals(childNodeInfo.getClassName())) {

                    List<AccessibilityNodeInfo> accessibilityNodeInfos1 = childNodeInfo.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
                    if (accessibilityNodeInfos1 != null && accessibilityNodeInfos1.size() > 0) {
                        for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos1) {
                            Log.e(TAG, "ListView 找到了 ByText : " + accessibilityNodeInfo.getText().toString());
                        }
                    } else {
                        Log.e(TAG, "ListView 没找到 ByText ");
                    }

                    List<AccessibilityNodeInfo> accessibilityNodeInfos = childNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aii");
                    if (accessibilityNodeInfos != null && accessibilityNodeInfos.size() > 0) {
                        for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos) {
                            Log.e(TAG, "ListView 找到了 ByViewId : " + accessibilityNodeInfo.getText().toString());
                            if (accessibilityNodeInfo.getText().toString().contains(WECHAT_NOTIFICATION_TIP)) {
                                while (accessibilityNodeInfo != null && !accessibilityNodeInfo.isClickable()) {
                                    accessibilityNodeInfo = accessibilityNodeInfo.getParent();
                                }
                                if (accessibilityNodeInfo != null && accessibilityNodeInfo.isClickable()) {
//                                        accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                break;
                            }
                        }
                        break;
                    } else {
                        Log.e(TAG, "ListView 没找到 ByViewId");
                    }
//                    }
                    getChildViewText(childNodeInfo, childNodeInfo.getChildCount());
                }
            }
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
