package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.slf4j.LoggerFactory;

import java.util.List;

import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Sqlite.FileLibController;
import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.Utls;
import wirelessfax.phonelink.com.cn.Utls.WakeLockUtil;
import wirelessfax.phonelink.com.cn.network.FaxManager;
import wirelessfax.phonelink.com.cn.network.PublicVariable;
import wirelessfax.phonelink.com.cn.network.StateObject;

import static wirelessfax.phonelink.com.cn.Sqlite.FileLibController.RECV_TYPE;
import static wirelessfax.phonelink.com.cn.Sqlite.FileLibController.SEND_TYPE;





public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        View.OnClickListener {
    private org.slf4j.Logger log = LoggerFactory.getLogger(MainActivity.class);

    private ListView listContentView = null;
    private ListContentViewAdapter listContentViewAdapter = null;

    private SearchView mSearchView = null;
    private EditText mEdit = null;

    private int miType = RECV_TYPE;

    private ImageView imgOnline = null;
    private TextView txtUserName = null;
    private ImageView imgUserLogo = null;

    private boolean bRunning = false;
    private Runnable updateMsg = null;
    private UpdateFaxListThread updateThread = null;


    private boolean bLogin = false;
    private Handler updateMsgHandler = null;

    public NotificationManager mNotificationManager;
    int notifyId = 100;

    NotificationCompat.Builder mBuilder = null;


    ActionBarDrawerToggle mDrawerToggle = null;
    DrawerLayout mDrawerLayout = null;
    /** drawer的监听 */
    private class MyDrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {// 打开drawer
            mDrawerToggle.onDrawerOpened(drawerView);//开关状态改为opened

                log.debug("toggle onClick");
                if(!StateObject.getInstance().getIsLogin()) {
                    imgOnline.setBackgroundResource (R.drawable.img_guide_point_normal);
                } else {
                    imgOnline.setBackgroundResource (R.drawable.img_guide_point_select);
                }
        }

        @Override
        public void onDrawerClosed(View drawerView) {// 关闭drawer
            mDrawerToggle.onDrawerClosed(drawerView);//开关状态改为closed
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {// drawer滑动的回调
            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {// drawer状态改变的回调
            mDrawerToggle.onDrawerStateChanged(newState);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WakeLockUtil.acquireWakeLock(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initNotify();

        mDrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(new MyDrawerListener());//设置drawer的开关监听
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        txtUserName = (TextView) headerView.findViewById(R.id.txtUserName);

        imgOnline = (ImageView) headerView.findViewById(R.id.imgOnline);


        txtUserName.setText("用户名: "+PublicVariable.UserCode);
        txtUserName.setOnClickListener(this);

        listContentView = (ListView) findViewById(R.id.listContent);
        listContentViewAdapter = new ListContentViewAdapter(this);
        listContentView.setAdapter(listContentViewAdapter);
        listContentView.setOnItemClickListener(this);
        listContentView.setOnItemLongClickListener(this);

        log.debug("add fileLib");
        //test
//        addTestItem(FileLibController.RECV_TYPE, "recvtest..");
//        addTestItem(SEND_TYPE, "sendtest..");
//        addTestItem(FileLibController.DRAFT_TYPE, "drafttest..");
//        addTestItem(FileLibController.STAR_TYPE, "startest..");

        this.setTitle(this.getResources().getString(R.string.recv_box));
        listContentViewAdapter.setDataList(FileLibController.getInstance().getMainPageRecvFax(), FileLibController.RECV_TYPE);
        listContentViewAdapter.notifyDataSetChanged();
        miType = FileLibController.RECV_TYPE;


        updateMsgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x01) {
                    if (updateLatestData()) {
                        //Log.v(C.TAG, "Update Monitoring.");
                    }

                    if(StateObject.getInstance().getIsLogin()) {
                        if(!bLogin) {
                            bLogin = true;
                            Toast.makeText(MainActivity.this, "在线....", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(bLogin) {
                            bLogin = false;
                            Toast.makeText(MainActivity.this, "离线....", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        };

        updateThread = new UpdateFaxListThread(updateMsgHandler);
        updateThread.start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();//该方法会自动和actionBar关联, 将开关的图片显示在了action上，如果不设置，也可以有抽屉的效果，不过是默认的图标
    }


    private void initNotify(){
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
//				.setAutoCancel(true)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.logo);
    }

    public PendingIntent getDefalutIntent(int flags){
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    private boolean updateLatestData()
    {
        if ( FaxManager.mbRecvNewFax) {
            FaxManager.mbRecvNewFax = false;

            if(Utls.isBackground(this.getApplicationContext())) {
//                Intent broadcastIntent = new Intent(this, FaxNotificationReceiver.class);
//                PendingIntent pendingIntent = PendingIntent.
//                        getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                log.debug("will send notification...");
                Intent resultIntent = new Intent(this, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setContentTitle("新传真")
                        .setTicker("新传真来啦")
                        .setContentText("一份新传真.");
                mNotificationManager.notify(notifyId, mBuilder.build());

            } else {
                Toast.makeText(MainActivity.this, "收到一份新传真.", Toast.LENGTH_LONG).show();
                if(miType == FileLibController.RECV_TYPE) {
                    showBox(miType);
                }
            }


        }
        if (FaxManager.mbSendNewFax && FaxManager.mbSendNewFax2) {
            FaxManager.mbSendNewFax = false;
            FaxManager.mbSendNewFax2 = false;
            if(Utls.isBackground(this.getApplicationContext())) {

                mBuilder.setContentTitle("新传真")
                        .setTicker("新传真发送成功。")
                        .setContentText("新传真发送成功。");
                mNotificationManager.notify(notifyId, mBuilder.build());

            } else {

                Toast.makeText(MainActivity.this, "成功发送一份新传真.", Toast.LENGTH_LONG).show();
                if(miType == FileLibController.SEND_TYPE) {
                    showBox(miType);
                }
            }
        }

        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("请确认是否退出？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.this.finish();

                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    })
                    .show();
        }

        return false;
    }

    //测试
    private void addTestItem(int iType, String strSubject)
    {
        FileLib fileLib = new FileLib();
        for (int i=0; i<3; i++) {
            FileLib logItem = new FileLib();
            logItem.setiType(iType);
            logItem.setStrSender("test");
            logItem.setStrReceiver("me");
            logItem.setStrFilePath("/sdcard/183/2pic.tif");
            logItem.setStrSubject(strSubject+i);
            log.debug("add fileLib    " + i);
            logItem.save();
        }
    }

    private void clear()
    {
        updateThread.setRunning(false);

        WakeLockUtil.releaseWakeLock(this);

        this.finish();

//        Intent intent = new Intent();
//        intent.setClass(MainActivity.this, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mEdit = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);


        mEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             /*判断是否是“GO”键*/
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                /*隐藏软键盘*/
                    mSearchView.clearFocus();

                    log.debug("content:"+mEdit.getText().toString());
                    search(mEdit.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create_fax) {

            Bundle bundle = new Bundle();
            FileLib fileLib = null;
            bundle.putSerializable(Constant.KEY_FILELIB_ITEM, fileLib);
            bundle.putBoolean("IsCreateFax", true);
            Intent intent = new Intent(MainActivity.this,FaxCreateActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constant.ITEM_SEND_REQ);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    /** 设备配置改变时 */
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mDrawerToggle.onConfigurationChanged(newConfig);
//    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_send_box) {
            log.debug("nav_send_box");
            // Handle the camera action
            this.setTitle(this.getResources().getString(R.string.send_box));

            listContentViewAdapter.clear();
            listContentView.setAdapter(listContentViewAdapter);
            listContentViewAdapter.setDataList(FileLibController.getInstance().getMainPageSendFax(), FileLibController.SEND_TYPE);
            listContentViewAdapter.notifyDataSetChanged();
            miType = FileLibController.SEND_TYPE;
        } else if (id == R.id.nav_recv_box) {
            listContentViewAdapter.clear();
            listContentView.setAdapter(listContentViewAdapter);
            listContentViewAdapter.setDataList(FileLibController.getInstance().getMainPageRecvFax(), FileLibController.RECV_TYPE);
            listContentViewAdapter.notifyDataSetChanged();
            this.setTitle(this.getResources().getString(R.string.recv_box));
            miType = FileLibController.RECV_TYPE;;
        } else if (id == R.id.nav_draft_box) {
            listContentViewAdapter.clear();
            listContentView.setAdapter(listContentViewAdapter);
            listContentViewAdapter.setDataList(FileLibController.getInstance().getDraftFax(), FileLibController.DRAFT_TYPE);
            listContentViewAdapter.notifyDataSetChanged();
            this.setTitle(this.getResources().getString(R.string.draft_box));
            miType = FileLibController.DRAFT_TYPE;
        } else if (id == R.id.nav_star_box) {
            listContentViewAdapter.clear();
            listContentView.setAdapter(listContentViewAdapter);
            listContentViewAdapter.setDataList(FileLibController.getInstance().getStarFax(), FileLibController.STAR_TYPE);
            listContentViewAdapter.notifyDataSetChanged();
            this.setTitle(this.getResources().getString(R.string.star_box));
            miType = FileLibController.STAR_TYPE;
        } else if (id == R.id.nav_phonebook_box) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putBoolean("IsSelectReceiverMode", false);
            intent.putExtras(bundle);
            intent.setClass(MainActivity.this, PhoneBookActivity.class);
            startActivity(intent);

        }  else if (id == R.id.nav_setting_box) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingActivity.class);

            startActivity(intent);

        }  else if (id == R.id.nav_exit) {
            new AlertDialog.Builder(this)
                    .setTitle("请确认是否退出本账户？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            PublicVariable.UserCode="";
                            PublicVariable.Password="";
                            StateObject.getInstance().setIsLogin(false);

                            SharedPreferences sp = null;
                            sp = getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
                            //删除用户名、密码、
                            SharedPreferences.Editor editor = sp.edit();
                           // editor.remove("USER_NAME");
                            editor.remove("PASSWORD");
                            editor.remove("ISLOAD");
                            editor.commit();

                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, LoginActivity.class);

                            startActivity(intent);

                            clear();

                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    })
                    .show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showBox(int iType)
    {
        listContentViewAdapter.clear();
        listContentView.setAdapter(listContentViewAdapter);
        List<FileLib> list = null;
        switch (iType) {
            case FileLibController.RECV_TYPE:
                list = FileLibController.getInstance().getMainPageRecvFax();
                break;
            case SEND_TYPE:
                list = FileLibController.getInstance().getMainPageSendFax();
                break;
            case FileLibController.DRAFT_TYPE:
                list = FileLibController.getInstance().getDraftFax();
                break;
            case FileLibController.STAR_TYPE:
                list = FileLibController.getInstance().getStarFax();
                break;
            default:
                list = FileLibController.getInstance().getMainPageRecvFax();
                break;


        }
        listContentViewAdapter.setDataList(list, iType);
        listContentViewAdapter.notifyDataSetChanged();
        miType = iType;
    }

    FileLib longItemDel = null;
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        // TODO Auto-generated method stub
        longItemDel = (FileLib)listContentViewAdapter.getItem(position);
        String strTitle = "请确认是否删除";
        strTitle += "\"";
        if(miType == FileLibController.SEND_TYPE || miType == FileLibController.RECV_TYPE) {
            strTitle += longItemDel.getStrSender();
        } else {
            if (!longItemDel.getStrSender().isEmpty()) {
                strTitle += longItemDel.getStrSender();
            } else {
                strTitle += longItemDel.getStrReceiver();
            }
        }
        strTitle += "\"";
        if(longItemDel.getiTotal() <= 1) {
            strTitle += "的传真？";
        } else {
            strTitle += "的全部传真？";
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(strTitle)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        String strRead = "1";
                        if (!longItemDel.isbRead()) {
                            strRead = "0";
                        }

                        if(longItemDel.getiTotal() <= 1) {
                            DataSupport.delete(FileLib.class, longItemDel.getId());
                        } else {
                            DataSupport.deleteAll(FileLib.class, "iType=? AND bRead=? AND strSender=? AND strReceiver=?", miType+"", strRead, longItemDel.getStrSender(), longItemDel.getStrReceiver());
                         }

                        showBox(miType);
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

        log.debug("cur item: "+item.getStrSubject());

        if(!item.isbRead() || item.getiTotal() <= 1) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constant.KEY_FILELIB_ITEM, item);
            Intent intent = new Intent(MainActivity.this,FaxViewActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constant.ITEM_VIEW_REQ);

        } else {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constant.KEY_FILELIB_ITEM, item);
            Intent intent = new Intent(MainActivity.this,UserSameMailViewActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constant.ITEM_VIEW_REQ);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        //requestCode标示请求的标示   resultCode表示有数据
        if (resultCode == Constant.ITEM_VIEW_RESP_DELETE) {
            Bundle bundle = data.getExtras();
            int iType = bundle.getInt(Constant.ITEM_TYPE);
            log.debug("delete one item.");
            showBox(iType);
        }

        if(resultCode == Constant.ITEM_SEND_REQ_OK || resultCode == Constant.ITEM_VIEW_RESP_OK) {
            showBox(miType);
        }
    }


    private void search(String strKeyword)
    {
        List<FileLib> list = FileLibController.getInstance().searchFax(strKeyword);
        for(int i=0; i<list.size(); i++) {
            log.debug("item:"+i+"  "+list.get(i).getStrSubject());
        }
        listContentViewAdapter.clear();
        listContentView.setAdapter(listContentViewAdapter);
        listContentViewAdapter.setDataList(list, miType);
        listContentViewAdapter.notifyDataSetChanged();
        this.setTitle(this.getResources().getString(R.string.search_result));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtUserName:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingActivity.class);

                startActivity(intent);

                break;

            default:
                break;
        }
    }



    ///Send thread.
    class UpdateFaxListThread extends Thread{

        private volatile boolean isRunning = true;
        private Handler handler = null;
        public UpdateFaxListThread(Handler handler) {
            this.handler = handler;
        }


        @Override
        public void run() {
            Message msg = null;
            while(isRunning()){
                msg = new Message();
                msg.what = 0x1;
                handler.sendMessage(msg);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

    }
}
