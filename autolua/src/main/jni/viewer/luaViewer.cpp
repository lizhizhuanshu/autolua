#include"mlua.h"
#include"viewer.h"
#include"feature.h"
#include<vector>


extern "C" {
	LUA_METHOD  int luaopen_viewer(lua_State* L);
}

#define SET_VIEWER_METHOD(m) {#m"ByShiftColor",m<Color>},{#m"ByShiftColorSum",m<int>}

#define PUSH_FIND_ORDER(L,i,name) lua_pushstring(L,#name);\
lua_pushinteger(L,READ_ORDER::name);\
lua_settable(L,i)

static constexpr char COORDINATES_OVERFLOW[] = "The coordinates are off screen";


static int getColor(lua_State* L);
template<class T1>
static int getColorCount(lua_State* L);
template<class T1>
static int findColor(lua_State* L);
template<class T1>
static int whichColor(lua_State* L);
template<class T1>
static int isFeature(lua_State* L);
template<class T1>
static int findFeature(lua_State* L);
template<class T1>
static int getColorCoordMatrix(lua_State* L);
template<class T1>
static int getDotMatrix(lua_State* L);
static int dotMatrixToTable(lua_State* L);
static int createDotMatrixOf(lua_State* L);
static int findMatrix(lua_State* L);
static int dotMatrixWidth(lua_State* L);
static int dotMatrixHeight(lua_State* L);


class DotMatrix;


int luaopen_viewer(lua_State* L)
{
	static luaL_Reg methods[] =
	{
		{"getColor",getColor},
		{"newDotMatrix",createDotMatrixOf},
		SET_VIEWER_METHOD(getColorCount),
		SET_VIEWER_METHOD(findColor),
		SET_VIEWER_METHOD(whichColor),
		SET_VIEWER_METHOD(findFeature),
		SET_VIEWER_METHOD(isFeature),
		SET_VIEWER_METHOD(getDotMatrix),
		SET_VIEWER_METHOD(getColorCoordMatrix),
		{NULL,NULL}
	};


	if (luaL_newClassMetatable(DotMatrix, L))
	{
		static luaL_Reg methods[] =
		{
			{"toTable",dotMatrixToTable},
			{"height",dotMatrixHeight},
			{"width",dotMatrixWidth},
			{"findMatrix",findMatrix},
			{"__gc",lua::finish<DotMatrix>},
			{NULL,NULL}
		};
		luaL_setfuncs(L, methods, 0);
		lua_pushvalue(L, -1);
		lua_setfield(L, -2, "__index");
	}

	luaL_newlib(L, methods);
	lua_pushstring(L, COORDINATES_OVERFLOW);
	lua_setfield(L, -2, "COORDINATES_OVERFLOW");
	lua_newtable(L);
	PUSH_FIND_ORDER(L, -3, UP_DOWN_LEFT_RIGHT);
	PUSH_FIND_ORDER(L, -3, UP_DOWN_RIGHT_LEFT);
	PUSH_FIND_ORDER(L, -3, DOWN_UP_LEFT_RIGHT);
	PUSH_FIND_ORDER(L, -3, DOWN_UP_RIGHT_LEFT);
	PUSH_FIND_ORDER(L, -3, LEFT_RIGHT_UP_DOWN);
	PUSH_FIND_ORDER(L, -3, LEFT_RIGHT_DOWN_UP);
	PUSH_FIND_ORDER(L, -3, RIGHT_LEFT_UP_DOWN);
	PUSH_FIND_ORDER(L, -3, RIGHT_LEFT_DOWN_UP);
	lua_setfield(L, -2, "FIND_ORDER");

	return 1;
}




inline static lua_Integer lua_getTableInteger(lua_State* L, int tableIndex, lua_Integer integerIndex)
{
	int type = lua_rawgeti(L, tableIndex, integerIndex);
	lua_Integer result = type == LUA_TNUMBER ? lua_tointeger(L, -1):0;
	lua_pop(L, 1);
	return result;
}

