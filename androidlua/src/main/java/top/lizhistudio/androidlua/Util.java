package top.lizhistudio.androidlua;

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
}
