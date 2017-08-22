package me.next.wemoney;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by NeXT on 17/8/22.
 */

public class NodeUtils {

    private static final String TAG = "微信红包";

    public static void getPackageInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) {
            return;
        }
        AccessibilityNodeInfo parentNodeInfo = accessibilityNodeInfo.getParent();
        if (parentNodeInfo == null) {
            return;
        }
        CharSequence cs = parentNodeInfo.getChild(0).getText();
        if (cs == null) {
            Log.d(TAG, "parentNodeInfo child(0) text is null");
            return;
        }
        Log.d(TAG, "parentNodeInfo : " + parentNodeInfo.toString());
        AccessibilityNodeInfo messageNode = parentNodeInfo.getParent();
        if (null == messageNode) {
            Log.d(TAG, "messageNode is null ");
            return;
        }
        Log.d(TAG, "messageNode : " + messageNode.toString());
        int nodeCount = messageNode.getChildCount();
        if (nodeCount == 0) {
            return;
        }
        for (int i = 0; i < nodeCount; i++) {
            AccessibilityNodeInfo nodeInfo = messageNode.getChild(i);
            switch (nodeInfo.getClassName().toString()) {
                case "android.widget.ImageView":
                    String imgDesc = nodeInfo.getContentDescription().toString();
                    Log.d(TAG, "imgDesc = " + imgDesc);
                    break;
                case "android.widget.TextView":
                    String text = nodeInfo.getText().toString();
                    Log.d(TAG, "text = " + text);
                    break;
            }
        }
    }

}