inline static void lua_pushTableInteger(lua_State* L, int tableIndex, lua_Integer value)
{
	lua_pushinteger(L, value);
	lua_rawseti(L, tableIndex, luaL_len(L, tableIndex) + 1);
}

template<class...Args>
inline static void checkCoordinates(Bitmap* bitmap,lua_State*L,Args && ...args)
{
	if (!isInBitmapScope(bitmap,std::forward<Args>(args)...))
		luaL_error(L,COORDINATES_OVERFLOW);
}



class DotMatrix
{
	int mWidth;
	int mHeight;
	char* mMatrix;
	size_t mSize;
public:
	DotMatrix(int width, int height,const char*data);
	DotMatrix(int width, int height);
	void check(int width, int height);
	~DotMatrix();
	void set(int index, int n)
	{
		mMatrix[index] = n;
	}
	int height()
	{
		return mHeight;
	}
	int width()
	{
		return mWidth;
	}
	bool equal(DotMatrix* dotMatrix, int canErrorSum, int ox, int oy);
	bool find(DotMatrix* dotMatrix, int canErrorSum, Point* out, int x, int y, int x1, int y1);
	bool find(DotMatrix* dotMatrix, float sim, Point* out, int x, int y, int x1, int y1);
	bool find(DotMatrix* dotMatrix, float sim, Point* out);
	void toTable(lua_State* L);
};



template<class TColor,class TShift>
class DotMatrixFinder
{
	size_t mIndex;
	TColor mColor;
	TShift mShift;
	DotMatrix* mDotMatrix;
public:
	DotMatrixFinder(TColor color, TShift shift,DotMatrix *dotMatrix);
	bool compare(int x, int y, const unsigned char* color);
};




template<class TColor,class TShift>
class ColorCoordMatrixFinder
{
	TColor mColor;
	TShift mShift;
	lua_State* L;
	int mIndex;
public:
	ColorCoordMatrixFinder(TColor color, TShift shift,lua_State*L ,int tableIndex);
	bool compare(int x, int y, const unsigned char* color);
};




int getColor(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	int x = luaL_checkinteger(L, 2);
	int y = luaL_checkinteger(L, 3);
	checkCoordinates(bitmap, L, x, y);
	Color color = getColor(bitmap, x, y);
	lua_pushinteger(L, (int)color);
	return 1;
}



int dotMatrixToTable(lua_State* L)
{
	DotMatrix* matrix = luaL_checkObject(DotMatrix, L, 1);
	matrix->toTable(L);
	return 1;
}

int createDotMatrixOf(lua_State* L)
{
	if (lua_istable(L,1))
	{
		lua_geti(L, 1, 1);
		lua_geti(L, 1, 2);
		lua_geti(L, 1, 3);
	}
	int width = luaL_checkinteger(L, -3);
	int height = luaL_checkinteger(L, -2);
	const char* s = luaL_checkstring(L, -1);
	luaL_pushNewObject(DotMatrix, L,width,height,s);
	return 1;
}

int findMatrix(lua_State* L)
{
	DotMatrix* self = luaL_checkObject(DotMatrix, L, 1);
	DotMatrix* matrix = luaL_checkObject(DotMatrix, L, 2);
	lua_Number sim = luaL_checknumber(L, 3);
	Point out(-1,-1);
	if (lua_isinteger(L, 4))
	{
		int x, y, x1, y1;
		x = luaL_checkinteger(L, 4);
		y = luaL_checkinteger(L, 5);
		x1 = luaL_checkinteger(L, 6);
		y1 = luaL_checkinteger(L,7);
		self->find(matrix, (float)sim, &out, x, y, x1, y1);
	}
	else
		self->find(matrix, (float)sim, &out);
	lua_pushinteger(L, out.x);
	lua_pushinteger(L, out.y);
	return 2;
}

int dotMatrixWidth(lua_State* L)
{
	DotMatrix* self = luaL_checkObject(DotMatrix, L, 1);
	lua_pushinteger(L, self->width());
	return 1;
}

int dotMatrixHeight(lua_State* L)
{
	DotMatrix* self = luaL_checkObject(DotMatrix, L, 1);
	lua_pushinteger(L, self->height());
	return 1;
}


