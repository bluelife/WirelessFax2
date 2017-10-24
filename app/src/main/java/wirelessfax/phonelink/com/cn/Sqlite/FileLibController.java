package wirelessfax.phonelink.com.cn.Sqlite;

import android.database.Cursor;
import android.util.Log;

import org.litepal.crud.DataSupport;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.Utls;
import wirelessfax.phonelink.com.cn.network.FaxInfo;

import static org.litepal.crud.DataSupport.where;

/**
 * Created by CNFAFAN3 on 11/12/2016.
 */

public class FileLibController {
    public  final static int RECV_TYPE  =1;
    public final  static int SEND_TYPE = 2;
    public final  static int DRAFT_TYPE = 3;
    public final  static int STAR_TYPE = 4;

    private org.slf4j.Logger log = LoggerFactory.getLogger(FileLibController.class);

    private static FileLibController gInstance = null;
    public static FileLibController getInstance()
    {
        if(gInstance == null) {
            gInstance = new FileLibController();
        }

        return gInstance;
    }


    private FileLibController()
    {

    }



    private String[] getConditions(DataQryCondition cond)
    {
        String[] whereCond = new String[6];
        boolean bHasCond = false;
        String strWhere = "";
        int index = 1;
        if(cond.iType > 0) {
            strWhere += " (iType = ?) ";
            whereCond[index] = ""+cond.iType;
            index++;
            bHasCond = true;
        }

        if (cond.iRead >= 0) {
            if(!strWhere.isEmpty()) {
                strWhere += " AND ";
            }

            strWhere += " (bRead = ?) ";
            whereCond[index] = ""+(cond.iRead==0?0:1);
            index++;
            bHasCond = true;
        }


        if(cond.lTime > 0) {
            if(!strWhere.isEmpty()) {
                strWhere += " AND ";
            }

            strWhere += " (lTime <  ?) ";
            whereCond[index] = ""+cond.lTime;
            index++;
            bHasCond = true;
        }

        if (!cond.strSender.isEmpty()) {
            if(!strWhere.isEmpty()) {
                strWhere += " AND ";
            }
            strWhere += " (strSender = ?) ";
            whereCond[index] = cond.strSender;
            index++;
        }

        if (!cond.strReceiver.isEmpty()) {
            if(!strWhere.isEmpty()) {
                strWhere += " AND ";
            }
            strWhere += " (strReceiver = ?) ";
            whereCond[index] = cond.strReceiver;
            index++;
        }


        if(!cond.strSearchKeyword.isEmpty()) {
            if(!strWhere.isEmpty()) {
                strWhere += " AND ";
            }

            strWhere += " (";
            strWhere += " (strSubject LIKE ?) ";
            whereCond[index] = "%"+cond.strSearchKeyword+"%";
            index++;

            strWhere += " or (strSender LIKE ?) ";
            whereCond[index] = "%"+cond.strSearchKeyword+"%";
            index++;

            strWhere += " or (strReceiver LIKE ?) ";
            whereCond[index] = "%"+cond.strSearchKeyword+"%";
            index++;

            strWhere += " or (strFilePath LIKE ?) ";
            whereCond[index] = "%"+cond.strSearchKeyword+"%";
            index++;
            strWhere += " )";

            bHasCond = true;
        }


        if(!bHasCond) {
            strWhere  = " id > ? ";
            whereCond[index] = "0";
            index++;
        }

        String[] whereCond2 = null;
        whereCond2 = new String[index];
        whereCond2[0] = strWhere;
        Log.v(Constant.TAG, " where: "+strWhere);
        for (int i=1; i<index; i++) {
            whereCond2[i] = whereCond[i];
            Log.v(Constant.TAG, " where  value: "+whereCond2[i]);
        }

        return whereCond2;
    }

    private List<FileLib> getItems(DataQryCondition cond)
    {
        String [] whereCond = this.getConditions(cond);

        String strOrderBy = "id desc";
        if(!cond.strOrderBy.isEmpty()) {
            strOrderBy = cond.strOrderBy;
        }

        int iLimitCount = 80;
        int iLimitOffset = 0;
        if(cond.iLimitCount > 0) {
            iLimitCount = cond.iLimitCount;
        }

        if(cond.iLimitOffset > 0) {
            iLimitOffset = cond.iLimitOffset;
        }


        List<FileLib> tmpItemList = where(whereCond).order(strOrderBy)
                .limit(iLimitCount).offset(iLimitOffset).find(FileLib.class);

        log.debug("FileLiblist size:"+tmpItemList.size());
        return tmpItemList;
    }

    public List<FileLib> searchFax(String strKeyword)
    {
        List<FileLib> list = new ArrayList<>();

        DataQryCondition cond = new DataQryCondition();

        cond.strSearchKeyword = strKeyword;

        list = getItems(cond);

        return list;
    }

    public List<FileLib> searchFax(int iType)
    {
        List<FileLib> list = new ArrayList<>();

        DataQryCondition cond = new DataQryCondition();

        cond.iType = iType;

        list = getItems(cond);

        return list;
    }

