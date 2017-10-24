package wirelessfax.phonelink.com.cn.network;

import android.util.Log;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.Utls;

/**
 * Created by CNFAFAN3 on 11/6/2016.
 */

public class TCPClientChannel extends CommChannel {
    private org.slf4j.Logger log = LoggerFactory.getLogger(TCPClientChannel.class);
    private Thread sendFaxThread = null;
    private TimeoutThread sendFaxRunnable = null;

    public boolean bConnecting = false;

    public TCPClientChannel(CommBase base) {
        super(base);

        sendFaxRunnable  = new TimeoutThread(this);

        sendFaxRunnable.StopThread();
        sendFaxThread = new Thread(sendFaxRunnable);
        sendFaxThread.start();
    }

    @Override
    public void init() {
        try {
            this.mChannel = SocketChannel.open();
            this.mChannel.configureBlocking(false);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void stopClient() {
//        if(sendFaxRunnable != null) {
//            this.sendFaxRunnable.setRunning(false);
//            if(sendFaxThread != null) {
//                sendFaxThread.interrupt();
//                sendFaxThread = null;
//            }
//        }
        log.debug("stop Client");
        this.clearAllDataList();

        try {
            this.mChannel.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void startClient() {
        log.debug("start Client");

        this.init();
        if(connect()) {
            log.debug(" connect OK!");
        } else {
            log.debug(" connect Failed!");
            return;
        }
    }

    private int nConnectCount = 0;
    public boolean connect()
    {
        log.debug("enter connected!");
        try {
            if(mChannel != null && (this.bConnecting && nConnectCount < 2)) {
                nConnectCount++;
                log.debug("has connecting!");
                return false;
            }
            nConnectCount = 0;
            bConnecting = true;

            if(mChannel == null || !mChannel.isOpen()) {
                this.init();

                if(mChannel == null) {
                    log.debug(" connected faild!  mChannel == null");
                    return false;
                }
            }

            ((SocketChannel)mChannel).socket().setSendBufferSize(1024*32);
            ((SocketChannel)mChannel).socket().setReceiveBufferSize(1024*32);
            ((SocketChannel)mChannel).socket().setKeepAlive(true);

            if (((SocketChannel)mChannel).isConnected()) {
                bConnecting = false;
                return true;
               // ((SocketChannel)mChannel).close();
            }

            log.debug("register connected! IP:"+serverAddress.getAddress().getHostAddress()+"  Port"+serverAddress.getPort());

            this.mSelector.registerConnect(this);
            log.debug("register2 connected! IP:"+serverAddress.getAddress().getHostAddress()+"  Port"+serverAddress.getPort());

            ((SocketChannel)mChannel).connect(serverAddress);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.debug(" IOException!");
        }

        return true;
    }

    @Override
    public void send(SelectionKey key) {
        NetworkData data = null;

    //    log.debug("send,  IP: "+this.mBase.getStrIp());


        long iCurTime = System.currentTimeMillis();
        if(iCurTime - iEachPktSendInterval < 1) {
            iEachPktSendInterval = iCurTime;
            //key.interestOps(SelectionKey.OP_WRITE|SelectionKey.OP_READ);
            return;
        }
        iEachPktSendInterval = iCurTime;

        data = getSendNetworkData();
        if(data == null) {
            //key.interestOps(SelectionKey.OP_READ);
           // log.debug("data == null");
            return;
        }

        if (data.getDataBytes().length > this.iBufSize) {
            removeSendNetworkData();
            log.debug("send data, the length of data > "+iBufSize);
            //key.interestOps(SelectionKey.OP_WRITE|SelectionKey.OP_READ);
            return;
        }
        sendBuf.clear();
        sendBuf.put(data.getDataBytes());
        log.debug("dstIP: "+ this.getMcuBase().getNetworkInfo().getStrIp()+" Port:"+this.getMcuBase().getNetworkInfo().getPort()+" len+"+data.getDataBytes().length);
        sendBuf.flip();
        int size = 0;
        try {
            if(!((SocketChannel)mChannel).isConnected()) {
                key.cancel();
                Log.v(Constant.TAG, "!dchannel.isConnected ");
                return;
            }

            size = ((SocketChannel)mChannel).write(sendBuf);
            log.debug(this.getMcuBase().getNetworkInfo().getStrIp()+"  send Size: "+size+"  datasize:"+data.getDataBytes().length +"  left pkt will be Send: "+this.getSendListSize());
        } catch (IOException e) {
            key.cancel();

            try {
                ((SocketChannel)mChannel).finishConnect();
                ((SocketChannel)mChannel).close();
                this.clearAllDataList();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            Log.v(Constant.TAG, "send IOException");
            e.printStackTrace();
            return;
        }
        removeSendNetworkData();
        log.debug("dstIP: "+ this.getMcuBase().getNetworkInfo().getStrIp()+" Port:"+this.getMcuBase().getNetworkInfo().getPort()+" nodecount:"+this.getSendListSize());

        key.interestOps(SelectionKey.OP_READ);
       if(this.getSendListSize() > 0) {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    @Override
    public void recv(SelectionKey key) {
        recvBuf.clear();
        int readLen = 0;
        log.debug("recv ");
        try {
            if(!((SocketChannel)mChannel).isConnected()) {
                log.debug("recv failed, disconnected!!!");
                key.cancel();
                key.channel().close();
                return;
            }

            readLen = ((SocketChannel)mChannel).read(recvBuf);
            recvBuf.flip();
        } catch (IOException e) {
            key.cancel();
            // TODO Auto-generated catch block
            log.debug("recv IOException");

            key.cancel();
            try {
                key.channel().close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            e.printStackTrace();
            return;
        }

        if(readLen >= 0) {
            this.mBase.setLastTick(PublicVariable.appTick);

            log.debug("recv length "+readLen);
            processData(recvBuf);
        } else {
            key.cancel();
            log.debug("recv length  < 0");
//            if(readLen == -1){
//                try {
//                    log.debug("recv readlen == -1, client is close? my closed.");
//                   key.channel().close();
//                } catch (IOException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//            }
        }
    }

    @Override
    public boolean isConnected() {

        return ((SocketChannel)mChannel).isConnected();
    }

    @Override
    public boolean sendOutputCmd(int identifer, Object param) {
        return false;
    }

    protected void processData(ByteBuffer buffer)
    {
        int length = buffer.limit();
        //Log.v(Constant.TAG, clientChannel.getMcuBase().networkInfo.getStrIp()+" readLen: "+length);
        if (length < 0) {
            log.debug("length <   0");
            return;
        }

        byte [] dstData = new byte[length];
        buffer.get(dstData);

       StateObject state = ((StateObject)mBase);
        log.debug("recv data: "+Utls.byte2HexStr(dstData));
        System.arraycopy(dstData, 0, state.buffer, 0, length);
//        buffer.get(state.buffer);

        if (length > 0) {
            // There might be more data, so store the data received so far.
            ((StateObject)mBase).AppendData(length);
            FaxLogFile.WriteControl(String.format("Socket从服务终端接收[%1$s]个字节数据", length));
            state.ParseData(); //解析数据
        } else {
            if (state.getDataLength() > 1) // All the data has arrived; put it in response.
            {
                state.ParseData(); //解析数据
            }

            FaxLogFile.WriteError("Socket断开远程连接");
        }
    }


    /**
     * 发送数据
     */
    public final boolean Send(byte[] data) {
        log.debug("send login data333");
         ((StateObject)this.mBase).SendData(data);
        return true;
    }

    /**
     * 登录请求
     */
    public final void Login() {
        log.debug("usercode:"+PublicVariable.UserCode+"  password:"+PublicVariable.Password);
        if (PublicVariable.UserCode.isEmpty() || PublicVariable.Password.isEmpty()) {
            log.debug("usercode:"+PublicVariable.UserCode
                    +"  password:"+PublicVariable.Password+" is empty.");
            return;
        }
        Send(ParseCommand.ParseS7(PublicVariable.UserCode, PublicVariable.Password));
    }

    /**
     * 心跳
     */
    public final void Timeout() {
        log.debug("timeout: "+PublicVariable.UserCode+"  "+PublicVariable.Password);
        Send(ParseCommand.ParseS0(PublicVariable.UserCode, PublicVariable.Password));
    }


    /**
     * 发送传真
     */
    public final boolean SendFax(String strFileName, String called, String senderMobile, byte pages) {
        log.debug("send fax   000");
        if (!this.isConnected()) {
            log.debug("!this.isConnected()");
            return false;
        }
        log.debug("send fax   111");
        if (this.mBase == null) {
            log.debug("this.mBase == null");
            return false;
        }

        log.debug("send fax   222");
        if (strFileName == null || strFileName.isEmpty() || called == null || called.isEmpty()) {
            log.debug("strFileName == null");
            return false;
        }

        log.debug("send fax   333");
//        int page = Utls.
//        if (pages <= 0) {
//            log.debug("send fax send pages "+pages+" <= 0");
//            return false;
//        }

        log.debug("send fax send 0000");
        return ((StateObject)this.mBase).SendFax(strFileName, called, senderMobile, pages);
    }


    /**
     * StateObject login or logout
     */
    public final boolean getIsLogin() {
        if (this.mBase == null) {
            return false;
        } else {
            return ((StateObject)this.mBase).getIsLogin();
        }
    }

    public final void setIsLogin(boolean value) {
        if (this.mBase == null) {
            return ;
        } else {
            ((StateObject)this.mBase).setIsLogin(value);
        }
    }
}
