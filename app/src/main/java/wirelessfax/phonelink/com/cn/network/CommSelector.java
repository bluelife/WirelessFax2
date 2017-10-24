package wirelessfax.phonelink.com.cn.network;

import android.util.Log;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import wirelessfax.phonelink.com.cn.Utls.Constant;


public class CommSelector {
	protected org.slf4j.Logger log = LoggerFactory.getLogger(CommSelector.class);
	
	
	private boolean bRunning = true;
	private Selector mSelector = null;
	private int iTimeout = 500;//ms
	private static CommSelector gmSelector = null;
	
	private Object gate=new Object();
	private Thread selectorThread = null;
	private SelectorRunnable selectorRunnable= null;

	public static CommSelector getInstantce()
	{
		if (gmSelector == null) {
			gmSelector = new CommSelector();
		}
		return gmSelector;
	}
	
	private  CommSelector()
	{
		try {
			mSelector = Selector.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		selectorRunnable = new SelectorRunnable();
		selectorThread = new Thread(selectorRunnable);
		selectorThread.setPriority(Thread.MAX_PRIORITY);
		selectorThread.start();
	}
	
	public void destroySelector()
	{
		selectorRunnable.setRunning(false);
		selectorThread.interrupt();
		gmSelector = null;
	}
	
	public boolean addOps(CommChannel clientChannel, int ops)
	{
		if (clientChannel == null) {
			return false;
		}
		
		if(clientChannel.getChannel().isRegistered()) {
			SelectionKey key = clientChannel.getChannel().keyFor(mSelector);
			key.interestOps(ops);

			return true;
		} 
		
		
		//Log.v(Constant.TAG, clientChannel.getMcuBase().networkInfo.getStrIp()+"will registor in selector.");
		try {
			//Log.v(Constant.TAG, "selector addWriteOps!");
			 synchronized(gate){
	                mSelector.wakeup();
	                clientChannel.getChannel().register(mSelector, ops, clientChannel);
	         }
			
			//Log.v(Constant.TAG, "selector addWriteOps ok!");
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
		//	Log.v(Constant.TAG, clientChannel.getMcuBase().getStrIp()+":addWriteOps failed");
			e.printStackTrace();
		}
        
        
		return true;
	}
	
	public boolean registerConnect(CommChannel clientChannel)
	{
		if (clientChannel == null) {
			return false;
		}
		return addOps(clientChannel, SelectionKey.OP_CONNECT);
	}
	
	public boolean addWriteOps(CommChannel clientChannel)
	{
		if (clientChannel == null) {
			return false;
		}

		return addOps(clientChannel, SelectionKey.OP_WRITE|SelectionKey.OP_READ);
	}
//
//	public boolean addReadOps(UDPClientChannel clientChannel)
//	{
//		if (clientChannel == null) {
//			return false;
//		}
//		if(clientChannel.getDatagramChannel().isRegistered()) {
//			return true;
//		}
//		
//		try {
//			//Log.v(Constant.TAG, "selector addReadOps!");
//			synchronized(gate){
//                mSelector.wakeup();
//                clientChannel.getDatagramChannel().register(mSelector, SelectionKey.OP_WRITE|SelectionKey.OP_READ, clientChannel);
//			}
//		} catch (ClosedChannelException e) {
//			// TODO Auto-generated catch block
//			Log.v(Constant.TAG, clientChannel.getMcuBase().networkInfo.getStrIp()+":addReadOps failed");
//			e.printStackTrace();
//		}
//        
//		return true;
//	}
	
	
	private void connect(SelectionKey key)
	{
		TCPClientChannel clientChannel = (TCPClientChannel) key.attachment();

		log.debug("connect!!!");
		SocketChannel client = (SocketChannel) (clientChannel.getChannel());
        if (client.isConnectionPending()) {  
        	try {
        		log.debug("connect  ok!!!");
				client.finishConnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					key.channel().close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				e.printStackTrace();
			}

			clientChannel.bConnecting = false;
        }
	}
	
	
	private void accept(SelectionKey key)
	{
		CommChannel clientChannel = (CommChannel) key.attachment(); 

		clientChannel.accept(key);
	}
	
	private void send(SelectionKey key)
	{
		CommChannel clientChannel = (CommChannel) key.attachment(); 

		clientChannel.send(key);
	}
	
	private void recv(SelectionKey key)
	{
		CommChannel clientChannel = (CommChannel) key.attachment(); 
	 
		clientChannel.recv(key);
	}


	class SelectorRunnable implements Runnable
	{
		
		private volatile boolean isRunning = true;
	
		public SelectorRunnable() {

		}
	
		@Override
		public void run() {
			while(bRunning)
			{
				int n = 0;
				try {
					synchronized(gate){}
					n = mSelector.select(iTimeout);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            if(n == 0){
	            	//Log.v(Constant.TAG, "selector not get key!");
	                continue;
	            }
	            //Log.v(Constant.TAG, "selector key count:"+n);
	            Set<SelectionKey> readyKeys = mSelector.selectedKeys();
	            Iterator it=readyKeys.iterator();
	            SelectionKey key = null;
	            while(it.hasNext()) {
	            	key=(SelectionKey)it.next();
	            	it.remove();
	               
	               if(!key.isValid()) {
	            	   //Log.v(Constant.TAG, "selector key invalid!");
	            	   continue;
	               }
	               

	               if (key.isValid() && key.isConnectable()) {
	            	   log.debug("isConnectable");
	            	   connect(key);
	               }
	               
	               if (key.isValid() && key.isAcceptable()) {
	            	   //Log.v(Constant.TAG, "selector key.isWritable");
	            	   accept(key);
	               }
	               
	               if (key.isValid() && key.isWritable()) {
	            	   Log.v(Constant.TAG, "selector key.isWritable");
	            	   send(key);
	               }
	               
	               if(key.isValid() && key.isReadable()){
	            	   Log.v(Constant.TAG, "selector key.isReadable!");
	            	   recv(key);
	                }
	            }
			}
		}
		
		public boolean isRunning() {
			return isRunning;
		}
	
		public void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}
	}
	
}