template<class T>
int getColorCount(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	int x = luaL_checkinteger(L, 2);
	int y = luaL_checkinteger(L, 3);
	int x1 = luaL_checkinteger(L, 4);
	int y1 = luaL_checkinteger(L, 5);
	checkCoordinates(bitmap, L, x, y,x1,y1);
	T shift = luaL_checkinteger(L, 7);
	int count;
	if (lua_isnumber(L,6))
	{
		Color color(lua_tointeger(L, 6));
		count = getColorCount(bitmap, x, y, x1, y1,&color, shift);
	}else if(lua_isinteger(L,6))
	{
		size_t size = 0;
		const char* str = lua_tolstring(L, 6, &size);
		StringViewColor color(str, size);
		count = getColorCount(bitmap, x, y, x1, y1,&color, shift);
	}else
	{
		luaL_error(L,"arg 6 expectant string or integer now type '%s'",luaL_typename(L,6));
	}
	lua_pushinteger(L, count);
	return 1;
}



template<class T>
int findColor(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	int x = luaL_checkinteger(L, 2);
	int y = luaL_checkinteger(L, 3);
	int x1 = luaL_checkinteger(L, 4);
	int y1 = luaL_checkinteger(L, 5);
	checkCoordinates(bitmap, L, x, y,x1,y1);
	T shift = luaL_checkinteger(L, 7);
	int order = luaL_checkinteger(L, 8);
	Point out(-1,-1);
	if (lua_isnumber(L,6))
	{
		Color color(lua_tointeger(L, 6));
		findColor(bitmap, x, y, x1, y1,&color, shift,order,&out);
	}else if(lua_isstring(L,6))
	{
		size_t size = 0;
		const char* str = lua_tolstring(L, 6, &size);
		StringViewColor color(str, size);
		findColor(bitmap, x, y, x1, y1,&color, shift,order,&out);
	}else
	{
		luaL_error(L,"arg 6 expectant string or integer now type '%s'",luaL_typename(L,6));
	}
	lua_pushinteger(L, out.x);
	lua_pushinteger(L, out.y);
	return 2;
}



template<class T>
int whichColor(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	int x = luaL_checkinteger(L, 2);
	int y = luaL_checkinteger(L, 3);
	checkCoordinates(bitmap, L, x, y);
	T shift = luaL_checkinteger(L, 5);
	int result = 0;
	if (lua_isnumber(L, 4))
	{
		Color color(lua_tointeger(L, 4));
		result = color.equal(computeCoordColor(bitmap, x, y), shift);
	}else if(lua_isstring(L, 4))
	{
		size_t size = 0;
		const char* str = lua_tolstring(L, 4, &size);
		StringViewColor color(str, size);
		result = color.equal(computeCoordColor(bitmap, x, y), shift);
	}
	else
	{
		luaL_error(L,"arg 4 expectant string or integer now type '%s'",luaL_typename(L,4));
	}
	lua_pushinteger(L, result);
	return 1;
}



template<class T>
int isFeature(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	T shift = luaL_checkinteger(L, 3);
	int canErrorSum = luaL_checkinteger(L, 4);
	size_t size = 0;
	const char * featureString = luaL_checklstring(L,2,&size);
	StringViewFeature feature(featureString,size);
	lua_pushboolean(L, feature.equal(bitmap,shift,canErrorSum));
	return 1;
}


