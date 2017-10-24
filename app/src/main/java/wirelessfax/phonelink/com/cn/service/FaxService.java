package wirelessfax.phonelink.com.cn.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.network.CommSelector;

public class FaxService extends Service {
    private CommSelector clientSelector = null;

    public FaxService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(Constant.TAG, "UDPClientService:onCreate()");

        clientSelector = CommSelector.getInstantce();

    }

    @Override
    public void onDestroy() {
        clientSelector.destroySelector();

        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v(Constant.TAG, "UDPClientService:onStart()");

        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.v(Constant.TAG, "UDPClientService:onStartCommand()");

        return super.onStartCommand(intent, flags, startId);
    }

}
