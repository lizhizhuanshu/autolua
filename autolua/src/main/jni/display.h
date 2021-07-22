//
// Created by lizhi on 2021/6/23.
//

#ifndef AUTOLUA2_DISPLAY_H
#define AUTOLUA2_DISPLAY_H
#include "autolua.h"
void onInitializeDisplayContext(JNIEnv*env);
void onReleaseDisplayContext(JNIEnv*env);
void pushDisplayObject(lua_State*L,jobject display);
#endif //AUTOLUA2_DISPLAY_H