    //主页显示的形态：未读的传真+ 已读的传真，已读的传真来自同一个用户名的传真整合在一个Item，然后点击进入后
    //显示该用户的所有传真。
    public List<FileLib> searchMainPageFax(int iType)
    {
        List<FileLib> list = new ArrayList<>();
        DataQryCondition cond = new DataQryCondition();

        cond.iType = iType;
        cond.iRead = 0;
        list = getItems(cond);

        log.debug("not read fax size:"+list.size()+" type:"+iType);

        String strSQL = "select count(*), ";
//        if (iType == FileLibController.RECV_TYPE) {
            strSQL += " strSender, ";
//        } else if (iType == FileLibController.SEND_TYPE) {
            strSQL += " strReceiver, ";
//        }

        strSQL+=" strFilePath, lTime, id from FileLib where (bRead = 1 AND iType = "+iType;
        strSQL+=" ) group by ";
        if (iType == FileLibController.RECV_TYPE) {
            strSQL += " strSender  ";
        } else if (iType == FileLibController.SEND_TYPE) {
            strSQL += " strReceiver ";
        }
        strSQL+=" order by id desc;";


        log.debug(strSQL);
        Cursor cursor = DataSupport.findBySQL(strSQL);
        FileLib fileLib = null;
        if(cursor!=null&&cursor.moveToFirst()){
            do{
                fileLib = new FileLib();


                fileLib.setbRead(true);
                fileLib.setiType(iType);

                fileLib.setiTotal(cursor.getInt(0));
//                if (iType == FileLibController.RECV_TYPE) {
                    fileLib.setStrSender(cursor.getString(1));
//                } else if (iType == FileLibController.SEND_TYPE) {
                    fileLib.setStrReceiver(cursor.getString(2));
//                }
                fileLib.setStrFilePath(cursor.getString(3));
                fileLib.setlTime(cursor.getLong(4));
                fileLib.setId(cursor.getLong(5));

                list.add(fileLib);
            }while(cursor.moveToNext());

        }

        return list;
    }

    public List<FileLib> searchFaxByUser(int iType, String strUser)
    {
        List<FileLib> list = new ArrayList<>();
        DataQryCondition cond = new DataQryCondition();

        cond.iType = iType;
        cond.iRead = 1;
        if (iType == FileLibController.RECV_TYPE) {
            cond.strSender = strUser;
        } else  if (iType == FileLibController.SEND_TYPE) {
            cond.strReceiver = strUser;
        }
        list = getItems(cond);

        return list;
    }

    //已发送
    public List<FileLib> getSendFax()
    {
        return searchFax(SEND_TYPE);
    }

    //已接收
    public List<FileLib> getMainPageSendFax()
    {
        return searchMainPageFax(SEND_TYPE);
    }

    //已接收
    public List<FileLib> getSendFaxByUser(String strReceiver)
    {
        return searchFaxByUser(SEND_TYPE, strReceiver);
    }



    //已接收
    public List<FileLib> getRecvFax()
    {
        return searchFax(RECV_TYPE);
    }

    //已接收
    public List<FileLib> getMainPageRecvFax()
    {
        return searchMainPageFax(RECV_TYPE);
    }

    //已接收
    public List<FileLib> getRecvFaxByUser(String strSender)
    {
        return searchFaxByUser(RECV_TYPE, strSender);
    }


    //当天接收
    public List<FileLib> getTodayRecvFax()
    {
        List<FileLib> list = new ArrayList<>();

        return list;
    }

    //历史接收
    public List<FileLib> getHistoryRecvFax()
    {
        List<FileLib> list = new ArrayList<>();
        DataQryCondition cond = new DataQryCondition();

        cond.iType = RECV_TYPE;

        long lCurrentTime = Utls.getLocalSeconds();
        long lBeforeToday = lCurrentTime - lCurrentTime%(24*60*60);
        cond.lTime = lBeforeToday;

        list = getItems(cond);
        return list;
    }

    //当天接收
    public List<FileLib> getStarFax()
    {
        return searchFax(STAR_TYPE);
    }

    public List<FileLib> getDraftFax()
    {
        return searchFax(DRAFT_TYPE);
    }

    public boolean saveFax(FileLib fileLib)
    {
        fileLib.save();
        return true;
    }

    public boolean saveFax(FaxInfo faxInfo)
    {
        FileLib fileLib = new FileLib();

        if(faxInfo.getFaxType() == 1) {
            fileLib.setbRead(true);
            fileLib.setStrReceiver(faxInfo.getCalled());
            fileLib.setStrSender(faxInfo.getSenderMobile());
            fileLib.setiType(FileLibController.SEND_TYPE);

            if(faxInfo.getStatus() == 3) //send failed
            {
                fileLib.setiType(FileLibController.DRAFT_TYPE);
            }

        } else if(faxInfo.getFaxType() == 2) {
            fileLib.setbRead(false);
            fileLib.setStrReceiver(faxInfo.getCalled());//???
            fileLib.setStrSender(faxInfo.getSenderMobile());
            fileLib.setiType(FileLibController.RECV_TYPE);
        }

        fileLib.setStrFilePath(faxInfo.getFaxFile());
        fileLib.setlTime(faxInfo.getSendTime());

        fileLib.save();

        return true;
    }


    public boolean deleteFax(FileLib fileLib)
    {
        fileLib.delete();
        return true;
    }


//    boolean deleteFax(FileLib fileLib)
//    {
//
//        return true;
//    }
}