template<class T>
int findFeature(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	int x = luaL_checkinteger(L, 2);
	int y = luaL_checkinteger(L, 3);
	int x1 = luaL_checkinteger(L, 4);
	int y1 = luaL_checkinteger(L, 5);
	checkCoordinates(bitmap, L, x, y, x1, y1);
	T shift = luaL_checkinteger(L, 8);
	int canErrorSum = luaL_checkinteger(L, 9);
	int order = luaL_checkinteger(L, 10);
	size_t featureSize = 0;
	const char * featureString = luaL_checklstring(L,7,&featureSize);
	StringViewFeature feature(featureString,featureSize);
	Point point(-1,-1);
	if (lua_isnumber(L,6))
	{
		Color color(lua_tointeger(L, 6));
		findFeature(bitmap,x,y,x1,y1,&color,&feature,shift,order,canErrorSum,&point);
	}else if(lua_isstring(L, 6))
	{
		size_t size = 0;
		const char* str = lua_tolstring(L, 6, &size);
		StringViewColor color(str, size);
		findFeature(bitmap, x, y, x1, y1, &color, &feature, shift, order, canErrorSum, &point);
	}
	else
	{
		luaL_error(L,"arg 6 expectant string or integer now type '%s'",luaL_typename(L,6));
	}
	lua_pushinteger(L, point.x);
	lua_pushinteger(L, point.y);
	return 2;
}


template<class T>
int getColorCoordMatrix(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	int x = luaL_checkinteger(L, 2);
	int y = luaL_checkinteger(L, 3);
	int x1 = luaL_checkinteger(L, 4);
	int y1 = luaL_checkinteger(L, 5);
	checkCoordinates(bitmap, L, x, y, x1, y1);

	T shift = luaL_checkinteger(L, 7);
	lua_newtable(L);
	int tableIndex = lua_gettop(L);

	if (lua_isnumber(L,6))
	{
		Color color(lua_tointeger(L, 6));
		ColorCoordMatrixFinder recorder(&color, shift, L, tableIndex);
		leftRightUpDownReadColor(bitmap, x, y, x1, y1, &recorder);
	}else if (lua_isstring(L, 6))
	{
		size_t size = 0;
		const char* str = lua_tolstring(L, 6, &size);
		StringViewColor color(str, size);
		ColorCoordMatrixFinder recorder(&color, shift, L, tableIndex);
		leftRightUpDownReadColor(bitmap, x, y, x1, y1, &recorder);
	}else{
		luaL_error(L,"arg 6 expectant string or integer now type '%s'",luaL_typename(L,6));
	}
	return 1;
}

template<class T1>
int getDotMatrix(lua_State* L)
{
	luaL_checktype(L, 1, LUA_TUSERDATA);
	Bitmap* bitmap = lua::toObject<Bitmap>(L, 1);
	int x = luaL_checkinteger(L, 2);
	int y = luaL_checkinteger(L, 3);
	int x1 = luaL_checkinteger(L, 4);
	int y1 = luaL_checkinteger(L, 5);
	checkCoordinates(bitmap, L, x, y, x1, y1);
	T1 shift = luaL_checkinteger(L, 7);
	int width = x1 - x + 1;
	int height = y1 - y + 1;
	DotMatrix* dotMatirx = luaL_testObject(DotMatrix, L, 8);
	if (!dotMatirx)
		dotMatirx = luaL_pushNewObject(DotMatrix, L, width, height);
	else
		dotMatirx->check(width, height);
	if (lua_isnumber(L,6))
	{
		Color color(lua_tointeger(L, 6));
		DotMatrixFinder recorder(&color, shift, dotMatirx);
		leftRightUpDownReadColor(bitmap, x, y, x1, y1, &recorder);
	}else if (lua_isstring(L, 6))
	{
		size_t size = 0;
		const char* str = lua_tolstring(L, 6, &size);
		StringViewColor color(str, size);
		DotMatrixFinder recorder(&color, shift, dotMatirx);
		leftRightUpDownReadColor(bitmap, x, y, x1, y1, &recorder);
	}else{
		luaL_error(L,"arg 6 expectant string or integer now type '%s'",luaL_typename(L,6));
	}
	return 1;
}





DotMatrix::DotMatrix(int width, int height,const char* data)
	:mWidth(width), mHeight(height)
{
	mSize = width * height;
	mMatrix = (char*)malloc(mSize);
	memcpy(mMatrix, data, mSize);
}

DotMatrix::DotMatrix(int width, int height)
	:mWidth(width),mHeight(height)
{
	mSize = width * height;
	mMatrix =(char*) malloc(mSize);
}

