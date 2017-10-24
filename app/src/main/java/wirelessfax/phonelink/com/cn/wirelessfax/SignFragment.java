package wirelessfax.phonelink.com.cn.wirelessfax;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import org.beyka.tiffbitmapfactory.TiffReplace;
import org.beyka.tiffbitmapfactory.TiffSaver;
import org.litepal.crud.DataSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Sqlite.Setting;
import wirelessfax.phonelink.com.cn.Utls.Constant;
import wirelessfax.phonelink.com.cn.Utls.FileUtil;
import wirelessfax.phonelink.com.cn.Utls.ImageManager;
import wirelessfax.phonelink.com.cn.Utls.ImageUtil;
import wirelessfax.phonelink.com.cn.Utls.TiffImages;
import wirelessfax.phonelink.com.cn.Utls.TouchImageView;

import static com.lowagie.text.pdf.codec.TIFFConstants.ORIENTATION_TOPLEFT;


public class SignFragment extends Fragment implements ImageManager.Listener, ActionMode.Callback,View.OnTouchListener {

    private Logger log = LoggerFactory.getLogger(SignFragment.class);

    private ImageManager imageManager;
    private AppCompatActivity activity;
    private final int FIX_WIDTH=1728;
    private SignaturePad signaturePad;
    private BottomSheetDialog bottomSheetDialog;
    private ImageView pointView;

    @BindView(R.id.drawer_layout)
    FrameLayout frameLayout;
    @BindView(R.id.root_layout)
    ViewGroup rootView;
    @BindView(R.id.email_bottom_image)
    TouchImageView tiffImage;
    @BindView(R.id.email_up_image)
    TouchImageView pickedImage;

    ImageView signImage;

    private String strMode = ImageManager.MODE_SIGN;

    private ProgressDialog loadingDialog = null;

    private FileLib fileLib = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_sign,container,false);
        ButterKnife.bind(this,view);
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        strMode = arguments.getString("Mode");
        fileLib = (FileLib) arguments.getSerializable(Constant.KEY_FILELIB_ITEM);



        frameLayout.setOnTouchListener(this);

        activity=(AppCompatActivity) getActivity();

        imageManager=new ImageManager(activity,this);
        imageManager.setListener(this);
        pickedImage.setMinZoom(0.25f);
        pickedImage.setUnderImage(tiffImage);

        imageManager.setImage(fileLib.getStrFilePath());

        showTiff();
        if (strMode.compareTo(ImageManager.MODE_IMAGE) == 0) {
           pickedImage.setVisibility(View.VISIBLE);

            getActivity().setTitle("盖章");
            Setting setting = DataSupport.findFirst(Setting.class);
            if (!setting.getStrImagePath().isEmpty()) {
                doCombin(setting.getStrImagePath());
            }
            //加载公章

        } else {
            getActivity().setTitle("签字");
            //签字无需pickedImage
            pickedImage.setVisibility(View.GONE);

            showSignatureDialog();
        }



        return view;
    }

    @Override
    public void onDestroy()
    {

        super.onDestroy();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sign_menu,menu);

    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            log.debug("cancel sign....");
            showTiff();
            if (strMode.compareTo(ImageManager.MODE_IMAGE) == 0) {
                pickedImage.setVisibility(View.VISIBLE);

                //加载公章
                Setting setting = DataSupport.findFirst(Setting.class);
                if(setting != null && !setting.getStrImagePath().isEmpty()) {
                    doCombin(setting.getStrImagePath());
                } else {
                    Toast.makeText(this.getActivity(), "加载公章失败.", Toast.LENGTH_LONG).show();
                }

            } else {
                //签字无需pickedImage
                pickedImage.setVisibility(View.GONE);
            }
            return true;
        }
        if (id == R.id.action_add) {
            list.clear();

            combinAll();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showTiff(){

        if(list.size() > 0) {
            for(Iterator it = list.iterator(); it.hasNext();) {
                frameLayout.removeView((ImageView) it.next());
            }
            list.clear();
        }

        if (strMode.compareTo(ImageManager.MODE_IMAGE) != 0) {
            pickedImage.setVisibility(View.INVISIBLE);
        }

        log.debug("curImg Path;"+imageManager.getImage());
        TiffImages.getInstance().clear();
        List<Bitmap> bitmaps= FileUtil.loadImage(imageManager.getImage());
        TiffImages.getInstance().setImages(bitmaps);
        Bitmap bitmap = TiffImages.getInstance().getSelectBitmap();

//        ViewGroup.LayoutParams layoutParams = tiffImage.getLayoutParams();
//        layoutParams.width = bitmap.getWidth();
//        layoutParams.height = bitmap.getHeight();
//        tiffImage.setLayoutParams(layoutParams);
        if(bitmap == null) {
            log.debug("outImageOrientation bitmap == null");
        }
        log.debug("outImageOrientation bitmap !=  null  0001");
        tiffImage.setImageBitmap(null);

        tiffImage.setImageBitmap(bitmap);
        log.debug("outImageOrientation bitmap !=  null  0002");


    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                attachPoint((int) x,(int) y);
                imageManager.setSignPoint(new PointF(x,y));
                showSignatureDialog();
                return true;
        }
        return false;
    }
    private void attachPoint(int x,int y){
        if(pointView != null) {
            removePoint();
        }
        pointView=new ImageView(getContext());
        pointView.setImageResource(R.drawable.detail_touch_point);
        int width = pointView.getDrawable().getIntrinsicWidth();
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(x-width/2,y-10,0,0);
        frameLayout.addView(pointView,layoutParams);
    }

    @Override
    public void PickFormatError() {

    }

    @Override
    public void setTiff() {

    }

    @Override
    public void doCombin(String image) {

        Bitmap bitmap= FileUtil.loadImage(image).get(0);

        Bitmap bm =((BitmapDrawable) ((ImageView) tiffImage).getDrawable()).getBitmap();
        Bitmap fixWidthBitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(fixWidthBitmap);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);

