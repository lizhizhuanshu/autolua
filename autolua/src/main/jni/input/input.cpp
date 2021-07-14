


#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <sys/types.h>
#include <fcntl.h>
#include <cerrno>
#include <ctime>
#include <linux/input.h>
#include<string>
#include<iostream>
#include <vector>
#include "input.h"
#include"mlua.h"






static int getScreenDevice(int & width,int & height)
{
	char          name[64];
	unsigned char mask[EV_MAX / 8 + 1];
	int           fd = 0;
	input_absinfo absinfo;

#define test_bit(bit) (mask[(bit)/8] & (1 << ((bit)%8)))

	for (int id = 0; id < 32; id++)
	{
		sprintf(name, "/dev/input/event%d", id);
		if ((fd = open(name, O_RDWR, 0)) == 0)
			break;
		ioctl(fd, EVIOCGBIT(0, sizeof(mask)), mask);
		if (test_bit(EV_KEY) && test_bit(EV_ABS))
		{
			ioctl(fd, EVIOCGABS(ABS_MT_POSITION_X), &absinfo);
			width = absinfo.maximum;
			ioctl(fd, EVIOCGABS(ABS_MT_POSITION_Y), &absinfo);
			height = absinfo.maximum;
			close(fd);
			if (width > 0 && height > 0)
				return id;
		}
		close(fd);
	}
	return 0;
}



static int getScreenDeviceInfo(lua_State* L)
{
	int width, height;
	int id = getScreenDevice(width,height);
	if (id)
	{
		lua_createtable(L, 0, 3);
		lua_pushinteger(L, id);
		lua_setfield(L, -2, "id");
		lua_pushinteger(L, width);
		lua_setfield(L, -2, "width");
		lua_pushinteger(L, height);
		lua_setfield(L, -2, "height");
	}
	else
		lua_pushnil(L);
	return 1;
}



static int luaUsleep(lua_State* L)
{
	lua_Integer time = luaL_checkinteger(L, 1);
	usleep(time);
	return 0;
}





static int getTouchEvents(lua_State* L)
{
	int needCountSum = luaL_checkinteger(L, 1);
	std::vector<input_event> events;
	input_event   event;
	char          name[64];
	int           fd = 0;
	int			  count = 0;
	memset(&event, 0, sizeof(input_event));
	int width, height;
	sprintf(name, "/dev/input/event%d", getScreenDevice(width, height));
	if ((fd = open(name, O_RDONLY, 0)) == 0)
		luaL_error(L, "don't open device in path '%s'", name);
	int hasTouchUp = 0;
	while (count < needCountSum)
	{
		read(fd, &event, sizeof(input_event));
		events.push_back(event);
		if (event.type == EV_ABS && event.code == ABS_MT_TRACKING_ID)
		{
			if (event.value > -1)
				hasTouchUp = 1;
			else
			{
				hasTouchUp = 0;
				count++;
			}
		}
	}
	close(fd);

	lua_createtable(L, events.size(), 0);
#define lua_setIntegerValue(L,index,value) lua_pushinteger(L, value);lua_seti(L, -2, index);
	for (size_t i = 0; i < events.size(); i++)
	{
		input_event& event = events.at(i);
		lua_createtable(L, 5, 0);
		lua_setIntegerValue(L, 1, event.time.tv_sec);
		lua_setIntegerValue(L, 2, event.time.tv_usec);
		lua_setIntegerValue(L, 3, event.type);
		lua_setIntegerValue(L, 4, event.code);
		lua_setIntegerValue(L, 5, event.value);
		lua_seti(L, -2, i+1);
	}
	return 1;
}



static  int openInputDevice(lua_State* L)
{
	int id = luaL_checkinteger(L, 1);
	char          name[64];
	int fd;
	sprintf(name, "/dev/input/event%d", id);
	fd = open(name, O_WRONLY, 0);
	lua_pushinteger(L, fd);
	return 1;
}

static int writeInputEvent(lua_State* L)
{
	int fd = luaL_checkinteger(L, 1);
	input_event event;
	event.type  = luaL_checkinteger(L, 2);
	event.code = luaL_checkinteger(L, 3);
	event.value = luaL_checkinteger(L, 4);
	ssize_t r = write(fd, &event, sizeof(input_event));
	lua_pushinteger(L, r);
	return 1;
}

static int closeInputDevice(lua_State* L)
{
	int fd = luaL_checkinteger(L, 1);
	close(fd);
	return 0;
}



int luaopen_input(lua_State* L)
{
#define METHOD(f) {#f,f}

	static luaL_Reg methods[] = {
		METHOD(getScreenDeviceInfo),
		METHOD(getTouchEvents),
		METHOD(openInputDevice),
		METHOD(writeInputEvent),
		METHOD(closeInputDevice),
		{"usleep",luaUsleep},
		{nullptr,nullptr},
	};
	luaL_newlib(L, methods);
	return 1;
}