package wirelessfax.phonelink.com.cn.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.WakeLockUtil;
import wirelessfax.phonelink.com.cn.network.CommSelector;
import wirelessfax.phonelink.com.cn.network.PublicVariable;
import wirelessfax.phonelink.com.cn.network.StateObject;

public class MainService extends Service {

    private Runnable runnable = null;

    private Thread tickThread = null;
    private TickRunnable tickRunnable = null;

    private int iSessionTimeout = 0; //min

    private CommSelector clientSelector = null;

    private Thread faxThread = null;
    private FaxRunnable faxRunnable = null;

    private SharedPreferences sp = null;
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.v(Constant.TAG, "MainService:onCreate()");

        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v(Constant.TAG, "MainService:onStart()");

        super.onStart(intent, startId);
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.v(Constant.TAG,"MainService onDestroy.");

        clientSelector.destroySelector();

        if (tickRunnable != null) {
            tickRunnable.setRunning(false);
        }
        tickThread.interrupted();

        if (faxRunnable != null) {
            faxRunnable.setRunning(false);
        }
        tickThread.interrupted();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.v(Constant.TAG, "MainService:onStartCommand()");


        PublicVariable.init(this.getApplicationContext());

        sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        //如果登陆过，直接登录
        if(sp.getBoolean("ISLOAD",false)) {
            PublicVariable.UserCode = sp.getString("USER_NAME", "");
            PublicVariable.Password = sp.getString("PASSWORD", "");
        }

        clientSelector = CommSelector.getInstantce();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (faxRunnable != null) {
            Log.v(Constant.TAG, "clear tickRunning");
            faxRunnable.setRunning(false);
        }

        faxRunnable = new FaxRunnable();
        faxThread = new Thread(faxRunnable);
        faxThread.start();

        if (tickRunnable != null) {
            Log.v(Constant.TAG, "clear tickRunning");
            tickRunnable.setRunning(false);
        }

        tickRunnable = new TickRunnable();
        tickThread = new Thread(tickRunnable);
        tickThread.start();

        return START_STICKY;
        //return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.v(Constant.TAG,"MainService onBind run.");
        return null;
    }

    class TickRunnable implements Runnable
    {

        private volatile boolean isRunning = true;

        public TickRunnable() {

        }

        @Override
        public void run() {
            int i = 0;
            while (isRunning) {
                PublicVariable.appTick++;
                //Log.v(Constant.TAG, "appTick ThreadID: "+Thread.currentThread().getId()+ ":  System Tick : "+SMiSViewStatic.appTick);
                if (PublicVariable.appTick%600 == 0) {
                    Log.v(Constant.TAG,"System Tick : "+PublicVariable.appTick/10);
                }

                try {
                    Thread.sleep(Constant.APP_TICK_UINT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }
    }


    class FaxRunnable implements Runnable
    {

        private volatile boolean isRunning = true;

        public FaxRunnable() {

        }

        @Override
        public void run() {
            int i = 0;

            StateObject statObject = StateObject.getInstance();

            while (isRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            statObject.getClientChannel().stopClient();

        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }
    }
}



