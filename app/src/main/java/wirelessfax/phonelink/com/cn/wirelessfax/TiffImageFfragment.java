package wirelessfax.phonelink.com.cn.wirelessfax;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import wirelessfax.phonelink.com.cn.Utls.TiffImages;

/**
 * Created by slomka.jin on 2016/11/15.
 */

public class TiffImageFfragment extends Fragment {
    @BindView(R.id.frag_tiff_imageview)
    ImageView tiffImage;
    private int index;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.frag_tiff_image,container,false);
        ButterKnife.bind(this,v);
        tiffImage.setImageBitmap(TiffImages.getInstance().getImages().get(index));
        return v;
    }

    public static TiffImageFfragment getInstance(){
        return new TiffImageFfragment();
    }
    public void setIndex(int pos){
        index=pos;
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
}
