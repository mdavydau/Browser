package com.example.issoft.Browser.Tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.example.issoft.Browser.R;
import com.example.issoft.Browser.Util.DebugTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.example.issoft.Browser.Util.Constants.*;

public class DownloadFileTask extends AsyncTask<String, Integer, Boolean> {

    private static DebugTools debugTools = new DebugTools();

    private ProgressDialog dialog;
    private Notification notification;
    private NotificationManager notificationManager;
    private String fileName = "";
    private String currentURL = "";
    private String directory = "";

    public DownloadFileTask(ProgressDialog dialog, String fileName, String currentURL, String directory) {
        this.dialog = dialog;
        this.fileName = fileName;
        this.currentURL = currentURL;
        this.directory = directory;
    }

    public DownloadFileTask(Notification notification, NotificationManager notificationManager, String fileName, String currentURL, String directory) {
        this.notification = notification;
        this.notificationManager = notificationManager;
        this.fileName = fileName;
        this.currentURL = currentURL;
        this.directory = directory;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.d(DOWNLOAD_FILE, " download start.");
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        saveFile(fileName, currentURL, directory);



        Log.d(DOWNLOAD_FILE, " file " + fileName + " download in process.");
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        Log.d(DOWNLOAD_FILE, " file " + fileName + " download finished.");
    }

    @Override
    protected void onCancelled() {
        super.cancel(true);
    }

    public boolean saveFile(String fileName, String currentURL, String directory) {
        boolean saveFlag = false;

        File file = new File(directory, fileName);

        URL url;

        URLConnection ucon;
        try {
            //create and open connection
            url = new URL(currentURL);
            ucon = url.openConnection();

            long startTime = debugTools.writeStartDebugInformation(url, fileName);

            //get stream from connection and send to bytes array
            InputStream is = ucon.getInputStream();

            FileOutputStream outputStream = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;

            if (dialog != null) {
                while ((len = is.read(buf)) > 0) {
                    if (isCancelled()) break;

                    outputStream.write(buf, 0, len);
                    dialog.incrementProgressBy(buf.length);
                }
            } else if (notification != null) {
                while ((len = is.read(buf)) > 0) {
                    if (isCancelled()) break;

                    outputStream.write(buf, 0, len);

                    notification.contentView.setTextViewText(R.id.status_text, String.valueOf(outputStream.getChannel().size()) + "/" + String.valueOf(CONTENT_LENGTH_GLOBAL));
                    notification.contentView.setProgressBar(R.id.status_progress, (int) CONTENT_LENGTH_GLOBAL, (int) outputStream.getChannel().size(), false);
                    notificationManager.notify(42, notification);
                }
            }
            if (notification != null) notificationManager.cancel(42);
            outputStream.close();
            dialog.dismiss();

            saveFlag = true;
            debugTools.writeEndDebugInformation(startTime);
        } catch (MalformedURLException e) {
            Log.e(DOWNLOAD_MANAGER + " malformed ", e.toString());
        } catch (IOException e) {
            Log.e(DOWNLOAD_MANAGER + " IO ", e.toString());
        }
        return saveFlag;
    }
}
