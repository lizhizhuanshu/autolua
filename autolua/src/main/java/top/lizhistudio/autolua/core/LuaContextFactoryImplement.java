package top.lizhistudio.autolua.core;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import top.lizhistudio.androidlua.LuaContext;


public class LuaContextFactoryImplement implements LuaContextFactory{
    private CommandLine commandLine;

    public LuaContextFactoryImplement()
    {
        commandLine = null;
    }

    public LuaContextFactoryImplement(String[] args)
    {
        Options options = new Options();
        options.addOption(null,"filePath",true,"file path");
        options.addOption(null,"rootPath",true,"root path");
        try{
            commandLine = new DefaultParser().parse(options,args);
        }catch (ParseException e)
        {
            e.printStackTrace(System.err);
        }
    }


    @Override
    public LuaContext newLuaContext() {
        LuaContextImplement context = new LuaContextImplement();
        context.injectAutoLua(true);
        if (commandLine != null)
        {
            if (commandLine.hasOption("filePath"))
            {
                context.push(commandLine.getOptionValue("filePath"));
                context.setGlobal("FILE_PATH");
            }
            if(commandLine.hasOption("rootPath"))
            {
                context.push(commandLine.getOptionValue("rootPath"));
                context.setGlobal("ROOT_PATH");
            }
        }
        return context;
    }
}
