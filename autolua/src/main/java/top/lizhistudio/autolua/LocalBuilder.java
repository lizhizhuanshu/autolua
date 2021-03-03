package top.lizhistudio.autolua;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Scanner;

import top.lizhistudio.autolua.service.Client;
import top.lizhistudio.autolua.service.Server;

public class LocalBuilder {
    private static final String TAG = "LocalBuilder";
    private boolean isDebugPrint = false;
    private boolean isUse64bit = false;
    private String niceName = null;
    private String scriptPath = null;
    private String password = null;
    private String feature = null;
    private final Context application;

    private final LuaInterpreter userInterface;
    public LocalBuilder(Context context,LuaInterpreter userInterface)
    {
        application = context.getApplicationContext();
        this.userInterface = userInterface;
    }

    private String getPackagePath()
    {
        return application.getPackageCodePath();
    }

    private String getLibraryPath()
    {
        return getPackagePath().substring(0,getPackagePath().lastIndexOf('/')+1)+"lib/";
    }

    private boolean isCanUse64Bit()
    {
        String libraryPath = getLibraryPath();
        File file = new File(libraryPath);
        for(String s:file.list())
        {
            if(s.indexOf("64")>0)
            {
                return true;
            }
        }
        return false;
    }


    public LocalBuilder setUse64Bit()
    {
        this.isUse64bit = isCanUse64Bit();
        return this;
    }

    public LocalBuilder setDebugPrint(boolean is)
    {
        this.isDebugPrint = is;
        return this;
    }

    public LocalBuilder setNiceName(String name)
    {
        niceName = name;
        return this;
    }

    public LocalBuilder setScriptPath(String path)
    {
        scriptPath = path;
        return this;
    }

    public LocalBuilder setPassword(String password)
    {
        this.password = password;
        return this;
    }

    public LocalBuilder setFeature(String feature)
    {
        this.feature = feature;
        return this;
    }


    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    private void append(StringBuilder builder,String arg)
    {
        builder.append(arg).append("  ");
    }

    private void appendArg(StringBuilder builder, String k,String v)
    {
        append(builder,k);
        append(builder,v);
    }

    private String getStartString()
    {
        StringBuilder command = new StringBuilder();
        String libPathHead = getLibraryPath();
        File file = new File(libPathHead);
        String libPath = null;
        if(isUse64bit)
        {
            for(String s:file.list()) {
                if (s.indexOf("64") > 0) {
                    libPath = libPathHead + s;
                    break;
                }
            }
        }else
            libPath = libPathHead + file.list()[0];


        if(scriptPath != null)
        {
            command.append("export LUA_PATH=\"")
                    .append(scriptPath)
                    .append("\"\n");
        }

        command.append("export LUA_CPATH=\"")
                .append( libPath)
                .append("/lib?.so;")
                .append(libPath)
                .append("/?/init.so")
                .append("\"\n");

        command.append("export LD_LIBRARY_PATH=\"")
                .append(System.getProperty("java.library.path"))
                .append(":" )
                .append(libPath)
                .append("\"\n");

        command.append("export CLASSPATH=")
                .append(getPackagePath())
                .append("\n");

        if(isUse64bit ||(libPath != null && libPath.contains("64")) )
            command.append("/system/bin/app_process64 ");
        else
            command.append("/system/bin/app_process32 ");

        command.append("/system/bin ");

        if(niceName != null)
        {
            command.append("--nice-name=");
            command.append(niceName);
            command.append(" ");
        }

        append(command,Server.class.getName());
        if (password  == null)
            password = getRandomString(16);
        if (feature == null)
            feature = getRandomString(16);
        appendArg(command,"-v",password);
        appendArg(command,"-f",feature);
        command.append('\n');
        return command.toString();
    }

    private boolean getStartResult(Process process)
    {
        final Scanner scanner = new Scanner(process.getInputStream());
        while (scanner.hasNext())
        {
            if(scanner.hasNextInt())
            {
                int code = scanner.nextInt();
                if(code == 0)
                {
                    Log.e(TAG,"start local AutoLuaEngine process error");
                    process.destroy();
                    break;
                }else if(code== 1) {
                    Log.d(TAG, "local AutoLuaEngine started");
                    if(isDebugPrint)
                    {
                        final Scanner scanner1 = new Scanner(process.getErrorStream());
                        new Thread(){
                            @Override
                            public void run() {
                                while (scanner.hasNext())
                                {
                                    Log.d("AutoLuaEngine",scanner.nextLine());
                                }
                            }
                        }.start();
                        new Thread(){
                            @Override
                            public void run() {
                                while (scanner1.hasNext())
                                {
                                    Log.e("AutoLuaEngine",scanner1.nextLine());
                                }
                            }
                        }.start();
                    }
                    return true;
                }else
                {
                    Log.d(TAG,String.valueOf(code));
                }
            }else
            {
                Log.d(TAG,scanner.nextLine());
            }
        }
        return false;
    }

    public AutoLuaEngine build()
    {
        try{
            String command = getStartString();
            Log.d(TAG,"AutoLuaEngine start command :\n"+command);
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
            if (getStartResult(process))
                return Client.newInstance(feature,password,userInterface);
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }


}
