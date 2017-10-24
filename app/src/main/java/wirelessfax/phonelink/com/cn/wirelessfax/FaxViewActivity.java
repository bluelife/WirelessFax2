package wirelessfax.phonelink.com.cn.wirelessfax;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Utls.Constant;

import static wirelessfax.phonelink.com.cn.Utls.Constant.ITEM_VIEW_RESP_OK;

public class FaxViewActivity extends AppCompatActivity  {
    private Logger log = LoggerFactory.getLogger(FaxViewActivity.class);
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private FileLib mFileLib = null;
    Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fax_view);
        ButterKnife.bind(this);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mFileLib = (FileLib) bundle.getSerializable(Constant.KEY_FILELIB_ITEM);

        setSupportActionBar(toolbar);

        this.setTitle("查阅");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.debug("back click");
                fragmentManager.popBackStack();
                onBackPressed();
            }
        });

        fragmentManager=getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment=new ViewFaxFragment();

        Bundle bundle2 = new Bundle();
        bundle2.putBoolean("IsCreateFax", false);
        bundle2.putSerializable(Constant.KEY_FILELIB_ITEM, mFileLib);
        fragment.setArguments(bundle2);

        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        log.debug("back!!!!!!!");

        setResult(ITEM_VIEW_RESP_OK);
        this.finish();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.create_fax_activity_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.sendfax) {
//            log.debug("send fax");
//            return true;
//        }
//        log.debug("not send fax");
//        return super.onOptionsItemSelected(item);
//    }

    public void openFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        super.onActivityResult(requestCode, resultCode, data);

        //requestCode标示请求的标示   resultCode表示有数据
        if (resultCode == Constant.UPDATE_IMGAGE) {
            log.debug("update image.");
            if(fragment != null) {
                log.debug("update image.2");
                ((ViewFaxFragment)fragment).showImage();
            }
        }
    }

}
