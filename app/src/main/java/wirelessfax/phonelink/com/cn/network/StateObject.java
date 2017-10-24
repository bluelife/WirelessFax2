package wirelessfax.phonelink.com.cn.network;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#endregion

import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;

import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Sqlite.FileLibController;
import wirelessfax.phonelink.com.cn.Utls.Utls;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#region 连接保存类
public class StateObject extends CommBase
{
    private org.slf4j.Logger log = LoggerFactory.getLogger(StateObject.class);

	public int mnConnectCount = 0;
		///#region 变量定义
	public CommChannel ClientSocket; // Association Classes
	public static final int BufferSize = 4096; // Size of receive buffer.
	public byte[] buffer; // Receive buffer.
	//public byte[] aryReceiveData; // Receive buffer.
	private java.util.ArrayList aryReceiveData; // Receive data
	private volatile boolean mbIsLogin; // Login symbol
	private String mstrPath; // FilePath
	private String mstrErrorSymbol; // Error symbol
	private byte mnCurrentPage; // Current page no
	private volatile boolean mbIsRun; // Receive or send file
	private String mstrMobile; // Destination number
    private String mstrSenderMobile;// source number, sender number
	private int mnTryViews; // Try send file degree
	private java.util.Hashtable hashTable; // Buffer receive file data
	private byte mnPageCount; // Fax file contain pages
	private static final byte Command0 = 32;
	private static final byte Command1 = 33;
	private static final byte Command2 = 34;
	private static final byte Command3 = 35;
	private static final byte Command4 = 36;
	private static final byte Command5 = 37;
	private static final byte Command6 = 38;
	private static final byte Command7 = 39;
	private static final byte Command8 = 40;
	private static final byte Command9 = 41;
	private static final byte CommandA = 42;
	private static final byte CommandB = 43;
	private static final byte CommandC = 44;
	private static final byte CommandE= 47;//S15  => 0x2F 代替S2
	private static final byte CommandF = 48;
	private static final byte EndPage = 49;
	private static final byte NextPage = 48;
	private static final int PackageCount = 128; //最大包数量
	private static final int MinLength = 10; //检查最小长度
	private static final byte BeginS = 42; //开始符
	private static final byte EndS = 35; //结束符
	private static final int LenLength = 2; //长度
	private static final int SignLength = 4; //符号长度
	private static final int Distance = 6; //间隔距离
	private static final int PackageLength = 512; //数据包长度
	private static final int ErrorLength = 16; //错误描述数据包长度
	private static final int MaxMobileLength = 12; //最大移动号码长度
	private static final int MaxBuffer = 20000; //最大缓存
	private static final int KeyLength = 10; //密钥长度
	private static final int NowLength = 14; //当前时间长度

	public static StateObject gInstance = null;
	public static StateObject getInstance()
	{
		if(gInstance == null) {
			gInstance = new StateObject();

            NetworkInfo networkInfo = new NetworkInfo(FaxPath.GetRemoteServer(), FaxPath.GetRemotePort(), null);
            gInstance.setNetworkInfo(networkInfo);
            CommChannel commChannel = new TCPClientChannel(gInstance);
            commChannel.startClient();

            gInstance.setClientChannel(commChannel);
		}

		return gInstance;
	}


