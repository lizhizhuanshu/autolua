package top.lizhistudio.sample;

public interface FloatControllerView {
    int STOPPED_STATE = 0;
    int EXECUTEING_STATE = 1;
    void show();
    void reShow();
    void conceal();
    void move(int x, int y);
    void setState(int id);
    void setOnClickListener(OnClickListener onClickListener);
    interface OnClickListener {
        void onClick(FloatControllerView floatControllerView, int state);
    }
}
