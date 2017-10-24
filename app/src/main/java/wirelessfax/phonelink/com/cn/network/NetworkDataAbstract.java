package wirelessfax.phonelink.com.cn.network;

public abstract class NetworkDataAbstract <T>{
	public abstract void addData(T t);
	public abstract T getData();
	public abstract boolean removeData();
}



