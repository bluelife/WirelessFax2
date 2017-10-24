package org.beyka.tiffbitmapfactory;

/**
 * Created by slomka.jin on 2016/11/15.
 */

public class TiffReplace {
    static {
        System.loadLibrary("tiff");
        System.loadLibrary("tiffsaver");
    }

    public static boolean replacePage(String path,String destPath,String resultPath,int index){
        return replace(path,destPath,resultPath,index);
    }
    private static synchronized native boolean replace(String path,String destPath,String resultPath,int index);
}
