package top.lizhistudio.autolua.uiautomator.filter;

import android.view.accessibility.AccessibilityNodeInfo;


public interface Filter {
    boolean isMatch(AccessibilityNodeInfo info);
}
