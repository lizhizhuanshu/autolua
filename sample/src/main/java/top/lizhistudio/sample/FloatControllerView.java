package top.lizhistudio.host;

public interface FloatView {
    int STOP_STATE = 0;
    int EXECUTE_STATE = 1;
    void show();
    void reShow();
    void conceal();
    void move(int x, int y);
    void setState(int id);
    void setOnClickListener(OnClickListener onClickListener);
    interface OnClickListener {
        void onClick(FloatView floatView, int state);
    }
}