//        ViewGroup.LayoutParams layoutParams = pickedImage.getLayoutParams();
//        layoutParams.width = bm.getWidth();
//        layoutParams.height = bm.getHeight();
//        pickedImage.setLayoutParams(layoutParams);

        BitmapFactory.Options options1 = new BitmapFactory.Options();
        options1.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image, options1);
        options1.inSampleSize = calculateInSampleSize(options1, 110, 160);  //110,160：转换后的宽和高，具体值会有些出入
        options1.inJustDecodeBounds = false;
        Bitmap bitmap2 = BitmapFactory.decodeFile(image, options1);



        pickedImage.setImageBitmap(bitmap2);
    }



    public int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }

        return inSampleSize;
    }

    @Override
    public void doSign() {

    }

    @Override
    public void onSelectSignPosition() {

    }

    @Override
    public void removeImage() {

    }

    @Override
    public void removeSign() {

    }

    //签名板
    public void showSignatureDialog(){
        if(bottomSheetDialog == null) {

            bottomSheetDialog = new BottomSheetDialog(getContext());

            View view1 = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
            signaturePad= (SignaturePad) view1.findViewById(R.id.signature_pad);
            bottomSheetDialog.setContentView(view1);
            Button button=(Button)view1.findViewById(R.id.detail_sign_done);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(signaturePad.isEmpty()) {
                        Toast.makeText(activity, "请先签名。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bottomSheetDialog.hide();
                    drawSignature();
                    imageManager.setHasSigned(true);
                }
            });
            Button delBtn=(Button)view1.findViewById(R.id.detail_sign_del);
            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(signaturePad.isEmpty()) {
                        bottomSheetDialog.hide();
                        return;
                    }
                    signaturePad.clear();
                }
            });
        }

        bottomSheetDialog.show();

    }

    private  List<ImageView> list = new ArrayList<ImageView>();
    private boolean drawSignature(){
        fadeOutAndHideImage(pointView);
        signaturePad.setDrawingCacheEnabled(true);
        Bitmap bitmap=signaturePad.getDrawingCache();
        if(bitmap == null) {
            signaturePad.setDrawingCacheEnabled(false);
            Toast.makeText(this.activity, "请先签名。", Toast.LENGTH_SHORT).show();
            return false;
        }
        bitmap= ImageUtil.TrimBitmap(bitmap);
        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/4,bitmap.getHeight()/4,false);
//        signImage=new ImageView(getContext());
//        signImage.setImageBitmap(scaledBitmap);
//        signaturePad.setDrawingCacheEnabled(false);
//        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams
//                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        PointF pointF=imageManager.getSignPoint();
//        int left= (int) pointF.x;
//        int top=(int)pointF.y;
        pickedImage.setVisibility(View.VISIBLE);
        pickedImage.setImageBitmap(scaledBitmap);
