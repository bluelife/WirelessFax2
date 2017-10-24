package wirelessfax.phonelink.com.cn.network;


import wirelessfax.phonelink.com.cn.Utls.Constant;

public class NetworkInfo {

	private String strIp = null;
	private int port = -1;
	private int state = 0;
	public int lastTick = 0;;
	public int tickCount = 0;
	private byte[] mac = null;
	
	public NetworkInfo(){
		this.strIp = Constant.IP_INVALID;
		this.port = 0;
		this.lastTick = 0;
	}
	
	public NetworkInfo(NetworkData data){
		this.strIp = data.getIp();
		this.port = 0;
		this.lastTick = PublicVariable.appTick;
	}
	
	public NetworkInfo(String strIp,int iPort, byte[] mac){
		this.strIp = strIp;
		this.port = iPort;
		this.mac = mac;
		this.lastTick = PublicVariable.appTick;
	}
	
	public String getStrIp() {
		return strIp;
	}
	
	public void setStrIp(String strIp) {
		this.strIp = strIp;
	} 
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int iPort) {
		this.port = iPort;
	}

	public byte[] getMac() {
		return mac;
	}
	
	public void setMac(byte[] mac) {
		this.mac = mac;
	}
	
	public synchronized int getState() {
		return state;
	}
	
	public synchronized void setState(int state) {
		this.state = state;
	}
	
	public void setLastTick(int iTick)
	{
		lastTick = iTick;
	}

	public int getLastTick()
	{
		return lastTick;
	}

}
