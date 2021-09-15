package top.lizhistudio.autolua.uiautomator.filter;

import android.view.accessibility.AccessibilityNodeInfo;

abstract class BaseFilter extends ClassOnlyFilter {
    protected final boolean m;
    BaseFilter(boolean v)
    {
        this.m = v;
    }
}

class ClickableFilter extends BaseFilter {
    private ClickableFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isClickable() == m;
    }
    static final ClickableFilter True = new ClickableFilter(true);
    static final ClickableFilter False = new ClickableFilter(false);
}


class SelectedFilter extends BaseFilter {
    private SelectedFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isSelected() == m;
    }
    static final SelectedFilter True = new SelectedFilter(true);
    static final SelectedFilter False = new SelectedFilter(false);
}

class CheckedFilter extends BaseFilter {
    private CheckedFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isChecked() == m;
    }
    static final CheckedFilter True = new CheckedFilter(true);
    static final CheckedFilter False = new CheckedFilter(false);
}

class CheckableFilter extends BaseFilter {
    private CheckableFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isCheckable() == m;
    }
    static final CheckableFilter True = new CheckableFilter(true);
    static final CheckableFilter False = new CheckableFilter(false);
}

class EnabledFilter extends BaseFilter {
    private EnabledFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isEnabled() == m;
    }
    static final EnabledFilter True = new EnabledFilter(true);
    static final EnabledFilter False = new EnabledFilter(false);
}

class FocusableFilter extends BaseFilter {
    private FocusableFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isFocusable() == m;
    }
    static final FocusableFilter True = new FocusableFilter(true);
    static final FocusableFilter False = new FocusableFilter(false);
}

class FocusedFilter extends BaseFilter {
    private FocusedFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isFocused() == m;
    }
    static final FocusedFilter True = new FocusedFilter(true);
    static final FocusedFilter False = new FocusedFilter(false);
}

class LongClickableFilter extends BaseFilter {
    private LongClickableFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isLongClickable() == m;
    }
    static final LongClickableFilter True = new LongClickableFilter(true);
    static final LongClickableFilter False = new LongClickableFilter(false);
}

class ScrollableFilter extends BaseFilter {
    private ScrollableFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isScrollable() == m;
    }
    static final ScrollableFilter True = new ScrollableFilter(true);
    static final ScrollableFilter False = new ScrollableFilter(false);
}

class VisibleFilter extends BaseFilter {
    private VisibleFilter(boolean v)
    {
        super(v);
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return info.isVisibleToUser() == m;
    }
    static final VisibleFilter True = new VisibleFilter(true);
    static final VisibleFilter False = new VisibleFilter(false);
}