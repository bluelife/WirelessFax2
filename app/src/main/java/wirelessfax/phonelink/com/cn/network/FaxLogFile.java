package wirelessfax.phonelink.com.cn.network;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#endregion

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import wirelessfax.phonelink.com.cn.Utls.Utls;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#region 日志记录类
public class FaxLogFile
{
	private static org.slf4j.Logger log = LoggerFactory.getLogger(FaxLogFile.class);
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 变量定义
	public static  String LogPath = "D:\\F1Client\\Log\\";
	private static final String LogFileName = ".txt";
	private static byte[] aryLog = new byte[8192]; //缓冲区
	private static final int BufferSize = 8192; //大小为8K
	private static int fullSize = 0; //实际大小
	private static Object mobjLock = new Object(); //锁变量
	private static int TimeOut = FaxPath.GetTimeOut(); //超时时间，单位为秒
	private static final boolean ShowProcess = FaxPath.GetShowProcess();
	private static long dLastDate = Utls.getLocalSeconds();
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 公用函数
	/** 
	 写错误日志
	 
	*/
	public static void WriteError(String strLog)
	{
		WriteLog(strLog, LogType.Error);
	}
	/** 
	 写过程日志
	 
	*/
	public static void WriteControl(String strLog)
	{
		WriteLog(strLog, LogType.Control);
	}
	/** 
	 写结束日志
	 
	*/
	public static void WriteEnd(String strLog)
	{
		WriteLog(strLog, LogType.End);
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 私有函数
	/** 
	 写系统日志
	 
	*/
	private static void WriteLog(String strLog, LogType type)
	{
		if (strLog.equals(""))
		{
			return;
		}

		byte[] aryData = null;
		boolean bExit = false, bWrite = false;
		strLog = new java.util.Date().toString() + " " + strLog + "\r\n";
		byte[] aryTemp = strLog.getBytes();
		int nLength = aryTemp.length;

		switch (type)
		{
			case Error:
				break;
			case Control:
				if (ShowProcess == false)
				{
					bExit = true;
				}

				break;
			case End:
				bWrite = true;
				break;
		}

		if (bExit)
		{
            log.debug("log file22: "+strLog);
			return;
		}

		synchronized (mobjLock)
		{
			long now = Utls.getLocalSeconds();
			if (now - dLastDate >= TimeOut)
			{
				bWrite = true;
			}
			dLastDate = Utls.getLocalSeconds();

			if (bWrite)
			{
				aryData = new byte[fullSize + nLength];
				System.arraycopy(aryLog, 0, aryData, 0, fullSize);
				System.arraycopy(aryTemp, 0, aryData, fullSize, nLength);
				fullSize = 0;
			}
			else
			{
				if (fullSize + nLength <= BufferSize)
				{
					System.arraycopy(aryTemp, 0, aryLog, fullSize, nLength);
					fullSize += nLength;
				}
				else
				{
					aryData = new byte[fullSize + nLength];
					System.arraycopy(aryLog, 0, aryData, 0, fullSize);
					System.arraycopy(aryTemp, 0, aryData, fullSize, nLength);
					fullSize = 0;
				}
			}
		}

		if (aryData != null)
		{
			WriteFile(aryData);
		}
	}
	/** 
	 产生日志文件全路径
	 
	*/
	private static String MakeFilePath()
	{
		StringBuilder sb = new StringBuilder();
		String strDate = Utls.getDateTime(Utls.FORMAT_DATA);

		sb.append(LogPath);
		sb.append(strDate);
		sb.append(LogFileName);

		return sb.toString();
	}
	/** 
	 将内容写入文件
	 
	*/
	private static void WriteFile(byte[] aryData)
	{
		String strPath = MakeFilePath();

		log.debug("log file:"+strPath);
        try {
            // 打开一个随机访问文件流，按读写方式
			File file = new File(strPath);
			if (!file.exists()) {
				file.createNewFile();
			}
            RandomAccessFile randomFile = new RandomAccessFile(strPath, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.write(aryData);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 日志类型
	private enum LogType
	{
		Error(0), //错误
		Control(1), //可控
		End(2); //结束

		private int intValue;
		private static java.util.HashMap<Integer, LogType> mappings;
		private synchronized static java.util.HashMap<Integer, LogType> getMappings()
		{
			if (mappings == null)
			{
				mappings = new java.util.HashMap<Integer, LogType>();
			}
			return mappings;
		}

		private LogType(int value)
		{
			intValue = value;
			LogType.getMappings().put(value, this);
		}

		public int getValue()
		{
			return intValue;
		}

		public static LogType forValue(int value)
		{
			return getMappings().get(value);
		}
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion
}