void DotMatrix::check(int width, int height)
{
	mWidth = width;
	mHeight = height;
	size_t newSize = width * height;
	if (mSize < newSize)
	{
		free(mMatrix);
		mMatrix = (char*)malloc(newSize);
		mSize = newSize;
	}
}

DotMatrix::~DotMatrix()
{
	free(mMatrix);
}

bool DotMatrix::equal(DotMatrix* dotMatrix, int canErrorSum, int ox, int oy)
{
	if (mWidth - ox < dotMatrix->mWidth || mHeight - oy < dotMatrix->mHeight)
		return false;
	int origin = mWidth * oy + ox;
	int otherOrigin = 0;
	int errorCount = 0;
	for (int y = 0; y < dotMatrix->mHeight; y++)
	{
		for (int x = 0; x < dotMatrix->mWidth; x++)
		{
			if (mMatrix[origin + x] != dotMatrix->mMatrix[otherOrigin + x]
				&& ++errorCount > canErrorSum)
				return false;
		}
		origin += mWidth;
		otherOrigin += dotMatrix->mWidth;
	}
	return true;
}

bool DotMatrix::find(DotMatrix* dotMatrix, int canErrorSum, Point* out, 
	int x, int y, int x1, int y1)
{

	for (int j =y; j <= y1+1-dotMatrix->mHeight; j++)
	{
		for (int i = x; i <= x1+1-dotMatrix->mWidth; i++)
		{
			if (equal(dotMatrix, canErrorSum, i, j))
			{
				if (out)
				{
					out->x = i;
					out->y = j;
				}
				return true;
			}
		}
	}
	return false;
}


bool DotMatrix::find(DotMatrix* dotMatrix, float sim, Point* out, int x, int y, int x1, int y1)
{
	int canErrorSum = dotMatrix->mWidth*dotMatrix->mHeight *(1-sim);
	return find(dotMatrix, canErrorSum, out,x,y,x1,y1);
}

bool DotMatrix::find(DotMatrix* dotMatrix, float sim, Point* out)
{
	return find(dotMatrix, sim, out, 0,0,mWidth-1,mHeight-1);
}

void DotMatrix::toTable(lua_State* L)
{
	lua_createtable(L,3, 0);
	int index = lua_gettop(L);
	lua_pushTableInteger(L, index, mWidth);
	lua_pushTableInteger(L, index, mHeight);
	lua_pushlstring(L, mMatrix, mSize);
	lua_seti(L, index, 3);
}


template<class TColor,class TShift>
DotMatrixFinder<TColor,TShift>::DotMatrixFinder(TColor color, TShift shift, DotMatrix* dotMatrix)
	: mIndex(0), mColor(color), mShift(shift), mDotMatrix(dotMatrix)
{
}

template<class TColor,class TShift>
bool DotMatrixFinder<TColor,TShift>::compare(int x, int y, const unsigned char* color)
{
	mDotMatrix->set(mIndex++,mColor->equal(color, mShift) ? '1' : '0');
	return false;
}

template<class TColor,class TShift>
ColorCoordMatrixFinder<TColor,TShift>::ColorCoordMatrixFinder(TColor color, TShift shift, lua_State* L, int tableIndex)
	: mColor(color), mShift(shift), L(L), mIndex(tableIndex)
{
}

template<class TColor,class TShift>
bool ColorCoordMatrixFinder<TColor,TShift>::compare(int x, int y, const unsigned char* color)
{
	int nColor;
	Pixel* pixel;
	if (mColor->equal(color, mShift))
	{
		if (lua_geti(L, mIndex, x) == LUA_TNIL)
		{
			lua_pop(L, 1);
			lua_newtable(L);
			lua_pushvalue(L, -1);
			lua_seti(L, mIndex, x);
		}
		nColor = 0;
		pixel = (Pixel*)&nColor;
		pixel->g = color[1];
#if UNORDER_PIXEL
		pixel->r = color[2];
		pixel->b = color[0];
#else
		pixel->r = color[0];
		pixel->b = color[2];
#endif
		lua_pushinteger(L, nColor);
		lua_seti(L, -2, y);
		lua_pop(L, 1);
	}
	return false;
}
