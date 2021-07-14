#ifndef AUTO_LUA_COLOR_H
#define AUTO_LUA_COLOR_H
#include"base.h"
#include<memory>
#include<string>


static int compareColor(const unsigned char* color, std::string_view* color1, const unsigned char* shift)
{

    const char* mColor = color1->data();
    size_t size = color1->length();
    size_t index = 0;
    int aColor = 0;
    int aShift = 0;
    int nowColor = 1;
    const unsigned char* nowShift;
    while (index+6<=size)
    {
        aColor = toIntColor(mColor+index);
        index+=7;
        if (size > index && mColor[index-1] == '-')
        {
            aShift = toIntColor(mColor +index);
            nowShift = (const unsigned char*)&aShift;
            index += 7;
        }
        else
        {
            nowShift = shift;
        }
        if (compareColor(color, (const unsigned char*)&aColor,nowShift))
            return nowColor;
        nowColor++;
    }
    return 0;
}

static int compareColor(const unsigned char* color, std::string_view* color1, int shift)
{

    const char* mColor = color1->data();
    size_t size = color1->length();
    size_t index = 0;
    int aColor = 0;
    int aShift = 0;
    int nowColor = 1;
    int result = 0;
    while (index + 6 <= size)
    {
        aColor = toIntColor(mColor + index);
        index += 7;
        if (size > index && mColor[index - 1] == '-')
        {
            aShift = toIntColor(mColor+index);
            index += 7;
            result = compareColor(color, (const unsigned char*)&aColor, (const unsigned char*)&aShift);
        }
        else
        {
            result = compareColor(color, (const unsigned char*)&aColor, shift);
        }
        if (result)
            return nowColor;
        nowColor++;
    }
    return 0;
}



class Color
{
protected:
    int mData;
public:
    Color()
            :mData(0)
    {}

    Color(int color)
            :mData(color)
    {}

    Color(const unsigned char* color)
            :mData(0)
    {
        memcpy(&mData, color, 3);
    }

    operator int()
    {
        return mData;
    }

    int operator == (const Color& other)
    {
        return mData == other.mData;
    }

    int operator == (const int other)
    {
        return mData == other;
    }

    int equal(int color)
    {
        return mData == color;
    }

    int equal(const unsigned char* color,Color shiftColor)
    {
        return compareColor((const unsigned char*)&mData,color,(const unsigned  char*)&shiftColor);
    }

    int equal(const unsigned  char* color,int shiftColorSum)
    {
        return compareColor((const unsigned char*)&mData,color,shiftColorSum);
    }
};



static int compareColor(const unsigned char* color, const unsigned char* color1, Color colorShift)
{
    return compareColor(color,color1,(unsigned  char*)&colorShift);
}

static int compareColor(const unsigned char* color, std::string_view* color1, Color shift)
{
    return compareColor(color,color1,(unsigned  char*)&shift);
}

class StringViewColor
{
    std::string_view mData;
public:
    StringViewColor(const char* str,size_t size)
            :mData(str,size)
    {}
    StringViewColor(std::string_view & color)
            :mData(color)
    {}

    int equal(const unsigned  char* color ,Color shiftColor)
    {
        return compareColor(color,&mData,(const unsigned char*)&shiftColor);
    }


    int equal(const unsigned char* color,int shiftColor)
    {
        return compareColor(color,&mData,shiftColor);
    }

    int equal(const unsigned char* color,const unsigned char* shiftColor)
    {
        return compareColor(color,&mData,shiftColor);
    }
};


struct Pixel
{
    unsigned char r;
    unsigned char g;
    unsigned char b;
    unsigned char o;
};
#endif


