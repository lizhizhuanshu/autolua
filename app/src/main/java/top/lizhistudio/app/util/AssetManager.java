package top.lizhistudio.autoluaapp.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class AssetManager {
    public static void copyAssetsTo(Context context, String srcPath, String dstPath, boolean rewrite) throws IOException
    {
        copyAssetsTo(context,srcPath,dstPath,null,rewrite);
    }

    public static void copyAssetsTo(Context context, String srcPath, String dstPath , byte[] buffer, boolean rewrite) throws IOException {
        if(buffer == null)
            buffer = new byte[1024];
        String[] fileNames = context.getAssets().list(srcPath);
        if(fileNames == null)
            throw new IOException("bad source path");
        else if (fileNames.length > 0) {
            File file = new File(dstPath);
            if (!file.exists()) {
                if(! file.mkdirs()) {
                    throw new IOException("error in create directory");
                }
            }
            for (String fileName : fileNames) {
                if (!srcPath.equals("")) {
                    copyAssetsTo(context, srcPath + File.separator + fileName,
                            dstPath + File.separator + fileName,buffer,rewrite);
                } else {
                    copyAssetsTo(context, fileName, dstPath + File.separator + fileName,buffer,rewrite);
                }
            }
        } else {
            File outFile = new File(dstPath);
            if(!rewrite && outFile.exists())
                return;
            InputStream is = context.getAssets().open(srcPath);
            FileOutputStream fos = new FileOutputStream(outFile);
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        }
    }

    public static byte[] read(Context context, String file){
        try{
            InputStream inputStream = context.getAssets().open(file);
            int allSize = inputStream.available();
            byte[] buffer = new byte[allSize];
            if(inputStream.read(buffer) != allSize)
                throw new IOException();
            return buffer;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
