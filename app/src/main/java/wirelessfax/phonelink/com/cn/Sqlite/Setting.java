package wirelessfax.phonelink.com.cn.Sqlite;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by CNFAFAN3 on 11/23/2016.
 */

public class Setting extends DataSupport implements Serializable {
    public long getId() {
        return id;
    }

    private long id;
    private String strPasswd;

    public String getStrImagePath() {
        return strImagePath;
    }

    public void setStrImagePath(String strImagePath) {
        this.strImagePath = strImagePath;
    }

    public String getStrPasswd() {
        return strPasswd;
    }

    public void setStrPasswd(String strPasswd) {
        this.strPasswd = strPasswd;
    }

    private String strImagePath;


}
