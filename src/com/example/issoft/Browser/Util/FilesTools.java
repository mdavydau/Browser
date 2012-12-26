package com.example.issoft.Browser.Util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.net.Uri;
import android.util.Log;
import com.example.issoft.Browser.Tasks.DownloadFileTask;
import org.apache.http.util.ByteArrayBuffer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.example.issoft.Browser.Util.Constants.*;

public class FilesTools {
    private static final int CURRENT_COUNT_POSITION = 0;

    private static ViewTools viewTools = new ViewTools();
    private static DebugTools debugTools = new DebugTools();

    private static DownloadFileTask downloadFileTask;

    public void saveWebPageFromURL(String directory, String currentURL) {

        if (viewTools.isEditTextFieldIsFill(currentURL)) {
            currentURL = addHTTPPrefixIfNotExist(currentURL);
            String fileName = removeHTTPPrefixIfExistAndAddHTMLPostfix(currentURL);
            fileName = replaceAllIllegalSymbols(fileName);
            saveHTML(fileName, currentURL, directory);

            Log.d(DOWNLOAD_MANAGER, " page " + fileName + " was successfully saved.");
        }
    }

    //for show notification Progress Bar
    public void saveFileFromURL(String directory, Uri uri, ProgressDialog dialog) {
        if (viewTools.isUriNotNull(uri)) {
            String currentURL = uri.toString();
            String fileName = generateOutputFileName(uri);
            downloadFileTask = new DownloadFileTask(dialog, fileName, currentURL, directory);

            dialog.setIndeterminate(false);
            downloadFileTask.execute();

            Log.d(DOWNLOAD_MANAGER, " file save progress.");
        }
    }

    //for hide notification Progress Bar
    public void saveFileFromURL(String directory, Uri uri, Notification notification, NotificationManager notificationManager) {
        if (viewTools.isUriNotNull(uri)) {
            String currentURL = uri.toString();
            String fileName = generateOutputFileName(uri);
            downloadFileTask = new DownloadFileTask(notification, notificationManager, fileName, currentURL, directory);

            downloadFileTask.execute();

            Log.d(DOWNLOAD_MANAGER, " file save progress.");
        }
    }

    public void stopDownloadFileTask() {
        downloadFileTask.cancel(true);
        Log.d(DOWNLOAD_MANAGER, " file download cancel: " + downloadFileTask.isCancelled());
    }

    public String addHTTPPrefixIfNotExist(String page) {
        page = (!page.contains(HTTP_PREFIX)) ? (HTTP_PREFIX + page) : page;

        Log.d(DOWNLOAD_MANAGER, " 'http://' successfully changed.");
        return page;
    }


    public String removeHTTPPrefixIfExistAndAddHTMLPostfix(String page) {
        page = (page.contains(HTTP_PREFIX)) ? page.substring(7, page.length()) : page;
        page = (page.contains(HTML_POSTFIX)) ? page : page + HTML_POSTFIX;
        page = (page.contains(" ")) ? page.replaceAll("\\s", "") : page;

        Log.d(DOWNLOAD_MANAGER, " prefix and postfix successfully changed.");
        return page;
    }

    public String replaceAllIllegalSymbols(String page) {
        return page.replaceAll("/", "_");
    }

    public String generateOutputFileName(Uri uri) {
        return uri.getLastPathSegment();
    }


    public boolean saveHTML(String fileName, String currentURL, String directory) {
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

            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(2048);
            int current = CURRENT_COUNT_POSITION;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            //write stream to file

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());

            //close all stuff
            fos.flush();
            fos.close();

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
