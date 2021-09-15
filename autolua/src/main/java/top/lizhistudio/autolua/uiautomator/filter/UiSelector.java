package top.lizhistudio.autolua.uiautomator.filter;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;

public class UiSelector extends CommonLuaObjectAdapter implements Filter{
    private final ArrayList<Filter> filters;
    public UiSelector()
    {
        filters = new ArrayList<>();
    }

    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        for (Filter f :
                filters) {
            if (!f.isMatch(info))
            {
                return false;
            }
        }
        return true;
    }


    public void select(AccessibilityNodeInfo nodeInfo, List<AccessibilityNodeInfo> out,
                       int depth, int maxSum)
    {
        if (nodeInfo == null)
            return;
        if (maxSum>0 && out.size()>=maxSum)
            return;
        boolean is = isMatch(nodeInfo);
        if (is)
            out.add(nodeInfo);
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            select(nodeInfo.getChild(i),out,depth+1,maxSum);
        }
        if (!is && depth>0)
            nodeInfo.recycle();
    }

    private final ArrayList<AccessibilityNodeInfo> oneSelectOut = new ArrayList<>(1);

    public synchronized AccessibilityNodeInfo select(AccessibilityNodeInfo nodeInfo)
    {
        oneSelectOut.clear();
        select(nodeInfo,oneSelectOut,0,1);
        if (oneSelectOut.size()>0)
            return oneSelectOut.get(0);
        return null;
    }

    public void Select(AccessibilityNodeInfo nodeInfo,List<AccessibilityNodeInfo> out)
    {
        select(nodeInfo,out,0,0);
    }

    private void addClassOnlyFilter(Class<?> clazz,Filter filter)
    {
        synchronized (filters)
        {
            for (int i = 0; i < filters.size(); i++) {
                Filter f = filters.get(i);
                if (clazz.isInstance(f))
                {
                    filters.set(i,filter);
                    return;
                }
            }
            filters.add(filter);
        }
    }

    private void addFilter(Filter filter)
    {
        if (filter instanceof ClassOnlyFilter)
            addClassOnlyFilter(filter.getClass(),filter);
        else if(filter instanceof ChildClassOnlyFilter)
            addClassOnlyFilter(filter.getClass().getSuperclass(),filter);
        else
        {
            synchronized (filters)
            {
                filters.add(filter);
            }
        }
    }


    public int className(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new ClassNameSimpleFilter(s));
        L.pushValue(1);
        return 1;
    }

    public int classNamePattern(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new ClassNamePatternFilter(Pattern.compile(s)));
        L.pushValue(1);
        return 1;
    }

    public int classNameStartsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(ClassNamePatternFilter.newFilterStartsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int classNameEndsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(ClassNamePatternFilter.newFilterEndsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int classNameContains(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(ClassNamePatternFilter.newFilterContains(s));
        L.pushValue(1);
        return 1;
    }

    public int packageName(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new PackageNameSimpleFilter(s));
        L.pushValue(1);
        return 1;
    }

    public int packageNamePattern(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new PackageNamePatternFilter(Pattern.compile(s)));
        L.pushValue(1);
        return 1;
    }

    public int packageNameStartsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(PackageNamePatternFilter.newFilterStartsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int packageNameEndsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(PackageNamePatternFilter.newFilterEndsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int packageNameContains(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(PackageNamePatternFilter.newFilterContains(s));
        L.pushValue(1);
        return 1;
    }


    public int text(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new TextSimpleFilter(s));
        L.pushValue(1);
        return 1;
    }

    public int textPattern(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new TextPatternFilter(Pattern.compile(s)));
        L.pushValue(1);
        return 1;
    }

    public int textStartsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(TextPatternFilter.newFilterStartsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int textEndsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(TextPatternFilter.newFilterEndsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int textContains(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(TextPatternFilter.newFilterContains(s));
        L.pushValue(1);
        return 1;
    }

    public int resource(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new ResourceSimpleFilter(s));
        L.pushValue(1);
        return 1;
    }

    public int resourcePattern(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new ResourcePatternFilter(Pattern.compile(s)));
        L.pushValue(1);
        return 1;
    }

    public int resourceStartsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(ResourcePatternFilter.newFilterStartsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int resourceEndsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(ResourcePatternFilter.newFilterEndsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int resourceContains(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(ResourcePatternFilter.newFilterContains(s));
        L.pushValue(1);
        return 1;
    }

    public int description(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new DescriptionSimpleFilter(s));
        L.pushValue(1);
        return 1;
    }

    public int descriptionPattern(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new DescriptionPatternFilter(Pattern.compile(s)));
        L.pushValue(1);
        return 1;
    }

    public int descriptionStartsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(DescriptionPatternFilter.newFilterStartsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int descriptionEndsWith(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(DescriptionPatternFilter.newFilterEndsWith(s));
        L.pushValue(1);
        return 1;
    }

    public int descriptionContains(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(DescriptionPatternFilter.newFilterContains(s));
        L.pushValue(1);
        return 1;
    }

    public int path(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new PathFilter(Pattern.compile(Pattern.quote(s))));
        L.pushValue(1);
        return 1;
    }

    public int pathPattern(LuaContext L)
    {
        String s = L.toString(2);
        addFilter(new PathFilter(Pattern.compile(s)));
        L.pushValue(1);
        return 1;
    }


    public int scrollable(LuaContext L)
    {
        addFilter(L.toBoolean(2)?ScrollableFilter.True:ScrollableFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int checked(LuaContext L)
    {
        addFilter(L.toBoolean(2)?CheckedFilter.True:CheckedFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int clickable(LuaContext L)
    {
        addFilter(L.toBoolean(2)?ClickableFilter.True:ClickableFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int enabled(LuaContext L)
    {
        addFilter(L.toBoolean(2)?EnabledFilter.True:EnabledFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int focusable(LuaContext L)
    {
        addFilter(L.toBoolean(2)?FocusableFilter.True:FocusableFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int focused(LuaContext L)
    {
        addFilter(L.toBoolean(2)?FocusedFilter.True:FocusedFilter.False);
        L.pushValue(1);
        return 1;
    }
    public int longClickable(LuaContext L)
    {
        addFilter(L.toBoolean(2)?LongClickableFilter.True:LongClickableFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int selected(LuaContext L)
    {
        addFilter(L.toBoolean(2)?SelectedFilter.True:SelectedFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int checkable(LuaContext L)
    {
        addFilter(L.toBoolean(2)?CheckableFilter.True:CheckableFilter.False);
        L.pushValue(1);
        return 1;
    }

    public int visible(LuaContext L)
    {
        addFilter(L.toBoolean(2)?VisibleFilter.True:VisibleFilter.False);
        L.pushValue(1);
        return 1;
    }
}
