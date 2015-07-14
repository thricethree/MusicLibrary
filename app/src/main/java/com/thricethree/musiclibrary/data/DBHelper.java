package com.thricethree.musiclibrary.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.thricethree.musiclibrary.model.AlbumInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thricethree on 6/24/15.
 */
public class DBHelper {

    private static final String LOGTAG = DBHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "albumDatabase.db";
    public static final String DATABASE_TABLE = "artistAlbums";
    private static final int DATABASE_VERSION = 1;

    // Column Names
    public static final String KEY_ID               = "_id";
    public static final String KEY_ALBUM            = "albumTitle";
    public static final String KEY_ARTIST           = "albumArtist";
    public static final String KEY_ARTIST_ID        = "artist_id";
    public static final String KEY_ALBUM_ID         = "album_id";
    public static final String KEY_COVER_SMALL_URL  = "cover_small";
    public static final String KEY_COVER_MED_URL    = "cover_med";

    // Column Indices
    public static final int COLUMN_ID           = 0;
    public static final int COLUMN_ALBUMTITLE   = 1;
    public static final int COLUMN_ARTIST       = 2;
    public static final int COLUMN_ARTIST_ID    = 3;
    public static final int COLUMN_ALBUM_ID     = 4;
    public static final int COLUMN_COVER_SMALL  = 5;
    public static final int COLUMN_COVER_MED    = 6;

    private Context context;
    private SQLiteDatabase db;
    private SQLiteStatement insertStmt;

    private static final String INSERT =
            "INSERT INTO " + DATABASE_TABLE + "(" +
                    KEY_ALBUM + ", " +
                    KEY_ARTIST + ", " +
                    KEY_ARTIST_ID  + ", " +
                    KEY_ALBUM_ID  + ", " +
                    KEY_COVER_SMALL_URL + ", " +
                    KEY_COVER_MED_URL + ") values (?, ?, ?, ?, ?, ?)";

    public DBHelper(Context context) {
        this.context = context;
        DBOpenHelper dbOpenHelper = new DBOpenHelper(this.context,
                DATABASE_NAME, null, DATABASE_VERSION);
        db = dbOpenHelper.getWritableDatabase();
        insertStmt = db.compileStatement(INSERT);
    }

    //	public void insert(ArtistInfo artistInfo) {
//		for ( AlbumInfo album :  artistInfo.getArtistAlbums()) {
//			insertAlbum(album);
//		}
//
//	}
    public long insert(AlbumInfo album){
        insertStmt.bindString(COLUMN_ALBUMTITLE, album.getAlbumTitle());
        insertStmt.bindString(COLUMN_ARTIST, album.getArtistName());
        insertStmt.bindString(COLUMN_ARTIST_ID, album.getArtistID());
        insertStmt.bindString(COLUMN_ALBUM_ID , album.getAlbumID());
        insertStmt.bindString(COLUMN_COVER_SMALL, album.getAlbumCoverUrlSmall());
        insertStmt.bindString(COLUMN_COVER_MED, album.getAlbumCoverUrlMedium());
        long value = insertStmt.executeInsert();
        return value;
    }

    public void deleteAll() {

        db.delete(DATABASE_TABLE, null, null);

    }

    public Cursor selectAll() {
        Cursor cursor = db.query(DATABASE_TABLE, new String[] { KEY_ID,
                        KEY_ALBUM, KEY_ARTIST, KEY_ARTIST_ID, KEY_ALBUM_ID,
                        KEY_COVER_SMALL_URL, KEY_COVER_MED_URL }, null, null,
                null, null, KEY_ARTIST);
        return cursor;
    }


    public List<AlbumInfo> displayList() {
        List<AlbumInfo> albumList = new ArrayList<AlbumInfo>();

        Cursor cursor = db.query(DATABASE_TABLE, new String[] { KEY_ID,
                        KEY_ALBUM, KEY_ARTIST, KEY_ARTIST_ID, KEY_ALBUM_ID,
                        KEY_COVER_SMALL_URL, KEY_COVER_MED_URL }, null, null,
                null, null, KEY_ARTIST);
        if (cursor.moveToFirst()) {
            do {
                AlbumInfo albumInfo = new AlbumInfo();
                albumInfo.setArtistName(cursor.getString(COLUMN_ARTIST));
                albumInfo.setArtistID(cursor.getString(COLUMN_ARTIST_ID));
                albumInfo.setAlbumTitle(cursor.getString(COLUMN_ALBUMTITLE));
                albumInfo.setAlbumID(cursor.getString(COLUMN_ALBUM_ID));
                albumInfo.setAlbumCoverUrlSmall(cursor.getString(COLUMN_COVER_SMALL));
                albumInfo.setAlbumCoverUrlMedium(cursor.getString(COLUMN_COVER_MED));
                albumList.add(albumInfo);

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return albumList;

    }

    private static class DBOpenHelper extends SQLiteOpenHelper {

        // Statement to create a new database if not present
        private static final String DATABASE_CREATE = "CREATE TABLE "
                + DATABASE_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_ALBUM + " TEXT, " + KEY_ARTIST + " TEXT, " + KEY_ARTIST_ID
                + " TEXT, " + KEY_ALBUM_ID + " TEXT, " + KEY_COVER_SMALL_URL
                + " TEXT, " + KEY_COVER_MED_URL + " TEXT);";



        public DBOpenHelper(Context context, String name,
                            CursorFactory factory, int version) {
            super(context, name, factory, version);
            // TODO Auto-generated constructor stub
        }

        public void onCreate(SQLiteDatabase db) {
            Log.d(LOGTAG, "Create!");
            db.execSQL(DATABASE_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log the version upgrade.
            Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion
                    + " to " + newVersion + " ");

            // Upgrade the existing database to conform to the new
            // version. Multiple previous versions can be handled by
            // comparing oldVersion and newVersion values.

            // The simplest case is to drop the old table and create a new one.
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
            // Create a new one.
            onCreate(db);
        }
    }

    public void close() {
        db.close();

    }
}