package wirelessfax.phonelink.com.cn.network;

import java.util.ArrayList;

import android.util.Log;

import wirelessfax.phonelink.com.cn.Utls.Constant;


public class NetworkDataList<T> extends NetworkDataAbstract<T> {
	
	public NetworkDataList() {
		super();
		this.list = new ArrayList<T>();
		//this.list.clear();
	}

	ArrayList<T> list;

	@Override
	public synchronized void addData(T t) {
		list.add(t);
	}

	@Override
	public synchronized T getData() {
		int len = list.size();
		if (len <= 0) {
			return null;
		} else {
			T data = list.get(0);
			if (data == null ) {
				Log.w(Constant.TAG, "get data is null."+list.size());
				return null;
			}

			return data;		
		}
	}

	public synchronized T getLastData() {
		int len = list.size();
		if (len <= 0) {
			return null;
		} else {
			T data = list.get(len-1);
			if (data == null ) {
				Log.w(Constant.TAG, "get data is null."+list.size());
				return null;
			}

			return data;		
		}
	}
	
	@Override
	public synchronized boolean removeData() {
		int len = list.size();
		if (len <= 0) {
			return false;
		} else {
			list.remove(0);
			return true;		
		}
	}
	
	
	public synchronized void removeAllData() {
		list.clear();
	}
	
	public synchronized int getLength() {
		return list.size();
	}
}
