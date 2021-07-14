//
// Created by lizhi on 2021/6/23.
//

#ifndef AUTOLUA2_DISPLAY_H
#define AUTOLUA2_DISPLAY_H
#include "../luajni.h"

void onInitializeDisplayContext(JNIEnv*env);
void onReleaseDisplayContext(JNIEnv*env);
int luaopen_display(lua_State*L);
#endif //AUTOLUA2_DISPLAY_H
