package wirelessfax.phonelink.com.cn.Utls;

/**
 * Created by CNFAFAN3 on 11/5/2016.
 */

public class Constant {
    public final static String TAG = "WirelessFax";

    public final static int APP_TICK_UINT = 100;//ms

    public final static String IP_INVALID = null;

    public final static int STREAM_FLOAT_TYPE_1 = 1;
    public final static int STREAM_FLOAT_TYPE_10 = 10;
    public final static int STREAM_FLOAT_TYPE_100 = 100;

    public final static String STRING_DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    public final static String STRING_DATE_FORMAT = "yyyyMMdd";
    public final static String STRING_TIME_FORMAT = "HH:mm:ss";

    public final static int STATE_RECIVE_FAX = 0;//正在接收传真

    public final static int STATE_REQ_SEND_FAX = 0;//请求发送传真
    public final static int STATE_SEND_FAX = 0;//正在发送传真
    public final static int STATE_REQ_SEND_NEXT_FAX = 0;//请求发送下一个传真
    public final static int STATE_REQ_RESEND_FAX = 0;//请求重新发送传真，个别包
    public final static int STATE_END_SEND_FAX = 0;//所有传真发送完毕

    public final static int STATE_REQ_TIEM = 0;//请求服务器时间

    public final static String KEY_FILELIB_ITEM = "filelib";
    public final static String ITEM_TYPE = "itemType";

    public final static int ITEM_VIEW_REQ = 10;
    public final static int ITEM_VIEW_RESP_DELETE = 2;
    public final static int ITEM_SEND_REQ = 3;
    public final static int ITEM_SEND_REQ_OK = 4;

    public final static int REQ_SELECT_RECEIVER = 5;
    public final static int RESP_SELECT_RECEIVER = 6;

    public final static int REQ_ADD_PERSON = 7;
    public final static int RESP_ADD_PERSON = 8;

    public final static int ITEM_VIEW_RESP_OK = 9;

    public final static int UPDATE_IMGAGE = 10;

}
