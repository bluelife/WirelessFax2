package wirelessfax.phonelink.com.cn.wirelessfax;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.merhold.extensiblepageindicator.ExtensiblePageIndicator;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.beyka.tiffbitmapfactory.TiffSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.FileUtil;
import wirelessfax.phonelink.com.cn.Utls.ImageManager;
import wirelessfax.phonelink.com.cn.Utls.TiffImages;
import wirelessfax.phonelink.com.cn.Utls.Utls;
import wirelessfax.phonelink.com.cn.mvp.presenter.HeaderPresenter;
import wirelessfax.phonelink.com.cn.mvp.view.IHeaderView;
import wirelessfax.phonelink.com.cn.network.FaxInfo;
import wirelessfax.phonelink.com.cn.network.FaxManager;
import wirelessfax.phonelink.com.cn.network.PublicVariable;
import wirelessfax.phonelink.com.cn.network.StateObject;

import static com.lowagie.text.pdf.codec.TIFFConstants.ORIENTATION_TOPLEFT;
import static wirelessfax.phonelink.com.cn.Utls.Constant.ITEM_SEND_REQ_OK;
import static wirelessfax.phonelink.com.cn.Utls.Constant.ITEM_VIEW_REQ;
import static wirelessfax.phonelink.com.cn.mvp.presenter.HeaderPresenter.IMAGE_FILE_NAME;

