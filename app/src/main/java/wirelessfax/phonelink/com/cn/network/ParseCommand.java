package wirelessfax.phonelink.com.cn.network;

import org.slf4j.LoggerFactory;

import wirelessfax.phonelink.com.cn.Utls.Utls;

///#region 命令解析类
public class ParseCommand
{
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ParseCommand.class);
		///#region 变量定义
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
    private static final byte CommandE = 47;//S15 --> 0x2F,代替S2
	private static final byte CommandF = 48;
	private static final String BeginSign = "****";
	private static final String EndSign = "####";
	private static final byte SignLength = 4;
	private static final byte LenLength = 2;
	private static final byte PasswordLength = 6;
	private static final byte OneLength = 1;
	private static final byte ErrorLength = 16;
	private static final byte ErrorStrLength = (byte)128;
	private static final byte EndPage = 49;
	private static final byte NextPage = 48;
	private static final byte ByteLength = 8;
	private static final byte KeyLength = 10;
	private static final byte TimeLength = 14;
	private static final byte[] KeyData = { 0x45, 0x4E, 0x44, 0x2D, 0x55, 0x53, 0x45, 0x52, 0x20, 0x4C, 0x49, 0x43, 0x45, 0x4E, 0x53, 0x45, 0x20, 0x41, 0x47, 0x52, 0x45, 0x45, 0x4D, 0x45, 0x4E, 0x54, 0x20, 0x46, 0x4F, 0x52, 0x20, 0x7A, 0x32, 0x20, 0x53, 0x4F, 0x46, 0x54, 0x57, 0x41, 0x52, 0x45, 0x0D, 0x0A, 0x49, 0x4D, 0x50, 0x4F, 0x52, 0x54, 0x41, 0x4E, 0x54, 0x20, 0x2D, 0x20, 0x52, 0x45, 0x41, 0x44, 0x20, 0x43, 0x41, 0x52, 0x45, 0x46, 0x55, 0x4C, 0x4C, 0x59, 0x3A, 0x20, 0x54, 0x68, 0x69, 0x73, 0x20, 0x7A, 0x32, 0x20, 0x45, 0x6E, 0x64, 0x2D, 0x55, 0x73, 0x65, 0x72, 0x20, 0x0D, 0x0A, 0x4C, 0x69, 0x63, 0x65, 0x6E, 0x73, 0x65, 0x20, 0x41, 0x67, 0x72, 0x65, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x6C, 0x65, 0x67, 0x61, 0x6C, 0x20, 0x61, 0x67, 0x72, 0x65, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x20, 0x62, 0x65, 0x74, 0x77, 0x65, 0x65, 0x6E, 0x0D, 0x0A, 0x79, 0x6F, 0x75, 0x20, 0x28, 0x65, 0x69, 0x74, 0x68, 0x65, 0x72, 0x20, 0x61, 0x6E, 0x20, 0x69, 0x6E, 0x64, 0x69, 0x76, 0x69, 0x64, 0x75, 0x61, 0x6C, 0x20, 0x6F, 0x72, 0x20, 0x61, 0x20, 0x73, 0x69, 0x6E, 0x67, 0x6C, 0x65, 0x20, 0x65, 0x6E, 0x74, 0x69, 0x74, 0x79, 0x29, 0x20, 0x61, 0x6E, 0x64, 0x20, 0x0D, 0x0A, 0x7A, 0x32, 0x20, 0x66, 0x6F, 0x72, 0x20, 0x74, 0x68, 0x65, 0x20, 0x7A, 0x32, 0x20, 0x73, 0x6F, 0x66, 0x74, 0x77, 0x61, 0x72, 0x65, 0x20, 0x70, 0x72, 0x6F, 0x64, 0x75, 0x63, 0x74, 0x20, 0x69, 0x64, 0x65, 0x6E, 0x74, 0x69, 0x66, 0x69, 0x65, 0x64, 0x20, 0x0D, 0x0A, 0x61, 0x62, 0x6F, 0x76, 0x65, 0x2C, 0x20, 0x77, 0x68, 0x69, 0x63, 0x68, 0x20, 0x69, 0x6E, 0x63, 0x6C, 0x75, 0x64, 0x65, 0x73, 0x20, 0x63, 0x6F, 0x6D, 0x70, 0x75, 0x74, 0x65, 0x72, 0x20, 0x73, 0x6F, 0x66, 0x74, 0x77, 0x61, 0x72, 0x65, 0x20, 0x61, 0x6E, 0x64, 0x20, 0x0D, 0x0A, 0x6D, 0x61, 0x79, 0x20, 0x69, 0x6E, 0x63, 0x6C, 0x75, 0x64, 0x65, 0x20, 0x61, 0x73, 0x73, 0x6F, 0x63, 0x69, 0x61, 0x74, 0x65, 0x64, 0x20, 0x6D, 0x65, 0x64, 0x69, 0x61, 0x2C, 0x20, 0x70, 0x72, 0x69, 0x6E, 0x74, 0x65, 0x64, 0x20, 0x6D, 0x61, 0x74, 0x65, 0x72, 0x69, 0x61, 0x6C, 0x73, 0x2C, 0x20, 0x61, 0x6E, 0x64, 0x20, 0x0D, 0x0A, 0x22, 0x6F, 0x6E, 0x6C, 0x69, 0x6E, 0x65, 0x22, 0x20, 0x6F, 0x72, 0x20, 0x65, 0x6C, 0x65, 0x63, 0x74, 0x72, 0x6F, 0x6E, 0x69, 0x63, 0x20, 0x64, 0x6F, 0x63, 0x75, 0x6D, 0x65, 0x6E, 0x74, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x20, 0x28, 0x22, 0x53, 0x4F, 0x46, 0x54, 0x57, 0x41, 0x52, 0x45, 0x20, 0x0D, 0x0A, 0x50, 0x52, 0x4F, 0x44, 0x55, 0x43, 0x54, 0x22, 0x29, 0x2E, 0x20, 0x54, 0x68, 0x65, 0x20, 0x53, 0x4F, 0x46, 0x54, 0x57, 0x41, 0x52, 0x45, 0x20, 0x50, 0x52, 0x4F, 0x44, 0x55, 0x43, 0x54, 0x20, 0x61, 0x6C, 0x73, 0x6F, 0x20, 0x69, 0x6E, 0x63, 0x6C, 0x75, 0x64, 0x65, 0x73, 0x20, 0x61, 0x6E, 0x79, 0x20, 0x0D, 0x0A, 0x75, 0x70, 0x64, 0x61, 0x74, 0x65, 0x73, 0x20, 0x61, 0x6E, 0x64, 0x20, 0x73, 0x75, 0x70, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73, 0x20, 0x74, 0x6F, 0x20, 0x74, 0x68, 0x65, 0x20, 0x6F, 0x72, 0x69, 0x67, 0x69, 0x6E, 0x61, 0x6C, 0x20, 0x53, 0x4F, 0x46, 0x54, 0x57, 0x41, 0x52, 0x45, 0x20, 0x50, 0x52, 0x4F, 0x44, 0x55, 0x43, 0x54, 0x0D, 0x0A, 0x70, 0x72, 0x6F, 0x76, 0x69, 0x64, 0x65, 0x64, 0x20, 0x74, 0x6F, 0x20, 0x79, 0x6F, 0x75, 0x20 };


	/** 
	 通知在线，心跳
	 
	*/
	public static byte[] ParseS0(String strUser, String strPassword)
	{
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		strUser = FillZero(strUser, PasswordLength);
		strPassword = FillZero(strPassword, PasswordLength);
		nDataLength = (short)(LenLength + OneLength + PasswordLength + PasswordLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);

        nIndex = 0;
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = Command0;
		nIndex += OneLength;
		aryTemp = strUser.getBytes();
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, PasswordLength);
		nIndex += PasswordLength;
		aryTemp = strPassword.getBytes();
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, PasswordLength);
		nTotal = TotalValue(aryTotal);

		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command0;
		nIndex += OneLength;
		aryTemp = strUser.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, PasswordLength);
		nIndex += PasswordLength;
		aryTemp = strPassword.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, PasswordLength);
		nIndex += PasswordLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		log.debug("timeout Hex Data: "+Utls.byte2HexStr(aryData));

		return aryData;
	}
	/** 
	 已登录应答
	 
	*/
	public static byte[] ParseS1()
	{
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null;

		nDataLength = (short)(LenLength + OneLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		nTotal = (byte)(nDataLength + Command1);
		aryData = new byte[nFactLength];
		aryTemp =  BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command1;
		nIndex += OneLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 呼叫对方，准备发送数据
	 
	*/
	public static byte[] ParseS2(byte nPageCount, String strMobile)
	{
		if (strMobile.equals(""))
		{
			return null;
		}

		int nIndex = 0, nLen = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		nLen = strMobile.length();
		nDataLength = (short)(LenLength + OneLength + OneLength + OneLength + nLen + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = Command2;
		nIndex += OneLength;
		aryTotal[nIndex] = nPageCount;
		nIndex += OneLength;
		aryTotal[nIndex] = (byte)nLen;
		nIndex += OneLength;
		aryTemp = strMobile.getBytes();
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, nLen);
		nIndex += nLen;
		nTotal = TotalValue(aryTotal);

		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp =BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command2;
		nIndex += OneLength;
		aryData[nIndex] = nPageCount;
		nIndex += OneLength;
		aryData[nIndex] = (byte)nLen;
		nIndex += OneLength;
		aryTemp = strMobile.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, nLen);
		nIndex += nLen;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		log.debug("send fax S2: "+Utls.byte2HexStr(aryData));
		return aryData;
	}

	/**
	 呼叫对方，准备发送数据

	 */
	public static byte[] ParseSE(byte nPageCount, String strMobile, String strSenderMobile)
	{
		log.debug("send fax SE: recv: "+strMobile+"  sender: "+strSenderMobile);
		if (strMobile.isEmpty() || strSenderMobile.isEmpty())
		{
			log.debug("strMobile/strSenderMobile is empty.");
			return null;
		}

		int nIndex = 0, nLen = 0, nSenderLen = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		nLen = strMobile.length();
        nSenderLen = strSenderMobile.length();
		nDataLength = (short)(LenLength + OneLength + OneLength + OneLength + nLen + OneLength + nSenderLen + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
		aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
        aryTotal[nIndex] = Command2;
        if(nSenderLen > 0) {
            aryTotal[nIndex] = CommandE;
        }
		nIndex += OneLength;
		aryTotal[nIndex] = nPageCount;
		nIndex += OneLength;
		aryTotal[nIndex] = (byte)nLen;
		nIndex += OneLength;
		aryTemp = strMobile.getBytes();
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, nLen);
		nIndex += nLen;

        if (nSenderLen > 0) {
            //sender mobile
            aryTotal[nIndex] = (byte) nSenderLen;
            nIndex += OneLength;
            aryTemp = strSenderMobile.getBytes();
            System.arraycopy(aryTemp, 0, aryTotal, nIndex, nSenderLen);
            nIndex += nSenderLen;
        }
		log.debug("2222send fax SE: recv: "+strMobile+"  sender: "+strSenderMobile);
		nTotal = TotalValue(aryTotal);

		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp =BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
		aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;

		aryData[nIndex] = Command2;
        if (nSenderLen > 0) {
            aryData[nIndex] = CommandE;
        }
		nIndex += OneLength;
		aryData[nIndex] = nPageCount;
		nIndex += OneLength;
		aryData[nIndex] = (byte)nLen;
		nIndex += OneLength;
		aryTemp = strMobile.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, nLen);
		nIndex += nLen;
		log.debug("2222000send fax SE: recv: "+strMobile+"  sender: "+strSenderMobile+"datalen:"+nDataLength+" real:"+nFactLength);
        if (nSenderLen > 0) {
            aryData[nIndex] = (byte) nSenderLen;
            nIndex += OneLength;
            aryTemp = strSenderMobile.getBytes();
            System.arraycopy(aryTemp, 0, aryData, nIndex, nSenderLen);
            nIndex += nSenderLen;
        }
		log.debug("333send fax SE: recv: "+strMobile+"  sender: "+strSenderMobile);
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		log.debug("send fax SE: "+Utls.byte2HexStr(aryData));
		return aryData;
	}

	/** 
	 应答对方，已处于接收数据
	 
	*/
	public static byte[] ParseS3()
	{
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null;

        log.debug("send fax Recv S3 cmd");

		nDataLength = (short)(LenLength + OneLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		nTotal = (byte)(nDataLength + Command3);
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command3;
		nIndex += OneLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 发送方发出页结束
	 
	*/
	public static byte[] ParseS4(byte nCurrentPage, byte nPackage, boolean bEnd)
	{
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		nDataLength = (short)(LenLength + OneLength + OneLength + OneLength + OneLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = Command4;
		nIndex += OneLength;
		aryTotal[nIndex] = nCurrentPage;
		nIndex += OneLength;
		aryTotal[nIndex] = nPackage;
		nIndex += OneLength;

		if (bEnd)
		{
			aryTotal[nIndex] = EndPage;
		}
		else
		{
			aryTotal[nIndex] = NextPage;
		}

		nTotal = TotalValue(aryTotal);
		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command4;
		nIndex += OneLength;
		aryData[nIndex] = nCurrentPage;
		nIndex += OneLength;
		aryData[nIndex] = nPackage;
		nIndex += OneLength;

		if (bEnd)
		{
			aryData[nIndex] = EndPage;
		}
		else
		{
			aryData[nIndex] = NextPage;
		}

		nIndex += OneLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 接收一页正确的命令
	 
	*/
	public static byte[] ParseS5(byte nCurrentPage)
	{
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null;

		nDataLength = (short)(LenLength + OneLength + OneLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		nTotal = (byte)(nDataLength + Command5 + nCurrentPage);
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command5;
		nIndex += OneLength;
		aryData[nIndex] = nCurrentPage;
		nIndex += OneLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 接收一页错误的命令，并指定错误包索引
	 
	*/
	public static byte[] ParseS6(byte nPage, String strError)
	{
		if (strError.equals(""))
		{
			return null;
		}

		if (strError.length() != ErrorStrLength)
		{
			return null;
		}

		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		nDataLength = (short)(LenLength + OneLength + OneLength + ErrorLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = Command6;
		nIndex += OneLength;
		aryTotal[nIndex] = nPage;
		nIndex += OneLength;
		System.arraycopy(MakeError(strError), 0, aryTotal, nIndex, ErrorLength);
		nTotal = TotalValue(aryTotal);
		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command6;
		nIndex += OneLength;
		aryData[nIndex] = nPage;
		nIndex += OneLength;
		System.arraycopy(MakeError(strError), 0, aryData, nIndex, ErrorLength);
		nIndex += ErrorLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 登录服务器
	 
	*/
	public static byte[] ParseS7(String strUser, String strPassword)
	{
		log.debug("send login data");
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

        log.debug("send login data000");

		strUser = FillZero(strUser, PasswordLength);
		strPassword = FillZero(strPassword, PasswordLength);
		nDataLength = (short)(LenLength + OneLength + PasswordLength + PasswordLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
        System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = Command7;
		nIndex += OneLength;
		aryTemp = strUser.getBytes();
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, PasswordLength);
		nIndex += PasswordLength;
		aryTemp = strPassword.getBytes();
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, PasswordLength);
		nTotal = TotalValue(aryTotal);
        log.debug("send login data222 HEX:"+Utls.byte2HexStr(aryTotal));

		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command7;
		nIndex += OneLength;
		aryTemp = strUser.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, PasswordLength);
		nIndex += PasswordLength;
		aryTemp = strPassword.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, PasswordLength);
		nIndex += PasswordLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
        log.debug("send login data222");

		return aryData;
	}
	/** 
	 通知密钥位置
	 
	*/
	public static byte[] ParseS8(RefObject<Byte> nKeyGroup, RefObject<Byte> nKeyPos)
	{
		long tick = System.currentTimeMillis();
		java.util.Random ran = new java.util.Random((int)(tick & 0xffffffffL) | (int)(tick >> 32));
		int nIndex = 0, nResult = 0;
		byte nTotal = 0, nGroup = 0, nRan1 = 0, nRan2 = 0, nRan3 = 0, nRan4 = 0, nPos = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;
		//产生随机数
		nResult = ran.nextInt(49);
		nGroup = (byte)(nResult&0xff); //组号
		nKeyGroup.argvalue = nGroup;

		nResult = ran.nextInt(9);
		nRan1 = (byte)(nResult&0xff); //随机数1

		nResult = ran.nextInt(9);
		nRan2 =(byte)(nResult&0xff); //随机数2

		nResult = ran.nextInt(9);
		nRan3 = (byte)(nResult&0xff); //随机数3

		nResult = ran.nextInt(9);
		nRan4 = (byte)(nResult&0xff); //随机数4

		nResult = ran.nextInt(9);
		nPos = (byte)(nResult&0xff); //偏移量
		nKeyPos.argvalue = nPos;

		nDataLength = (short)(LenLength + OneLength + LenLength + OneLength + LenLength + OneLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = Command8;
		nIndex += OneLength;
		aryTotal[nIndex] = nRan1;
		nIndex += OneLength;
		aryTotal[nIndex] = nRan2;
		nIndex += OneLength;
		aryTotal[nIndex] = nGroup;
		nIndex += OneLength;
		aryTotal[nIndex] = nRan3;
		nIndex += OneLength;
		aryTotal[nIndex] = nRan4;
		nIndex += OneLength;
		aryTotal[nIndex] = nPos;
		nTotal = TotalValue(aryTotal);
		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command8;
		nIndex += OneLength;
		aryData[nIndex] = nRan1;
		nIndex += OneLength;
		aryData[nIndex] = nRan2;
		nIndex += OneLength;
		aryData[nIndex] = nGroup;
		nIndex += OneLength;
		aryData[nIndex] = nRan3;
		nIndex += OneLength;
		aryData[nIndex] = nRan4;
		nIndex += OneLength;
		aryData[nIndex] = nPos;
		nIndex += OneLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 发送密钥
	 
	*/
	public static byte[] ParseS9(byte nGroup, byte nPos)
	{
		int nIndex = 0, nKeyPos = 0, i = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		nDataLength = (short)(LenLength + OneLength + KeyLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
		aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = Command9;
		nIndex += OneLength;
		//最后一组不计算偏移量
		if (nGroup < 49)
		{
			for (i = 0; i < KeyLength; i++)
			{
				nKeyPos = nGroup * KeyLength + nPos + i;
				aryTotal[nIndex] = KeyData[nKeyPos];
				nIndex += OneLength;
			}
		}
		else
		{
			for (i = 0; i < KeyLength; i++)
			{
				nKeyPos = 490 + i;
				aryTotal[nIndex] = KeyData[nKeyPos];
				nIndex += OneLength;
			}
		}

		nTotal = TotalValue(aryTotal);
		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = Command9;
		nIndex += OneLength;
		//最后一组不计算偏移量
		if (nGroup < 49)
		{
			for (i = 0; i < KeyLength; i++)
			{
				nKeyPos = nGroup * KeyLength + nPos + i;
				aryData[nIndex] = KeyData[nKeyPos];
				nIndex += OneLength;
			}
		}
		else
		{
			for (i = 0; i < KeyLength; i++)
			{
				nKeyPos = 490 + i;
				aryData[nIndex] = KeyData[nKeyPos];
				nIndex += OneLength;
			}
		}

		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 终端通知服务器其类型
	 
	*/
	public static byte[] ParseSA(byte nType)
	{
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null;

		nDataLength = (short)(LenLength + OneLength + OneLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		nTotal = (byte)(nDataLength + CommandA + nType);
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = CommandA;
		nIndex += OneLength;
		aryData[nIndex] = nType;
		nIndex += OneLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 终端请求服务器时间
	 
	*/
	public static byte[] ParseSB()
	{
		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null;

		nDataLength = (short)(LenLength + OneLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		nTotal = (byte)(nDataLength + CommandB);
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = CommandB;
		nIndex += OneLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 服务器通知终端当前时间
	 
	*/
	public static byte[] ParseSC(String strNow)
	{
		if (strNow.equals("") || strNow.length() != TimeLength)
		{
			return null;
		}

		int nIndex = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		nDataLength = (short)(LenLength + OneLength + TimeLength + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = CommandC;
		nIndex += OneLength;
		aryTemp = strNow.getBytes();
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, TimeLength);
		nTotal = TotalValue(aryTotal);
		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = CommandC;
		nIndex += OneLength;
		aryTemp = strNow.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, TimeLength);
		nIndex += TimeLength;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 发送文件数据包
	 
	*/
	public static byte[] ParseSF(byte nPage, byte nPackage, byte[] aryContent)
	{
		int nIndex = 0, nLen = 0;
		byte nTotal = 0;
		short nDataLength = 0, nFactLength = 0;
		byte[] aryTemp = null, aryData = null, aryTotal = null;

		nLen = aryContent.length;
		nDataLength = (short)(LenLength + OneLength + OneLength + OneLength + nLen + OneLength);
		nFactLength = (short)(SignLength + nDataLength + SignLength);
		aryTotal = new byte[nDataLength - OneLength];
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryTotal, nIndex, LenLength);
		nIndex += LenLength;
		aryTotal[nIndex] = CommandF;
		nIndex += OneLength;
		aryTotal[nIndex] = nPage;
		nIndex += OneLength;
		aryTotal[nIndex] = nPackage;
		nIndex += OneLength;
		System.arraycopy(aryContent, 0, aryTotal, nIndex, nLen);
		nTotal = TotalValue(aryTotal);
		nIndex = 0;
		aryData = new byte[nFactLength];
		aryTemp = BeginSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);
		nIndex += SignLength;
        aryTemp = Utls.intTo2BigByteArray(nDataLength);
		System.arraycopy(aryTemp, 0, aryData, nIndex, LenLength);
		nIndex += LenLength;
		aryData[nIndex] = CommandF;
		nIndex += OneLength;
		aryData[nIndex] = nPage;
		nIndex += OneLength;
		aryData[nIndex] = nPackage;
		nIndex += OneLength;
		System.arraycopy(aryContent, 0, aryData, nIndex, nLen);
		nIndex += nLen;
		aryData[nIndex] = nTotal;
		nIndex += OneLength;
		aryTemp = EndSign.getBytes();
		System.arraycopy(aryTemp, 0, aryData, nIndex, SignLength);

		return aryData;
	}
	/** 
	 检查密钥是否正确
	 
	*/
	public static boolean CheckKey(byte nGroup, byte nPos, byte[] aryKey)
	{
		if (aryKey == null)
		{
			return false;
		}

		if (aryKey.length != KeyLength)
		{
			return false;
		}

		int nKeyPos = 0, i = 0;
		byte nValue = 0;
		boolean bSuccess = true;

		if (nGroup < 49)
		{
			for (i = 0; i < KeyLength; i++)
			{
				nKeyPos = nGroup * KeyLength + nPos + i;
				nValue = KeyData[nKeyPos];

				if (nValue != aryKey[i])
				{
					bSuccess = false;
					break;
				}
			}
		}
		else
		{
			for (i = 0; i < KeyLength; i++)
			{
				nKeyPos = 490 + i;
				nValue = KeyData[nKeyPos];

				if (nValue != aryKey[i])
				{
					bSuccess = false;
					break;
				}
			}
		}

		return bSuccess;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 静态私有函数
	/** 
	 数字字符串补零
	 
	*/
	private static String FillZero(String strValue, int nLength)
	{
		return Utls.padRight(strValue, nLength, '0');
	}
	/** 
	 累加和
	 
	*/
	private static byte TotalValue(byte[] aryData)
	{
		int i = 0, nLength = aryData.length;
		int nValue = 0;

		for (i = 0; i < nLength; i++)
		{
			nValue = nValue + aryData[i];
			nValue = nValue & 0xff;
		}

		return (byte)(nValue & 0xff);
	}
	/** 
	 产生错误描述包
	 
	*/
	private static byte[] MakeError(String strError)
	{
		byte[] aryData = new byte[ErrorLength];
		int i = 0, nIndex = 0;

		for (i = 0; i < ErrorStrLength; i += ByteLength)
		{
			aryData[nIndex] = MakeByte(strError.substring(i, i + ByteLength));
			nIndex++;
		}

		return aryData;
	}
	/** 
	 产生单字节值
	 
	*/
	private static byte MakeByte(String strError)
	{
		byte nResult = 0;

		if (strError.charAt(0) == '1')
		{
			nResult += 1;
		}

		if (strError.charAt(1) == '1')
		{
			nResult += 2;
		}

		if (strError.charAt(2) == '1')
		{
			nResult += 4;
		}

		if (strError.charAt(3) == '1')
		{
			nResult += 8;
		}

		if (strError.charAt(4) == '1')
		{
			nResult += 16;
		}

		if (strError.charAt(5) == '1')
		{
			nResult += 32;
		}

		if (strError.charAt(6) == '1')
		{
			nResult += 64;
		}

		if (strError.charAt(7) == '1')
		{
			nResult += 128;
		}

		return nResult;
	}
}
