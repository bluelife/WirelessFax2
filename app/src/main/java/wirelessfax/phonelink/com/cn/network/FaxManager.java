package wirelessfax.phonelink.com.cn.network;


import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import wirelessfax.phonelink.com.cn.Sqlite.Person;
import wirelessfax.phonelink.com.cn.Utls.Utls;

///#region 传真管理类
public class FaxManager
{
		///#region 静态变量定义
	private static List<FaxInfo> faxList = new ArrayList<FaxInfo>();
	private static Object mobjLock = new Object(); //锁变量

	public static  boolean mbSendNewFax = false;
	//用于分解发送新传真阶段性判断。
	public static  boolean mbSendNewFax2 = false;

	public static  boolean mbRecvNewFax = false;

	public static boolean bLogining = false;


	///#region 静态公用函数
	/** 
	 添加元素
	 
	*/
	public static boolean Add(FaxInfo fax)
	{
		boolean add = true;

		synchronized (mobjLock)
		{
			FaxInfo item = null;
			for (int i=0; i<faxList.size(); i++)
			{
				item = faxList.get(i);
				if (item.getFaxType() == fax.getFaxType()
						&& item.getFaxFile().equals(fax.getFaxFile())
						&& item.getCalled().equals(fax.getCalled())
                        )
				{
					add = false;
					break;
				}
			}

			if (add)
			{

				faxList.add(fax);
			}
		}

		return add;
	}
	/** 
	 删除某个元素
	 
	*/
	public static void Remove(int index)
	{
		if (index + 1 >= faxList.size())
		{
			return;
		}

		synchronized (mobjLock)
		{
			faxList.remove(index);
		}
	}
	/** 
	 删除某个元素
	 
	*/
	public static void Remove(FaxInfo fax)
	{
		synchronized (mobjLock)
		{
			faxList.remove(fax);
		}
	}
	/** 
	 删除某个元素
	 
	*/
	public static void Remove(int faxType, String faxFile)
	{
		FaxInfo fax = null;

		synchronized (mobjLock)
		{
			FaxInfo item = null;
			for (int i=0; i<faxList.size(); i++)
			{
				item = faxList.get(i);
				if (item.getFaxType() == faxType && item.getFaxFile().equals(faxFile))
				{
					fax = item;
					break;
				}
			}

			if (fax != null)
			{
				faxList.remove(fax);
			}
		}
	}
	/** 
	 获取某个元素
	 
	*/
	public static FaxInfo GetAt(int index)
	{
		if (index + 1 >= faxList.size())
		{
			return null;
		}

		FaxInfo fax = null;

		synchronized (mobjLock)
		{
			fax = (FaxInfo)faxList.get(index);
		}

		return fax;
	}
	/** 
	 获取某个元素
	 
	*/
	public static FaxInfo GetAt(int faxType, String faxFile)
	{
		FaxInfo fax = null;

		synchronized (mobjLock)
		{
			for (FaxInfo item : faxList)
			{
				if (item.getFaxType() == faxType && item.getFaxFile().equals(faxFile))
				{
					fax = item;
					break;
				}
			}
		}

		return fax;
	}
	/** 
	 改变状态
	 
	*/
	public static void ChangeStatus(int index, int status)
	{
		if (index + 1 >= faxList.size())
		{
			return;
		}

		((FaxInfo)faxList.get(index)).setStatus(status);
		if (status == 1)
		{
			((FaxInfo)faxList.get(index)).setSendTime(Utls.getLocalSeconds());
		}
	}
	/** 
	 改变状态
	 
	*/
	public static void ChangeStatus(int faxType, String faxFile, int status)
	{
		FaxInfo fax = null;

		synchronized (mobjLock)
		{
			for (FaxInfo item : faxList)
			{
				if (item.getFaxType() == faxType && item.getFaxFile().equals(faxFile))
				{
					fax = item;
					break;
				}
			}

			if (fax != null)
			{
				fax.setStatus(status);
				fax.setSendTime(Utls.getLocalSeconds());
			}
		}
	}
	/** 
	 获取待发送传真
	 
	*/
	public static FaxInfo GetWaitSendFile()
	{
		FaxInfo fax = null;

		synchronized (mobjLock)
		{
			for (FaxInfo item : faxList)
			{
				if (item.getFaxType() == 1)
				{
					fax = item;
					break;
				}
			}
		}

		return fax;
	}
	/** 
	 获取待打印传真
	 
	*/
	public static FaxInfo GetWaitPrintFile()
	{
		FaxInfo fax = null;

		synchronized (mobjLock)
		{
			for (FaxInfo item : faxList)
			{
				if (item.getFaxType() == 2)
				{
					fax = item;
					break;
				}
			}
		}

		return fax;
	}

	/**
	 增加通信录

	 */
	public static void Add2PhoneBook(String strName, String strNumber)
	{
		List<Person> personList = DataSupport.where("userCode = ?", strNumber).find(Person.class);
		if (personList != null && personList.size() > 0) {
			return;
		}

		Person person = new Person();
		person.setUserName(strName);
		person.setUserCode(strNumber);

		person.save();
	}

}
