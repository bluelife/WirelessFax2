package wirelessfax.phonelink.com.cn.network;


import org.slf4j.LoggerFactory;

import wirelessfax.phonelink.com.cn.Sqlite.FileLibController;
import wirelessfax.phonelink.com.cn.Utls.Utls;

///#region 心跳线程
public class TimeoutThread implements Runnable
{
	private volatile boolean mbTimeoutStop = true; // The symbol for the thread.
	private TCPClientChannel clientSocket = null; // Client socket class.
	private int mnConnectCount = 0; // The counter for reconnect remote server.
	private int mnTimeoutCount = 0; // The counter for timeout.
	private int mnLoginCount = 0; // The counter for login.
	private int mnSendCount = 0; // The counter for send.

	public boolean isRunning()
	{
		return !mbTimeoutStop;
	}

	private org.slf4j.Logger log = LoggerFactory.getLogger(TimeoutThread.class);

		///#region 公用函数
	public TimeoutThread(TCPClientChannel client)
	{
		clientSocket = client;
		mbTimeoutStop = true;
		mnConnectCount = 0;
		mnTimeoutCount = 0;
		mnLoginCount = 0;
		mnSendCount = 0;
	}
	/** 
	 停止心跳线程
	 
	*/
	public final void StopThread()
	{
		mbTimeoutStop = true;
		try {
			Thread.sleep(250);
		} catch (Exception e) {

		}

		mnConnectCount = 0;
		mnTimeoutCount = 0;
		mnLoginCount = 0;
		mnSendCount = 0;
	}
	/** 
	 开始心跳线程
	 
	*/
	public final void TimeoutWorkThread()
	{
		try
		{
			FaxLogFile.WriteError("启动心跳线程");
			mbTimeoutStop = false;

			while (true)
			{
//                if(FaxManager.bLogining) {
//                    FaxManager.bLogining = false;
//                    mnConnectCount = 100;
//                }

				if (mnConnectCount >= 100) //10秒重连服务器
				{
					log.debug("TCP thread will stop and start. restart.");
					mnConnectCount = 0;
					mnTimeoutCount = 0;
					mnLoginCount = 0;
					mnSendCount = 0;
					if(!clientSocket.isConnected()) {

                        clientSocket.setIsLogin(false);

						clientSocket.stopClient();
						clientSocket.startClient();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				//登陆状态，如果密码和用户清空，表示退出状态，设置登出，并关闭网络。
				//如果没有登陆状态，则用户还没登陆，处在登陆页面，等输入用户名及密码后，登陆。所以一直跳过本判断。
				if(PublicVariable.Password.isEmpty() && PublicVariable.UserCode.isEmpty() && clientSocket.getIsLogin()) {
					clientSocket.setIsLogin(false);
					clientSocket.stopClient();
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (mbTimeoutStop)
				{
					break;
				}


				if (!clientSocket.isConnected())
				{
                    log.debug("socket isnot connect....");
					++mnConnectCount;
					continue;
				}

				if (clientSocket.getIsLogin())
				{
					//log.debug("user has login.");
					++mnTimeoutCount;
					++mnSendCount;

					if (mnTimeoutCount >= 3000)
					//if (mnTimeoutCount >= 5100)
                    //if (mnTimeoutCount >= 60)
					{
						mnTimeoutCount = 0;
						clientSocket.Timeout(); //心跳
                        log.debug("timeout relogin");
					}

					if (mnSendCount >= 30) //3秒请求发送传真
					{
                        log.debug("get send fax....mnTimeoutCount:"+mnTimeoutCount);
						mnSendCount = 0;



						FaxInfo fax = FaxManager.GetWaitSendFile();

						if (fax != null)
						{
							log.debug("start send fax:"+fax.getFaxFile());
							switch (fax.getStatus())
							{
								case 0: //未请求
									log.debug("send fax 0: "+fax.getFaxFile()+"   "+fax.getCalled()+"  "+fax.getPages());
									byte page = (byte)(fax.getPages() &0xff);
									clientSocket.SendFax(fax.getFaxFile(), fax.getCalled(), fax.getSenderMobile(), page);
									break;
								case 1: //发送中
									log.debug("send fax sending");
									long tmpTime = Utls.getLocalSeconds() - fax.getSendTime();
									if (tmpTime >= 60) //发送超时
									{
										log.debug("send fax timeout, resend");
										fax.setStatus(0); //重新提交发送
									}
									break;
								case 2: //发送成功
									log.debug("send fax send ok");
									//add to sql
									FileLibController.getInstance().saveFax(fax);

									FaxManager.Remove(fax);
									FaxManager.Add2PhoneBook(fax.getCalled(), fax.getCalled());
                                    FaxManager.mbSendNewFax = true;

									break;
								case 3: //发送失败
									log.debug("send fax send failed, resend?");
									if (fax.getTryviews() >= PublicVariable.TryViews)
									{
										log.debug("more than resend fax times, send fax faild :"+fax.getFaxFile());
                                        FileLibController.getInstance().saveFax(fax);
                                        FaxManager.Remove(fax);
									}
									else
									{
										clientSocket.SendFax(fax.getFaxFile(), fax.getCalled(), fax.getSenderMobile(), Byte.parseByte((new Integer(fax.getPages())).toString()));
									}
									break;
							}
						}
					}
				}
				else
				{
					++mnLoginCount;

					if (mnLoginCount >=10) //连接后5秒请求登录
					{
						mnLoginCount = 0;
						log.debug("send login.");
						clientSocket.Login();
					}
				}
			}
		}
		catch (Exception e)
		{
			log.debug("send fax exception..");
			FaxLogFile.WriteError(String.format("心跳检测线程失败，由于%1$s", e.getMessage()));
		}
		finally
		{
			mbTimeoutStop = true;
			mnConnectCount = 0;
			mnTimeoutCount = 0;
			mnLoginCount = 0;
			mnSendCount = 0;
			FaxLogFile.WriteEnd("停止心跳线程");

			log.debug("send fax stop..");
		}
	}

    @Override
    public void run() {
        this.TimeoutWorkThread();
    }
}