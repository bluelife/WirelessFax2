package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import wirelessfax.phonelink.com.cn.network.FaxManager;


public class PendingProgressMergeImageActivity extends Activity {
	private int recLen = 10;  
    private TextView txtView;  

    private Intent intent = null;
    public static boolean isWaiting = true;
    private String strShow = "";
    private ProgressBar progressBar = null;

    
    public void onCreate(Bundle savedInstanceState){  
        super.onCreate(savedInstanceState);  
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pending_progress);  
          
        intent = getIntent();

        FaxManager.mbSendNewFax2 = false;

        txtView = (TextView)findViewById(R.id.txtProgressTitle); 
        txtView.setText("正在合成...");

        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        handler.postDelayed(runnable, 100);  
    }

    private void doneSuccessfull()
    {
        Toast.makeText(PendingProgressMergeImageActivity.this, "成功发送一份新传真。", Toast.LENGTH_LONG).show();
        this.finish();
    }

    private void doneFail()
    {
        FaxManager.mbSendNewFax2 = true;
        Toast.makeText(PendingProgressMergeImageActivity.this, "传真文件比较大，已转到后台发送。", Toast.LENGTH_LONG).show();
        this.finish();
    }

    int iCount = 0;
    Handler handler = new Handler();  
    Runnable runnable = new Runnable() {
        @Override  
        public void run() {
                if (FaxManager.mbSendNewFax) {
                    FaxManager.mbSendNewFax = false;
                    FaxManager.mbSendNewFax2 = false;
                    //doneSuccessfull();
                    return;
                }
                progressBar.setIndeterminate(true);
                progressBar.setProgress(iCount);

                if(iCount>600) {
                   // doneFail();
                    return;
                }
                iCount++;
                handler.postDelayed(this, 100);
        }  
    }; 
}

