package wirelessfax.phonelink.com.cn.Utls;

import android.graphics.Bitmap;

import org.beyka.tiffbitmapfactory.TiffBitmapFactory;

import java.util.List;

/**
 * Created by slomka.jin on 2016/11/15.
 */

public class TiffImages {
    private static TiffImages tiffImages = null;
    private List<Bitmap> images = null;
    private List<TiffBitmapFactory.ImageOrientation> optionsList = null;
    private int selectIndex = 0;
    public static TiffImages getInstance(){
        if(null==tiffImages)
            tiffImages=new TiffImages();
        return tiffImages;
    }
    public void setOptionsList(List<TiffBitmapFactory.ImageOrientation> optionsList){
        this.optionsList=optionsList;
    }
    public List<TiffBitmapFactory.ImageOrientation> getOptionsList(){
        return optionsList;
    }
    public void setSelectIndex(int index){
        selectIndex=index;
    }

    public int getSelectIndex(){
        return selectIndex;
    }
    public Bitmap getSelectBitmap(){
        if (images == null) {
            return  null;
        }
        return images.get(selectIndex);
    }

    public void setImages(List<Bitmap> bitmaps){
        images=bitmaps;
    }
    public List<Bitmap> getImages(){
        return images;
    }

    public void clear()
    {
        if(images != null) {
            for(Bitmap bm:images) {
                bm.recycle();
            }

            images.clear();
        }

    }
}
