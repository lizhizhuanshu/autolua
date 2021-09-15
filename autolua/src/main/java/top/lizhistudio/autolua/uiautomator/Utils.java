package top.lizhistudio.autolua.uiautomator;

import android.view.accessibility.AccessibilityNodeInfo;

public class Utils {
    public static void getNodePath(AccessibilityNodeInfo node, StringBuilder builder)
    {
        AccessibilityNodeInfo father = node.getParent();
        if (father != null)
        {
            getNodePath(father,builder);
        }
        builder.append('/');
        CharSequence className = node.getClassName();
        int index = className.toString().lastIndexOf('.');
        builder.append(className,index+1,className.length()-1);
    }

    public static String getNodePath(AccessibilityNodeInfo nodeInfo)
    {
        StringBuilder builder = new StringBuilder();
        getNodePath(nodeInfo,builder);
        return builder.toString();
    }
}
