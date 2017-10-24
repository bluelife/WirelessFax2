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
import wirelessfax.phonelink.com.cn.Utls.ImageManager;

public class SignActivity extends AppCompatActivity  {
    private Logger log = LoggerFactory.getLogger(SignActivity.class);
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private FileLib mFileLib = null;
    private String strMode = ImageManager.MODE_SIGN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        ButterKnife.bind(this);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        strMode = bundle.getString("Mode", strMode);

        setSupportActionBar(toolbar);

        this.setTitle("Sign");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fragmentManager=getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    log.debug("fragment count:"+getSupportFragmentManager().getBackStackEntryCount());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                }
            }
        });

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment=new SignFragment();
        fragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {

        log.debug("back!!!!!!!");
        Intent intent = new Intent();

        setResult(Constant.UPDATE_IMGAGE);

        this.finish();

    }



}


