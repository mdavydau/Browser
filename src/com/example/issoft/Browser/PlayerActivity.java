package com.example.issoft.Browser;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.*;
import com.example.issoft.Browser.Util.Constants;
import com.example.issoft.Browser.Util.PlayerTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * User: nikitadavydov
 * Date: 12/17/12
 */
public class PlayerActivity extends Activity {
    private Button buttonPlayStop;

    private SeekBar seekBar;
    private ListView listView;

    private Cursor musicListInternalMemoryCursor;
    private Cursor musicListSDCardCursor;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static List<String> trackList = new LinkedList<String>();
    private static int currentPosition = -1;
    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_widget);
        initViews();
    }

    // This method set the setOnClickListener and method for it (buttonClick())
    private void initViews() {
        buttonPlayStop = (Button) findViewById(R.id.ButtonPlayStop);
        buttonPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClick();
            }
        });

        Button buttonGoBack = (Button) findViewById(R.id.prev_button);
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPreviousSongFromTrackList();
            }
        });

        Button buttonGoNext = (Button) findViewById(R.id.next_button);
        buttonGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNextSongFromTrackList();
            }
        });

        Button buttonCreateNewPlayList = (Button) findViewById(R.id.newPlayList);
        buttonCreateNewPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: need implement!
                createEmptyPlayList();
            }
        });

        Button scan = (Button) findViewById(R.id.scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanPlayList();
            }
        });

        seekBar = (SeekBar) findViewById(R.id.SeekBar01);
        seekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                seekChange(v);
                return false;
            }
        });

        //TODO: Hardcode here
        setDataSourceAndPrepare(null);

        listView = (ListView) findViewById(R.id.listView);
        scanPlayList();

        // listening to single list item on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                String path = ((TextView) view).getText().toString();
                setDataSourceAndPrepare(path);
                addSongToTrackList(path);
                clearAllFromCurrentToEnd();
            }
        });
    }

    private void createEmptyPlayList() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Playlists.NAME, "New playlist");
        /*Create in external memory*/
        Uri uri = getContentResolver().insert(MediaStore.Audio.Playlists.getContentUri("external"), cv);
    }

    /*if we are click on track - need delete elements
    Remove elements from current to end of the track list*/
    private void clearAllFromCurrentToEnd() {
        while (trackList.get(currentPosition) != null) {
            try {
                trackList.remove(currentPosition + 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(this, "List is empty", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    private void addSongToTrackList(String path) {
        if (path != null) {
            currentPosition++;
            trackList.add(currentPosition, path);

            if (currentPosition == 19) {
                /*if 20 elements in track list we are rewrite all element without first
                * we are cut the head and move the tail + one null element*/
                trackList.addAll(0, trackList.subList(1, 20));
                /*set previous position, because we are have increment when add*/
                currentPosition = 18;
                Toast.makeText(this, "List is full", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getPreviousSongFromTrackList() {
        String track = trackList.get(currentPosition);
        if (currentPosition - 1 >= 0) {
            track = trackList.get(currentPosition - 1);
            setDataSourceAndPrepare(track);
            currentPosition -= 1;
        }
        return track;
    }

    private String getNextSongFromTrackList() {
        String track = trackList.get(currentPosition);
        if (currentPosition + 1 < trackList.size() && trackList.get(currentPosition) != null) {
            track = trackList.get(currentPosition + 1);
            setDataSourceAndPrepare(track);
            currentPosition += 1;
        }
        return track;
    }

    private void setDataSourceAndPrepare(String path) {
        //TODO: Hardcode here change to " "
        if (path == null) path = "/data/data/com.example.issoft.Browser/files/rihanna_-_diamonds_(zaycev.net).mp3";
        //end
        try {
            //i don't really know about mediaPlayer.reset() but i think need to do this
            mediaPlayer.reset();
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);

            if (file.exists()) {
                mediaPlayer.setDataSource(fileInputStream.getFD());
                mediaPlayer.prepare();

                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
                startPlayProgressUpdater();

                Toast.makeText(this, path, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "File not exist", Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            Log.e(this.getClass() + " FileNotFoundException ", e.toString());
        } catch (IOException e) {
            Log.e(this.getClass() + " IOException ", e.toString());
        }
    }

    public void startPlayProgressUpdater() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        } else {
            mediaPlayer.pause();
            buttonPlayStop.setText(getString(R.string.play_str));
        }
    }

    // This is event handler thumb moving event
    private void seekChange(View v) {
        /*i don't need isPlaying here*/
        SeekBar sb = (SeekBar) v;
        mediaPlayer.seekTo(sb.getProgress());
    }

    // This is event handler for buttonClick event
    private void buttonClick() {
        if (buttonPlayStop.getText() == getString(R.string.play_str)) {
            buttonPlayStop.setText(getString(R.string.pause_str));
            try {
                mediaPlayer.start();
                startPlayProgressUpdater();
            } catch (IllegalStateException e) {
                mediaPlayer.pause();
            }
        } else {
            buttonPlayStop.setText(getString(R.string.play_str));
            mediaPlayer.pause();
        }
    }

    private void scanPlayList() {
        /*First parameter - Context
        * Second parameter - Layout for the row
        * Third parameter - ID of the TextView to which the data is written
        * Forth - the Array of data*/
        createPlayListWithAllMusicOnDevice();
        Vector<String> tracks = copyTrackToList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, tracks);
        listView.setAdapter(adapter);
    }

    private void createPlayListWithAllMusicOnDevice() {
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";

        musicListInternalMemoryCursor = PlayerTools.myquery(
                this,
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                projection, selection, null, null, 0);

        musicListSDCardCursor = PlayerTools.myquery(
                this,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null, null, 0);
    }

    private Vector<String> copyTrackToList() {
        Vector<String> paths = new Vector<String>();
        if (musicListSDCardCursor != null) {
            for (int i = 0; i < musicListSDCardCursor.getCount(); i++) {
                musicListSDCardCursor.moveToPosition(i);
                String p = musicListSDCardCursor.getString(1);
                if (p.endsWith("mp3"))
                    paths.addElement(p);
            }
            musicListSDCardCursor.close();
        }

        if (musicListInternalMemoryCursor != null) {
            for (int i = 0; i < musicListInternalMemoryCursor.getCount(); i++) {
                musicListInternalMemoryCursor.moveToPosition(i);
                String p = musicListInternalMemoryCursor.getString(1);
                if (p.endsWith("mp3"))
                    paths.addElement(p);
            }
            musicListInternalMemoryCursor.close();
        }

        if (paths.isEmpty()) {
            //TODO: Hardcode
            paths.addElement(Constants.DIAMONDS);
            paths.addElement(Constants.SKYFALL);
            //end
            Toast.makeText(this, "no media found", Toast.LENGTH_LONG).show();
            return paths;
        }

        return paths;
    }

    private void randomizeElements(Vector<String> paths) {
        Random r = new Random();
        int pos = r.nextInt(paths.size() - 1);
        if (paths.size() == 1) pos = 0;

        String url = paths.elementAt(pos);

        if (url.length() == 0) return;

        Toast.makeText(this, url, Toast.LENGTH_LONG).show();

        try {
            setDataSourceAndPrepare(url);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}