public class FaxCreateActivity extends AppCompatActivity implements View.OnClickListener,IHeaderView {
    @BindView(R.id.toolbarlayout)
    AppBarLayout toolbarlayout;
    @BindView(R.id.textView3)
    TextView textView3;
    @BindView(R.id.etReceiver)
    EditText etReceiver;
    @BindView(R.id.btAddReceiver)
    Button btAddReceiver;
    @BindView(R.id.senderlayout)
    LinearLayout senderlayout;
    @BindView(R.id.textView4)
    TextView textView4;
    @BindView(R.id.btnSelectAttach)
    Button btnSelectAttach;
    @BindView(R.id.selectAttachLayout)
    LinearLayout selectAttachLayout;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.select_photo)
    Button selectPhoto;
    private Logger log = LoggerFactory.getLogger(FaxCreateActivity.class);
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.flexibleIndicator)
    ExtensiblePageIndicator extensiblePageIndicator;
    @BindView(R.id.frag_choose_viewpager)
    ViewPager viewPager;


    private ImageManager imageManager;
    private EditText editNumber = null;
    private Button btnAddReceiver = null;
    private Button btnAddAttach = null;

    private boolean bCreateFax = true;
    private HeaderPresenter headerPresenter;

    private ProgressDialog loadingDialog = null;
    private Handler handler = new Handler();

    private int iCount = 0;
    private Runnable runnable= new Runnable() {
        public void run() {
            if (FaxManager.mbSendNewFax) {
                FaxManager.mbSendNewFax = false;
                FaxManager.mbSendNewFax2 = false;
                loadingDialog.setProgress(100);
                loadingDialog.dismiss();

                clear();

                Toast.makeText(FaxCreateActivity.this, "传真发送成功。", Toast.LENGTH_LONG).show();
                return;
            }

            if(!StateObject.getInstance().getIsLogin()) {
                clear();
                loadingDialog.dismiss();
                Toast.makeText(FaxCreateActivity.this, "用户离线状态，传真发送终止，请等待程序重新登陆再尝试，或者程序退出，再登陆。", Toast.LENGTH_LONG).show();
                return;
            }

            if(iCount>600) {
                iCount = 0;
                clear();
                FaxManager.mbSendNewFax2 = true;
                loadingDialog.dismiss();
                Toast.makeText(FaxCreateActivity.this, "传真文件比较大，已转到后台发送。", Toast.LENGTH_LONG).show();
                return;
            }
            iCount++;
            handler.postDelayed(this, 1000);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fax_create);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        selectPhoto.setOnClickListener(this);
        headerPresenter = new HeaderPresenter(this, this);

        imageManager=new ImageManager(FaxCreateActivity.this, null);


        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        FileLib fileLib = (FileLib) bundle.get(Constant.KEY_FILELIB_ITEM);
        if (fileLib != null) {
            imageManager.setImage(fileLib.getStrFilePath());
            this.setTiff(imageManager.getImage());;
            if (!Utls.isFileExist(imageManager.getTiffPathSrc())) {
                if (TiffImages.getInstance().getImages().size() == 1) { //多张图，跳过
                    new SaveTiffTask().execute(TiffImages.getInstance().getSelectBitmap());
                }
            }
        }

        bCreateFax = bundle.getBoolean("IsCreateFax");

        if (bCreateFax) {
            selectAttachLayout.setVisibility(View.VISIBLE);
        } else {
            selectAttachLayout.setVisibility(View.GONE);
        }

        editNumber = (EditText) findViewById(R.id.etReceiver);
        btnAddReceiver = (Button) findViewById(R.id.btAddReceiver);
        btnAddReceiver.setOnClickListener(this);

        btnAddAttach = (Button) findViewById(R.id.btnSelectAttach);
        btnAddAttach.setOnClickListener(this);

        this.setTitle("新传真");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void clear()
    {
        iCount = 0;
        setResult(ITEM_SEND_REQ_OK);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_fax_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sendfax) {
            sendFax();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendFax() {
        //判断接收人格式是否正确
        String strReceiver = editNumber.getText().toString().trim();
        if(strReceiver.isEmpty()) {
            Toast.makeText(FaxCreateActivity.this, "请输入传真号。", Toast.LENGTH_LONG).show();
            return;
        }

        if (imageManager.getImage() == null) {
            Toast.makeText(FaxCreateActivity.this, "请选择附件。", Toast.LENGTH_LONG).show();
            return;
        }


        File tmpFile = new File(imageManager.getImage());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int page = 1;
        try {
            int fileLen = fis.available();

            //1byte == 256pages, 每页128个pkt，每格pkt大小是512字节。
            if(fileLen > 256*128*512) {
                Toast.makeText(FaxCreateActivity.this, "文件过大（超过16M)，不能发送。", Toast.LENGTH_LONG).show();
                return;
            }

            page = fileLen/(128*512);
            if (fileLen %(128*512) != 0) {
                page++;
            }

            log.debug("send fax file total pages:" + page);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //检查图片尺寸，如果大小超过1728/1280,则缩小保存
        if (TiffImages.getInstance().getImages().size()  == 1) {
            Bitmap bitmap = TiffImages.getInstance().getSelectBitmap();
            if (bitmap != null) {
                log.debug("compress jpg: " + imageManager.getImage());
                ImageManager.saveMyBitmap(bitmap, imageManager.getImage());
                //bitmap是否有缩小？

                tmpFile = new File(imageManager.getTiffPathSrc());
                if(!tmpFile.exists()) {
                    //转成
                    //new SaveTiffTask().execute(bitmap);
                    int index=TiffImages.getInstance().getSelectIndex();
                    int orientationType = ORIENTATION_TOPLEFT;
                    if(TiffImages.getInstance().getOptionsList() != null) {
                        orientationType=TiffImages.getInstance().getOptionsList().get(index).getValue();
                    }

                    TiffSaver.Orientation orientation= TiffSaver.Orientation.fromInt(orientationType);
                    imageManager.saveImage(bitmap,false,orientation);
                }

            }
        }


        String[] list = strReceiver.split(";");

        for (int i = 0; i < list.length; i++) {
            if (!list[i].isEmpty() && list[i].trim().length() > 0) {
                FaxInfo faxInfo = new FaxInfo();
                faxInfo.setFaxType(1);
                faxInfo.setPages(page);
                faxInfo.setFaxFile(imageManager.getTiffImage());
                log.debug("before send fax file:" + imageManager.getTiffImage());
                if (list[i].contains("10086")) {

                    //多张图片的tif，首次发送的话，此时没有src文件。
                    if(Utls.isFileExist(imageManager.getTiffPathSrc())) {
                        faxInfo.setFaxFile(imageManager.getTiffPathSrc());
                        log.debug("10086 send fax file:" + imageManager.getTiffPathSrc());
                    }
                }
                faxInfo.setCalled(list[i]);
                faxInfo.setSenderMobile(PublicVariable.UserCode);

                FaxManager.Add(faxInfo);
            }
        }

        if(loadingDialog == null) {
            loadingDialog = new ProgressDialog(FaxCreateActivity.this);
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingDialog.setMax(100);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        }
        iCount = 0;
        loadingDialog.setTitle("正在发送传真...");
        loadingDialog.show();
        handler.postDelayed(runnable, 100);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btAddReceiver:
                selectReceiver();
                break;
            case R.id.btnSelectAttach:
                selectAttach();
                break;
            case R.id.select_photo:
                headerPresenter.showHeadDialog();
                break;
            default:
                break;
        }
    }

    public void selectReceiver() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putBoolean("IsSelectReceiverMode", true);
        intent.putExtras(bundle);
        intent.setClass(FaxCreateActivity.this, PhoneBookActivity.class);
        startActivityForResult(intent, Constant.REQ_SELECT_RECEIVER);
    }

    public void selectAttach() {

        Intent i = new Intent(FaxCreateActivity.this, FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Utls.getSelectedExtenedPath());
        startActivityForResult(i, ITEM_VIEW_REQ);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //选择附件
        if (requestCode == ITEM_VIEW_REQ && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String strPath = uri.getPath();
            imageManager.setImage(strPath);
            setTiff(imageManager.getImage());

            return;
        }

        //选择 接收人
        if (resultCode == Constant.RESP_SELECT_RECEIVER) {
            Bundle bundle = data.getExtras();
            ArrayList<String> list = bundle.getStringArrayList("UserCodeList");

            String str = editNumber.getText().toString();
            for (int i = 0; i < list.size(); i++) {

                if (!str.isEmpty()) {
                    str += ";";
                }
                if (!list.get(i).isEmpty()) {
                    str += list.get(i);
                }
            }
            editNumber.setText(str);
            return;
        }

        // 点击取消按钮
        if (resultCode == RESULT_CANCELED) {
            return;
        }

        //拍照及相册
        String strFilePath = null;
        switch (requestCode) {
            case HeaderPresenter.IMAGE_REQUEST_CODE:
                strFilePath = getPath(FaxCreateActivity.this, data.getData());
                break;
            case HeaderPresenter.SELECT_PIC_KITKAT:
                strFilePath = getPath(FaxCreateActivity.this, data.getData());
                break;
            case HeaderPresenter.CAMERA_REQUEST_CODE:
                if (HeaderPresenter.hasSdcard()) {
                    File srcFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
                    String strTmpPath = Environment.getExternalStorageDirectory() + "/" + "fax_photo_"+Utls.getDateTime(Utls.FORMAT_DATA_TIME)+".jpg";

                    //重命名
                    File dstFile = new File(strTmpPath);
                    srcFile.renameTo(dstFile);
                    strFilePath = strTmpPath;

                    log.debug("filepath: "+strFilePath);

                } else {
                    Toast.makeText(FaxCreateActivity.this, "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

        imageManager.setImage(strFilePath);
        setTiff(imageManager.getImage());

        super.onActivityResult(requestCode, resultCode, data);
    }

    void setTiff(String strImagePath) {
        TiffImages.getInstance().clear();

        List<Bitmap> bitmaps = FileUtil.loadImage(strImagePath);

        TiffImages.getInstance().setImages(bitmaps);

        TiffPagerAdapter tiffPagerAdapter = new TiffPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tiffPagerAdapter);
        extensiblePageIndicator.initViewPager(viewPager);
        tiffPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setHeaderBitmap(Bitmap bitmap) {

        log.debug("test.....setHeaderBitmap");
        List<Bitmap> bitmapList = new ArrayList<Bitmap>();
        bitmapList.add(bitmap);

        TiffImages.getInstance().setImages(bitmapList);

        TiffPagerAdapter tiffPagerAdapter = new TiffPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tiffPagerAdapter);
        extensiblePageIndicator.initViewPager(viewPager);
    }


    //以下是关键，原本uri返回的是file:///...来着的，android4.4返回的是content:///...
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }



    private class SaveTiffTask extends AsyncTask<Bitmap,Void,Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            boolean isMultiple=TiffImages.getInstance().getImages().size()>1;
            int index=TiffImages.getInstance().getSelectIndex();
            int orientationType = ORIENTATION_TOPLEFT;
            if(TiffImages.getInstance().getOptionsList() != null) {
                orientationType=TiffImages.getInstance().getOptionsList().get(index).getValue();
            }

            TiffSaver.Orientation orientation= TiffSaver.Orientation.fromInt(orientationType);

            boolean success=imageManager.saveImage(params[0],isMultiple,orientation);
            Log.v("save image",imageManager.getTempTiffPath()+"=="+imageManager.getImage()+"="+imageManager.getResultTiffPath());
            if(isMultiple) {

            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean done) {

        }
    }

}