		///#region 公用函数
	/** 
	 构造函数
	 
	*/
	public StateObject()
	{
		ClientSocket = null;
		buffer = new byte[BufferSize];
		aryReceiveData = new java.util.ArrayList();
		mbIsLogin = false;
		mstrPath = "";
		InitErrorSymbol();
		mnCurrentPage = 0;
		mbIsRun = false;
		mstrMobile = "";
        mstrSenderMobile="";
		mnTryViews = 0;
		hashTable = new java.util.Hashtable();
		mnPageCount = 0;
	}
	/** 
	 解析数据
	 
	*/
	public final void ParseData()
	{
		boolean bSuccess = false, bBegin = false, bEnd = false;
		int nLength = 0, nBegin = -1, nEnd = -1, nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = new byte[2], aryData = null;

		nLength = aryReceiveData.size();

		if (nLength >= MinLength)
		{
			nBegin = aryReceiveData.indexOf(BeginS);
			nEnd = aryReceiveData.indexOf(EndS);
            log.debug("data  start tag: "+nBegin+"  end:"+nEnd);

			while (nBegin < nEnd)
			{
				if (bBegin == false)
				{
					if (nBegin + SignLength <= nLength) //判断是否连续四个开始符
					{
						if (((byte)aryReceiveData.get(nBegin + 1)) == BeginS
								&& ((byte)aryReceiveData.get(nBegin + 2)) == BeginS
								&& ((byte)aryReceiveData.get(nBegin + 3)) == BeginS)
						{
                            log.debug("data found start tag");
							bBegin = true;
						}
					}
				}

				if (bEnd == false)
				{
					if (nEnd + SignLength <= nLength) //判断是否连续四个结束符
					{
						if (((byte)aryReceiveData.get(nEnd + 1)) == EndS
								&& ((byte)aryReceiveData.get(nEnd + 2)) == EndS
								&& ((byte)aryReceiveData.get(nEnd + 3)) == EndS)
						{
                            log.debug("data found end tag: "+nEnd+" total length:"+nLength);
							if (nEnd + SignLength + 1 <= nLength)
							{
								if (((byte)aryReceiveData.get(nEnd + SignLength)) == EndS) //判断是否连续五个结束符
								{
									nEnd++;
								}
							}

                            log.debug("222data found end tag: "+nEnd+" total length:"+nLength);
							bEnd = true;
						}
					} else {
                        log.debug("data not found end tag: "+nEnd+" total length:"+nLength);
                        break;
                    }
				}

				if (bBegin && bEnd)
				{
					if (nEnd - nBegin >= Distance)
					{
						//System.arraycopy(aryReceiveData, nBegin + SignLength, aryTemp, 0, Distance - SignLength);
						for(int i=nBegin + SignLength, j=0; i<aryReceiveData.size(); i++, j++) {
							aryTemp[j] = (byte)aryReceiveData.get(i);
							if(j>=(Distance - SignLength-1)) {
								break;
							}
						}

						//aryReceiveData.CopyTo(nBegin + SignLength, aryTemp, 0, Distance - SignLength);
						nDataLength = ((aryTemp[0] & 0x000000FF));
                        nDataLength |= ((aryTemp[1] & 0x000000FF)<<8);;
                        log.debug("real data length "+nDataLength+ "  "+ Utls.byte2HexStr(aryTemp));

						nFactLength = nEnd - nBegin - SignLength;

						if (nFactLength == nDataLength)
						{
							aryData = new byte[nFactLength];
							//System.arraycopy(aryReceiveData, nBegin + SignLength, aryData, 0, nFactLength);
							//aryReceiveData.CopyTo(nBegin + SignLength, aryData, 0, nFactLength);
							for(int i=nBegin + SignLength, j=0; i<aryReceiveData.size(); i++, j++) {
                                aryData[j] = (byte)aryReceiveData.get(i);
								if(j>=nFactLength-1) {
									break;
								}
							}

							bSuccess = true;
							bBegin = false;
							bEnd = false;

							if (CheckData(aryData))
							{
                                log.debug("before DealWithCommand");
								DealWithCommand(aryData);
							}
                            log.debug("after DealWithCommand");
						}
						else
						{
							bEnd = false;
						}
					}
					else
					{
                        log.debug("data not found end tag: "+nEnd+" total length:"+nLength);
						bEnd = false;
					}
				}

				if (bSuccess)
				{
					//aryReceiveData.removeRange(0, nEnd + SignLength + 0); //清空已处理数据
					for (int i=0; i<nEnd + SignLength; i++) {
						aryReceiveData.remove(0);
					}

					nBegin = aryReceiveData.indexOf(BeginS);
					nEnd = aryReceiveData.indexOf(EndS);
					nLength = aryReceiveData.size();
					bSuccess = false;
				}
				else
				{
//                    if(nEnd + 1 < nLength) {
//                        nEnd = aryReceiveData.indexOf(EndS, nEnd + 1);
//                    } else {
//                        break;
//                    }
                    if(nEnd +1 < nLength) {
                        while ((++nEnd) < aryReceiveData.size()) {
                            if ((byte) aryReceiveData.get(nEnd) == EndS) {
                                break;
                            }
                        }
                    } else {
                        break;
                    }

				}
			}
		}
		//判断是否超出最大缓存
		nLength = aryReceiveData.size();

		if (nLength >= MaxBuffer)
		{
			aryReceiveData.clear();
		}
	}
	/** 
	 合并数据
	 
	*/
	public final void AppendData(int nLength)
	{
		if (nLength <= 0)
		{
			return;
		}

		int nIndex = 0;
		//StringBuilder sb = new StringBuilder();

		//开始添加数据
		for (byte nValue : buffer)
		{
			aryReceiveData.add(nValue);
			//sb.Append(string.Format("{0:X2} ", nValue));
			++nIndex;

			if (nIndex >= nLength)
			{
				break;
			}
		}

		//FaxLogFile.WriteEnd(sb.ToString());
	}
	/** 
	 发送传真
	 
	*/
	public final boolean SendFax(String strFileName, String called, String strSenderMobile, byte pages)
	{
		if (!mbIsLogin)
		{
			log.debug("not login.");
			return false;
		}

		if (mbIsRun)
		{
			log.debug("not running.");
			return false;
		}

		log.debug("请求发送传真["+strFileName+"]，页数为["+pages+"]");
//		RaiseInfoEvent(String.format("请求发送传真[%1$s]，页数为[%2$s]", strFileName, pages));
		SendData(ParseCommand.ParseSE(pages, called, strSenderMobile));
		mstrPath = strFileName;
		mnPageCount = pages;
		mnCurrentPage = 0;
		mstrMobile = called;

        mstrSenderMobile = strSenderMobile;

        return true;
	}

