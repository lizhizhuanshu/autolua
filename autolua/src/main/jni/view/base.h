#pragma once
#include"Bitmap.h"
#include <cmath>
#ifndef __ANDROID__
#define UNORDERED_PIXEL 1
#endif

static int compareColor(const unsigned char* color, const unsigned char* color1, const unsigned char* shift);
static int compareColor(const unsigned char* color, const unsigned char* color1, const unsigned char* shift, const unsigned char* shift1);
static int computeColorShiftSum(const unsigned char* color, const unsigned char* color1);
static int compareColor(const unsigned char* color, const unsigned char* color1, int colorShiftSum);



struct Point
{
	int x;
	int y;
	Point()
		:x(-1), y(-1)
	{}
	Point(int x, int y)
		:x(x), y(y)
	{}
};

enum READ_ORDER {
	UP_DOWN_LEFT_RIGHT,
	UP_DOWN_RIGHT_LEFT,
	DOWN_UP_LEFT_RIGHT,
	DOWN_UP_RIGHT_LEFT,
	LEFT_RIGHT_UP_DOWN,
	LEFT_RIGHT_DOWN_UP,
	RIGHT_LEFT_UP_DOWN,
	RIGHT_LEFT_DOWN_UP,
};



template<class T1>
static bool upDownLeftRightReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool upDownRightLeftReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool downUpLeftRightReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool downUpRightLeftReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool leftRightUpDownReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool leftRightUpDownReadColor(Bitmap* bitmap, int x, int y, int x1, int y1,int size, T1* comparator);
template<class T1>
static bool rightLeftUpDownReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool leftRightDownUpReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool rightLeftDownUpReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator);
template<class T1>
static bool orderFindColor(Bitmap* bitmap, int x, int y, int x1, int y1, int readOrder, T1* comparator);


static bool isInBitmapScope(Bitmap* bitmap, int x, int y);
static bool isInBitmapScope(Bitmap* bitmap, int x, int y, int x1, int y1);

inline void checkRect(Bitmap* bitmap, int& x, int& y, int& x1, int& y1)
{
	if (x < 0)
		x = 0;
	else if (x >= bitmap->width)
		x = bitmap->width - 1;
	if (x1 < x)
		x1 = x;
	if (y < 0)
		y = 0;
	else if (y >= bitmap->height)
		y = bitmap->height - 1;
	if (y1 < y)
		y1 = y;
}

inline unsigned char* computeCoordColor(Bitmap* bitmap, int x, int y)
{
	return bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
}



#if UNORDERED_PIXEL
inline int compareColor(const unsigned char* color, const unsigned char* color1, const unsigned char* shift)
{
	return abs(color1[0] - color[2]) <= shift[0] &&
		abs(color1[1] - color[1]) <= shift[1] &&
		abs(color1[2] - color[0]) <= shift[2];
}

inline int compareColor(const unsigned char* color, const unsigned char* color1, const unsigned char* shift, const unsigned char* shift1)
{
	return abs(color1[0] - color[2]) <= (shift[0]+shift1[0]) &&
		abs(color1[1] - color[1]) <= (shift[1] + shift1[1]) &&
		abs(color1[2] - color[0]) <= (shift[2] + shift1[2]);
}

inline int computeColorShiftSum(const unsigned char* color, const unsigned char* color1)
{
	return (abs(color1[0] - color[2]) + abs(color1[1] - color[1]) + abs(color1[2] - color[0]));
}

inline int computeColorShiftSum(const unsigned char* color, const unsigned char* color1, const unsigned char* shift)
{
	int r = 0;
	int c = abs(color1[0] - color[2]) - shift[0];
	if (c > 0)
		r += c;
	c = abs(color1[1] - color[1]) - shift[1];
	if (c > 0)
		r += c;
	c = abs(color1[2] - color[0]) - shift[2];
	if (c > 0)
		r += c;
	return r;
}

#else
inline int compareColor(const unsigned char* color, const unsigned char* color1, const unsigned char* shift)
{
	return abs(color1[0] - color[0]) <= shift[0] &&
		abs(color1[1] - color[1]) <= shift[1] &&
		abs(color1[2] - color[2]) <= shift[2];
}

inline int compareColor(const unsigned char* color, const unsigned char* color1, const unsigned char* shift, const unsigned char* shift1)
{
	return abs(color1[0] - color[0]) <= (shift[0] + shift1[0]) &&
		abs(color1[1] - color[1]) <= (shift[1] + shift1[1]) &&
		abs(color1[2] - color[2]) <= (shift[2] + shift1[2]);
}



