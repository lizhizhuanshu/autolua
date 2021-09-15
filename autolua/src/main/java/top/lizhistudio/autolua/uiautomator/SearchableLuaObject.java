package top.lizhistudio.autolua.uiautomator;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.autolua.uiautomator.filter.UiSelector;

public abstract class SearchableLuaObject extends CommonLuaObjectAdapter implements Searchable {

    public int hasObject(LuaContext L)
    {
        UiSelector o = (UiSelector)L.toLuaObjectAdapter(2);
        AccessibilityNodeInfo nodeInfo = o.select(getAccessibilityNodeInfo());
        boolean r = nodeInfo != null;
        if (r)
            nodeInfo.recycle();
        L.push(r);
        return 1;
    }

    public int findObject(LuaContext L)
    {
        UiSelector o = (UiSelector)L.toLuaObjectAdapter(2);
        AccessibilityNodeInfo nodeInfo = o.select(getAccessibilityNodeInfo());
        if (nodeInfo != null)
        {
            L.push(new UiObject(nodeInfo,o));
        }else
            L.pushNil();
        return 1;
    }

    static void pushUiObjects(LuaContext context, List<AccessibilityNodeInfo> list)
    {
        context.createTable(list.size(),0);
        for (int i=0;i<list.size();i++)
        {
            context.push(i+1);
            context.push(new UiObject(list.get(i),null));
            context.setTable(-3);
        }
    }

    public int findObjects(LuaContext L)
    {
        UiSelector o = (UiSelector)L.toLuaObjectAdapter(2);
        int maxSum = 0;
        if (L.isInteger(3))
            maxSum = (int)L.toLong(3);
        List<AccessibilityNodeInfo> r = new ArrayList<>();
        o.select(getAccessibilityNodeInfo(),r,0,maxSum);
        pushUiObjects(L,r);
        return 1;
    }

    protected abstract AccessibilityNodeInfo getAccessibilityNodeInfo();

}
