package wirelessfax.phonelink.com.cn.network;

import android.util.Log;

import java.io.File;

import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.Utls;

///#region 传真文件操作类
public class FilePath
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 变量定义
	public static  String FixupPath = "D:\\F1Client\\"; //固定路径
	private static final String FixupSuffix = ".tif"; //固定后缀
	private static final String FixupPrefix = "fax"; //固定前缀
	private String mstrSubPath;
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 公用函数
	/** 
	 默认构造函数
	 
	*/
	public FilePath()
	{
		mstrSubPath = "";
	}
	/** 
	 带参数构造函数
	 
	*/
	public FilePath(String strMobile)
	{
		mstrSubPath = strMobile;
	}
	/** 
	 获取并产生全路径，包括文件名
	 
	*/
	public final String MakeFullPath()
	{
		if (mstrSubPath.equals(""))
		{
			return "";
		}

		String strDate = Utls.getDateTime(Utls.FORMAT_DATA), strPath = "", strFileName = "";
		StringBuilder sb = new StringBuilder();

		sb.append(FixupPath);
		sb.append(strDate);
		sb.append("/");
		sb.append(mstrSubPath);

		strPath = sb.toString();

		if (CreatePath(strPath) == false)
		{
			return "";
		}

		strFileName = MakeFileName();

		if (strFileName.equals(""))
		{
			return "";
		}

		sb.append("/");
		sb.append(strFileName);

		return sb.toString();
	}

	/** 
	 子路径
	 
	*/
	public final String getSubPath()
	{
		return mstrSubPath;
	}
	public final void setSubPath(String value)
	{
		mstrSubPath = value;
	}

	/** 
	 根据路径产生文件名
	 
	*/
	private String MakeFileName()
	{
		String strFileName = "", strNow = Utls.getDateTime(Utls.FORMAT_DATA_TIME);

		strFileName = String.format("%1$s%2$s%3$s", FixupPrefix, strNow, FixupSuffix);

		return strFileName;
	}
	/** 
	 创建路径
	 
	*/
	private boolean CreatePath(String strPath)
	{
		if (strPath.equals(""))
		{
			return false;
		}

		try
		{
			File file = new File(strPath);
			if (file.exists())
			{
				return true;
			}
			file.mkdirs();

			return true;
		}
		catch (RuntimeException e)
		{
			Log.e(Constant.TAG, String.format("创建路径[%1$s]出错，由于%2$s", strPath, e.getMessage()));
			return false;
		}
	}
}
