package com.thricethree.musiclibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.thricethree.musiclibrary.model.AlbumInfo;
import com.thricethree.musiclibrary.model.ArtistInfo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by thricethree on 6/24/15.
 */

public class FoundArtistListActivity extends Activity {

    //	private Button backToLibraryButton;
    private TextView foundAlbumTextView;
    private TextView foundArtistTextView;
    private ImageView foundCoverArtImageView;
    private TableLayout artistTableScrollView;
    private TextView libraryListTextView;
    private ProgressBar progressBar;
    private Bitmap coverArt;
    private ArtistInfo artist;
    private String artistSearchName = "";
    private Cursor cursor;
    private DBHelper db;
    private FetchDataTask fetchDataTask;

    private static final String LOGTAG = FoundArtistListActivity.class
            .getSimpleName();

    private String lastFmUrlPrefix = "http://ws.audioscrobbler.com/2.0/?";
    private static final String apiKey = "api_key=dc5fa846a22b158c42612c953c1e2576";
    private String urlArtist = "method=artist.gettopalbums&artist=";

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "*** Initialize ArtistListActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_info_list);
        initialze();

    }

    private void initialze() {
        // TODO Auto-generated method stub
//		backToLibraryButton = (Button) findViewById(R.id.backToLibraryButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        libraryListTextView = (TextView) findViewById(R.id.libraryTextView);
        artistTableScrollView = (TableLayout) findViewById(R.id.artistTableScrollView);
        Intent callingIntent = this.getIntent();

        artist = (ArtistInfo) callingIntent.getSerializableExtra("artistInfo");
        artistSearchName = artist.getArtistName();
        startFetchdata();
        Toast.makeText(FoundArtistListActivity.this, artist.getArtistName(),
                Toast.LENGTH_SHORT).show();

    }

    private void insertAlbums(ArtistInfo artistInfo) {
        db = new DBHelper(this);
        for (AlbumInfo album : artistInfo.getArtistAlbums()) {
            Log.d(LOGTAG, "albumInfo Artist Name:" + album.getArtistName());
            Log.d(LOGTAG, "albumInfo Album Title:" + album.getAlbumTitle());
            Log.d(LOGTAG,
                    "albumInfo Cover URL:" + album.getAlbumCoverUrlSmall());
            db.insert(album);
        }
        db.close();
    }

    private ArrayList<ArtistInfo> displayList() {

        ArrayList<ArtistInfo> artists = new ArrayList<ArtistInfo>();
        cursor = db.selectAll();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    ArtistInfo artist = new ArtistInfo();
                    AlbumInfo album = new AlbumInfo();
                    artist.setArtistName(cursor.getString(db.COLUMN_ARTIST));
                    album.setArtistName(cursor.getString(db.COLUMN_ARTIST));
                    album.setAlbumTitle(cursor.getString(db.COLUMN_ALBUMTITLE));
                    album.setAlbumCoverUrlSmall(cursor
                            .getString(db.COLUMN_COVER_SMALL));
                    artist.addAlbumToList(album);
                    if (!artist.getArtistName().equalsIgnoreCase(
                            cursor.getString(db.COLUMN_ARTIST))) {
                        artists.add(artist);
                    }
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return artists;

    }

    private void displayFoundArtists(ArtistInfo artist) {
        libraryListTextView.setText("Albums for " + artist.getArtistName());
        for (AlbumInfo album : artist.getArtistAlbums()) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View newAlbumRow = inflater.inflate(R.layout.found_album_row, null);
            foundAlbumTextView = (TextView) newAlbumRow
                    .findViewById(R.id.foundAlbumTextView);
            foundAlbumTextView.setText(album.getAlbumTitle());

            TextView foundArtistTextView = (TextView) newAlbumRow
                    .findViewById(R.id.foundArtistTextView);
            foundArtistTextView.setText(artist.getArtistName());
            ImageView foundCoverArtImageView = (ImageView) newAlbumRow
                    .findViewById(R.id.foundCoverArtImageView);
            foundCoverArtImageView.setContentDescription(album
                    .getAlbumCoverUrlSmall());
            new ImageLoadTask(foundCoverArtImageView).execute(album
                    .getAlbumCoverUrlSmall());
            Button addAlbumButton = (Button) newAlbumRow
                    .findViewById(R.id.addAlbumButton);
            addAlbumButton.setOnClickListener(addAlbumToDBListener);

            artistTableScrollView.addView(newAlbumRow);

        }
    }

    public OnClickListener addAlbumToDBListener = new OnClickListener() {

        public void onClick(View v) {

            TableRow tr = (TableRow) v.getParent();
            RelativeLayout artist = (RelativeLayout) tr.getChildAt(0);
            TextView albumTitle = (TextView) artist.getChildAt(0);
            TextView albumArtist = (TextView) artist.getChildAt(1);
            Log.d(LOGTAG, "albumTitle Child at 0: "
                    + albumTitle.getText().toString());
            Log.d(LOGTAG, "albumArtist Child at 1: "
                    + albumArtist.getText().toString());
            ImageView cover = (ImageView) tr.getChildAt(1);

            ArtistInfo artistInfo = new ArtistInfo();
            AlbumInfo album = new AlbumInfo();
            artistInfo.setArtistName(albumArtist.getText().toString());
            album.setAlbumTitle(albumTitle.getText().toString());
            album.setArtistName(artistInfo.getArtistName());
            album.setAlbumCoverUrlSmall(cover.getContentDescription()
                    .toString());
            album.setArtistID("1223146");
            album.setSelected(true);
            artistInfo.addAlbumToList(album);
            Log.d(LOGTAG, "Add button clicked!");
            addArtistToDB(artistInfo);

        }

    };

    private void addArtistToDB(ArtistInfo artistInfo) {
        DBHelper db = new DBHelper(this);
        for (AlbumInfo album : artistInfo.getArtistAlbums()) {
            db.insert(album);
            Toast.makeText(getApplicationContext(), "Album Added to Database!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void startFetchdata() {
        if (fetchDataTask != null)
            return;
        fetchDataTask = new FetchDataTask();
        fetchDataTask.execute(lastFmUrlPrefix, artistSearchName.replace(" ", "%20"));

    }

    private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;

        public ImageLoadTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        private Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
//			progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                URL url;
                url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
            progressBar.setVisibility(View.GONE);
        }
    }



    // **********************************************************************************************
    // Extend AsynTask to implement a Task that will be run partly in the Main
    // thread, which allows
    // UI updates, and partly in a separate background Thread so as to not
    // affect UI liveness.
    // **********************************************************************************************
    private class FetchDataTask extends AsyncTask<String, Void, ArtistInfo> {
        private final String LOGTAG = FetchDataTask.class.getSimpleName();
        private final boolean DEBUG = true;
        private boolean noArtist;

        @Override
        protected void onPreExecute() {
            if (DEBUG)
                Log.d(LOGTAG, "**** onPreExecute() STARTING");
//			progressBar.setVisibility(View.VISIBLE);
        }

        protected ArtistInfo doInBackground(String... params) {
            if (DEBUG)
                Log.d(LOGTAG, "**** doInBackground() STARTING");
            ArtistInfo artistInfo = null;
            InputStream in = null;
            final String apiUrl = params[0];
            String artist = params[1];
            try {
                final StringBuilder sb = new StringBuilder(apiUrl);
                if (artist != null) {
                    Log.d(LOGTAG, "artist=" + artist);
                }
                sb.append(urlArtist);
                sb.append(artist);
                sb.append("&");
                sb.append(apiKey);
                Log.d(LOGTAG, "sb: " + sb.toString());
                final URL url = new URL(sb.toString());
                final HttpURLConnection httpConnection = (HttpURLConnection) url
                        .openConnection();
                final int responseCode = httpConnection.getResponseCode();
                Log.d(LOGTAG, "responseCode=" + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    // Handle error.
                    noArtist = true;
                    Log.e(LOGTAG, "responseCode=" + responseCode);
                    return null;
                }

                in = httpConnection.getInputStream();
                artistInfo = new ArtistInfoSAX().parse(in);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
            return artistInfo;

        }

        @Override
        protected void onPostExecute(ArtistInfo artistInfo) {
            if (DEBUG) {
                Log.d(LOGTAG, "**** onPostExecute() STARTING");
                Log.d(LOGTAG, "ArtistInfo Object is null"
                        + (artistInfo == null));
            }
            if (noArtist || artistInfo.getArtistAlbums().size() < 1) {
                Toast.makeText(getApplicationContext(),
                        "No artist by that name found!", Toast.LENGTH_LONG)
                        .show();
//				getArtistsFromDB();
                progressBar.setVisibility(View.GONE);
                return;
            }
//			progressBar.setVisibility(View.GONE);
//			artistName.setEnabled(true);
            displayFoundArtists(artistInfo);
//			backToLibraryButton.setVisibility(View.VISIBLE);

        }
    }



}