	/** 
	 数据长度
	 
	*/
	public final int getDataLength()
	{
		return aryReceiveData.size();
	}
	/** 
	 Thread signal.

	/** 
	 Run or stop
	 
	*/
	public final boolean getIsRun()
	{
		return mbIsRun;
	}
	public final void setIsRun(boolean value)
	{
		mbIsRun = value;
	}

		///#region 私有函数
	/** 
	 校验数据是否正确接收
	 
	*/
	private boolean CheckData(byte[] aryData)
	{
		if (aryData == null)
		{
			return false;
		}

		int nLength = aryData.length;
		byte nTotal = 0;
		byte[] aryTotal = null;

		aryTotal = new byte[nLength - 1];
		System.arraycopy(aryData, 0, aryTotal, 0, nLength - 1);
		nTotal = TotalValue(aryTotal);

		if (nTotal == aryData[nLength - 1])
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	/** 
	 处理命令
	 
	*/
	private void DealWithCommand(byte[] aryData)
	{
		String strNow = "";
		byte nCommand = 0, nMobileLength = 0, nPage = 0, nSenderMobileLength=0;
        int nPackage = 0;
		boolean bFinish = false;
		int nLength = 0;
		byte[] aryTemp = null, aryFile = null;

		nCommand = aryData[LenLength];

		switch (nCommand)
		{
			case Command0: //登录验证
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				break;
			case Command1: //验证成功
				if (mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				mbIsLogin = true;
				SendData(ParseCommand.ParseSA((byte)1));
//				RaiseInfoEvent("终端登录成功");
//				RaiseLoginEvent(true);
				break;
            case CommandE:
			case Command2: //请求传送数据
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}

				if (mbIsRun == false)
				{
					mnPageCount = aryData[3];
					nMobileLength = aryData[4];
					aryTemp = new byte[nMobileLength];
					System.arraycopy(aryData, 5, aryTemp, 0, nMobileLength);
					mstrMobile = FilterMobile(new String(aryTemp));
					MakeFileName(mstrMobile);

                    if(nCommand == CommandE) {
                        //发送者
                        if(aryData.length <= 5+nMobileLength) {
                            return;
                        }
                        nSenderMobileLength = aryData[5+nMobileLength];

                        if(aryData.length <= 5+nMobileLength+1+nSenderMobileLength) {
                            return;
                        }
                        aryTemp = new byte[nSenderMobileLength];
                        System.arraycopy(aryData, 5+nMobileLength+1, aryTemp, 0, nSenderMobileLength);
                        mstrSenderMobile = FilterMobile(new String(aryTemp));
                    }

					mbIsRun = true;
					hashTable.clear();
					SendData(ParseCommand.ParseS3());
					aryTemp = null;

                    log.debug("recv fax start to recv fax."+mstrSenderMobile+" send to "+mstrMobile+"  filepath:"+mstrPath);
				}
				break;
			case Command3: //开始传送数据
				if (!mbIsLogin)
				{
                    log.debug("send fax start sending not login");
                    this.getClientChannel().stopClient();
					break;
				}

				mbIsRun = true; //设置状态为运行
				FaxManager.ChangeStatus(1, mstrPath, 1); //传真文件发送中
				log.debug("send fax start sending");
				SendFile(""); //开始传送传真文件

				break;
			case Command4: //发送页结束
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}

				mnCurrentPage = aryData[3]; //页号
                byte tmp = aryData[4];
                nPackage = (tmp&0x000000ff); //总包数
                log.debug("recv fax current Page:"+mnCurrentPage+"    total pkt:"+nPackage+"   has nextPage:"+aryData[5]);
				if (aryData[5] == NextPage)
				{
					bFinish = false;
				}
				else
				{
					bFinish = true;
				}

				if (CheckReceiveSuccess(nPackage, bFinish))
				{
					if (bFinish)
					{
						//add to sql
						FileLib fileLib = new FileLib();
						fileLib.setiType(FileLibController.RECV_TYPE);

                        fileLib.setStrSender(mstrMobile);
                        fileLib.setStrReceiver(mstrSenderMobile);

						fileLib.setStrFilePath(mstrPath);
						fileLib.setbRead(false);
						FileLibController.getInstance().saveFax(fileLib);

						FaxManager.Add2PhoneBook(mstrMobile, mstrMobile);
						FaxManager.mbRecvNewFax = true;

						log.debug("recv fax ,successfully."+mstrPath);
						InitErrorSymbol(); //初始化错误变量
						mbIsRun = false;
						mstrPath = "";
						mstrMobile = "";
						mnPageCount = 0;
					}
					else
					{
						log.debug("recv fax , resend error page");
						InitErrorSymbol(); //初始化错误变量
					}
                    log.debug("recv fax current Page:"+mnCurrentPage+"  ok.");
					SendData(ParseCommand.ParseS5(mnCurrentPage));
				}
				else
				{
                    log.debug("recv fax current Page:"+mnCurrentPage+"  has error exist.");
					ResetErrorSymbol(nPackage);
					SendData(ParseCommand.ParseS6(mnCurrentPage, mstrErrorSymbol));
				}

				break;
			case Command5: //接收数据成功
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}

				nPage = aryData[3];
				mnTryViews = 0;
                log.debug("send fax  S5  file send ok."+nPage+" ?= "+mnCurrentPage);

				if (nPage == mnCurrentPage)
				{
//					RaiseInfoEvent(String.format("传真文件[%1$s]发送至[%2$s]成功", mstrPath, mstrMobile));
//					RaiseFaxEvent(1, true, mstrPath);
					FaxLogFile.WriteError(String.format("Socket发送文件[%1$s]成功", mstrPath));
                    log.debug("send fax  S5  file send successfully.");
                    FaxInfo fax = FaxManager.GetAt(1,this.mstrPath);
                    if(fax != null) {
                        log.debug("send fax  S5  file send successfully. set status");
                        fax.setStatus(2);
                    }
					InitErrorSymbol();
					mstrPath = "";
					mstrMobile = "";
					mbIsRun = false;
					mnCurrentPage = 0;
					mnPageCount = 0;
				}
				else
				{
					SendFile("");
				}

				break;
			case Command6: //接收数据错误
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
                log.debug("send fax  S6 has error.");
				nPage = aryData[3];
				mnCurrentPage = nPage;
				aryTemp = new byte[ErrorLength];
				System.arraycopy(aryData, 4, aryTemp, 0, ErrorLength);
				ReplayData(nPage, aryTemp);
				aryTemp = null;
				break;
			case Command7: //登录服务器
				if (mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				//模拟终端发起
				break;
			case Command8: //通知密钥位置
				if (mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}

				aryTemp = new byte[Distance];
				System.arraycopy(aryData, 3, aryTemp, 0, Distance);
                log.debug("send Command8 :"+Utls.byte2HexStr(aryTemp));
				SendData(ParseCommand.ParseS9(aryTemp[2], aryTemp[5]));
				break;
			case Command9: //获取密钥并检查是否正确
				if (mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				//模拟终端发起
				break;
			case CommandA: //终端通知服务器其类型
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				//模拟终端发起
				break;
			case CommandB: //终端请求服务器当前时间
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				//模拟终端发起
				break;
			case CommandC: //服务器通知终端当前时间
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				aryTemp = new byte[NowLength];
				System.arraycopy(aryData, 3, aryTemp, 0, NowLength);
				strNow = new String(aryTemp);
				break;

			case CommandF: //接收数据
				if (!mbIsLogin)
				{
                    this.getClientChannel().stopClient();
					break;
				}
				nLength = aryData.length;
				mnCurrentPage = aryData[3]; //页号
				nPackage = aryData[4]; //包号
				ChangeErrorSymbol(nPackage);
				aryFile = new byte[nLength - Distance];
				System.arraycopy(aryData, 5, aryFile, 0, nLength - Distance);
				WriteHashTable(nPackage, aryFile);
				aryFile = null;
				break;
		}
	}
	/** 
	 初始化错误标志
	 
	*/
	private void InitErrorSymbol()
	{
		mstrErrorSymbol = "";
		mstrErrorSymbol = Utls.padLeft(mstrErrorSymbol, PackageCount, '0');
	}
	/** 
	 过滤移动号码
	 
	*/
	private String FilterMobile(String strMobile)
	{
		if (strMobile.equals(""))
		{
			return "";
		}
		//过滤*#
		strMobile = strMobile.replace("*", "");
		strMobile = strMobile.replace("#", "");

		return strMobile;
	}
	/** 
	 改变错误标志
	 
	*/
	private void ChangeErrorSymbol(int nPackage)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < PackageCount; i++)
		{
			if (i == nPackage)
			{
				sb.append('1');
			}
			else
			{
				sb.append(mstrErrorSymbol.charAt(i));
			}
		}

