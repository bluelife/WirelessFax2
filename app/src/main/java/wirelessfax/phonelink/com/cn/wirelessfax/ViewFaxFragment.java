package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.merhold.extensiblepageindicator.ExtensiblePageIndicator;

import org.litepal.crud.DataSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoViewAttacher;
import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Sqlite.FileLibController;
import wirelessfax.phonelink.com.cn.Sqlite.Setting;
import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.FileUtil;
import wirelessfax.phonelink.com.cn.Utls.ImageManager;
import wirelessfax.phonelink.com.cn.Utls.PhotoView;
import wirelessfax.phonelink.com.cn.Utls.Tiff2Pdf;
import wirelessfax.phonelink.com.cn.Utls.TiffImages;
import wirelessfax.phonelink.com.cn.Utls.Utls;

/**
 * Created by slomka.jin on 2016/11/4.
 */

public class ViewFaxFragment extends Fragment implements View.OnClickListener {
    @BindView(R.id.detail_control_panel)
    LinearLayout detailControlPanel;
    @BindView(R.id.txtSender)
    TextView txtSender;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtTime)
    TextView txtTime;
    @BindView(R.id.detail_content)
    LinearLayout detailContent;
    @BindView(R.id.detail_print)
    TextView detailPrint;
    private Logger log = LoggerFactory.getLogger(ViewFaxFragment.class);

    @BindView(R.id.signText)
    TextView signText;
    @BindView(R.id.signImage)
    TextView signImageText;
    @BindView(R.id.detail_reward)
    TextView reward;
    @BindView(R.id.detail_del)
    TextView del;
    @BindView(R.id.detail_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.root_layout)
    ViewGroup rootView;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.2F);
    private AppCompatActivity activity;

    @BindView(R.id.flexibleIndicator)
    ExtensiblePageIndicator extensiblePageIndicator;
    @BindView(R.id.frag_choose_viewpager)
    ViewPager viewPager;

    private FileLib fileLib = null;
    private Bundle mBundle = null;
    @Nullable


    private PopupWindow mPopupWindow = null;
    private EditText mstrPasswd = null;
    private Button mBtnPasswdOk = null;

    private View parentView = null;
    private  View popupView = null;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_view_fax, container, false);
        ButterKnife.bind(this, parentView);
        setHasOptionsMenu(true);
        activity = (AppCompatActivity) getActivity();

        popupView = inflater.inflate(R.layout.sign_password_input, null);

        mPopupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, 240, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        mstrPasswd = (EditText) popupView.findViewById(R.id.txtPasswd);
        mBtnPasswdOk = (Button) popupView.findViewById(R.id.btnOk);
        mBtnPasswdOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //check passwd,
                Setting setting = DataSupport.findFirst(Setting.class);
                String strPwd = mstrPasswd.getText().toString().trim();

                if (strPwd.compareTo(setting.getStrPasswd()) != 0) {
                    Toast.makeText(activity, "密码错误.", Toast.LENGTH_LONG).show();
                    mstrPasswd.setText("");
                    return;
                }

                mstrPasswd.setText("");
                mPopupWindow.dismiss();
                popupView = null;

                TiffImages.getInstance().setSelectIndex(viewPager.getCurrentItem());

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("Mode", ImageManager.MODE_IMAGE);
                bundle.putSerializable(Constant.KEY_FILELIB_ITEM, fileLib);
                intent.putExtras(bundle);
                intent.setClass(activity, SignActivity.class);
                startActivityForResult(intent, Constant.UPDATE_IMGAGE);
            }
        });


        mBundle = getArguments();
        fileLib = (FileLib) mBundle.getSerializable(Constant.KEY_FILELIB_ITEM);

        if (fileLib.getiType() == FileLibController.SEND_TYPE) {
            txtSender.setText(fileLib.getStrReceiver());
        } else if (fileLib.getiType() == FileLibController.RECV_TYPE) {
            txtSender.setText(fileLib.getStrSender());
        } else {
            if (!fileLib.getStrSender().isEmpty()) {
                txtSender.setText(fileLib.getStrSender());
            } else {
                txtSender.setText(fileLib.getStrReceiver());
            }
        }

        if (!fileLib.isbRead()) {
            ContentValues values = new ContentValues();
            values.put("bRead", true);
            DataSupport.update(FileLib.class, values, fileLib.getId());
        }

        String str2 = fileLib.getStrFilePath();
        int pos = str2.lastIndexOf('/');
        if (pos >= 0) {
            txtTitle.setText(str2.substring(pos + 1, str2.length()));
        } else {
            txtTitle.setText(str2);
        }

        txtTime.setText(Utls.getUIShowTime(fileLib.getlTime()));


        log.debug("xxxsubject:" + fileLib.getStrSubject());
        log.debug("xxxpath:" + fileLib.getStrFilePath());

        File file = new File(fileLib.getStrFilePath());
        if (file.exists()) {
            showImage();
        }

        txtSender.setOnClickListener(this);

        return parentView;
    }

    @Override
    public void onDestroy()
    {
        mPopupWindow = null;
        popupView = null;

        super.onDestroy();
    }

    @OnClick(R.id.signText)
    void onSignText() {
        fadeButton(signText);

        if (TiffImages.getInstance().getImages().size() <= 0) {
            Toast.makeText(activity, "没有传真文件,不可签名。", Toast.LENGTH_LONG).show();
            return;
        }

        TiffImages.getInstance().setSelectIndex(viewPager.getCurrentItem());

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("Mode", ImageManager.MODE_SIGN);
        bundle.putSerializable(Constant.KEY_FILELIB_ITEM, fileLib);
        intent.putExtras(bundle);
        intent.setClass(activity, SignActivity.class);
        startActivityForResult(intent, Constant.UPDATE_IMGAGE);
    }

    public void showImage()
    {
        String strTmp = fileLib.getStrFilePath();
        String tiffPathSrc=strTmp.substring(0,strTmp.lastIndexOf("."))+"_src.tif";
        File file = new File(tiffPathSrc);
        if (file.exists()) {
            strTmp = tiffPathSrc;
        }
        log.debug("xxxpath:000  " + strTmp);
        TiffImages.getInstance().clear();
        List<Bitmap> bitmaps = FileUtil.loadImage(strTmp);
        if (bitmaps == null) {
            Toast.makeText(activity, "加载传真失败，传真可能损坏.", Toast.LENGTH_LONG).show();
            bitmaps = new ArrayList<Bitmap>();
        }
        TiffImages.getInstance().setImages(bitmaps);
        TiffPagerAdapter tiffPagerAdapter = new TiffPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(tiffPagerAdapter);
        extensiblePageIndicator.initViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int arg0) {

                int childCount = viewPager.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = viewPager.getChildAt(i);

                    try {
                        if (childAt != null && childAt instanceof PhotoView) {
                            PhotoView photoView = (PhotoView) childAt;
                            PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);
                            mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @OnClick(R.id.signImage)
    void onSignImage() {
        fadeButton(signImageText);
        if (TiffImages.getInstance().getImages().size() <= 0) {
            Toast.makeText(activity, "没有传真文件,不可盖章。", Toast.LENGTH_LONG).show();
            return;
        }

        Setting setting = DataSupport.findFirst(Setting.class);
        if (setting == null || setting.getStrImagePath().isEmpty()) {
            Toast.makeText(activity, "未设置公章，请先设置公章。", Toast.LENGTH_LONG).show();
            return;
        }
        int[] location = new int[2];
        detailControlPanel.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(detailControlPanel, Gravity.NO_GRAVITY, location[0], location[1] - mPopupWindow.getHeight());
        openKeyboard();
    }

    @OnClick(R.id.detail_reward)
    void onReward() {
        fadeButton(reward);

        if (TiffImages.getInstance().getImages().size() <= 0) {
            Toast.makeText(activity, "没有传真文件,不可转发。", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtras(mBundle);
        intent.setClass(activity, FaxCreateActivity.class);
        startActivity(intent);
        this.getActivity().finish();
    }

    @OnClick(R.id.detail_del)
    void onDel() {
        fadeButton(del);

        new AlertDialog.Builder(this.activity)
                .setTitle("请确认是否删除？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constant.ITEM_TYPE, fileLib.getiType());
                        intent.putExtras(bundle);

                        String strFileName = fileLib.getStrFilePath();

                        DataSupport.delete(FileLib.class, fileLib.getId());
                        File file = new File(strFileName);
                        log.debug("fileid:"+fileLib.getId());

                        Pattern pattern = Pattern.compile("(.*).tif");
                        Matcher matcher = pattern.matcher(file.getName());
                        boolean match_found = matcher.find();

                        // Check if src is a TIFF file
                        if (match_found) {
                            // Keep name, change extension to .pdf
                            String pdf_name = matcher.group(1) + ".pdf";
                            File pdfFile = new File(getTmpDirs(), pdf_name);
                            if (pdfFile.exists()) {
                                pdfFile.delete();
                            }
                        }
                        if(file.exists()) {
                            file.delete();
                        }

                        Toast.makeText(getActivity(), "删除传真成功。", Toast.LENGTH_SHORT).show();

                        getActivity().setResult(Constant.ITEM_VIEW_RESP_DELETE, intent);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .show();

    }

    void fadeButton(View view) {
        buttonClick.setDuration(500);
        view.startAnimation(buttonClick);
    }

    /**
     * 打开软键盘
     */
    private void openKeyboard() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            }
        }, 1);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
        progressBar.setVisibility(View.GONE);
    }

    private void updateStar() {
        if (fileLib.getiType() == FileLibController.STAR_TYPE) {
            unstatItem.setVisible(false);
            statItem.setVisible(true);
        } else {
            unstatItem.setVisible(true);
            statItem.setVisible(false);
        }
    }


    private MenuItem statItem;
    private MenuItem unstatItem;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_fax_menu, menu);

        statItem = menu.findItem(R.id.action_star);
        unstatItem = menu.findItem(R.id.action_unstar);

        updateStar();
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        ContentValues values = new ContentValues();
        if (id == R.id.action_star) {
            values.put("iOldType", FileLibController.DRAFT_TYPE);
            values.put("iType", fileLib.getiOldType());

            unstatItem.setVisible(true);
            statItem.setVisible(false);

            Toast.makeText(this.getActivity(), "取消标星。", Toast.LENGTH_SHORT).show();

        } else {
            values.put("iOldType", fileLib.getiType());
            values.put("iType", FileLibController.STAR_TYPE);

            unstatItem.setVisible(false);
            statItem.setVisible(true);

            Toast.makeText(this.getActivity(), "标星。", Toast.LENGTH_SHORT).show();
        }

        DataSupport.update(FileLib.class, values, fileLib.getId());

        return super.onOptionsItemSelected(item);
    }


    @OnClick({R.id.txtSender, R.id.txtTitle, R.id.txtTime})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtSender:

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putBoolean("IsPhoneBookMgr", false);
                intent.putExtras(bundle);
                intent.setClass(activity, AddPersonActivity.class);
                startActivity(intent);


                break;
            case R.id.txtTitle:
                break;
            case R.id.txtTime:
                break;
        }
    }

    @OnClick(R.id.detail_print)
    public void onClick() {
        if(!appIsInstalled(this.activity, "com.dynamixsoftware.printershare")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = this.getAssetFileToCacheDir(activity,"printershare.apk");
            intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
            activity.startActivity(intent);
        }

        //convert tiff 2 pdf
        File srcFile = new File(fileLib.getStrFilePath());
        if(Tiff2Pdf.tiff2Pdf(srcFile, new File(getTmpDirs()))) {
            Pattern pattern = Pattern.compile("(.*).tif");
            Matcher matcher = pattern.matcher(srcFile.getName());
            boolean match_found = matcher.find();
            File dstFile = new File(getTmpDirs() +"/"+ matcher.group(1) + ".pdf");
            if (dstFile != null && dstFile.exists()) {
                Toast.makeText(this.getActivity(), "tiff2PDF sucessfully.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                ComponentName comp = new ComponentName("com.dynamixsoftware.printershare", "com.dynamixsoftware.printershare.ActivityPrintPDF");
                intent = new Intent();
                intent.setComponent(comp);
                intent.setAction("android.intent.action.VIEW");
                intent.setType("application/pdf");
                intent.setData(Uri.fromFile(dstFile));
                startActivity(intent);
            } else {
                Toast.makeText(this.getActivity(), "tiff2PDF failed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.getActivity(), "tiff2PDF failed.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean appIsInstalled(Context context, String pageName) {
        try {
            context.getPackageManager().getPackageInfo(pageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public  File getAssetFileToCacheDir(Context context, String fileName) {
        try {
            File cacheDir = this.getCacheDir(context);
            final String cachePath = cacheDir.getAbsolutePath()+ File.separator + fileName;
            InputStream is = context.getAssets().open(fileName);
            File file = new File(cachePath);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];

            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getCacheDir(Context context) {
        String APP_DIR_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/";
        File dir = new File(APP_DIR_NAME + context.getPackageName() + "/cache/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public String getTmpDirs()
    {
        File file = new File(this.activity.getExternalCacheDir()+"/"+"Pdf");
        if(!file.exists()) {
            file.mkdirs();
        }

        return file.getAbsolutePath();
    }



}
