package wirelessfax.phonelink.com.cn.wirelessfax;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import wirelessfax.phonelink.com.cn.Utls.Utls;
import wirelessfax.phonelink.com.cn.network.FaxManager;
import wirelessfax.phonelink.com.cn.network.PublicVariable;
import wirelessfax.phonelink.com.cn.network.StateObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private org.slf4j.Logger log = LoggerFactory.getLogger(LoginActivity.class);
    private Intent intentMainService;
    private Intent intentFaxService;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.setTitle("登陆");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        //test
//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

        sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);

        String strUserName = sp.getString("USER_NAME", "");

        //如果登陆过，直接登录
        if(sp.getBoolean("ISLOAD",true)) {
            PublicVariable.UserCode = sp.getString("USER_NAME", "");
            PublicVariable.Password = sp.getString("PASSWORD", "");
            mEmailView.setText(PublicVariable.UserCode);
            mPasswordView.setText(PublicVariable.Password);

            long curTime = Utls.getLocalTimeMillis();
            long pretime = sp.getLong("FirstTimeLogin", curTime-5*24*60*60*1000);

            long duration = curTime - pretime;
//            //15天
//            if(pretime == 0 || (duration > 45*24*60*60*1000)) {
//                if(pretime == 0) {
//                    SharedPreferences.Editor editor = sp.edit();
//                    editor.putLong("FirstTimeLogin", Utls.getLocalTimeMillis());
//                    editor.commit();
//                    return;
//                }
//
//                //android.os.Process.killProcess(android.os.Process.myPid());
//
//                return;
//            }

            attemptLogin();


//            Intent intent = new Intent(this, MainActivity.class);
//            intent.putExtra("USER",sp.getString("USER_NAME", ""));
//            startActivity(intent);
//            this.finish();//关闭登录界面
        } else if (!strUserName.isEmpty()) {
            mEmailView.setText(strUserName);
            mPasswordView.setText("");
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private int nWaitCount = 0;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;

            FaxManager.bLogining = true;

            PublicVariable.UserCode = email;
            PublicVariable.Password = mPassword;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            nWaitCount = 0;
            try {
                // Simulate network access.
                while (true) {
                    if (!StateObject.getInstance().getIsLogin()) {
                        if (nWaitCount > 10) {
                            break;
                        }
                        Thread.sleep(2000);
                        nWaitCount++;
                    } else {
                        nWaitCount = 0;
                        break;
                    }
                }

            } catch (InterruptedException e) {
                return false;
            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

            if (nWaitCount > 5) {

                return  false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                Message msg = new Message();
                uiHandler.sendMessage(msg);

                PublicVariable.UserCode = mEmail;
                PublicVariable.Password = mPassword;

                finish();
            } else {
//                PublicVariable.UserCode = "";
//                PublicVariable.Password = "";

                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private Handler uiHandler= new Handler() {
        @Override
        public void handleMessage(Message msg) {


            if (!StateObject.getInstance().getIsLogin()) {
                log.debug("login failed, tiemout.");
                showProgress(false);
                return;
            }

            //记住用户名、密码、
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("USER_NAME", mEmailView.getText().toString());
            editor.putString("PASSWORD", mPasswordView.getText().toString());
            editor.putLong("FirstTimeLogin", Utls.getLocalTimeMillis());
            editor.putBoolean("ISLOAD", true);
            editor.commit();

            // Activity跳转
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };

}

