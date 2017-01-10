package org.elitanaroda.domkvzpvnk;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * Created by Dominik on 1/9/2017.
 */
/*
public class DownloadHelper extends AsyncTask<String, String, String> {
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    // File url to download
    private static String file_url = "http://www.qwikisoft.com/demo/ashade/20001.kml";

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }


    /**
     * Before starting background thread Show Progress Bar Dialog
     * *//*
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showDialog(progress_bar_type);
    }

    /**
     * Downloading file in background thread
     * *//*
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(Environment
                    .getExternalStorageDirectory().toString()
                    + "/2011.kml");

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    /**
     * Updating progress bar
     * *//*
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **//*
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        dismissDialog(progress_bar_type);

    }

}
}
*/