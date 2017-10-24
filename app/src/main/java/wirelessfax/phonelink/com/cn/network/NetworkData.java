package wirelessfax.phonelink.com.cn.network;

import java.net.SocketAddress;

import wirelessfax.phonelink.com.cn.Utls.Constant;

public class NetworkData {
	private String ip = Constant.IP_INVALID;
	private byte[] data;
	private SocketAddress address;
	private byte[] MAC;
	
	public int toDo = 0;

	public NetworkData(){
		
	}
	public NetworkData(byte[] data){
		this.data = data;
	}
	public NetworkData(byte[] data, String ip){
		this.data = data;
		this.ip = ip;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public byte[] getDataBytes() {
		return data;
	}
	public void setDataBytes(byte[] data) {
//		if (data == null) {
//			Log.v(C.TAG, "Set data bytes null");
//		}
//		if (data.length == 0) {
//			Log.v(C.TAG, "Set data length is 0");
//		}
		this.data = data;
	}
	public SocketAddress getAddress() {
		return address;
	}
	public void setAddress(SocketAddress address) {
		this.address = address;
	}
	public byte[] getMAC() {
		return MAC;
	}
	public void setMAC(byte[] mAC) {
		MAC = mAC;
	}
}
