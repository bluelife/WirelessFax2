package wirelessfax.phonelink.com.cn.pdf;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import wirelessfax.phonelink.com.cn.wirelessfax.R;

/**
 * Created by HiWin10 on 10/19/2017.
 */
@RuntimePermissions
public class PdfConvertActivity extends AppCompatActivity {
    private CompositeDisposable disposables;
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private long startTime;
    int count = 0;

    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.tip_text)
    TextView tip;
    private int total;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_convert_activity);
        ButterKnife.bind(this);
        disposables = new CompositeDisposable();


    }

    @OnClick(R.id.start_btn)
    public void convertPdf() {
        startTime = System.nanoTime();
        PdfConvertActivityPermissionsDispatcher.testWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void test() {
        animationView.playAnimation();
        Observable<Boolean> observable = Observable.fromIterable(getTasks()).map(task -> task.run());
        Disposable disposable = observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(done -> onTaskDone(done), error -> onError(error), () -> onComplete());
        disposables.add(disposable);
        Log.d("oow","start");
    }

    private List<Convert2TiffTask> getTasks() {
        List<Convert2TiffTask> tasks = new ArrayList<>();
        try {
            String pdfPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/test.pdf";
            String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/result.tif";
            File pdfFile = new File(pdfPath);
            File tifFile = new File(targetPath);
            pdfiumCore = new PdfiumCore(this);
            if (tifFile.exists())
                tifFile.delete();
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfDocument = pdfiumCore.newDocument(fileDescriptor);


            total = pdfiumCore.getPageCount(pdfDocument);
            //total = 2;
            for (int i = 0; i < total; i++) {
                tasks.add(new Convert2TiffTask(pdfiumCore, pdfDocument, i, total, targetPath));
            }
            Log.d("oo",total+"");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    private void onTaskDone(boolean done) {
        Log.d("work", done + "");
        count++;
        tip.setText("progress:" + count + ",total " + total);
    }

    private void onError(Throwable error) {
        error.printStackTrace();
        Log.d("work", "error");

        tip.setText("failed work!");
        closePdf();
    }

    private void onComplete() {
        Log.d("work", "complete");
        animationView.cancelAnimation();

        long elapseTime = System.nanoTime() - startTime;
        Log.d("time", TimeUnit.NANOSECONDS.toMillis(elapseTime) + "");
        tip.setText("work completed! elapse time is " + TimeUnit.NANOSECONDS.toMillis(elapseTime) + "ms");
        closePdf();
    }

    private void closePdf() {
        pdfiumCore.closeDocument(pdfDocument);
    }


    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("request permission")
                .setPositiveButton("allow", (dialog, button) -> request.proceed())
                .setNegativeButton("deny", (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showDenied() {
        Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        PdfConvertActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onStop() {
        super.onStop();
        animationView.cancelAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
