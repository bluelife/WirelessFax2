package wirelessfax.phonelink.com.cn.pdf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.beyka.tiffbitmapfactory.TiffSaver;

import java.io.File;
import java.io.FileOutputStream;

import wirelessfax.phonelink.com.cn.Utls.TiffImages;

import static com.lowagie.text.pdf.codec.TIFFConstants.ORIENTATION_TOPLEFT;

/**
 * Created by HiWin10 on 10/19/2017.
 */

public class Convert2TiffTask {

    private PdfiumCore pdfiumCore;
    private PdfDocument document;
    private int index;
    private int total;
    private String path;

    public Convert2TiffTask(PdfiumCore core, PdfDocument document,int index,int total,String path) {
        this.pdfiumCore = core;
        this.document = document;
        this.index=index;
        this.total=total;
        this.path=path;
    }

    public boolean run(){
        boolean done=false;
        try {


            pdfiumCore.openPage(document, index);

            int width = pdfiumCore.getPageWidthPoint(document, index)*2;
            int height = pdfiumCore.getPageHeightPoint(document, index)*2;

            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.RGB_565);

            pdfiumCore.renderPageBitmap(document, bitmap, index, 0, 0,
                    width, height);
            Log.d("rr",bitmap+" "+width);
            int orientationType = ORIENTATION_TOPLEFT;
            SaveImage(bitmap,index);
            Bitmap bitmap2 = BitmapFactory. decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/image.png");
            TiffSaver.Orientation orientation = TiffSaver.Orientation.fromInt(orientationType);
            TiffSaver.SaveOptions options = new TiffSaver.SaveOptions();
            options.compressionMode = TiffSaver.CompressionMode.COMPRESSION_CCITTFAX3;
            options.orientation = orientation;
            Log.d("rr2",bitmap+" "+width);
            done = TiffSaver.appendImage(path, bitmap2, options);
            Log.d("ooooo","done");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return done;
    }
    private void SaveImage(Bitmap finalBitmap,int index) {

        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        String fname = "Image-"+ index +".png";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
