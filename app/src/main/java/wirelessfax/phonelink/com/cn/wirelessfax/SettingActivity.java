package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import org.litepal.crud.DataSupport;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wirelessfax.phonelink.com.cn.Sqlite.Setting;
import wirelessfax.phonelink.com.cn.Utls.Utls;

import static wirelessfax.phonelink.com.cn.Utls.Constant.ITEM_VIEW_REQ;

public class SettingActivity extends AppCompatActivity {
    private org.slf4j.Logger log = LoggerFactory.getLogger(SettingActivity.class);
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbarlayout)
    AppBarLayout toolbarlayout;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.editOldPassword)
    EditText editOldPassword;
    @BindView(R.id.nameLayout)
    LinearLayout nameLayout;
    @BindView(R.id.textView3)
    TextView textView3;
    @BindView(R.id.editNewPassword)
    EditText editNewPassword;
    @BindView(R.id.newpasswordLayout)
    LinearLayout newpasswordLayout;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.txtMagePath)
    TextView txtMagePath;
    @BindView(R.id.senderlayout)
    LinearLayout senderlayout;
    @BindView(R.id.activity_setting)
    RelativeLayout activitySetting;
    @BindView(R.id.btnSelectImage)
    Button btnSelectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        Setting setting = DataSupport.findFirst(Setting.class);
        if (setting == null) {
            setting = new Setting();
            setting.setStrPasswd("123456");
            setting.setStrImagePath("");
            setting.save();
        }
        txtMagePath.setText(setting.getStrImagePath());


        this.setTitle("设置");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.phone_book_menu, menu);

        MenuItem del = menu.findItem(R.id.action_del);
        del.setVisible(false);

        MenuItem add = menu.findItem(R.id.action_add);
        add.setTitleCondensed("确定");


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            String strOldPwd = editOldPassword.getText().toString();
            String strNewPwd = editNewPassword.getText().toString();

            Setting setting = DataSupport.findFirst(Setting.class);
            if (setting == null) {
                Toast.makeText(SettingActivity.this, "原密码未设置，错误。", Toast.LENGTH_LONG).show();

                return false;
            }

            if (setting.getStrPasswd().compareTo(strOldPwd) != 0) {
                log.debug("compare pwd  "+setting.getStrPasswd()+"  "+strOldPwd);
                Toast.makeText(SettingActivity.this, "原密码错误。", Toast.LENGTH_LONG).show();
                return false;

            }

            if (strNewPwd.length() < 6) {
                Toast.makeText(SettingActivity.this, "密码长度不能小于6位", Toast.LENGTH_LONG).show();
                return false;
            }

            setting.setStrPasswd(strNewPwd);

            setting.update(setting.getId());
            if (strNewPwd.compareTo(strOldPwd) != 0) {
                Toast.makeText(SettingActivity.this, "更新密码成功", Toast.LENGTH_LONG).show();
            }

            String strImagePath = txtMagePath.getText().toString().trim();
            if (strImagePath.isEmpty()) {
                Toast.makeText(SettingActivity.this, "请选择印章。", Toast.LENGTH_LONG).show();
                return false;
            }

            setting.setStrImagePath(strImagePath);

            setting.update(setting.getId());

            if (strImagePath.compareTo(setting.getStrImagePath()) != 0) {
                Toast.makeText(SettingActivity.this, "保存印章成功。", Toast.LENGTH_LONG).show();

            }


            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.editNewPassword, R.id.newpasswordLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editNewPassword:
                break;
            case R.id.newpasswordLayout:
                break;
        }
    }

    @OnClick(R.id.btnSelectImage)
    public void onClick() {
        selectAttach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ITEM_VIEW_REQ && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String strPath = uri.getPath();
            txtMagePath.setText(strPath);
        }
    }

    public void selectAttach()
    {
        Intent i = new Intent(SettingActivity.this, FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);



        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Utls.getSelectedExtenedPath());
        startActivityForResult(i, ITEM_VIEW_REQ);
    }

}
