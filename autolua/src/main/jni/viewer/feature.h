#include "base.h"
#include<string>


static size_t computeStringColorSize(const char* color)
{
    size_t result = 6;
    int c = color[result];
    while (c == '|' || c == '-')
    {
        result +=7;
        c = color[result];
    }
    return result;
}

static size_t computeStringFeaturePointCount(const char* feature,size_t size)
{
    size_t index = 10;
    size_t count = 0;
    while (index<size)
    {
        if (feature[index] == ',')
        {
            count +=1;
            index +=10;
        }else if(feature[index] == '\0')
        {
            return count+1;
        }
        else{
            index++;
        }
    }
    return count;
}


class StringViewFeature
{
    const char* mFeature;
    size_t mFeatureStringSize;
    size_t mPointCount;
public:
    StringViewFeature(const char* featureString,size_t featureStringSize,size_t pointCount)
        :mFeature(featureString),mFeatureStringSize(featureStringSize),
        mPointCount(pointCount)
    {}

    StringViewFeature(const char* featureString,size_t featureStringSize)
        :mFeature(featureString),mFeatureStringSize(featureStringSize)
    {
        mPointCount = computeStringFeaturePointCount(featureString,featureStringSize);
    }

    template<class TShift>
    int equal(Bitmap* bitmap, int x,int y,TShift shift,int canErrorPointCount)
    {
        const char* nowFeature = mFeature;
        size_t nowPointCount = 1;
        size_t nowErrorPointCount = 0;
        int nowX,nowY;
        size_t pos = 0;
        while (nowPointCount++ <= mPointCount)
        {
            nowX = std::stoi(nowFeature, &pos, 10);
            nowFeature+=pos+1;
            nowY = std::stoi(nowFeature, &pos, 10);
            nowFeature+=pos+1;
            pos = computeStringColorSize(nowFeature);
            std::string_view color(nowFeature,pos);
            nowFeature += pos + 1;
            nowX += x;
            nowY += y;
            if ((!isInBitmapScope(bitmap,nowX,nowY) || !compareColor(computeCoordColor(bitmap,nowX,nowY),&color,shift))
                && ++nowErrorPointCount > canErrorPointCount)
            {
                return 0;
            }
        }
        return 1;
    }

    template<class TShift>
    int equal(Bitmap* bitmap,TShift shift,int canErrorPointCount)
    {
        const char* nowFeature = mFeature;
        size_t nowPointCount = 1;
        size_t nowErrorPointCount = 0;
        int x,y;
        size_t pos = 0;
        while (nowPointCount++ <=mPointCount)
        {
            x = std::stoi(nowFeature, &pos, 10);
            nowFeature+=pos+1;
            y = std::stoi(nowFeature, &pos, 10);
            nowFeature+=pos+1;
            pos = computeStringColorSize(nowFeature);
            std::string_view color(nowFeature,pos);
            nowFeature += pos + 1;
            if (!compareColor(computeCoordColor(bitmap,x,y),&color,shift)
                && ++nowErrorPointCount > canErrorPointCount)
            {
                return 0;
            }
        }
        return 1;
    }
};