package org.elitanaroda.domcikuvzpevnik;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.tkhosravi.chordpro.core.ChordproRoot;
import org.tkhosravi.chordpro.core.Element;
import org.tkhosravi.chordpro.core.InlineElement;
import org.tkhosravi.chordpro.core.InlineElementType;
import org.tkhosravi.chordpro.parser.ChordproParser;
import org.tkhosravi.chordpro.parser.ChordproTokenizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * This activity presents the user with a song in the ChordPro format, which it also downloads beforehand
 */
public class ChordProActivity extends AppCompatActivity {
    private static final String TAG = "ChordProActivity";
    private TextView mChordProContentTV;
    private Song mSong;
    private ScrollView mChordProSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord_pro);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        MainActivity.setToolbarText(this, mToolbar);
        setSupportActionBar(mToolbar);

        mChordProContentTV = (TextView) findViewById(R.id.chordProContent);
        mChordProSV = (ScrollView) findViewById(R.id.chordProSV);
        Intent intent = getIntent();
        mSong = intent.getParcelableExtra(PDFActivity.SONG_KEY);
        String mFilesRootUrl = intent.getStringExtra(PDFActivity.FILES_ROOT_KEY);
        new LoadChordPro().execute(mFilesRootUrl + mSong.getChordProFileName());
    }

    /**
     * Load a txt file from web to a variable
     *
     * @param txtUrl URL of the file
     * @return Null on fail, otherwise returns the contents of the file
     */
    private String getOnline(String txtUrl) {
        URLConnection feedUrl;
        try {
            feedUrl = new URL(txtUrl).openConnection();
            InputStream is = feedUrl.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            is.close();

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Generates an HTML file with the correct formatting
     *
     * @param chordProText A String in the chordpro format
     * @return An HTML to show to the user
     */
    private String generateHtml(String chordProText) {
        ChordproTokenizer tokenizer = new ChordproTokenizer(chordProText.toCharArray());
        ChordproParser parser = new ChordproParser(tokenizer);
        ChordproRoot root = parser.parseTokenizer();
        List<Element> elementList = root.getElements();
        StringBuilder stringBuilder = new StringBuilder();
        for (Element element : elementList) {
            if (element instanceof InlineElement) {
                InlineElement inlineElement = (InlineElement) element;
                if (inlineElement.getType() == InlineElementType.CHORDPRO_CHORD) {
                    stringBuilder.append("<sup><strong>");
                    stringBuilder.append(inlineElement.getContent());
                    stringBuilder.append("</strong></sup>");
                } else if (inlineElement.getType() == InlineElementType.CHORDPRO_LYRIC) {
                    stringBuilder.append(inlineElement.getContent());
                }
            }
        }
        return stringBuilder.toString().replaceAll("\\n", "<br>");
    }

    /**
     * Loads a chordpro file located at the url provided
     */
    private class LoadChordPro extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            return getOnline(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                s = generateHtml(s);
                mChordProContentTV.setText(Utils.fromHtml(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}