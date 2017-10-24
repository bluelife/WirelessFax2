package wirelessfax.phonelink.com.cn.Sqlite;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by CNFAFAN3 on 11/22/2016.
 */

public class Person extends DataSupport implements Serializable {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private long  id;
    private String userCode = "";
    private String userName = "";
    private boolean checked  = false;

}