inline int computeColorShiftSum(const unsigned char* color, const unsigned char* color1)
{
	return (abs(color1[0] - color[0]) + abs(color1[1] - color[1]) + abs(color1[2] - color[2]));
}

inline int computeColorShiftSum(const unsigned char* color, const unsigned char* color1, const unsigned char* shift)
{
	int r = 0;
	int c = abs(color1[0] - color[0]) - shift[0];
	if (c > 0)
		r += c;
	c = abs(color1[1] - color[1]) - shift[1];
	if (c > 0)
		r += c;
	c = abs(color1[2] - color[2]) - shift[2];
	if (c > 0)
		r += c;
	return r;
}


#endif

inline int compareColor(const unsigned char* color, const unsigned char* color1, int colorShiftSum)
{
	return colorShiftSum >= computeColorShiftSum(color, color1);
}

template<class T1>
bool upDownLeftRightReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveVerticalPointer;
	const unsigned char* moveLinePointer = bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
	for (int intx = x; intx <= x1; intx++)
	{
		moveVerticalPointer = moveLinePointer;
		for (int inty = y; inty <= y1; inty++)
		{
			if (comparator->compare(intx, inty, moveVerticalPointer))
				return true;
			moveVerticalPointer += bitmap->rowShift;
		}
		moveLinePointer += bitmap->pixelStride;
	}
	return false;
}

template<class T1>
bool upDownRightLeftReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveVerticalPointer;
	const unsigned char* moveLinePointer = bitmap->origin + y * bitmap->rowShift + x1 * bitmap->pixelStride;
	for (int intx = x1; intx >= x; intx--)
	{
		moveVerticalPointer = moveLinePointer;
		for (int inty = y; inty <= y1; inty++)
		{
			if (comparator->compare(intx, inty, moveVerticalPointer))
				return true;
			moveVerticalPointer += bitmap->rowShift;
		}
		moveLinePointer -= bitmap->pixelStride;
	}
	return false;
}

template<class T1>
bool downUpLeftRightReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveVerticalPointer;
	const unsigned char* moveLinePointer = bitmap->origin + y1 * bitmap->rowShift + x * bitmap->pixelStride;
	for (int intx = x; intx <= x1; intx++)
	{
		moveVerticalPointer = moveLinePointer;
		for (int inty = y1; inty >= y; inty--)
		{
			if (comparator->compare(intx, inty, moveVerticalPointer))
				return true;
			moveVerticalPointer -= bitmap->rowShift;
		}
		moveLinePointer += bitmap->pixelStride;
	}
	return false;
}

template<class T1>
bool downUpRightLeftReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveVerticalPointer;
	const unsigned char* moveLinePointer = bitmap->origin + y1 * bitmap->rowShift + x1 * bitmap->pixelStride;
	for (int intx = x1; intx >= x; intx--)
	{
		moveVerticalPointer = moveLinePointer;
		for (int inty = y1; inty >= y; inty--)
		{
			if (comparator->compare(intx, inty, moveVerticalPointer))
				return true;
			moveVerticalPointer -= bitmap->rowShift;
		}
		moveLinePointer -= bitmap->pixelStride;
	}
	return false;
}

template<class T1>
bool leftRightUpDownReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveLinePointer;
	const unsigned char* moveVerticalPointer = bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
	for (int inty = y; inty <= y1; inty++)
	{
		moveLinePointer = moveVerticalPointer;
		for (int intx = x; intx <= x1; intx++)
		{
			if (comparator->compare(intx, inty, moveLinePointer))
				return true;
			moveLinePointer += bitmap->pixelStride;
		}
		moveVerticalPointer += bitmap->rowShift;
	}
	return false;
}

template<class T1>
inline bool leftRightUpDownReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, int size, T1* comparator)
{
	const unsigned char* moveLinePointer;
	const unsigned char* moveVerticalPointer = bitmap->origin + y * bitmap->rowShift + x * bitmap->pixelStride;
	int xSize = bitmap->pixelStride * size;
	int ySize = bitmap->rowShift * size;
	for (int inty = y; inty <= y1; inty+=size)
	{
		moveLinePointer = moveVerticalPointer;
		for (int intx = x; intx <= x1; intx+= size)
		{
			if (comparator->compare(intx, inty, moveLinePointer))
				return true;
			moveLinePointer += xSize;
		}
		moveVerticalPointer += ySize;
	}
	return false;
}



