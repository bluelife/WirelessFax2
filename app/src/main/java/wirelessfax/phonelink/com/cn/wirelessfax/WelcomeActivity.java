package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.Utls;
import wirelessfax.phonelink.com.cn.network.FaxManager;
import wirelessfax.phonelink.com.cn.network.PublicVariable;
import wirelessfax.phonelink.com.cn.network.StateObject;
import wirelessfax.phonelink.com.cn.service.MainService;

public class WelcomeActivity extends Activity {
    private ImageView welcomeImg = null;
    private Intent intentMainService;
    private WelcomeActivity.UserLoginTask mAuthTask = null;
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        welcomeImg = (ImageView) this.findViewById(R.id.welcome_img);
        AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);
        anima.setDuration(3000);// 设置动画显示时间
        welcomeImg.startAnimation(anima);
        anima.setAnimationListener(new AnimationImpl());

        PublicVariable.init(this.getApplicationContext());
        startAllService();

        sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        //如果登陆过，直接登录
        if(sp.getBoolean("ISLOAD",true)) {
            PublicVariable.UserCode = sp.getString("USER_NAME", "");
            PublicVariable.Password = sp.getString("PASSWORD", "");
            long curTime = Utls.getLocalTimeMillis();
            long pretime = sp.getLong("FirstTimeLogin", curTime-5*24*60*60*1000);
            long duration = curTime - pretime;
            Log.v(Constant.TAG, " pretime:"+pretime+" duration:"+duration);
            //15天
//            if(pretime == 0 || duration > (45*24*60*60*1000)) {
//                if(pretime == 0) {
//                    SharedPreferences.Editor editor = sp.edit();
//                    editor.putLong("FirstTimeLogin", Utls.getLocalTimeMillis());
//                    editor.commit();
//                    return;
//                }
//
//                android.os.Process.killProcess(android.os.Process.myPid());
//                return;
//            }
            Log.v(Constant.TAG, " after pretime:"+pretime+" duration:"+duration);
            FaxManager.bLogining = true;
            attemptLogin();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        mAuthTask =  new UserLoginTask(null, null);
        mAuthTask.execute((Void) null);
    }


    private class AnimationImpl implements AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            welcomeImg.setBackgroundResource(R.mipmap.flash);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            skip(); // 动画结束后跳转到别的页面
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

    }

    private void skip() {
        if(StateObject.getInstance().getIsLogin()) {
            startActivity(new Intent(this, MainActivity.class));
        } else  {
            startActivity(new Intent(this, LoginActivity.class));
        }
       finish();
    }

    public void startAllService(){
        Log.v(Constant.TAG, "Starting Services");
        intentMainService = new Intent(WelcomeActivity.this,MainService.class);
        startService(intentMainService);

//        intentFaxService = new Intent(MainActivity.this,FaxService.class);
//        startService(intentFaxService);
    }

    public void stopAllService(){
        Log.v(Constant.TAG, "Stopping Services");

    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private int nWaitCount = 0;

        UserLoginTask(String email, String password) {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            nWaitCount = 0;
            try {
                // Simulate network access.
                while (true) {
                    if (!StateObject.getInstance().getIsLogin()) {
                        if (nWaitCount > 5) {
                            break;
                        }
                        Thread.sleep(300);
                        nWaitCount++;
                    } else {
                        nWaitCount = 0;
                        break;
                    }
                }

            } catch (InterruptedException e) {
                return false;
            }

            if (nWaitCount > 5) {

                return  false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {

            } else {


            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}

