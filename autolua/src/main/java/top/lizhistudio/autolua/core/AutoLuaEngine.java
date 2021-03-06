package top.lizhistudio.autolua;

public interface AutoLuaEngine {
    void destroy();
    LuaInterpreter getInterrupt();
}
