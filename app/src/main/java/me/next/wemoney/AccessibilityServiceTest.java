package me.next.wemoney;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Path;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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

    private static final String TAG = "微信红包";
    private static final String WECHAT_NOTIFICATION_TIP = "[微信红包]";
    private static final String WECHAT_LUCKY_MONEY_GET = "领取红包";
    private static final String PAGE_LUCK_MONEY_OPEN = "En_fba4b94f";

    /*
    @Override
    protected void onServiceConnected() {

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // Set the type of events that this service wants to listen to.  Others
        // won't be passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;

        // If you only want this service to work with specific applications, set their
        // package names here.  Otherwise, when the service is activated, it will listen
        // to events from all applications.
        info.packageNames = new String[]
                {"com.tencent.mm"};

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated.  This service *is*
        // application-specific, so the flag isn't necessary.  If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);

    }
    */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        final int eventType = accessibilityEvent.getEventType();

        try {
            Log.d(TAG, accessibilityEvent.getClassName().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        handleChatListLuckyMoney(accessibilityEvent);
        if (handleNotification(accessibilityEvent)) {
            return;
        }
        //监听聊天列表，进入聊天室页
        handleChatListLuckyMoney(accessibilityEvent);
        //聊天室点击红包
        handleListLuckyMoney(accessibilityEvent);
        //红包详情拆开红包
        openLuckyPackage(accessibilityEvent);

//        switch(eventType) {
//            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                Log.d(TAG, "state : TYPE_NOTIFICATION_STATE_CHANGED");
//                //收到通知栏红包提醒
//                break;
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: //Represents the event of opening a PopupWindow, Menu, Dialog, etc.
////                Log.d(TAG, "state : TYPE_WINDOW_STATE_CHANGED");
//                handleListLuckyMoney(accessibilityEvent);
//                break;
//            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: //Represents the event of changing the content of a window and more specifically the sub-tree rooted at the event's source.
////                Log.d(TAG, "state : TYPE_WINDOW_CONTENT_CHANGED");
//                handleListLuckyMoney(accessibilityEvent);
//                break;
//        }

    }

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


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListLuckyMoney(AccessibilityEvent accessibilityEvent) {
//        if (accessibilityEvent == null || accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            return;
//        }
        AccessibilityNodeInfo nodeInfo = accessibilityEvent.getSource();
//        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }

        while (nodeInfo != null) {
            int nodeChildCount = nodeInfo.getChildCount();
            getChildViewText(nodeInfo, nodeChildCount);
            nodeInfo = nodeInfo.getParent();
        }


//        while (true) {
//            if (nodeInfo == null) {
//                break;
//            }
//            List<AccessibilityNodeInfo> accessibilityNodeInfos = nodeInfo.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
//            if (!accessibilityNodeInfos.isEmpty()) {
//                Log.e(TAG, "找到了？" + accessibilityNodeInfos.get(0).toString());
//                break;
//            }
//            Log.e(TAG, "遍历一遍：" + nodeInfo.toString());
//            nodeInfo = nodeInfo.getParent();
//
//        }

        List<AccessibilityNodeInfo> accessibilityNodeInfos = nodeInfo.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
        if (accessibilityNodeInfos.isEmpty()) {
            return;
        }
        AccessibilityNodeInfo nodeToClick;
        nodeToClick = accessibilityNodeInfos.get(0);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getChildViewText(AccessibilityNodeInfo nodeInfo, int nodeChildCount) {
        if (nodeChildCount != 0) {
            for (int i = 0; i < nodeChildCount; i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (childNodeInfo != null) {
                    if ("android.widget.ListView".equals(childNodeInfo.getClassName())) {
                        Log.e(TAG, "ListView : " + childNodeInfo.toString());
                        Log.e(TAG, "ListView ChildCount : " + childNodeInfo.getChildCount());
                        List<AccessibilityNodeInfo> accessibilityNodeInfos1 = childNodeInfo.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
                        if (accessibilityNodeInfos1 != null && accessibilityNodeInfos1.size() > 0) {
                            for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos1) {
                                Log.e(TAG, "ListView 找到了 111 : " + accessibilityNodeInfo.getText().toString());
                            }
                        } else {
                            Log.e(TAG, "ListView 没找到 111 ");
                        }
                        List<AccessibilityNodeInfo> accessibilityNodeInfos = childNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aii");
                        if (accessibilityNodeInfos != null && accessibilityNodeInfos.size() > 0) {
                            for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos) {
                                Log.e(TAG, "ListView 找到了: " + accessibilityNodeInfo.getText().toString());
                                if (accessibilityNodeInfo.getText().toString().contains(WECHAT_NOTIFICATION_TIP)) {
                                    while (!accessibilityNodeInfo.isClickable()) {
                                        accessibilityNodeInfo = accessibilityNodeInfo.getParent();
                                    }
                                    if (accessibilityNodeInfo.isClickable()) {
                                        accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "ListView 没找到");
                        }
                    }

//                    List<AccessibilityNodeInfo> accessibilityNodeInfos = childNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aii");
                    List<AccessibilityNodeInfo> accessibilityNodeInfos = childNodeInfo.findAccessibilityNodeInfosByText("微信红包");
                    if (accessibilityNodeInfos != null && accessibilityNodeInfos.size() > 0) {
                        for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos) {
                            Log.e(TAG, "找到了: " + accessibilityNodeInfo.getText().toString());
                        }
                    } else {
//                        Log.e(TAG, "没找到");
                    }

                    CharSequence viewText = childNodeInfo.getText();
                    if (!TextUtils.isEmpty(viewText)) {
                        Log.e(TAG, "viewText : " + viewText);
                    }
                    getChildViewText(childNodeInfo, childNodeInfo.getChildCount());
                }
            }
        }
    }

    /**
     * 遍历聊天列表
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleListLuckyMoney(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent == null || accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }
        AccessibilityNodeInfo nodeInfo = accessibilityEvent.getSource();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> accessibilityNodeInfos = nodeInfo.findAccessibilityNodeInfosByText(WECHAT_LUCKY_MONEY_GET);
        if (accessibilityNodeInfos.isEmpty()) {
            return;
        }
        Log.d(TAG, "聊天页面 ： " + nodeInfo.toString());
        AccessibilityNodeInfo nodeToClick;
        nodeToClick = accessibilityNodeInfos.get(0);
        if (nodeToClick == null) {
            return;
        }
        CharSequence description = nodeToClick.getContentDescription();
        CharSequence parentDescription = nodeToClick.getParent().getContentDescription();

        Log.d(TAG, "nodeDesc : " + nodeToClick.toString() + " - " + nodeToClick.getParent().toString());
        // 领取红包按钮没有点击事件，需要调用父控件的点击事件
        if (nodeToClick.getParent() != null) {
            NodeUtils.getPackageInfo(nodeToClick);
            nodeToClick.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

    }

    /**
     * 点击「開」按钮
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void openLuckyPackage(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo rootNodeInfo = accessibilityEvent.getSource();
        AccessibilityNodeInfo openButtonNodeInfo = findOpenButton(rootNodeInfo);
        if (!accessibilityEvent.getClassName().toString().contains(PAGE_LUCK_MONEY_OPEN)) {
            return;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float dpi = metrics.density;
        if (android.os.Build.VERSION.SDK_INT <= 23) {
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
        if ("android.widget.Button".equals(rootNodeInfo.getClassName())) {
            return rootNodeInfo;
        }
        int childCount = rootNodeInfo.getChildCount();
        if (childCount == 0) {
            return null;
        }
        AccessibilityNodeInfo button;
        int nodeChildCount = rootNodeInfo.getChildCount();
        for (int i = 0; i < nodeChildCount; i++) {
            button = findOpenButton(rootNodeInfo.getChild(i));
            if (button != null) {
                return button;
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean performClick(List<AccessibilityNodeInfo> nodeInfos) {
        for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
            if (nodeInfo.isClickable() && nodeInfo.isEnabled()) {
                return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {

    }
}
