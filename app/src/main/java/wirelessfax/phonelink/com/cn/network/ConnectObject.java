package wirelessfax.phonelink.com.cn.network;

import java.net.Socket;


import java.net.Socket;

	///#region 连接控制类
public class ConnectObject
{
	public Socket workSocket; // Client socket.
	public int RemotePort = 0; // Remote port.
	public String RemoteServer = ""; // Remote server.
}