//        layoutParams.setMargins(left,top,0,0);
//        frameLayout.addView(signImage,layoutParams);
//        list.add(signImage);

        return true;
    }


    private void fadeOutAndHideImage(final ImageView img) {
        if(img == null) {
            return;
        }

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                removePoint();
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        img.startAnimation(fadeOut);
    }
    private void removePoint(){
        if(pointView!=null){
            frameLayout.removeView(pointView);
            pointView=null;
        }
    }
    private void removeSignImage(){
        frameLayout.removeView(signImage);
        signImage=null;
    }
    public void removeTouchListener(){
        frameLayout.setOnTouchListener(null);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.combin_menu, menu);

        return true;
    }


    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.w("actionmode",item.getItemId()+"");
        switch (item.getItemId()) {
            case R.id.action_combin:
                combinAll();
                mode.finish();

                break;

        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    private void combinAll(){

        if(((TouchImageView) pickedImage).getDrawable() == null) {
            Toast.makeText(this.getActivity(), "请签字或盖章。", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bm2 =((BitmapDrawable) ((TouchImageView) pickedImage).getDrawable()).getBitmap();
        if(bm2 == null) {
            Toast.makeText(this.getActivity(), "请签字或盖章。", Toast.LENGTH_SHORT).show();
            return;
        }

        //Bitmap bm =((BitmapDrawable) ((ImageView) tiffImage).getDrawable()).getBitmap();
        Bitmap bm =TiffImages.getInstance().getSelectBitmap();
        //log.debug("org width: "+bm.getWidth()+"  height:"+bm.getHeight());
        //log.debug("org left: "+tiffImage.getLeft()+"  top:"+tiffImage.getTop());
        //log.debug("org right: "+tiffImage.getRight()+"  bottom:"+tiffImage.getBottom());


        pickedImage.setDrawingCacheEnabled(true);
        Bitmap capture = pickedImage.getDrawingCache();

        int capx = 0;
        int capy = 0;

        int capdstx = 0;
        int capdsty = 0;

        boolean bFlag = false;
        for(int i=0; i<capture.getWidth(); i++) {
            for (int j=0; j<capture.getHeight(); j++) {
                if (capture.getPixel(i,j) != 0 ) {
                    bFlag = true;
                    capx = i;
                    //log.debug("sign x: "+i+"  y:"+j);
                    break;
                }
            }
            if (bFlag) {
                break;
            }
        }

        bFlag = false;

        for(int i=0; i<capture.getHeight(); i++) {
            for (int j=0; j<capture.getWidth(); j++) {
                if (capture.getPixel(j,i) != 0) {
                    bFlag = true;
                    capy = i;
                    //log.debug("sign x: "+j+"  y:"+i);
                    break;
                }
            }
            if (bFlag) {
                break;
            }
        }

        bFlag = false;
        for(int i=capture.getWidth()-1; i>=0; i--) {
            for (int j=capture.getHeight()-1; j>=0; j--) {
                if (capture.getPixel(i,j) != 0) {
                    bFlag = true;
                    capdstx = i;
                    //log.debug("sign x: "+i+"  y:"+j);
                    break;
                }
            }
            if (bFlag) {
                break;
            }
        }


        bFlag = false;
        for(int i=capture.getHeight()-1; i>=0; i--) {
            for (int j=capture.getWidth()-1; j>=0; j--) {
                if (capture.getPixel(j,i) != 0) {
                    bFlag = true;
                    capdsty = i;
                    //log.debug("sign x: "+j+"  y:"+i);
                    break;
                }
            }
            if (bFlag) {
                break;
            }
        }


        //log.debug("capdstx:"+capdstx+" capx:"+capx+"   capdsty:"+capdsty+"  capy:"+capy);
        //切割当前公章
        Bitmap bm3 = Bitmap.createBitmap(capture, capx, capy, (capdstx-capx), (capdsty-capy), null, true);
        Matrix matrix=tiffImage.getImageMatrix();
        float[] n = new float[9];
        matrix.getValues(n);
        Log.w("capture",capture.getWidth()+"  height:"+capture.getHeight()+" "+n[Matrix.MSCALE_X]+","+n[Matrix.MSCALE_Y]);
//        log.debug("sign capture width: "+capture.getWidth()+"  height:"+capture.getHeight());
        float scale=n[Matrix.MSCALE_X];
        float bgX=n[Matrix.MTRANS_X];
        float bgY=n[Matrix.MTRANS_Y];

        int realWidth = bm.getWidth();
        if (realWidth < 1728) {
            realWidth = 1728;
        }

        Bitmap fixWidthBitmap = Bitmap.createBitmap(realWidth, bm.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(fixWidthBitmap);
        float width = 0;
        if(realWidth > 1728) {
        } else {
            width = 1728 - bm.getWidth();
            if (width > 0) {
                width /=2;
            }
        }

        canvas.drawColor(Color.argb(255,255,255,255));
        canvas.drawBitmap(bm, width, 0.0f, null);

        matrix.reset();
        matrix.postScale(1/scale,1/scale);
        PointF pos=pickedImage.getPos();
        matrix.postTranslate((pos.x-bgX)/scale+width,(pos.y-bgY)/scale);
        canvas.drawBitmap(bm3,matrix,null);


        pickedImage.setDrawingCacheEnabled(false);
        
        pickedImage.setVisibility(View.INVISIBLE);

        if(loadingDialog == null) {

            loadingDialog = new ProgressDialog(this.getActivity());
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            loadingDialog.setMax(100);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        }

        String strInfo = "";
        if (strMode.compareTo(ImageManager.MODE_IMAGE) == 0) {
            strInfo = "正在盖章...";
        } else {
            strInfo = "正在签名...";
        }

        loadingDialog.setTitle(strInfo);
        loadingDialog.show();

        new SaveTiffTask().execute(fixWidthBitmap);
        if(strMode.compareTo(ImageManager.MODE_SIGN) == 0){
            removeSignImage();
        }

        tiffImage.setImageBitmap(fixWidthBitmap);

        Matrix matrixSrc=tiffImage.getImageMatrix();
        Bitmap fixWidthBitmapSrc = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Log.v(Constant.TAG, "outImageOrientation 000 width: "+fixWidthBitmapSrc.getWidth()+"  heigth:"+fixWidthBitmapSrc.getHeight());

        Canvas canvasSrc = new Canvas(fixWidthBitmapSrc);
        canvasSrc.drawBitmap(bm, 0.0f, 0.0f, null);

        matrixSrc.reset();
        matrixSrc.postScale(1/scale,1/scale);
        matrixSrc.postTranslate((pos.x-bgX)/scale,(pos.y-bgY)/scale);
        canvasSrc.drawBitmap(bm3,matrixSrc,null);
        Log.v(Constant.TAG, "outImageOrientation 001 width: "+fixWidthBitmapSrc.getWidth()+"  heigth:"+fixWidthBitmapSrc.getHeight());

        new SaveTiffTaskSrc().execute(fixWidthBitmapSrc);

        bm.recycle();
        bm3.recycle();
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
            loadingDialog.setProgress(10);
            boolean success=imageManager.saveImage(params[0],isMultiple,orientation);
            Log.v("save image",imageManager.getTempTiffPath()+"=="+imageManager.getImage()+"="+imageManager.getResultTiffPath());
            if(isMultiple) {
                loadingDialog.setProgress(20);
                success &= TiffReplace.replacePage(imageManager.getTempTiffPath(),
                        imageManager.getTiffImage(),imageManager.getResultTiffPath(),index);
            }

//            File file = new File(imageManager.getTempTiffPath());
//            if (file.exists()) {
//                file.delete();
//            }
//
//            if(imageManager.getTempTiffPathSrc() != null) {
//                file = new File(imageManager.getTempTiffPathSrc());
//                if (file.exists()) {
//                    file.delete();
//                }
//            }


            return success;
        }

        @Override
        protected void onPostExecute(Boolean done) {
            loadingDialog.setProgress(30);
            if(done){
//                imageManager.init();
//                showTiff();
//                pickedImage.setImageBitmap(null);

//                String strInfo = "";
//                if (strMode.compareTo(ImageManager.MODE_IMAGE) == 0) {
//                    strInfo = "完成盖章。";
//                } else {
//                    strInfo = "完成签名。";
//                }
//
//                Toast.makeText(activity, strInfo, Toast.LENGTH_LONG).show();
            } else {
                String strInfo = "";
                if (strMode.compareTo(ImageManager.MODE_IMAGE) == 0) {
                    strInfo = "发生错误，盖章失败。";
                } else {
                    strInfo = "发生错误，签名失败。";
                }

                Toast.makeText(activity, strInfo, Toast.LENGTH_LONG).show();
            }
//            loadingDialog.setProgress(100);
//            loadingDialog.dismiss();
        }
    }

    private class SaveTiffTaskSrc extends AsyncTask<Bitmap,Void,Boolean> {

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
            loadingDialog.setProgress(50);

            boolean success=imageManager.saveImageSrc(params[0],isMultiple,orientation);
            Log.v(Constant.TAG , "save image  "+ imageManager.getTempTiffPathSrc()+"=="+imageManager.getTiffPathSrc()+"="+imageManager.getResultTiffPathSrc());
            params[0].recycle();

            if(isMultiple) {
                loadingDialog.setProgress(60);
                success &= TiffReplace.replacePage(imageManager.getTempTiffPathSrc(),
                        imageManager.getTiffPathSrc(),imageManager.getResultTiffPathSrc(),index);
                Log.v(Constant.TAG , "save image  "+ imageManager.getTempTiffPath()+"= replacePage ok ");
            }


//            File file = new File(imageManager.getTempTiffPath());
//            if (file.exists()) {
//                file.delete();
//            }
//
//            if(imageManager.getTempTiffPathSrc() != null) {
//                file = new File(imageManager.getTempTiffPathSrc());
//                if (file.exists()) {
//                    file.delete();
//                }
//            }
            loadingDialog.setProgress(80);
            if(success) {
                //如果是单张，可能该图片来自相册或拍照，则进行二次操作，才能使照片缩小。
                // 对于多张的tiff文件，说明不是来自照片和拍照，则不用处理。如果处理，或出现异常。
//                if(!isMultiple) {
//                    convert2Tiff(imageManager.getImage());
//                }

                loadingDialog.setProgress(90);
            }

            Log.v(Constant.TAG , "save image  "+ imageManager.getTempTiffPath()+"=  success");
            return success;
        }

        @Override
        protected void onPostExecute(Boolean done) {
            loadingDialog.setProgress(95);
            if(done){
                Log.v(Constant.TAG , "save image  "+ imageManager.getTempTiffPath()+"=  showTiff");
                imageManager.init();
                showTiff();
                Log.v(Constant.TAG , "save image  "+ imageManager.getTempTiffPath()+"=  showTiff 2");
                pickedImage.setImageBitmap(null);

                String strInfo = "";
                if (strMode.compareTo(ImageManager.MODE_IMAGE) == 0) {
                    strInfo = "完成盖章。";
                } else {
                    strInfo = "完成签名。";
                }



                Toast.makeText(activity, strInfo, Toast.LENGTH_LONG).show();
            } else {
                String strInfo = "";
                if (strMode.compareTo(ImageManager.MODE_IMAGE) == 0) {
                    strInfo = "发生错误，盖章失败。";
                } else {
                    strInfo = "发生错误，签名失败。";
                }

                Toast.makeText(activity, strInfo, Toast.LENGTH_LONG).show();
            }
            loadingDialog.setProgress(100);
            loadingDialog.dismiss();
        }
    }


    String convert2Tiff(String strImagePath)
    {
        List<Bitmap> bitmaps = FileUtil.loadImage(strImagePath);
        if(bitmaps == null) {
            Log.v(Constant.TAG, "outImageOrientation width: bitmaps == null");
            return strImagePath;
        }

        //仅有一张,所以为0
        Bitmap bitmap = bitmaps.get(0);
        TiffSaver.SaveOptions options = null;
        options = new TiffSaver.SaveOptions();
        options.compressionMode = TiffSaver.CompressionMode.COMPRESSION_LZW;
        int orientationType = ORIENTATION_TOPLEFT;
        if(TiffImages.getInstance().getOptionsList() != null) {
            orientationType=TiffImages.getInstance().getOptionsList().get(0).getValue();
        }
        options.orientation= TiffSaver.Orientation.fromInt(orientationType);

        String tempPathSrc=strImagePath.substring(0,strImagePath.lastIndexOf("."))+".tif";
        File saved_image_file = new File(tempPathSrc);

        if(bitmap == null) {
            Log.v(Constant.TAG, "outImageOrientation width: bitmap == null");
            return strImagePath;
        }

        TiffSaver.saveBitmap(saved_image_file, bitmap, options, 130);

        return tempPathSrc;
    }
}
