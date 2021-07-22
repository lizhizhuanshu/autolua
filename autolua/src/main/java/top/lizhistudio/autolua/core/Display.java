package top.lizhistudio.autolua.core;

import java.nio.ByteBuffer;

public interface Display {
    int getBaseWidth();
    int getBaseHeight();
    int getBaseDirection();
    int getRotation();
    int getBaseDensity();
    void destroy();
    int getDirection();
    boolean isChangeDirection();
    void update();
    ByteBuffer getDisplayBuffer();
    int getWidth();
    int getHeight();
    int getRowStride();
    int getPixelStride();
    boolean initialize(int width,int height);
}
