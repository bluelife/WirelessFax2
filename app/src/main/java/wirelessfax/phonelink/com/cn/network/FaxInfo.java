package wirelessfax.phonelink.com.cn.network;

import wirelessfax.phonelink.com.cn.Utls.Utls;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#region 传真信息
public class FaxInfo
{
	private int faxType;
	private int pages;
	private int status;
	private String faxFile;
	private String called;

	public String getSenderMobile() {
		return senderMobile;
	}

	public void setSenderMobile(String senderMobile) {
		this.senderMobile = senderMobile;
	}

	private String senderMobile;
	private int tryviews;
	private long sendTime = Utls.getLocalSeconds();

	public FaxInfo()
	{
		faxType = 0;
		pages = 0;
		status = 0;
		faxFile = "";
		called = "";
		tryviews = 0;
		sendTime = Utls.getLocalSeconds();
	}

	public final int getFaxType()
	{
		return faxType;
	}
	public final void setFaxType(int value)
	{
		faxType = value;
	}

	public final int getPages()
	{
		return pages;
	}
	public final void setPages(int value)
	{
		pages = value;
	}

	public final int getStatus()
	{
		return status;
	}
	public final void setStatus(int value)
	{
		status = value;
	}

	public final String getFaxFile()
	{
		return faxFile;
	}
	public final void setFaxFile(String value)
	{
		faxFile = value;
	}

	public final String getCalled()
	{
		return called;
	}
	public final void setCalled(String value)
	{
		called = value;
	}

	public final int getTryviews()
	{
		return tryviews;
	}
	public final void setTryviews(int value)
	{
		tryviews = value;
	}

	public final long getSendTime()
	{
		return sendTime;
	}
	public final void setSendTime(long value)
	{
		sendTime = value;
	}
}