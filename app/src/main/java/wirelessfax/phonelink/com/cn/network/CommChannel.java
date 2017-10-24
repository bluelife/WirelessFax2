package wirelessfax.phonelink.com.cn.network;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.List;


public abstract class CommChannel {
	protected CommSelector mSelector = null;
	protected AbstractSelectableChannel mChannel = null;
	protected InetSocketAddress serverAddress = null;

	protected CommBase mBase = null;
	
	protected int iBufSize = 3000;
	protected ByteBuffer recvBuf = null;
	protected byte[] recvByteBuf = null;
	
	protected ByteBuffer sendBuf = null;
	
	public long iEachPktSendInterval = 0;
	private NetworkDataList<NetworkData> sendList = null;	
	private NetworkDataList<NetworkData>  rcvList = null;
	private NetworkDataList<NetworkData>  delaySendPktList = null;

	
	public CommChannel(CommBase base)
	{
		mBase = base;
		mSelector = CommSelector.getInstantce();
		serverAddress = new InetSocketAddress(mBase.getStrIp(), mBase.getPort());
		
		recvBuf = ByteBuffer.allocate(iBufSize);
		recvByteBuf = new byte[iBufSize];
		sendBuf = ByteBuffer.allocate(iBufSize);
	
		sendList = new NetworkDataList();
		rcvList = new NetworkDataList();
		delaySendPktList = new NetworkDataList();
	}
	
	public abstract void init();
	public abstract void stopClient();
	public abstract void startClient();
	

	public void accept(SelectionKey key){};
	public abstract void send(SelectionKey key);
	public abstract void recv(SelectionKey key);
	public abstract boolean isConnected();
	public abstract boolean sendOutputCmd(int identifer, Object param);
	
	public AbstractSelectableChannel getChannel()
	{
		return this.mChannel;
	}
	
	
	public CommBase getMcuBase()
	{
		return mBase;
	}
	
	public InetSocketAddress getServerAddress()
	{
		return this.serverAddress;
	}
	
	public synchronized void addToSendList(NetworkData data) {
		this.sendList.addData(data);

		mSelector.addWriteOps(this);
	}
	
	public synchronized void addToRcvList(NetworkData data) {
		//Log.v(Constant.TAG, "Channel addToRcvList! size: "+this.rcvList.getLength());
		this.rcvList.addData(data);
	}
	
	public synchronized void addToDelaySendPktList(NetworkData data){
		this.delaySendPktList.addData(data);
	}
	
	public int getSendListSize(){
		return this.sendList.getLength();
	}
	
	public synchronized NetworkData getSendNetworkData(){
		return this.sendList.getData();
	}
	
	public synchronized NetworkData getLastSendNetworkData(){
		return this.sendList.getLastData();
	}
	
	public synchronized NetworkData getRcvNetworkData(){
		return this.rcvList.getData();
	}
	
	public synchronized NetworkData getLastRcvNetworkData(){
		//Log.v(Constant.TAG, "Channel getRcvNetworkData! size:"+this.rcvList.getLength());
		return this.rcvList.getLastData();
	}
	
	public synchronized NetworkData getDelaySendPktNetworkData(){
		return this.delaySendPktList.getData();
	}
	
	public synchronized boolean removeSendNetworkData(){
		if (this.sendList.getData() == null) {
			return false;
		} else {
			this.sendList.removeData();
			return true;
		}
	}
	
	public synchronized boolean removeRcvNetworkData(){
		if (this.rcvList.getData() == null) {
			return false;
		} else {
			this.rcvList.removeData();
			return true;
		}
	}
	
	public synchronized boolean removeDelaySendPktNetworkData(){
		if (this.delaySendPktList.getData() == null) {
			return false;
		} else {
			this.delaySendPktList.removeData();
			return true;
		}
	}
	
	public synchronized void clearSendList()
	{
		sendList.removeAllData();
	}

	public synchronized void clearDelaySendList()
	{
		delaySendPktList.removeAllData();
	}
	
	public synchronized void clearRecvList()
	{
		rcvList.removeAllData();
	}
	
	public synchronized void clearAllDataList()
	{
		clearSendList();
		clearRecvList();
		clearDelaySendList();
	}


}
