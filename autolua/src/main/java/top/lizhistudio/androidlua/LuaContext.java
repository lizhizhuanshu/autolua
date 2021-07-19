package top.lizhistudio.androidlua;


import top.lizhistudio.androidlua.exception.LuaTypeError;

public interface LuaContext {
    int MAX_STACK = 1000000;
    int REGISTRY_INDEX = -1001000;
    int REGISTRY_INDEX_MAIN_THREAD = 1;
    int REGISTRY_INDEX_GLOBALS = 2;
    int MULTI_RESULT = -1;

    long toPointer(int index);
    long toLong(int index) throws LuaTypeError;
    double toDouble(int index)throws LuaTypeError;
    String toString(int index)throws LuaTypeError;
    byte[] toBytes(int index)throws LuaTypeError;
    boolean toBoolean(int index);
    LuaObjectAdapter toLuaObjectAdapter(int index)throws LuaTypeError;

    void push(long v);
    void push(double v);
    void push(String v);
    void push(byte[] v);
    void push(boolean v);
    void push(LuaFunctionAdapter v);
    void push(LuaObjectAdapter v);
    void pushNil();

    int getTable(int tableIndex);
    void setTable(int tableIndex);
    int getGlobal(String key);
    void setGlobal(String key);
    int rawGet(int tableIndex);
    void rawSet(int tableIndex);

    int getTop();
    void setTop(int n);
    void pop(int n);
    int type(int index);
    boolean isInteger(int index);
    boolean isLuaObjectAdapter(int index);
    void loadFile(String filePath,CODE_TYPE mode) ;
    void loadBuffer(byte[] code,String chunkName,CODE_TYPE mode);
    void pCall(int argNumber, int resultNumber, int errorFunctionIndex);
    void createTable(int arraySize,int dictionarySize);
    void destroy();
    void interrupt();
    enum CODE_TYPE{
        TEXT_BINARY(0),TEXT(1),BINARY(2);
        private final int code;
        CODE_TYPE(int code)
        {
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }

    enum VALUE_TYPE{
        NONE(-1),
        NIL(0),
        BOOLEAN(1),
        LIGHT_USERDATA(2),
        NUMBER(3),
        STRING(4),
        TABLE(5),
        FUNCTION(6),
        USERDATA(7),
        THREAD(8);
        private final int code;
        VALUE_TYPE(int code){
            this.code = code;
        }
        public int getCode(){return code;}
        public static VALUE_TYPE valueOf(int code)
        {
            switch (code)
            {
                case -1:return NONE;
                case 0:return NIL;
                case 1:return BOOLEAN;
                case 2:return LIGHT_USERDATA;
                case 3:return NUMBER;
                case 4:return STRING;
                case 5:return TABLE;
                case 6:return FUNCTION;
                case 7:return USERDATA;
                case 8:return THREAD;
            }
            throw new RuntimeException(String.format("The code '%d' can't to type 'VALUE_TYPE'",code));
        }
    }

}
