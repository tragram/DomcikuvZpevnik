package org.elitanaroda.domkvzpvnk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class PDFActivity extends AppCompatActivity implements OnLoadCompleteListener {
    public static String SAMPLE_FILE;
    private static String TAG = "PDFViewActivity";
    private PDFView pdfView;
    private String pdfFileName;
    private int pageNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);
        pdfView = (PDFView) findViewById(R.id.pdfView);

        Intent intent=getIntent();
        SAMPLE_FILE=intent.getStringExtra("nazevPisne");

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
}
