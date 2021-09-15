package top.lizhistudio.autolua.uiautomator.filter;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.regex.Pattern;

import top.lizhistudio.autolua.uiautomator.Utils;

class PathFilter implements Filter{
    private final Pattern pattern;
    PathFilter(Pattern pattern)
    {
        this.pattern = pattern;
    }

    @Override
    public synchronized boolean isMatch(AccessibilityNodeInfo info) {
        StringBuilder stringBuilder = new StringBuilder();
        Utils.getNodePath(info, stringBuilder);
        return pattern.matcher(stringBuilder.toString()).matches();
    }
}
