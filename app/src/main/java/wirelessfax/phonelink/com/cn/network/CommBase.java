package wirelessfax.phonelink.com.cn.network;


import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.LoggerFactory;

public class CommBase implements Cloneable {
	
	private org.slf4j.Logger log = LoggerFactory.getLogger(CommBase.class);

	private int inputSendIndex = 1;
	
	protected NetworkInfo networkInfo = null;
	protected CommChannel clientTransceiver = null;
	
	public CommBase()
	{
		
	}
	
	public CommBase(NetworkInfo networkInfo)
	{
		this.networkInfo = networkInfo;
	}

	public void clear()
	{

	}

	
	public boolean checkValid()
	{

		
		return true;
	}


	
	public void setCommonChannel(CommChannel commonChannel) 
	{
		if (this.clientTransceiver != null) {
			//todo...
			
		}
		
		this.clientTransceiver = commonChannel;
	}
	
	public CommChannel getCommonChannel()
	{
		return this.clientTransceiver;
	}
	

	public void setNetworkInfo(NetworkInfo networkInfo)
	{
		this.networkInfo = networkInfo;
	}
	
	public NetworkInfo getNetworkInfo()
	{
		return networkInfo;
	}
	
	public CommChannel getClientChannel()
	{
		return this.clientTransceiver;
	}
	
	public void setClientChannel(CommChannel clientChannel)
	{
		if(clientTransceiver != null) {
			clientTransceiver.stopClient();
		}
		
		this.clientTransceiver = clientChannel;
	}
	
	
	public byte[] getMac()
	{
		if(networkInfo == null) {
			return null;
		}
		
		return networkInfo.getMac();
	}
	
	public void setMac(byte[] mac)
	{
		if(networkInfo == null) {
			return ;
		}
		
		networkInfo.setMac(mac);
	} 
	
	public String getStrIp()
	{
		if(networkInfo == null) {
			return null;
		}
		return networkInfo.getStrIp();
	}
	
	public void setStrIp(String strIp)
	{
		if(networkInfo == null) {
			return ;
		}
		networkInfo.setStrIp(strIp);
	}
	
	public int getPort()
	{
		if(networkInfo == null) {
			return -1;
		}
		
		return networkInfo.getPort();
	}
	
	public int getState()
	{
		if(networkInfo == null) {
			return -1;
		}
		
		return networkInfo.getState();
	}
	
	public void setState(int state)
	{
		if(networkInfo == null) {
			return ;
		}
		
		networkInfo.setState(state);
	}

	
	public void setLastTick(int iTick)
	{
		if(networkInfo == null) {
			return ;
		}
		
		networkInfo.setLastTick(iTick);
	}
	
	public int getLastTick()
	{
		if(networkInfo == null) {
			return -1;
		}
		
		return networkInfo.getLastTick();
	}
	
	public int getTickCount()
	{
		if(networkInfo == null) {
			return -1;
		}
		
		return networkInfo.tickCount;
	}
	
	public void setTickCount(int iTickCount)
	{
		if(networkInfo == null) {
			return ;
		}
		
		networkInfo.tickCount = iTickCount;
	}
	
	public void incTickCount()
	{
		if(networkInfo == null) {
			return  ;
		}
		
		networkInfo.tickCount++;
	}
	
	public void clearTickCount()
	{
		if(networkInfo == null) {
			return ;
		}
		
		networkInfo.tickCount = 0;
	}
	
	public boolean sendPkt(NetworkData data) 
	{
		if(clientTransceiver == null) {
			return false;
		}
		
		this.clientTransceiver.addToSendList(data);
		return true;
	}
	
	public boolean sendDelayPkt(NetworkData data)
	{
		if(clientTransceiver == null) {
			return false;
		}
		
		this.clientTransceiver.addToDelaySendPktList(data);
		return true;
	}
	
	public void stopTransceiver()
	{
		if(clientTransceiver == null) {
			return ;
		}
		
		this.clientTransceiver.stopClient();
	}
	
	public void startTransceiver()
	{
		if(clientTransceiver == null) {
			return ;
		}
		
		this.clientTransceiver.startClient();
	}
	

	
	public int getCurrentInputBlockIndex()
	{
		return inputSendIndex;
	}

	
	public void reInitNextInputBlockIndex()
	{
		inputSendIndex = 1;
	}
}
