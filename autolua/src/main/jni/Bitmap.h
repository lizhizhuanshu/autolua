#ifndef BITMAP_H
#define BITMAP_H
struct Bitmap{
	unsigned char* origin;
	int width;
	int height;
	int rowShift;
	int pixelStride;
};
#endif

