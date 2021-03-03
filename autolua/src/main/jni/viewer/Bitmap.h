#ifndef VIEWER_BITMAP_H
#define VIEWER_BITMAP_H
typedef struct Bitmap{
	unsigned char* origin;
	int width;
	int height;
	int rowShift;
	int pixelStride;
}LBitmap;

#endif