		mstrErrorSymbol = sb.toString();
	}
	/** 
	 整理错误标志
	 
	*/
	private void ResetErrorSymbol(int nPackage)
	{
		mstrErrorSymbol = mstrErrorSymbol.substring(0, nPackage);
		mstrErrorSymbol = Utls.padRight(mstrErrorSymbol, PackageCount, '1');
	}
	/** 
	 检查是否完成接收成功
	 
	*/
	private boolean CheckReceiveSuccess(int nPackage, boolean bFinish)
	{
		boolean bSucees = true;

		for (int i = 0; i < nPackage; i++)
		{
			if (mstrErrorSymbol.charAt(i) == '0')
			{
				log.debug("recv fax has some pages error."+i);
				bSucees = false;
				break;
			}
		}

		if (bSucees) //接收成功，写入文件
		{
			log.debug("recv fax successfully, write to file.");
			WriteFile(nPackage);

			if (bFinish)
			{
//				RaiseInfoEvent(String.format("成功接收传真文件[%1$s]", mstrPath));
//				RaiseFaxEvent(2, true, mstrPath);
				FaxLogFile.WriteError(String.format("Socket成功接收文件[%1$s]", mstrPath));
			}
		}

		return bSucees;
	}
	/** 
	 产生文件名包括路径
	 
	*/
	private void MakeFileName(String strMobile)
	{
		FilePath filePath = new FilePath(strMobile);

		mstrPath = filePath.MakeFullPath();
	}
	/** 
	 将内容写入哈希表
	 
	*/
	private void WriteHashTable(int nPackage, byte[] aryData)
	{
		String strKey = String.format("key%1$s", nPackage);

		hashTable.put(strKey, aryData);
	}
	/** 
	 将内容写入文件
	 
	*/
	private void WriteFile(int nPackage)
	{
		if (mstrPath.equals(""))
		{
            log.debug("recv fax , file path:"+mstrPath);
			return;
		}

		byte[] aryData = null;
		int i = 0;
		String strKey = "";

		try
		{
            log.debug("recv fax, start to write file:");
			RandomAccessFile fs = new RandomAccessFile(mstrPath,"rw");
            log.debug("recv fax, start to write file2222");
			//FileStream fs = File.Open(mstrPath, FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.ReadWrite);
			try
			{
                long fileLength = fs.length();
                // 将写文件指针移到文件尾。
                fs.seek(fileLength);
                //fs.Position = fs.getLength(); //移动游标

				for (i = 0; i < nPackage; i++)
				{
                    log.debug("recv fax, get key:"+i);
					strKey = String.format("key%1$s", i);
					aryData = (byte[])hashTable.get(strKey);
					fs.write(aryData, 0, aryData.length);
				}

				fs.close();
			}
			finally
			{
				if(fs != null) {
					fs.close();
				}
			}

			hashTable.clear();
		}
		catch (Exception e)
		{
//			RaiseInfoEvent(String.format("创建文件[%1$s]失败", mstrPath));
			FaxLogFile.WriteError(String.format("Socket尝试创建文件[%1$s]失败，由于%2$s", mstrPath, e.getMessage()));
		}
	}
	/** 
	 发送传真文件
	 
	*/
	private void SendFile(String strError)
	{
        log.debug("send fax start sending222");
		if (mstrPath.equals(""))
		{
			log.debug("send fax file is empty.");
			return;
		}

		if (mnTryViews >= PublicVariable.TryViews)
		{
            log.debug("send fax  mnTryViews >= PublicVariable.TryViews.");
//			RaiseInfoEvent(String.format("传真文件[%1$s]发送失败", mstrPath));
//			RaiseFaxEvent(1, false, mstrPath);
			FaxLogFile.WriteError(String.format("Socket尝试3次重发文件[%1$s]失败", mstrPath));
			mbIsRun = false;
			mstrPath = "";
			mstrMobile = "";
			mnPageCount = 0;
			mnTryViews = 0;
			return;
		}

		mnTryViews++; //累计重试次数

		int nIndex = 0;

		try
		{
            log.debug("send fax start sending333:"+mstrPath);
			RandomAccessFile fs = new RandomAccessFile(mstrPath,"r");
            log.debug("send fax start sending3330");
			try
			{
				byte[] aryContent = new byte[PackageLength];
				byte[] aryData = null;
				boolean bError = false;
				int nRead = 0;

				if (!strError.equals(""))
				{

					bError = true;
				}

				int page = (mnCurrentPage&0x000000ff);
				log.debug("send fax start sending444  "+page);
				fs.seek(page * PackageLength * PackageCount);
                log.debug("send fax start sending444");
				while ((nRead = fs.read(aryContent, 0, PackageLength)) > 0)
				{
                    log.debug("send fax file while: "+nIndex);
					if (nIndex == PackageCount)
					{
                        log.debug("send fax file page: "+page+" break.");
						break;
					}

					aryData = new byte[nRead];
					System.arraycopy(aryContent, 0, aryData, 0, nRead);

					if (bError)
					{
                        log.debug("send fax file error page resend");
						if (strError.charAt(nIndex) == '0')
						{
							SendData(ParseCommand.ParseSF(mnCurrentPage, (byte)(nIndex&0x000000ff), aryData));
						}
					}
					else
					{
						log.debug("send fax  send real data.");
						SendData(ParseCommand.ParseSF(mnCurrentPage, (byte)(nIndex&0x000000ff), aryData));
					}

					nIndex++;
				}
				//发送页结束标志
				if (nIndex == PackageCount)
				{
                    log.debug("send fax file more than  "+PackageCount);
					nRead = fs.read(aryContent, 0, PackageLength);
					fs.close(); //关闭文件

					if (nRead > 0)
					{
						SendData(ParseCommand.ParseS4(mnCurrentPage, (byte)(nIndex&0x000000ff), false));
						mnCurrentPage++;
					}
					else
					{
						SendData(ParseCommand.ParseS4(mnCurrentPage, (byte)(nIndex&0x000000ff), true));
					}
				}
				else
				{
                    log.debug("send fax file S4 ");
					fs.close(); //关闭文件
					SendData(ParseCommand.ParseS4(mnCurrentPage, (byte)(nIndex&0x000000ff), true));
				}
			}
			finally
			{
				if(fs != null) {
					fs.close();
				}
			}
		}
		catch (Exception e)
		{
            log.debug("send fax file Exception "+e.getMessage());
//			RaiseInfoEvent(String.format("打开文件[%1$s]失败", mstrPath));
			FaxLogFile.WriteError(String.format("Socket尝试打开文件[%1$s]失败，由于%2$s", mstrPath, e.getMessage()));
			mbIsRun = false;
			mstrPath = "";
			mstrMobile = "";
			mnPageCount = 0;
		}
	}
	/** 
	 重新发送接收错误的数据包
	 
	*/
	private boolean ReplayData(int nPage, byte[] aryError)
	{
		if (mnTryViews >= PublicVariable.TryViews)
		{
//			RaiseInfoEvent(String.format("传真文件[%1$s]发送失败", mstrPath));
//			RaiseFaxEvent(1, false, mstrPath);
			FaxLogFile.WriteError(String.format("Socket尝试3次重发文件[%1$s]失败", mstrPath));
			mbIsRun = false;
			mstrPath = "";
			mstrMobile = "";
			mnPageCount = 0;
			return false;
		}

		ParseError(aryError);
		SendFile(mstrErrorSymbol);

		return true;
	}
	/** 
	 解析错误
	 
	*/
	private void ParseError(byte[] aryError)
	{
		byte nData = 0;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ErrorLength; i++)
		{
			nData = aryError[i];
			sb.append(SplitError(nData));
		}

		mstrErrorSymbol = sb.toString();
	}
	/** 
	 分解错误字节
	 
	*/
	private String SplitError(byte nError)
	{
		StringBuilder sb = new StringBuilder();

		if ((nError & 0x1) == 0x1)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		if ((nError & 0x2) == 0x2)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		if ((nError & 0x4) == 0x4)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		if ((nError & 0x8) == 0x8)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		if ((nError & 0x10) == 0x10)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		if ((nError & 0x20) == 0x20)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		if ((nError & 0x40) == 0x40)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		if ((nError & 0x80) == 0x80)
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}

		return sb.toString();
	}
	/** 
	 累加和
	 
	*/
	private byte TotalValue(byte[] aryData)
	{
		int i = 0, nLength = aryData.length;
		int nValue = 0;

		for (i = 0; i < nLength; i++)
		{
			nValue = nValue + aryData[i];
			nValue = nValue & 0xff;
		}

		return (byte)nValue;
	}
	/** 
	 发送回复
	 
	*/
	public void SendData(byte[] data)
	{
		log.debug("send  data333");
		if (!this.getClientChannel().isConnected())
		{
			log.debug("send  data444");
			return;
		}
		log.debug("send login data555");
        NetworkData tmpData = new NetworkData(data);
        this.getClientChannel().addToSendList(tmpData);
	}

    public boolean getIsLogin()
    {
        return mbIsLogin;
    }

    public void setIsLogin(boolean value)
    {
       mbIsLogin = value;
    }
}