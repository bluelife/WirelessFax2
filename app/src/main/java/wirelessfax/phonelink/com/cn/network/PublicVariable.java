package wirelessfax.phonelink.com.cn.network;
import android.content.Context;
import android.util.Log;


import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.Utls;

public class PublicVariable
{
    private static org.slf4j.Logger log = LoggerFactory.getLogger(PublicVariable.class);

    public static String INIFILE_NAME = "F1Client.ini";
	public static String SetupPath = "";

    public static int appTick = 0;

	public static int FileSaveDays = 3; //文件保留天数
	public static int TryViews = 15; //重试次数
	public static String UserCode = ""; //设备号
	public static String Password = ""; //验证码

	private  static Context mCtx;
	public static void init(Context ctx)
	{
		mCtx = ctx;
        getIniFileInstance();
	}

    public static Context getMainCtx()
    {
        return mCtx;
    }

	private static IniEditor gIniFileInstance = null;
	public static IniEditor getIniFileInstance()
	{
		if(gIniFileInstance == null) {
            gIniFileInstance = new IniEditor(true);

            try {
                if (mCtx == null || mCtx.getResources() == null) {

                    log.debug("mCtx == null");
                    return null;
                }
				InputStream inputStream = mCtx.getResources().getAssets().open(INIFILE_NAME);

                gIniFileInstance.load(inputStream);

                FileSaveDays = FaxPath.GetFileSaveDays(); //文件保留天数
                TryViews = FaxPath.GetTryViews(); //重试次数
                UserCode = FaxPath.GetUserCode(); //设备号
                Password = FaxPath.GetPassword(); //验证码

                FaxPath.printInfo();

                //String strFilesDir = "/mnt/sdcard/183";
                String strFilesDir = mCtx.getFilesDir().getAbsolutePath()+"/";

                FilePath.FixupPath = strFilesDir+"/Fax/";
                //FilePath.FixupPath = mCtx.getFilesDir().getAbsolutePath() +"/" + "Fax";
                Utls.isFolderExists(FilePath.FixupPath);//make folder.

                FaxLogFile.LogPath = strFilesDir+"/log/";
               // FaxLogFile.LogPath = mCtx.getFilesDir().getAbsolutePath() +"/" +"Logs";
                Utls.isFolderExists(FaxLogFile.LogPath);//make folder.
            } catch (IOException e) {
                // TODO Auto-generated catch block

                Log.v(Constant.TAG, "[readIni] IOException e");
                e.printStackTrace();
            }

		}

        return gIniFileInstance;
	}


}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#endregion
