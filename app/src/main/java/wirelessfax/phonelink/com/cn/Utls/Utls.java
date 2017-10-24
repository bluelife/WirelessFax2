package wirelessfax.phonelink.com.cn.Utls;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import wirelessfax.phonelink.com.cn.network.PublicVariable;
import wirelessfax.phonelink.com.cn.wirelessfax.R;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by CNFAFAN3 on 11/5/2016.
 */

public class Utls {


    /**
     * int to byte[]
     *
     * @param i
     * @return
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];

        result[3] = (byte) (i & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[0] = (byte) ((i >> 24) & 0xFF);
        return result;
    }

    /**
     * int to byte[]
     *
     * @param i
     * @return
     */
    public static byte[] intTo3ByteArray(int i) {
        byte[] result = new byte[3];

        result[2] = (byte) ((i) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) ((i >> 16) & 0xFF);
        return result;
    }

    /**
     * int to byte[]
     *
     * @param i
     * @return
     */
    public static byte[] intTo2ByteArray(int i) {
        byte[] result = new byte[2];
        result[1] = (byte) ((i) & 0xFF);
        result[0] = (byte) ((i >> 8) & 0xFF);
        return result;
    }

    /**
     * int to byte[]
     *
     * @param i
     * @return
     */
    public static byte[] intTo1ByteArray(int i) {
        byte[] result = new byte[1];

        result[0] = (byte) ((i) & 0xFF);
        return result;
    }

