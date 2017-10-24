package wirelessfax.phonelink.com.cn.mvp.presenter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

import wirelessfax.phonelink.com.cn.mvp.view.IHeaderView;
import wirelessfax.phonelink.com.cn.wirelessfax.R;

/**
 * Created by ${hcc} on 2016/10/17.
 */
public class HeaderPresenter {
    public static final String IMAGE_FILE_NAME = "iMon.jpg";

    public static final int IMAGE_REQUEST_CODE = 0;
    public static final int SELECT_PIC_KITKAT = 3;
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int RESULT_REQUEST_CODE = 2;

    private IHeaderView iHeaderView;
    private Context context;

    public HeaderPresenter(IHeaderView iHeaderView, Context context) {
        this.iHeaderView = iHeaderView;
        this.context = context;
    }

    /*设置并显示Dialog*/
    public void showHeadDialog() {
        View view = LayoutInflater.from(context).inflate(R.layout.head_dialog, null);
        final Dialog dialog = new Dialog(context, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.anim_style);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.x = 0;
        layoutParams.y = ((Activity) context).getWindowManager().getDefaultDisplay().getHeight();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT; //保证dialog窗体可以水平铺满
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        dialog.onWindowAttributesChanged(layoutParams);//设置dialog的摆放位置
        dialog.setCanceledOnTouchOutside(true);//设置点击dialog以为的区域dialog消失
        dialog.show();

        /*相册选择*/
        dialog.findViewById(R.id.tv_select_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFromAlbum();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

        /*拍照*/
        dialog.findViewById(R.id.tv_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void selectFromAlbum() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            ((Activity) context).startActivityForResult(intent,SELECT_PIC_KITKAT);
        } else {
            ((Activity) context).startActivityForResult(intent,IMAGE_REQUEST_CODE);
        }
    }

    private void takePhoto() {
        Intent intentFromCapture = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (hasSdcard()) {

            intentFromCapture.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(Environment
                            .getExternalStorageDirectory(),
                            IMAGE_FILE_NAME)));
        }
        ((Activity) context).startActivityForResult(intentFromCapture,
                CAMERA_REQUEST_CODE);
    }

    public static boolean hasSdcard()
    {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        return false;
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 4);
        }
    }

    public void setView(Intent date) {
        Bundle bundle = date.getExtras();
        Bitmap bitmap = bundle.getParcelable("data");
        iHeaderView.setHeaderBitmap(bitmap);
    }
}
