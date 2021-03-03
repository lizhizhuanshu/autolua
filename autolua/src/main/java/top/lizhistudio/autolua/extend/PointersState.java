package top.lizhistudio.autolua.extend;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class PointersState {

    public static final int MAX_POINTERS = 10;
    private final List<Pointer> pointers = new ArrayList<>();

    private boolean isLocalIdAvailable(int localId) {
        for (int i = 0; i < pointers.size(); ++i) {
            Pointer pointer = pointers.get(i);
            if (pointer.getLocalId() == localId) {
                return false;
            }
        }
        return true;
    }

    private int nextUnusedLocalId() {
        for (int localId = 0; localId < MAX_POINTERS; ++localId) {
            if (isLocalIdAvailable(localId)) {
                return localId;
            }
        }
        return -1;
    }

    public Pointer get(int index) {
        return pointers.get(index);
    }

    public Pointer newPointer()
    {
        if (pointers.size() >= MAX_POINTERS) {
            // it's full
            return null;
        }
        int localId = nextUnusedLocalId();
        if (localId == -1) {
            throw new AssertionError("pointers.size() < maxFingers implies that a local id is available");
        }
        Pointer pointer = new Pointer(localId);
        pointers.add(pointer);
        return pointer;
    }


    public int getPointerIndex(int id) {
        for (int i = 0; i < pointers.size(); ++i) {
            Pointer pointer = pointers.get(i);
            if (pointer.getLocalId() == id) {
                return i;
            }
        }
        return -1;
    }


    public int update(MotionEvent.PointerProperties[] props, MotionEvent.PointerCoords[] coords) {
        int count = pointers.size();
        for (int i = 0; i < count; ++i) {
            Pointer pointer = pointers.get(i);
            // id 0 is reserved for mouse events
            props[i].id = pointer.getLocalId();
            Point point = pointer.getPoint();
            coords[i].x = point.x;
            coords[i].y = point.y;
            coords[i].pressure = pointer.getPressure();
            coords[i].touchMajor = pointer.getMajor();
            coords[i].touchMinor = pointer.getMinor();
        }
        cleanUp();
        return count;
    }

    public void remove(int index)
    {
        pointers.remove(index);
    }

    /**
     * Remove all pointers which are UP.
     */
    private void cleanUp() {
        for (int i = pointers.size() - 1; i >= 0; --i) {

            Pointer pointer = pointers.get(i);
            if (pointer.isUp()) {
                pointers.remove(i);
            }
        }
    }
}