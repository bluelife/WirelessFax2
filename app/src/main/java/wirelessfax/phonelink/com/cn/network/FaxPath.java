package wirelessfax.phonelink.com.cn.network;

import org.slf4j.LoggerFactory;

import wirelessfax.phonelink.com.cn.Utls.Utls;

import static java.lang.Integer.parseInt;

///#region 配置文件操作类
public class FaxPath
{
	private static  org.slf4j.Logger log = LoggerFactory.getLogger(FaxPath.class);
	/** 
	 获取显示过程选项
	 
	*/
	public static boolean GetShowProcess()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "ShowProcess");
		log.debug("ini  ShowProcess:"+strType);
        if (strType == null || strType.isEmpty()) {
            strType = "0";
        }

		if (parseInt(strType) == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	/** 
	 获取超时时间
	 
	*/
	public static int GetTimeOut()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "TimeOut");
		log.debug("ini  TimeOut:"+strType);
        if (strType == null || strType.isEmpty()) {
            strType = "60";
        }

		return parseInt(strType);
	}
	/** 
	 获取远程服务器端口
	 
	*/
	public static int GetRemotePort()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "RemotePort");
		log.debug("ini  RemotePort:"+strType);
        if (strType == null || strType.isEmpty()) {
            strType = "11000";
        }

		if (!strType.equals("11000"))
		{
            try {
                strType = new String(Des.decrypt(strType.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }

		}

        int Port = 0;
        try {
            Port = Integer.parseInt(strType);
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }


		return Port;
	}
	/** 
	 获取远程服务器IP地址
	 
	*/
	public static String GetRemoteServer()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "RemoteServer");
		log.debug("ini  RemoteServer:"+strType);
        if (strType == null || strType.isEmpty()) {
            strType = "127.0.0.1";
        }

		if (!strType.equals("127.0.0.1"))
		{
            try {
                strType = new String(Des.decrypt(strType.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
		}

		return strType;
	}
	/** 
	 获取重试次数
	 
	*/
	public static int GetTryViews()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "TryViews");
		log.debug("ini  TryViews:"+strType);
        if (strType == null || strType.isEmpty()) {
            strType = "3";
        }

		return parseInt(strType);
	}
	/** 
	 获取文件保留天数
	 
	*/
	public static int GetFileSaveDays()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "SaveDays");
        if (strType == null || strType.isEmpty()) {
            strType = "15";
        }

		return parseInt(strType);
	}
	/** 
	 模拟终端代码
	 
	*/
	public static String GetUserCode()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "UserCode");
		log.debug("ini  UserCode:"+strType);
        if (strType == null || strType.isEmpty()) {
          //  strType = "000000";
			strType = "";
        }


		if (!strType.equals("000000"))
		{
            try {
                strType = new String(Des.decrypt(strType.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();;
            }

		}

		return strType;
	}
	/** 
	 模拟终端密码
	 
	*/
	public static String GetPassword()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "Password");
        if (strType == null || strType.isEmpty()) {
          //  strType = "123456";
			strType = "";
        }

		if (!strType.equals("123456"))
		{
            try {
                strType = new String(Des.decrypt(strType.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();;
            }
		}

		return strType;
	}
	/** 
	 硬盘系列号
	 
	*/
	public static String GetHardSn()
	{

        String strType = PublicVariable.getIniFileInstance().get("Other", "SN");
        if (strType == null || strType.isEmpty()) {
            strType = "";
        }

		return strType;
	}
	/** 
	 本机号码
	 
	*/
	public static String GetMobile()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "Mobile");
        if (strType == null || strType.isEmpty()) {
            strType = "18100000000";
        }
		if (!strType.equals("18100000000"))
		{
            try {
                strType = new String(Des.decrypt(strType.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();;
            }
		}

		return strType;
	}
	/** 
	 虚拟打印机
	 
	*/
	public static String GetVirtualPrinter()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "Virtual");
        if (strType == null || strType.isEmpty()) {
            strType = "FaxServer Printer";
        }

		return strType;
	}
	/** 
	 真实打印机
	 
	*/
	public static String GetPrinter()
	{
        String strType = PublicVariable.getIniFileInstance().get("Other", "Printer");
        if (strType == null || strType.isEmpty()) {
            strType = "HP Officejet 150 Mobile L511";
        }

		return strType;
	}

	public static void printInfo()
	{
		log.debug("IniFile  Mobile:"+GetMobile());
		log.debug("IniFile  ShowProcess:"+GetShowProcess());
		log.debug("IniFile  TimeOut:"+GetTimeOut());
		log.debug("IniFile  RemotePort:"+GetRemotePort());
		log.debug("IniFile  RemoteServer:"+GetRemoteServer());
		log.debug("IniFile  TryViews:"+GetTryViews());
		log.debug("IniFile  SaveDays:"+GetTimeOut());
		log.debug("IniFile  UserCode:"+GetUserCode());
		log.debug("IniFile  Password:"+GetPassword());
        log.debug("IniFile  sn:"+GetHardSn());

		log.debug("IniFile  113399 bianhao :"+ Utls.byte2HexStr(Des.encrypt("113399".getBytes())));
		log.debug("IniFile  18059286546 haoma :"+Utls.byte2HexStr(Des.encrypt("18059286546".getBytes())));
        log.debug("IniFile  218.6.71.118 haoma :"+Utls.byte2HexStr(Des.encrypt("218.6.71.118".getBytes())));
	}
}