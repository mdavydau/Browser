package com.example.issoft.Browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import static com.example.issoft.Browser.Util.Constants.*;

/**
 * User: nikitadavydov
 * Date: 11/28/12
 */
public class AlertExplorerActivity extends Activity {

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<String>();

    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;

    private Item[] fileList;
    private File path = new File(ROOT_DIRECTORY);
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;
    private Intent resultData;

    ListAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        resultData = getIntent();
        if (resultData.getStringExtra(ACTION_NAME).equals("file_manager")) path = new File(ROOT_DATA);
        loadFileList();

        showDialog(DIALOG_LOAD_FILE);
        Log.d(FILE_EXPLORER, path.getAbsolutePath());
    }

    private void loadFileList() {
        try {
            /*check here: can i read and write to path
            * yes, i can make isRead and isWrite*/
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(FILE_EXPLORER, " unable to write on the sd card ");
        }

        try {
            /*Checks whether path exists*/
            if (path.exists()) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        File sel = new File(dir, filename);
                        /*Filters based on whether the file is hidden or not*/
                        return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
                    }
                };

                String[] fList = path.list(filter);
                int j = 0;

                /*just add "Save here!" on top of the list if need save something*/
                if (resultData.getStringExtra(ACTION_NAME).equals("save")) {
                    fileList = new Item[fList.length + 1];
                    fileList[0] = new Item("Save here!", R.drawable.save);
                    j += 1;
                } else if (resultData.getStringExtra(ACTION_NAME).equals("file_manager")) {
                    if (fList == null) fileList = new Item[1];
                    else fileList = new Item[fList.length];
                } else {
                    fileList = new Item[fList.length];
                }
                if (fList != null) {
                    for (String aFList : fList) {
                        fileList[j] = new Item(aFList, R.drawable.file_icon);
                    /*Convert into file path*/
                        File sel = new File(path, aFList);
                    /*Set drawables*/
                        if (sel.isDirectory()) {
                            fileList[j].icon = R.drawable.directory_icon;
                            Log.d("DIRECTORY", fileList[j].file);
                        } else {
                            Log.d("FILE", fileList[j].file);
                        }
                        j += 1;
                    }
                } else {
                    /*create list only with one element: "UP"*/
                    Item temp[] = new Item[1];
                    temp[0] = new Item("Up", R.drawable.directory_up);
                    fileList = temp;
                    /*set, because we are don't need check after that*/
                    firstLvl = true;
                }

                if (!firstLvl) {
                    Item temp[] = new Item[fileList.length + 1];
                    System.arraycopy(fileList, 0, temp, 1, fileList.length);
                    temp[0] = new Item("Up", R.drawable.directory_up);
                    fileList = temp;
                }

            } else {
                Log.e(FILE_EXPLORER, "path does not exist");
            }

            adapter = new ArrayAdapter<Item>(this, android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    /*creates view*/
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);

                    /*put the image on the text view*/
                    textView.setCompoundDrawablesWithIntrinsicBounds(fileList[position].icon, 0, 0, 0);

                    /*add margin between image and text (support various screen densities)*/
                    int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                    textView.setCompoundDrawablePadding(dp5);

                    return view;
                }
            };
        } catch (NullPointerException e) {
            /*TODO: if files not exist need show UP button*/
            Toast.makeText(getApplicationContext(), "There is no files in this directory.", Toast.LENGTH_LONG).show();
            Log.e(FILE_EXPLORER, "exception:" + e + AlertExplorerActivity.class);
        }
    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder = new Builder(this);

        if (fileList == null) {
            Log.e(FILE_EXPLORER, "No files loaded");
            dialog = builder.create();
            return dialog;
        }

        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenFile = fileList[which].file;
                        File sel = new File(path + "/" + chosenFile);
                        if (sel.isDirectory()) {
                            firstLvl = false;

                            // Adds chosen directory to list
                            str.add(chosenFile);
                            fileList = null;
                            path = new File(sel + "");

                            loadFileList();

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(FILE_EXPLORER, path.getAbsolutePath());
                        }

                        /*Checks if 'up' was clicked*/
                        else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {
                            /*present directory removed from list*/
                            String s = str.remove(str.size() - 1);

                            /*path modified to exclude present directory*/
                            path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));
                            fileList = null;

                            /*if there are no more directories in the list, then
                            its the first level*/
                            if (str.isEmpty()) {
                                firstLvl = true;
                            }
                            loadFileList();

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(FILE_EXPLORER, path.getAbsolutePath());

                        }
                        /*TODO: Open file
                        * File or Save picked
                        * here i make activity is file open*/
                        else {
                            Intent resultData = new Intent();
                            resultData.putExtra(FILE_URL, sel.getAbsolutePath());
                            setResult(Activity.RESULT_OK, resultData);
                            finish();
                            Log.d(FILE_EXPLORER, " you are picked file: " + chosenFile);
                        }
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }
}