package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Sqlite.FileLibController;
import wirelessfax.phonelink.com.cn.Utls.Constant;

import static wirelessfax.phonelink.com.cn.Utls.Constant.ITEM_VIEW_RESP_DELETE;

public class UserSameMailViewActivity extends AppCompatActivity  implements  AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private ListView listContentView = null;
    private ListContentViewAdapter listContentViewAdapter = null;
    private FileLib mFileLib = null;


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_same_mail_view);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mFileLib = (FileLib) bundle.getSerializable(Constant.KEY_FILELIB_ITEM);

        List<FileLib> list = null;
        String strUser = "";
        if (mFileLib.getiType() == FileLibController.SEND_TYPE)
        {
            strUser = mFileLib.getStrReceiver();
            list = FileLibController.getInstance().getSendFaxByUser(strUser);

        } else if (mFileLib.getiType() == FileLibController.RECV_TYPE) {
            strUser = mFileLib.getStrSender();
            list = FileLibController.getInstance().getRecvFaxByUser(strUser);
        }

        this.setTitle("已读传真"+strUser);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.ITEM_TYPE, mFileLib.getiType());
                intent.putExtras(bundle);
                setResult(Constant.ITEM_VIEW_RESP_DELETE, intent);
                finish();
                onBackPressed();
            }
        });

        listContentView = (ListView) findViewById(R.id.listContent);
        listContentViewAdapter = new ListContentViewAdapter(this);
        listContentView.setAdapter(listContentViewAdapter);
        listContentView.setOnItemClickListener(this);
        listContentView.setOnItemLongClickListener(this);

        if(list != null) {
            listContentViewAdapter.setDataList(list, FileLibController.RECV_TYPE);
            listContentViewAdapter.notifyDataSetChanged();
        }
    }


    FileLib longItemDel = null;
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        // TODO Auto-generated method stub
        longItemDel = (FileLib)listContentViewAdapter.getItem(position);
        String strTitle = "请确认是否删除";
        strTitle += "\"";

        String str2 = longItemDel.getStrFilePath();
        int pos = str2.lastIndexOf('/');
        if (pos >= 0) {
            str2 = str2.substring(pos+1, str2.length());
        }

        strTitle += str2;
//        if(longItemDel.getiType() == FileLibController.SEND_TYPE ){
//            strTitle += longItemDel.getStrReceiver();
//        }if(longItemDel.getiType() == FileLibController.RECV_TYPE) {
//            strTitle += longItemDel.getStrSender();
//        } else {
//            if (!longItemDel.getStrSender().isEmpty()) {
//                strTitle += longItemDel.getStrSender();
//            } else {
//                strTitle += longItemDel.getStrReceiver();
//            }
//        }
        strTitle += "\"";
        strTitle += "？";

        new AlertDialog.Builder(UserSameMailViewActivity.this)
                .setTitle(strTitle)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        String strRead = "1";
                        if (!longItemDel.isbRead()) {
                            strRead = "0";
                        }
                        DataSupport.delete(FileLib.class, longItemDel.getId());

                        //update list
                        List<FileLib> list = null;
                        String strUser = "";
                        if (mFileLib.getiType() == FileLibController.SEND_TYPE)
                        {
                            strUser = mFileLib.getStrReceiver();
                            list = FileLibController.getInstance().getSendFaxByUser(strUser);

                        } else if (mFileLib.getiType() == FileLibController.RECV_TYPE) {
                            strUser = mFileLib.getStrSender();
                            list = FileLibController.getInstance().getRecvFaxByUser(strUser);
                        }
                        listContentViewAdapter.clear();
                        listContentView.setAdapter(listContentViewAdapter);
                        if(list != null) {
                            listContentViewAdapter.setDataList(list, FileLibController.RECV_TYPE);
                            listContentViewAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .show();
        return  true ;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileLib item = (FileLib)listContentViewAdapter.getItem(position);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.KEY_FILELIB_ITEM, item);
        Intent intent = new Intent(UserSameMailViewActivity.this,FaxViewActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, Constant.ITEM_VIEW_REQ);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        //requestCode标示请求的标示   resultCode表示有数据
        if (resultCode == ITEM_VIEW_RESP_DELETE) {
            Bundle bundle = data.getExtras();
            int iType = bundle.getInt(Constant.ITEM_TYPE);
            showList();
        }

        if(resultCode == Constant.ITEM_SEND_REQ_OK || resultCode == Constant.ITEM_VIEW_RESP_OK) {
            showList();
        }
    }

    private void showList()
    {
        List<FileLib> list = null;
        String strUser = "";
        if (mFileLib.getiType() == FileLibController.SEND_TYPE)
        {
            strUser = mFileLib.getStrReceiver();
            list = FileLibController.getInstance().getSendFaxByUser(strUser);

        } else if (mFileLib.getiType() == FileLibController.RECV_TYPE) {
            strUser = mFileLib.getStrSender();
            list = FileLibController.getInstance().getRecvFaxByUser(strUser);
        }

        if(list != null) {
            listContentViewAdapter.clear();
            listContentView.setAdapter(listContentViewAdapter);
            listContentViewAdapter.setDataList(list, FileLibController.RECV_TYPE);
            listContentViewAdapter.notifyDataSetChanged();
        }
    }
}
