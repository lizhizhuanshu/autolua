package top.lizhistudio.autolua.extend;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.annotation.LuaMethod;
import top.lizhistudio.autolua.conceal.InputManagerWrap;

public class Controller {
    private final InputDevice touchDevice;

    private long lastTouchDown;
    private final PointersState pointersState = new PointersState();
    private final MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[PointersState.MAX_POINTERS];
    private final MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[PointersState.MAX_POINTERS];


    private void initPointers() {
        for (int i = 0; i < PointersState.MAX_POINTERS; ++i) {
            MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
            props.toolType = MotionEvent.TOOL_TYPE_FINGER;
            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            coords.orientation = 0;
            coords.size = 1;
            pointerProperties[i] = props;
            pointerCoords[i] = coords;
        }
    }

    private Controller()
    {
        touchDevice = InputManagerWrap.getTouchDevice();
        initPointers();
    }


    private boolean updateTouch(int action, int pointerIndex, int buttons) {
        long now = SystemClock.uptimeMillis();

        int pointerCount = pointersState.update(pointerProperties, pointerCoords);

        if (pointerCount == 1) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchDown = now;
            }
        } else {
            // secondary pointers must use ACTION_POINTER_* ORed with the pointerIndex
            if (action == MotionEvent.ACTION_UP) {
                action = MotionEvent.ACTION_POINTER_UP | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            } else if (action == MotionEvent.ACTION_DOWN) {
                action = MotionEvent.ACTION_POINTER_DOWN | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            }
        }

        MotionEvent event = MotionEvent
                .obtain(lastTouchDown, now, action, pointerCount, pointerProperties, pointerCoords, 0, buttons, 1f, 1f,touchDevice.getId(), 0,
                        touchDevice.getSources(), 0);
        return InputManagerWrap.injectInputEvent(event, 2);
    }


    @LuaMethod
    public int touchDown(LuaContext context)
    {
        Pointer pointer = pointersState.newPointer();
        if (pointer != null)
        {
            pointer.getPoint().x = (float)context.toNumber(2);
            pointer.getPoint().y = (float)context.toNumber(3);
            if (context.type(4) == LuaContext.LUA_TNUMBER)
                pointer.setMajor((float)context.toNumber(4));
            if (context.type(5) == LuaContext.LUA_TNUMBER)
                pointer.setMinor((float)context.toNumber(5));
            if (context.type(6) == LuaContext.LUA_TNUMBER)
                pointer.setPressure((float)context.toNumber(6));
            int index = pointersState.getPointerIndex(pointer.getLocalId());
            if(updateTouch(MotionEvent.ACTION_DOWN,index,0))
            {
                context.push(pointer.getLocalId());
            }else {
                pointersState.remove(index);
                context.push(-1);
            }

        }else
        {
            context.push(-1);
        }
        return 1;
    }


    @LuaMethod
    public int touchMove(LuaContext context)
    {
        int id = (int)context.toInteger(2);
        int index = pointersState.getPointerIndex(id);
        if (index >=0)
        {
            Pointer pointer = pointersState.get(index);
            pointer.getPoint().x = (float)context.toNumber(3);
            pointer.getPoint().y = (float)context.toNumber(4);
            if (context.type(5) == LuaContext.LUA_TNUMBER)
                pointer.setMajor((float)context.toNumber(5));
            if (context.type(6) == LuaContext.LUA_TNUMBER)
                pointer.setMinor((float)context.toNumber(6));
            if (context.type(7) == LuaContext.LUA_TNUMBER)
                pointer.setPressure((float)context.toNumber(7));
            boolean result = updateTouch(MotionEvent.ACTION_MOVE,index,0);
            context.push(result);
            if(!result)
            {
                pointersState.remove(index);
            }
        }else{
            context.push(false);
        }
        return 1;
    }

    @LuaMethod
    public int touchUp(LuaContext context)
    {
        int id = (int)context.toInteger(2);
        int index = pointersState.getPointerIndex(id);
        context.push(index>=0 && updateTouch(MotionEvent.ACTION_UP,index,0));
        return 1;
    }


    private static final class Default
    {
        private static final Controller instance = new Controller();
    }

    public static Controller getDefault()
    {
        return Default.instance;
    }
}
