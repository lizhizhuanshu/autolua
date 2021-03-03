package top.lizhistudio.autolua.extend;


public class Pointer {
    /**
     * Local pointer id, using the lowest possible values to fill the {@link android.view.MotionEvent.PointerProperties PointerProperties}.
     */
    private final int localId;

    private final Point point;
    private float pressure;
    private float mainAxis;
    private float minor;
    private boolean up;

    public Pointer(int localId) {
        this.localId = localId;
        point = new Point();
    }


    public int getLocalId() {
        return localId;
    }

    public Point getPoint() {
        return point;
    }


    public float getPressure() {
        return pressure;
    }

    public float getMajor()
    {
        return mainAxis;
    }

    public float getMinor(){
        return minor;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public void setMajor(float mainAxis)
    {
        this.mainAxis = mainAxis;
    }

    public void setMinor(float minor)
    {
        this.minor = minor;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }
}