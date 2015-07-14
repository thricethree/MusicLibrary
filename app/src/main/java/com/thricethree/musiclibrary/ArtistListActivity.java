package com.thricethree.musiclibrary;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.thricethree.musiclibrary.model.AlbumInfo;
import com.thricethree.musiclibrary.model.ArtistInfo;

/**
 * Created by thricethree on 6/24/15.
 */
public class ArtistListActivity extends ListActivity {

    private SimpleCursorAdapter cursorAdapter;
    private Cursor cursor;
    private DBHelper db;

    private static final String LOGTAG = ArtistListActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_info_list);
        initialize();

    }

    private void initialize() {
        Intent callingIntent = this.getIntent();

        ArtistInfo artistInfo = (ArtistInfo) callingIntent
                .getSerializableExtra("recordInfo");
        db = new DBHelper(this);

        if (artistInfo == null) {

            displayList();

        } else {
            Log.d(LOGTAG, "RecordInfo Album Name:" + artistInfo.getArtistName());
            for( AlbumInfo album : artistInfo.getArtistAlbums() ){
                db.insert(album);
            }

            displayList();
        }
        db.close();

    }

    // Column Names
//		public static final String KEY_ID               = "_id";
//		public static final String KEY_ALBUM            = "albumTitle";
//		public static final String KEY_ARTIST           = "albumArtist";
//		public static final String KEY_ARTIST_ID        = "artist_id";
//		public static final String KEY_ALBUM_ID         = "album_id";
//		public static final String KEY_COVER_SMALL_URL  = "cover_small";
//		public static final String KEY_COVER_MED_URL    = "cover_med";

    private void displayList() {

        cursor = db.selectAll();

        String[] columns = new String[] { DBHelper.KEY_ALBUM,
                DBHelper.KEY_ARTIST, DBHelper.KEY_COVER_MED_URL };

        int[] to = new int[] { R.id.albumTitleTextEntry, R.id.albumArtistTextView,
                R.id.coverArtImageView};

        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.record_info_entry, cursor, columns, to, 0);

        this.setListAdapter(cursorAdapter);

    }

}