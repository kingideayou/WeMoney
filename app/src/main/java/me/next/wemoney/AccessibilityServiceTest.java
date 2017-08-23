package me.next.wemoney;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by NeXT on 17/8/8.
 * see: https://developer.android.com/training/accessibility/service.html
 * see: https://developer.android.com/training/accessibility/service.html#events
 */

public class AccessibilityServiceTest extends AccessibilityService {

    private static final String TAG = AccessibilityService.class.getSimpleName();

    private static final String ACTIVITY_HOME = "LauncherUI";
    private static final String ACTIVITY_CHAT_WITH = "En_5b8fbb1e"; //与他人聊天页，不常出现
    private static final String ACTIVITY_PACKAGE = "En_fba4b94f"; //打开红包页

    private String currentActivityName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        getActivityName(accessibilityEvent);

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
