package top.lizhistudio.androidlua;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Util {
    public static String structToString(Object o)
    {
        if (o == null)
            return "null";
        Class<?> aClass = o.getClass();
        StringBuilder builder = new StringBuilder(aClass.getName());
        builder.append("@")
                .append(o.hashCode())
                .append("{");
        for (Field field :
            aClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers()))
            {
                builder.append(field.getName())
                        .append(":");
                String value = null;
                try{
                    Object v = field.get(o);
                    if (v == null)
                        value = "null";
                    else
                        value = v.toString();
                } catch (IllegalAccessException e) {
                    value = "null";
                }
                builder.append(value)
                        .append(",");
            }
        }
        return builder.deleteCharAt(builder.length()-1)
                .append("}").toString();
    }

    public static String luaValueToString(LuaContext context,int index)
    {
        switch (LuaContext.VALUE_TYPE.valueOf( context.type(index)))
        {
            case NONE:
            case NIL:
                return "nil";
            case BOOLEAN:
                return String.valueOf(context.toBoolean(index));
            case LIGHT_USERDATA:
                return "lightUserdata@"+context.toPointer(index);
            case NUMBER:
                if (context.isInteger(index))
                    return String.valueOf(context.toLong(index));
                return String.valueOf(context.toDouble(index));
            case STRING:
                return context.toString(index);
            case TABLE:
                return "table@"+context.toPointer(index);
            case FUNCTION:
                return "function@"+context.toPointer(index);
            case USERDATA: {
                if (context.isLuaObjectAdapter(index))
                    return "LuaObjectAdapter@" + context.toPointer(index);
                else
                    return "userdata@" + context.toPointer(index);
            }
            case THREAD:
                return "thread@"+context.toPointer(index);
        }
        return "unknown";
    }
}
