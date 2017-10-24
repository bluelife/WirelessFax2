package wirelessfax.phonelink.com.cn.Sqlite;

public class DataQryCondition {
	public int iType = -1;
	public long lTime = 0;
	public int iRead = -1;
	public int iLimitCount = 500;
	public int iLimitOffset = 0;
    public String strSender = "";
    public String strReceiver = "";
	public String strOrderBy = "id desc";
	public String strSearchKeyword = "";//sender, receiver , subject. filename
}
