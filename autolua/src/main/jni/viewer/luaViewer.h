//
// Created by lizhi on 2021/3/22.
//

#ifndef AUTOLUA_LUAVIEWER_H
#define AUTOLUA_LUAVIEWER_H
#include "lua.h"
#ifdef __cplusplus
extern "C"
{
#endif

void pushDotMatrixConstructor(lua_State*L);
void pushViewerMethodTable(lua_State*L);
#ifdef __cplusplus
}
#endif

#endif //AUTOLUA_LUAVIEWER_H
