package wirelessfax.phonelink.com.cn.wirelessfax;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wirelessfax.phonelink.com.cn.Sqlite.Person;
import wirelessfax.phonelink.com.cn.Utls.Constant;

public class AddPersonActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbarlayout)
    AppBarLayout toolbarlayout;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.editPersonName)
    EditText editPersonName;
    @BindView(R.id.textView3)
    TextView textView3;
    @BindView(R.id.editNumber)
    EditText editNumber;
    @BindView(R.id.activity_add_person)
    RelativeLayout activityAddPerson;

    private boolean bPhoneBookMgr = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        this.bPhoneBookMgr = bundle.getBoolean("IsPhoneBookMgr");

        this.setTitle("添加联系人");
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
            String strUserName = editPersonName.getText().toString();
            if (strUserName.isEmpty()) {
                Toast.makeText(AddPersonActivity.this, "请输入姓名。", Toast.LENGTH_LONG).show();
                return true;
            }

            String strNumber = editNumber.getText().toString();
            if (strNumber.isEmpty()) {
                Toast.makeText(AddPersonActivity.this, "请输入传真号。", Toast.LENGTH_LONG).show();
                return true;
            }

            List<Person> list = DataSupport.where("userCode = ?", strUserName).find(Person.class);
            if (list != null && list.size() > 0) {
                Toast.makeText(AddPersonActivity.this, "用户已经存在。", Toast.LENGTH_LONG).show();
                return true;
            }

            Person person = new Person();
            person.setUserName(strUserName);
            person.setUserCode(strNumber);

            person.save();

            Toast.makeText(AddPersonActivity.this, "保存成功。", Toast.LENGTH_LONG).show();

            if(bPhoneBookMgr) {
                setResult(Constant.RESP_ADD_PERSON);
            }

            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.editPersonName, R.id.editNumber})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editPersonName:
                break;
            case R.id.editNumber:
                break;
        }
    }
}
