package org.elitanaroda.domcikuvzpevnik;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import org.tkhosravi.chordpro.core.ChordproRoot;
import org.tkhosravi.chordpro.core.Element;
import org.tkhosravi.chordpro.core.InlineElement;
import org.tkhosravi.chordpro.core.InlineElementType;
import org.tkhosravi.chordpro.parser.ChordproParser;
import org.tkhosravi.chordpro.parser.ChordproTokenizer;

import java.util.List;

public class ChordProActivity extends AppCompatActivity {

    private static final String TAG = "ChordProActivity";
    private TextView mContentTextView;

    //Return the number of spaces as a string
    private static String spaces(int numberOfSpaces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfSpaces; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord_pro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContentTextView = (TextView) findViewById(R.id.content);

        String song = "Můj [F#mi]nový Mac,\n" +
                "zas včera klek´,\n" +
                "[E]snad je to tím že byl úplněk.\n" +
                "[F#mi]Nemohu se na něj [H]zlobit\n" +
                "vloni [E]velký hit letos [E7]trilobit.\n" +
                "\n" +
                "Stejně tak [F#mi]mé kdysi zbrusu nové PC, \n" +
                "[H]dalo mi vale asi po měsíci,\n" +
                "[E]nemohu se na něj [H]zlobit, \n" +
                "[E]včera nový komp dnes [E7] mastodont.\n" +
                "\n" +
                "Tak se [F#mi]ptám[H], kde že [E]loňské kompy [F#]jsou?\n" +
                "Bloudí [F#mi]dál[H]dvojkovou [E]sousta[F#]vou!\n" +
                "\n" +
                "[F#mi]Kilo, mega, giga, tera,\n" +
                "[E]marně vzpomínám co bylo včera,\n" +
                "[F#mi]snad diskety, [H]v paměti ještě [E]mám, \n" +
                "vaše [E7]stříbrné rety.\n" +
                "\n" +
                "[F#mi]Kilo, mega, giga, tera, \n" +
                "[E]marně vzpomínám co bylo včera,\n" +
                "sbohem můj drahý [H]kompaktní disku\n" +
                "[E]vždycky tu u mě budeš plnou[E7] mít misku.\n" +
                "\n" +
                "Tak se [F#mi]ptám[H], kde že [E]loňské kompy [F#]jsou?\n" +
                "Bloudí [F#mi]dál[H]dvojkovou [E]sousta[F#]vou!\n" +
                "\n" +
                "[F#mi]Procesory usedavě roní,\n" +
                "[E]malinkaté křemíkové slzy.\n" +
                "[F#mi]Nikdo z nich [H]není [E]stár,\n" +
                "tak [E7]brzy jako oni.\n" +
                "\n" +
                "[F#mi]Procesory usedavě roní,\n" +
                "[E]malinkaté křemíkové slzy,\n" +
                "[F#mi]neboť jsou menší a [H]menší a menší a [E]menší\n" +
                "a [E7]tenčí než loni.\n" +
                "\n" +
                "Tak se [F#mi]ptám[H], kde že [E]loňské kompy [F#]jsou?\n" +
                "Bloudí [F#mi]dál[H]dvojkovou [E]sousta[F#]vou!\n" +
                "\n" +
                "[F#mi]Můj nový Mac,\n" +
                "včera mi řek´: \n" +
                "[E]\"Sorry, za ten výpadek.\"\n" +
                "[F#mi]Tak jsem mu na to [H]řek´: \n" +
                "\"Tvůj [E]operační systém pozná [E7]středověk!\"\n" +
                "\n" +
                "Tak se ptám ...\n" +
                "\n";
        ChordproTokenizer tokenizer = new ChordproTokenizer(song.toCharArray());
        ChordproParser parser = new ChordproParser(tokenizer);
        ChordproRoot root = parser.parseTokenizer();
        List<Element> elementList = root.getElements();
        /*String chords = "";
        String lyrics = "";
        for (Element element : elementList) {
            InlineElement inlineElement = (InlineElement) element;
            if (inlineElement.getType() == InlineElementType.CHORDPRO_CHORD) {
                chords += inlineElement.getContent();
            } else if (inlineElement.getType() == InlineElementType.CHORDPRO_LYRIC) {
                lyrics += inlineElement.getContent();
                chords += spaces(inlineElement.getContent().length() - 1);
                if (inlineElement.getContent().contains("\n")) {
                    chords += "\n";
                }
            }
        }
        String chordLines[] = chords.split("\\r?\\n");
        String lyricsLines[] = lyrics.split("\\r?\\n");

        String completeSong = "";
        for (int i = 0; i < chordLines.length; i++) {
            completeSong += chordLines[i];
            completeSong += "\n";
            completeSong += lyricsLines[i];
            completeSong += "\n";
        }

        completeSong = completeSong.replaceAll("/\n[ \t]*\n/is", "\n");
        Log.e(TAG, completeSong);*/
        StringBuilder stringBuilder = new StringBuilder();
        for (Element element : elementList) {
            if (element instanceof InlineElement) {
                InlineElement inlineElement = (InlineElement) element;
                if (inlineElement.getType() == InlineElementType.CHORDPRO_CHORD) {
                    stringBuilder.append("<sup>");
                    stringBuilder.append(inlineElement.getContent());
                    stringBuilder.append("</sup>");
                } else if (inlineElement.getType() == InlineElementType.CHORDPRO_LYRIC) {
                    stringBuilder.append(inlineElement.getContent());
                }
            }
        }
        mContentTextView.setText(Html.fromHtml(stringBuilder.toString().replaceAll("\n", "<br/>")));
    }

}