    public static byte[] BooleanTo1ByteArray(boolean b) {
        int i = 0;
        if (b) {
            i = 1;
        }
        return intTo1ByteArray(i);
    }


    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }


    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            //sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    public static String byte2HexStr(byte[] b, int len) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            //sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }


    public static String byte2HexStrPrint(byte[] b) {
        int n = 0;
        String stmp = "";
        String str = "";
        StringBuilder sb = new StringBuilder("PrintLength:" + b.length + ":");
        for (n = 0; n < b.length; n++) {
            if (n % 20 == 0) {
                sb.append("\r\n[" + n + "]_");
            }
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append("_");
            str += " ";
            str += stmp;
        }
        sb.append(",totalPrint:" + n);
        // return sb.toString().toUpperCase();
        return str;
    }


    public static String byte2HexStrPrint(byte[] b, int len) {
        String stmp = "";
        int n = 0;
        String sb = new String("PrintLength:" + len + ":\r\n");
        for (n = 0; n < len; n++) {
            if (n % 20 == 0) {
                sb += ("\r\n[" + n + "]_");
            }
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb += ((stmp.length() == 1) ? "0" + stmp : stmp);
            sb += ("_");
        }
        sb += (",totalPrint:" + n);
        return sb.toString().toUpperCase();
    }


    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            //ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
            String strTmp = src.substring(i * 2, m) + src.substring(m, n);
            ret[i] = (byte) (Integer.parseInt(strTmp, 16));
        }
        return ret;
    }


    /**
     * byte[] to int
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0]) << 24);
        value |= ((bytes[1] & 0x000000FF) << 16);
        value |= ((bytes[2] & 0x000000FF) << 8);
        value |= ((bytes[3] & 0x000000FF));
        return value;
    }

    public static int byteArray3ToInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0]) << 16);
        value |= ((bytes[1] & 0x000000FF) << 8);
        value |= ((bytes[2] & 0x000000FF));
        return value;
    }

    public static int byteArray2ToInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0]) << 8);
        value |= ((bytes[1] & 0x000000FF));
        return value;
    }

    public static int byteArray1ToInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0] & 0x000000FF));
        return value;
    }


    public static int byteArrayToUnsignInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0] & 0x000000FF) << 24);
        value |= ((bytes[1] & 0x000000FF) << 16);
        value |= ((bytes[2] & 0x000000FF) << 8);
        value |= ((bytes[3] & 0x000000FF));
        return value;
    }

    public static int byteArray3ToUnsignInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0] & 0x000000FF) << 16);
        value |= ((bytes[1] & 0x000000FF) << 8);
        value |= ((bytes[2] & 0x000000FF));
        return value;
    }

    public static int byteArray2ToUnsignInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0] & 0x000000FF) << 8);
        value |= ((bytes[1] & 0x000000FF));
        return value;
    }

    public static int byteArray1ToUnsignInt(byte[] bytes) {
        int value = 0;
        value |= ((bytes[0] & 0x000000FF));
        return value;
    }

    /**
     * int to byte[]
     *
     * @param i
     * @return
     */
    public static byte[] shortToByteArray(short i) {
        byte[] result = new byte[2];
        //
        result[1] = (byte) (i & 0xFF);
        result[0] = (byte) ((i >> 8) & 0xFF);
        return result;
    }

    /**
     * byte[] to int
     *
     * @param bytes
     * @return
     */
    public static short byteArrayToShort(byte[] bytes) {
        short value = 0;
        //
        value |= ((bytes[0]) << 8);
        value |= ((bytes[1] & 0x00FF));
        return value;
    }


    public static String strToUnicode(String strText) throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // ��λ��ǰ�油00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }


    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);

            String s1 = s.substring(2, 4) + "00";

            String s2 = s.substring(4);

            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);

            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    public static float strInt2floatScale10(String strValue) {
        float fValue = Float.parseFloat(strValue);
        fValue /= Constant.STREAM_FLOAT_TYPE_10;

        return fValue;
    }

    public static float int2floatScale10(int iValue) {
        float fValue = (float) iValue;
        fValue /= Constant.STREAM_FLOAT_TYPE_10;

        return fValue;
    }

    public static String float2strIntScale10(float fValue) {
        fValue = fValue * Constant.STREAM_FLOAT_TYPE_100;

        int iValue = (int) fValue;
        iValue /= Constant.STREAM_FLOAT_TYPE_10;

        return iValue + "";
    }

    public static int float2intScale10(float fValue) {
        fValue = fValue * Constant.STREAM_FLOAT_TYPE_100;

        int iValue = (int) fValue;
        iValue /= Constant.STREAM_FLOAT_TYPE_10;

        return iValue;
    }

    public static final int FORMAT_DATA_TIME = 0;
    public static final int FORMAT_DATA = 1;
    public static final int FORMAT_TIME = 2;

    public static String getDateTime(int type) {
        String str = "";
        SimpleDateFormat formatter = null;
        Date curDate = new Date(System.currentTimeMillis());
        switch (type) {
            case FORMAT_DATA_TIME:
                formatter = new SimpleDateFormat(Constant.STRING_DATE_TIME_FORMAT);
                str = formatter.format(curDate);

                break;
            case FORMAT_DATA:
                formatter = new SimpleDateFormat(Constant.STRING_DATE_FORMAT);
                str = formatter.format(curDate);
                break;
            case FORMAT_TIME:
                formatter = new SimpleDateFormat(Constant.STRING_TIME_FORMAT);
                str = formatter.format(curDate);
                break;
            default:
                break;
        }

        return str;
    }

    public static Date getTimeFromString(String str, int type) {
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat(Constant.STRING_DATE_TIME_FORMAT);
        try {
            date = formatter.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    public static long getLocalTimeMillis() {
        long time = System.currentTimeMillis();
        return time;
    }

    public static long getLocalSeconds() {
        long time = System.currentTimeMillis() / 1000;
        return time;
    }

    public static String getUIShowTime(long time)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String strDataTime = sdf.format(time*1000);

        long iCurrentTime = getLocalSeconds();
        long oneDay = 24*60*60;//s

        long iOldToday = time/oneDay;
        long iNewToday = iCurrentTime/oneDay;
        if (iOldToday == iNewToday) {  // 同一天
            String[] list = strDataTime.split(" ");
            if (list.length > 1) {
                return list[1];
            }
            return strDataTime;
        }

        //小于一天内，即昨天
        if(iNewToday == (iOldToday +1) ) {
            return PublicVariable.getMainCtx().getResources().getString(R.string.yesterday);
        }

//        long oneWeek = oneDay*7;
//        long iOld = time/oneWeek;
//        long iNew = iCurrentTime/oneWeek;
//        //不是同一天，同一周？
//        if (iOld == iNew) {//同一周
//            long left = time%oneWeek;
//            int tmpWeek = (int)(left/oneDay);
//            switch (tmpWeek) {
//                case 0:
//                    return PublicVariable.getMainCtx().getResources().getString(R.string.monday);
//                case 1:
//                    return PublicVariable.getMainCtx().getResources().getString(R.string.tuesday);
//                case 2:
//                    return PublicVariable.getMainCtx().getResources().getString(R.string.wednesday);
//                case 3:
//                    return PublicVariable.getMainCtx().getResources().getString(R.string.thursday);
//                case 4:
//                    return PublicVariable.getMainCtx().getResources().getString(R.string.friday);
//                case 5:
//                    return PublicVariable.getMainCtx().getResources().getString(R.string.saturday);
//                case 6:
//                    return PublicVariable.getMainCtx().getResources().getString(R.string.sunday);
//                default:
//                    return strDataTime;
//            }
//        }

        //反馈具体日期。
        String[] list = strDataTime.split(" ");
        if (list.length > 1) {
            return list[0];
        }

        return strDataTime;

    }



    /**
     * 右补位，左对齐
     *
     * @param oriStr 原字符串
     * @param len    目标字符串长度
     * @param alexin 补位字符
     * @return 目标字符串
     */
    public static String padRight(String oriStr, int len, char alexin) {
        String str = "";
        int strlen = oriStr.length();
        if (strlen < len) {
            for (int i = 0; i < len - strlen; i++) {
                str = str + alexin;
            }
        }
        str = str + oriStr;
        return str;
    }

    /**
     * 左补位，右对齐
     *
     * @param oriStr 原字符串
     * @param len    目标字符串长度
     * @param alexin 补位字符
     * @return 目标字符串
     */
    public static String padLeft(String oriStr, int len, char alexin) {
        String str = "";
        int strlen = oriStr.length();
        if (strlen < len) {
            for (int i = 0; i < len - strlen; i++) {
                str = str + alexin;
            }
        }
        str = oriStr + str;
        return str;
    }

    public static boolean isFolderExists(String strFolder){
        File file = new File(strFolder);

        boolean bRet = true;
        if (!file.exists()) {
            if (!file.mkdirs()) {
                bRet = false;
            }
        }

        return bRet;
    }


    /**
     * int to byte[]
     * @param i
     * @return
     */
    public static byte[] intToBigByteArray(int i) {
        byte[] result = new byte[4];

        result[0] = (byte)(i & 0xFF);
        result[1] = (byte)((i >> 8) & 0xFF);
        result[2] = (byte)((i >> 16) & 0xFF);
        result[3] = (byte)((i >> 24) & 0xFF);
        return result;
    }

    /**
     * int to byte[]
     * @param i
     * @return
     */
    public static byte[] intTo3BigByteArray(int i) {
        byte[] result = new byte[3];

        result[0] = (byte)((i) & 0xFF);
        result[1] = (byte)((i >> 8) & 0xFF);
        result[2] = (byte)((i >> 16)  & 0xFF);
        return result;
    }

    /**
     * int to byte[]
     * @param i
     * @return
     */
    public static byte[] intTo2BigByteArray(int i) {
        byte[] result = new byte[2];
        result[0] = (byte)((i) & 0xFF);
        result[1] = (byte)((i >> 8) & 0xFF);
        return result;
    }

    /**
     * int to byte[]
     * @param i
     * @return
     */
    public static byte[] intTo1BigByteArray(int i) {
        byte[] result = new byte[1];

        result[0] = (byte)((i) & 0xFF);
        return result;
    }

    public static byte[] BooleanTo1BigByteArray(boolean b) {
        int i = 0;
        if (b) {
            i = 1;
        }
        return intTo1BigByteArray(i);
    }

    public static boolean isAppAlive(Context context, String packageName){
        ActivityManager activityManager =
                (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for(int i = 0; i < processInfos.size(); i++){
            if(processInfos.get(i).processName.equals(packageName)){
                Log.i("NotificationLaunch",
                        String.format("the %s is running, isAppAlive return true", packageName));
                return true;
            }
        }
        Log.i("NotificationLaunch",
                String.format("the %s is not running, isAppAlive return false", packageName));
        return false;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i(context.getPackageName(), "此appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }
    public static void customBufferBufferedStreamCopy(File source, File target) {
        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(source));
            fos = new BufferedOutputStream(new FileOutputStream(target));
            byte[] buf = new byte[4096];
            int i;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getSelectedExtenedPath()
    {
        String strPath = Environment.getExternalStorageDirectory().getPath();
        String strQQPath = strPath + "/tencent/QQfile_recv/";
        File file = new File(strQQPath);
        if (file.exists()) {
            strPath = strQQPath;
        }

        return  strPath;

    }

    public static  boolean isFileExist(String strPath) {
        if(strPath == null) {
            return false;
        }

        File file = new File(strPath);
        if(file.exists()) {
            return true;
        }

        return false;
    }
}
