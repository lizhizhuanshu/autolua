package top.lizhistudio.autolua.uiautomator;


import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;


import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.autolua.conceal.IWindowManager;
import top.lizhistudio.autolua.uiautomator.filter.UiSelector;

public class UiObject extends SearchableLuaObject{
    private static final String TAG = UiObject.class.getSimpleName();
    private AccessibilityNodeInfo mCachedNode;
    private UiSelector selector;

    UiObject(AccessibilityNodeInfo nodeInfo,UiSelector selector)
    {
        this.mCachedNode = nodeInfo;
        this.selector = selector;
    }

    public int getParent(LuaContext L)
    {
        AccessibilityNodeInfo parent = getAccessibilityNodeInfo().getParent();
        L.push( parent!=null?new UiObject(parent,selector):null);
        return 1;
    }

    public int getChildCount(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().getChildCount());
        return 1;
    }

    public int getChildren(LuaContext L)
    {
        AccessibilityNodeInfo nodeInfo = getAccessibilityNodeInfo();
        int size = nodeInfo.getChildCount();
        L.createTable(size,0);
        int index = 0;
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (child != null)
            {
                L.push(++index);
                L.push(new UiObject(child,selector));
                L.setTable(-3);
            }
        }
        return 1;
    }

    public int getChild(LuaContext L)
    {
        int index = (int)L.toLong(2);
        AccessibilityNodeInfo nodeInfo = getAccessibilityNodeInfo();
        int size = nodeInfo.getChildCount();
        L.pushNil();
        if (index <size)
        {
            AccessibilityNodeInfo child = nodeInfo.getChild(index);
            if (child != null)
            {
                L.push(new UiObject(child,selector));
            }
        }
        return 1;
    }


    private Rect getVisibleBounds(AccessibilityNodeInfo node) {
        // Get the object bounds in screen coordinates
        Rect ret = new Rect();
        node.getBoundsInScreen(ret);
        Point point = new Point();
        IWindowManager.getBaseDisplaySize(IWindowManager.MAIN_DISPLAY_TOKEN,point);
        // Trim any portion of the bounds that are not on the screen
        Rect screen = new Rect(0, 0, point.x, point.y);
        ret.intersect(screen);
        Rect window = new Rect();
        if (node.getWindow() != null) {
            node.getWindow().getBoundsInScreen(window);
            ret.intersect(window);
        }

        // Find the visible bounds of our first scrollable ancestor
        AccessibilityNodeInfo ancestor = null;
        for (ancestor = node.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
            // If this ancestor is scrollable
            if (ancestor.isScrollable()) {
                // Trim any portion of the bounds that are hidden by the non-visible portion of our
                // ancestor
                Rect ancestorRect = getVisibleBounds(ancestor);
                ret.intersect(ancestorRect);
                break;
            }
        }

        return ret;
    }

    public int getVisibleBounds(LuaContext L)
    {
        Rect rect = getVisibleBounds(getAccessibilityNodeInfo());
        L.push(rect.left);
        L.push(rect.top);
        L.push(rect.right);
        L.push(rect.bottom);
        return 4;
    }

    public int bounds(LuaContext L)
    {
        Rect rect = new Rect();
        getAccessibilityNodeInfo().getBoundsInScreen(rect);
        L.push(rect.left);
        L.push(rect.top);
        L.push(rect.right);
        L.push(rect.bottom);
        return 4;
    }

    public int getClassName(LuaContext L)
    {
        CharSequence chars = getAccessibilityNodeInfo().getClassName();
        L.push(chars != null ? chars.toString() : null);
        return 1;
    }

    public int getContentDescription(LuaContext L)
    {
        CharSequence chars = getAccessibilityNodeInfo().getContentDescription();
        L.push(chars != null ? chars.toString() : null);
        return 1;
    }

    public int getPackageName(LuaContext L)
    {
        CharSequence chars = getAccessibilityNodeInfo().getPackageName();
        L.push(chars != null ? chars.toString() : null);
        return 1;
    }

    public int getResourceName(LuaContext L)
    {
        CharSequence chars = getAccessibilityNodeInfo().getViewIdResourceName();
        L.push(chars != null ? chars.toString() : null);
        return 1;
    }

    public int getText(LuaContext L)
    {
        CharSequence chars = getAccessibilityNodeInfo().getText();
        L.push(chars != null ? chars.toString() : null);
        return 1;
    }

    public int getPath(LuaContext L)
    {
        L.push(Utils.getNodePath(getAccessibilityNodeInfo()));
        return 1;
    }


    public int isChecked(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isChecked());
        return 1;
    }

    public int isSelected(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isSelected());
        return 1;
    }

    public int isClickable(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isClickable());
        return 1;
    }

    public int isEnabled(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isEnabled());
        return 1;
    }

    public int isFocusable(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isFocusable());
        return 1;
    }

    public int isFocused(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isFocused());
        return 1;
    }


    public int isLongClickable(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isLongClickable());
        return 1;
    }

    public int isScrollable(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isScrollable());
        return 1;
    }

    public int isCheckable(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isCheckable());
        return 1;
    }

    public int isVisibleToUser(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().isVisibleToUser());
        return 1;
    }

    //actions

    public int setText(LuaContext L)
    {
        String s = L.toString(2);
        Bundle args = new Bundle();
        AccessibilityNodeInfo node = getAccessibilityNodeInfo();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, s);
        boolean r = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        if (!r) {
            // TODO: Decide if we should throw here
            Log.w(TAG, "AccessibilityNodeInfo#performAction(ACTION_SET_TEXT) failed");
        }
        L.push(r);
        return 1;
    }

    private int performAction(LuaContext L,int action)
    {
        L.push(getAccessibilityNodeInfo().performAction(action));
        return 1;
    }

    public int click(LuaContext L)
    {
        return performAction(L,AccessibilityNodeInfo.ACTION_CLICK);
    }

    public int longClick(LuaContext L)
    {
        return performAction(L,AccessibilityNodeInfo.ACTION_LONG_CLICK);
    }

    public int select(LuaContext L)
    {
        return performAction(L,AccessibilityNodeInfo.ACTION_SELECT);
    }

    public int copy(LuaContext L)
    {
        return performAction(L,AccessibilityNodeInfo.ACTION_COPY);
    }

    public int paste(LuaContext L)
    {
        return performAction(L,AccessibilityNodeInfo.ACTION_PASTE);
    }

    public int cut(LuaContext L)
    {
        return performAction(L,AccessibilityNodeInfo.ACTION_CUT);
    }


    private int performActionByCompat(LuaContext L,int action)
    {
        L.push(getAccessibilityNodeInfoCompat().performAction(action));
        return 1;
    }
    public int scrollForward(LuaContext L)
    {
        return performActionByCompat(L,AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
    }

    public int scrollBackward(LuaContext L)
    {
        return performActionByCompat(L,AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
    }

    public int scrollDown(LuaContext L)
    {
        return performActionByCompat(L,
                AccessibilityActionCompat.ACTION_SCROLL_DOWN.getId());
    }

    public int scrollUp(LuaContext L)
    {
        return performActionByCompat(L,
                AccessibilityActionCompat.ACTION_SCROLL_UP.getId());
    }

    public int scrollLeft(LuaContext L)
    {
        return performActionByCompat(L,
                AccessibilityActionCompat.ACTION_SCROLL_LEFT.getId());
    }

    public int scrollRight(LuaContext L)
    {
        return performActionByCompat(L,
                AccessibilityActionCompat.ACTION_SCROLL_RIGHT.getId());
    }

    public int scrollTo(LuaContext L)
    {
        int row = (int)L.toLong(2);
        int column = (int)L.toLong(3);
        Bundle bundle = new Bundle();
        bundle.putInt(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_ROW_INT,row);
        bundle.putInt(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_COLUMN_INT,column);
        boolean result = getAccessibilityNodeInfoCompat()
                .performAction(AccessibilityActionCompat.ACTION_SCROLL_TO_POSITION.getId(),bundle);
        L.push(result);
        return 1;
    }

    public int addAction(LuaContext L)
    {
        int action = (int)L.toLong(2);
        getAccessibilityNodeInfoCompat().addAction(action);
        return 0;
    }


    public int toString(LuaContext L)
    {
        L.push(getAccessibilityNodeInfo().toString());
        return 1;
    }



    protected AccessibilityNodeInfo getAccessibilityNodeInfo() {

        if (mCachedNode == null) {
            throw new IllegalStateException("This object has already been recycled");
        }
//        try{
//            UiAutomatorAdapter.getInstance().getUiAutomation().waitForIdle(500,300);
//        }catch (TimeoutException e)
//        {
//            e.printStackTrace();
//        }
        if (!mCachedNode.refresh())
        {
            throw new IllegalStateException("This node not existent");
        }
        return mCachedNode;
    }

    private AccessibilityNodeInfoCompat mCachedNodeCompat = null;

    protected AccessibilityNodeInfoCompat getAccessibilityNodeInfoCompat()
    {
        if (mCachedNodeCompat == null)
        {
            synchronized (this)
            {
                if (mCachedNodeCompat == null)
                {
                    mCachedNodeCompat = AccessibilityNodeInfoCompat.wrap(mCachedNode);
                }
            }
        }
        if (!mCachedNodeCompat.refresh())
        {
            throw new IllegalStateException("This node not existent");
        }
        return mCachedNodeCompat;
    }

    @Override
    public void onRelease() {
        if (mCachedNodeCompat!=null)
        {
            mCachedNodeCompat.recycle();
            mCachedNodeCompat = null;
            mCachedNode = null;
        }else if (mCachedNode!=null)
        {
            mCachedNode.recycle();
            mCachedNode = null;
        }
    }
}
