package wirelessfax.phonelink.com.cn.Sqlite;

import org.litepal.crud.DataSupport;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import wirelessfax.phonelink.com.cn.Utls.Utls;

/**
 * Created by CNFAFAN3 on 11/6/2016.
 */

public class FileLib extends DataSupport implements Serializable {

    private org.slf4j.Logger log = LoggerFactory.getLogger(FileLib.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private long id;
    private int iType = FileLibController.DRAFT_TYPE;

    public int getiOldType() {
        return iOldType;
    }

    public void setiOldType(int iOldType) {
        this.iOldType = iOldType;
    }

    private int iOldType = FileLibController.DRAFT_TYPE;
    private String strFilePath = "";
    private boolean bRead = false;
    private String strSender = "";
    private String strReceiver = "";
    private String strSubject = "";
    private long lTime  = Utls.getLocalSeconds();

    public int getiTotal() {
        return iTotal;
    }

    public void setiTotal(int iTotal) {
        this.iTotal = iTotal;
    }

    private int iTotal = 0;

    public int getiType() {
        return iType;
    }

    public void setiType(int iType) {
        this.iType = iType;
    }

    public String getStrFilePath() {
        return strFilePath;
    }

    public void setStrFilePath(String strFilePath) {
        this.strFilePath = strFilePath;
    }

    public boolean isbRead() {
        return bRead;
    }

    public void setbRead(boolean bRead) {
        this.bRead = bRead;
    }

    public String getStrSender() {
        return strSender;
    }

    public void setStrSender(String strSender) {
        this.strSender = strSender;
    }

    public String getStrReceiver() {
        return strReceiver;
    }

    public void setStrReceiver(String strReceiver) {
        this.strReceiver = strReceiver;
    }

    public String getStrSubject() {
        return strSubject;
    }

    public void setStrSubject(String strSubject) {
        this.strSubject = strSubject;
    }

    public long getlTime() {
        return lTime;
    }

    public void setlTime(long lTime) {
        this.lTime = lTime;
    }
}
