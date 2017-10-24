package wirelessfax.phonelink.com.cn.Utls;

import android.graphics.Bitmap;
import android.util.Log;

import org.beyka.tiffbitmapfactory.TiffBitmapFactory;
import org.beyka.tiffbitmapfactory.exceptions.NoSuchFileException;
import org.beyka.tiffbitmapfactory.exceptions.NotEnoughtMemoryException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by slomka.jin on 2016/10/31.
 */

public class FileUtil {
    private static int reqHeight=1200;
    private static int reqWidth=1500;

    public static List<Bitmap> loadTiff(String path) throws NoSuchFileException,NotEnoughtMemoryException {
        List<Bitmap> bitmaps=new ArrayList<>();
        List<TiffBitmapFactory.ImageOrientation> orientations=new ArrayList<>();
        TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
        options.inJustDecodeBounds = true;
        File file = new File(path);

        TiffBitmapFactory.decodeFile(file.getAbsoluteFile(), options);

        int dirCount = options.outDirectoryCount;
//Read and process all images in file

        options.inDirectoryNumber = 0;
        TiffBitmapFactory.decodeFile(file, options);
        for (int i = 0; i < dirCount; i++) {
            options.inDirectoryNumber = i;
            options.inAvailableMemory = 150000000; // bytes
            TiffBitmapFactory.decodeFile(file, options);
            int curDir = options.outCurDirectoryNumber;
            int width = options.outWidth;
            int height = options.outHeight;
            Log.w("image", path + " " + options.inPreferredConfig);


            //Change sample size if width or height bigger than required width or height
            int inSampleSize = 1;
            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            Log.v(Constant.TAG, "outImageOrientation inSampleSize: "+inSampleSize);
            // Specify the amount of memory available for the final bitmap and temporary storage.
            options.inAvailableMemory = 150000000; // bytes
            if(options.outImageOrientation == null) {
                Log.v(Constant.TAG, "outImageOrientation == null");
            } else {
                Log.v(Constant.TAG, "outImageOrientation != null");
            }

            orientations.add(options.outImageOrientation);
            Bitmap bmp = TiffBitmapFactory.decodeFile(file, options);
            if(bmp == null) {
                Log.v(Constant.TAG, "outImageOrientation bmp== null");
            } else {
                Log.v(Constant.TAG, "outImageOrientation bmp != null");
            }
            bitmaps.add(bmp);

        }
        TiffImages.getInstance().setOptionsList(orientations);
        return bitmaps;
    }
    public static List<Bitmap> loadImage(String path){
        if (path == null) {
            return null;
        }

        if(path.substring(path.lastIndexOf(".")+1).equalsIgnoreCase("tif")){
            List<Bitmap> list = null;
            try {
                list = FileUtil.loadTiff(path);
            } catch (Exception e) {
                e.printStackTrace();
                //如果打开失败，尝试用bmp打开。
                Log.v(Constant.TAG, "outImageOrientation exception..0.");

                List<Bitmap> bitmaps = null;
                try {
                    Bitmap bitmap = BitmapUtls.getBitmapFromFile(path, 1728, 1280);
                    if(bitmap != null) {
                        bitmaps=new ArrayList<>(1);
                        bitmaps.add(bitmap);
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                    Log.v(Constant.TAG, "outImageOrientation exception..1.");
                    return  null;
                }

                return bitmaps;
            }
            return list ;
        }
        else {
            List<Bitmap> bitmaps = null;

            Bitmap bitmap = BitmapUtls.getBitmapFromFile(path, 1728, 1280);
            if(bitmap != null) {
                Log.v(Constant.TAG, "after process width:"+bitmap.getWidth()+"  height: "+bitmap.getHeight());
                bitmaps = new ArrayList<>(1);

                bitmaps.add(bitmap);
            }

            return bitmaps;
        }
    }


    public static List<Bitmap> loadImage(String path, TiffBitmapFactory.Options options){
        if(path.substring(path.lastIndexOf(".")+1).equalsIgnoreCase("tif")){
            List<Bitmap> list = null;
            try {
                list = FileUtil.loadTiff(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list ;
        }
        else {
            List<Bitmap> bitmaps=new ArrayList<>(1);
            bitmaps.add(TiffBitmapFactory.decodeFile(new File(path), options));
            return bitmaps;
        }
    }
}