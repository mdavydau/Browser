package com.example.issoft.Browser.Util;

import android.net.Uri;

public class ViewTools {
    public boolean isEditTextFieldIsFill(String currentURLFromEditTextToString) {
        return !currentURLFromEditTextToString.equals("");
    }

    public boolean isUriNotNull(Uri uri) {
        return (uri != null);
    }

    public String deleteSaveHereFromDirectoryPath(String directoryPath) {
        return directoryPath.substring(0, directoryPath.length() - 10);
    }
}
