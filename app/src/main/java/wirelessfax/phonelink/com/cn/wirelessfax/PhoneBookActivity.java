package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wirelessfax.phonelink.com.cn.Sqlite.Person;
import wirelessfax.phonelink.com.cn.Utls.Constant;

public class PhoneBookActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private org.slf4j.Logger log = LoggerFactory.getLogger(MainActivity.class);

    private ListView listView = null;
    private List<Person> personList = new ArrayList<Person>();

    private boolean bSelectReceiverMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_book);

//        Person person = new Person();
//        person.setUserCode("10086112233");
//        person.setUserName("test1");
//        person.save();
//
//        Person person2 = new Person();
//        person2.setUserCode("10086113355");
//        person2.setUserName("test2");
//        person2.save();

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        this.bSelectReceiverMode = bundle.getBoolean("IsSelectReceiverMode");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        this.setTitle(this.getResources().getString(R.string.phonebook_box));

        listView = (ListView)findViewById(R.id.listPerson);

        personList = DataSupport.findAll(Person.class);

        initListData(personList);
    }

    private void initListData(List<Person> personList) {
        DispatchSelectUserAdapter adapter = new DispatchSelectUserAdapter(personList,
                R.layout.phone_book_item);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.dispatch_item_select_user_ckb);
        checkbox.setChecked(!checkbox.isChecked());
        personList.get(i).setChecked(checkbox.isChecked());
    }


    public class DispatchSelectUserAdapter extends BaseAdapter {
        private List<Person> mPersonList;
        private int mResource;
        private LayoutInflater mInflater;

        public DispatchSelectUserAdapter(List<Person> personList,
                                         int resource) {
            mPersonList = personList;
            mResource = resource;
            mInflater = LayoutInflater.from(PhoneBookActivity.this);
        }

        @Override
        public int getCount() {
            return mPersonList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPersonList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(mResource, parent, false);
            }
            TextView tvUserName = (TextView) convertView.findViewById(
                    R.id.dispatch_item_select_user_name);
            final CheckBox ckbItem = (CheckBox) convertView.findViewById(
                    R.id.dispatch_item_select_user_ckb);
            Person person = mPersonList.get(position);
            String strText = "";
            if (!person.getUserName().isEmpty()) {
                strText += person.getUserName();
                strText += "(";
            }
            strText += person.getUserCode();
            if (!person.getUserName().isEmpty()) {
                strText += ")";
            }
            tvUserName.setText(strText);
            System.out.println(person.getUserName());
            ckbItem.setChecked(person.isChecked());
            ckbItem.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mPersonList.get(position).setChecked(ckbItem.isChecked());//保存checkbox状态至位置对应的列表对象Person中

                }
            });
            person = null;
            return convertView;
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.phone_book_menu, menu);

        if(bSelectReceiverMode) {
            MenuItem add = menu.findItem(R.id.action_add);
           // add.setIcon()

            MenuItem del = menu.findItem(R.id.action_del);
            del.setVisible(false);
        }

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

            if(bSelectReceiverMode) {
                ArrayList<String> tmpPersonList = new ArrayList<String>();
                for(Iterator<Person> it = personList.iterator(); it.hasNext();) {
                    Person person = (Person)it.next();
                    if (person.isChecked()) {
                        tmpPersonList.add(person.getUserCode());
                    }
                }

                if (tmpPersonList.size() == 0) {
                    Toast.makeText(PhoneBookActivity.this, "请选择发送用户。", Toast.LENGTH_LONG).show();

                    return true;
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("UserCodeList", tmpPersonList);
                intent.putExtras(bundle);
                setResult(Constant.RESP_SELECT_RECEIVER, intent);
                this.finish();
                return  true;
            }

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putBoolean("IsPhoneBookMgr", true);
            intent.putExtras(bundle);
            intent.setClass(PhoneBookActivity.this, AddPersonActivity.class);
            startActivityForResult(intent, Constant.REQ_ADD_PERSON);
            return true;
        } else if (id == R.id.action_del) {
            new AlertDialog.Builder(this)
                    .setTitle("请确认是否删除？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            boolean bFlag = false;
                            for(Iterator<Person> it = personList.iterator(); it.hasNext();) {
                                Person person = (Person)it.next();
                                if (person.isChecked()) {
                                    DataSupport.delete(Person.class, person.getId());
                                    bFlag = true;
                                }
                            }
                            if(bFlag) {
                                personList.clear();
                                personList = DataSupport.findAll(Person.class);
                                initListData(personList);

                                Toast.makeText(PhoneBookActivity.this, "删除成功。", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PhoneBookActivity.this, "请选择用户后，再删除。", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    })
                    .show();




        }

        return super.onOptionsItemSelected(item);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        //requestCode标示请求的标示   resultCode表示有数据

        personList.clear();
        personList = DataSupport.findAll(Person.class);
        initListData(personList);
    }
}