template<class T1>
bool rightLeftUpDownReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveLinePointer;
	const unsigned char* moveVerticalPointer = bitmap->origin + y * bitmap->rowShift + x1 * bitmap->pixelStride;
	for (int inty = y; inty <= y1; inty++)
	{
		moveLinePointer = moveVerticalPointer;
		for (int intx = x1; intx >= x; intx--)
		{
			if (comparator->compare(intx, inty, moveLinePointer))
				return true;
			moveLinePointer -= bitmap->pixelStride;
		}
		moveVerticalPointer += bitmap->rowShift;
	}
	return false;
}

template<class T1>
bool leftRightDownUpReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveLinePointer;
	const unsigned char* moveVerticalPointer = bitmap->origin + y1 * bitmap->rowShift + x * bitmap->pixelStride;
	for (int inty = y1; inty >= y; inty--)
	{
		moveLinePointer = moveVerticalPointer;
		for (int intx = x; intx <= x1; intx++)
		{
			if (comparator->compare(intx, inty, moveLinePointer))
				return true;
			moveLinePointer += bitmap->pixelStride;
		}
		moveVerticalPointer -= bitmap->rowShift;
	}
	return false;
}

template<class T1>
bool rightLeftDownUpReadColor(Bitmap* bitmap, int x, int y, int x1, int y1, T1* comparator)
{
	const unsigned char* moveLinePointer;
	const unsigned char* moveVerticalPointer = bitmap->origin + y1 * bitmap->rowShift + x1 * bitmap->pixelStride;
	for (int inty = y1; inty >= y; inty--)
	{
		moveLinePointer = moveVerticalPointer;
		for (int intx = x1; intx >= x; intx--)
		{
			if (comparator->compare(intx, inty, moveLinePointer))
				return true;
			moveLinePointer -= bitmap->pixelStride;
		}
		moveVerticalPointer -= bitmap->rowShift;
	}
	return false;
}

template<class T1>
bool orderFindColor(Bitmap* bitmap, int x, int y, int x1, int y1, int readOrder, T1* comparator)
{
	switch (readOrder)
	{
	case UP_DOWN_LEFT_RIGHT:return upDownLeftRightReadColor(bitmap, x, y, x1, y1, comparator);
	case UP_DOWN_RIGHT_LEFT:return upDownRightLeftReadColor(bitmap, x, y, x1, y1, comparator);
	case DOWN_UP_LEFT_RIGHT:return downUpLeftRightReadColor(bitmap, x, y, x1, y1, comparator);
	case DOWN_UP_RIGHT_LEFT:return downUpRightLeftReadColor(bitmap, x, y, x1, y1, comparator);
	case LEFT_RIGHT_UP_DOWN:return leftRightUpDownReadColor(bitmap, x, y, x1, y1, comparator);
	case RIGHT_LEFT_UP_DOWN:return rightLeftUpDownReadColor(bitmap, x, y, x1, y1, comparator);
	case LEFT_RIGHT_DOWN_UP:return leftRightDownUpReadColor(bitmap, x, y, x1, y1, comparator);
	case RIGHT_LEFT_DOWN_UP:return rightLeftDownUpReadColor(bitmap, x, y, x1, y1, comparator);
	}
	return false;
}



inline bool isInBitmapScope(Bitmap* bitmap, int x, int y)
{
	return x >= 0 && x < bitmap->width && y >= 0 && y < bitmap->height;;
}

inline bool isInBitmapScope(Bitmap* bitmap, int x, int y, int x1, int y1)
{
	return x1 >= x && y1 >= y && x >= 0 && y >= 0 && x1 < bitmap->width && y1 < bitmap->height;
}

inline void catCoord(Bitmap* bitmap, int& x, int& y)
{
	if (x < 0)
		x = 0;
	else if (x >= bitmap->width)
		x = bitmap->width - 1;
	if (y < 0)
		y = 0;
	else if (y >= bitmap->height)
		y = bitmap->height - 1;
}

inline void catScope(Bitmap* bitmap, int& x, int& y, int& x1, int& y1)
{
	if (x < 0)
		x = 0;
	if (y < 0)
		y = 0;
	if(x1>bitmap->width)
		x1= bitmap->width - 1;
	if (y1 >= bitmap->height)
		y1 = bitmap->height - 1;
}


inline int computeCharBit(int c)
{
    if (c <= '9')
        c -= '0';
    else if (c <= 'F')
        c -= 55;
    else
        c -= 87;
    return c;
}

inline int toIntColor(const char* s)
{
    int r = 0;
    r |= (computeCharBit(s[0]) << 20);
    r |= (computeCharBit(s[1]) << 16);
    r |= (computeCharBit(s[2]) << 12);
    r |= (computeCharBit(s[3]) << 8);
    r |= (computeCharBit(s[4]) << 4);
    r |= (computeCharBit(s[5]) << 0);
    return r;
}