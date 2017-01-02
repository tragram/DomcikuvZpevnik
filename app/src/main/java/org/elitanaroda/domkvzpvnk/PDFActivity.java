package org.elitanaroda.domkvzpvnk;

import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class PDFActivity extends AppCompatActivity implements OnLoadCompleteListener {
    public static String SAMPLE_FILE;
    private static String TAG = "PDFViewActivity";
    boolean doScroll = false;
    private PDFView pdfView;
    private String pdfFileName;
    private int pageNumber = 0;
    private Button mScrollButton;
    private Handler mScrollHandler;
    private Runnable ScrollRunnable = new Runnable() {
        @Override
        public void run() {
            //pdfView.setPositionOffset(pdfView.getCurrentYOffset() + 0.05f);
            pdfView.moveRelativeTo(0, -2);
            mScrollHandler.postDelayed(this, 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);
        pdfView = (PDFView) findViewById(R.id.pdfView);

        mScrollButton = (Button) findViewById(R.id.scrollButton);
        mScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doScroll) {
                    doScroll = true;
                    mScrollHandler = new Handler();
                    mScrollHandler.post(ScrollRunnable);

                } else {
                    doScroll = false;
                    mScrollHandler.removeCallbacks(ScrollRunnable);
                }
            }
        });

        Intent intent = getIntent();
        SAMPLE_FILE = intent.getStringExtra("nazevPisne");

        displayFromAsset(SAMPLE_FILE);
    }

    //Načtení konkrétního souboru
    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (doScroll)
            mScrollHandler.post(ScrollRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScrollHandler.removeCallbacks(ScrollRunnable);
    }
}
