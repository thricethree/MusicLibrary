package com.thricethree.musiclibrary;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thricethree.musiclibrary.data.DBHelper;
import com.thricethree.musiclibrary.model.AlbumInfo;
import com.thricethree.musiclibrary.model.ArtistInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends Activity {

    private TextView artistListHeader;
    private ImageView foundCoverArtImageView;
    private TextView libraryHeader;
    // private Button searchButton;
    private ProgressBar progressBar;
    private ListView libraryListView;
    private CustomListAdapter listAdapter;
    private Bitmap coverArt;


    private EditText artistName;
    private TextView albumTitleTextView;
    private TextView libraryListTextView;
    private Button searchButton;
    private Button backToLibraryButton;
    private TextView foundAlbumTextView;
    private TextView foundArtistTextView;
    private TableLayout artistTableScrollView;



    private static final String LOGTAG = "Main Activity";
    private boolean DEBUG = true;

    private String artistSearchName = "";

    private String lastFmUrlPrefix = "http://ws.audioscrobbler.com/2.0/?";
    private static final String apiKey = "api_key=dc5fa846a22b158c42612c953c1e2576";
    private String urlArtist = "method=artist.gettopalbums&artist=";
    // Artist & album Url
    // http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=
    // dc5fa846a22b158c42612c953c1e2576&artist=Cher&album=Believe

    // Artist get albums URL
    // http://ws.audioscrobbler.com/2.0/?method=artist.gettopalbums&artist=cher&api_key=
    // dc5fa846a22b158c42612c953c1e2576
    private FetchArtistsTask fetchArtists;
    private FetchDataTask fetchDataTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Music Library");
        initialize();
    }



    @SuppressWarnings("unchecked")
    private void initialize(){
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listAdapter = new CustomListAdapter(this, null);

        // addButton = (Button) findViewById(R.id.addArtistEntry);
        // searchButton = (Button) findViewById(R.id.searchButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        libraryListTextView = (TextView) findViewById(R.id.libraryListTextView);
        artistTableScrollView = (TableLayout) findViewById(R.id.artistTableScrollView);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getArtists();



//		List<String> artistList = getAlbumsFromFile();
//
//		fetchArtists = new FetchArtistsTask();
//		fetchArtists.execute(artistList);
//
//		ListView libraryListView = (ListView) this.findViewById(R.id.libraryView);
//		libraryListView.setClickable(true);
//		libraryListView.setAdapter(listAdapter);
//
//		TextView artistNameText = (TextView) findViewById(R.id.albumArtistTextView);
//		TextView albumTitle = (TextView) findViewById(R.id.albumTitleTextView);
//		ImageView foundCoverArtImageView = (ImageView) findViewById(R.id.coverArtImageView);
//		new ImageLoadTask(foundCoverArtImageView).execute("http://userserve-ak.last.fm/serve/126/62409097.png"/*album.getAlbumCoverUrlSmall()*/);
    }

    private void getArtists() {

        DBHelper db = new DBHelper(this);

        ArrayList<AlbumInfo> artistsInDB = (ArrayList<AlbumInfo>) db
                .displayList();
        if (artistsInDB.size() < 1){
            List<String> artistList = getAlbumsFromFile();
            fetchArtists = new FetchArtistsTask();
            fetchArtists.execute(artistList);
        } else {
            for (AlbumInfo album : artistsInDB) {
                insertArtistsInScrollView(album);
            }
        }

    }



    private void insertArtistsInScrollView(AlbumInfo album) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View newAlbumRow = inflater.inflate(R.layout.album_info_row, null);

        TextView artistTextView = (TextView) newAlbumRow
                .findViewById(R.id.artistNameTextView);
        TextView albumTitleTextView = (TextView) newAlbumRow
                .findViewById(R.id.albumTitleTextView);

        artistTextView.setText(album.getArtistName());
        albumTitleTextView.setText(album.getAlbumTitle());

        ImageView albumCoverImageView = (ImageView) newAlbumRow
                .findViewById(R.id.coverArtImageView);

        new ImageLoadTask(albumCoverImageView).execute(album
                .getAlbumCoverUrlSmall());

        Log.d(LOGTAG,
                "album.getAlbumCoverUrlSmall() is: "
                        + album.getAlbumCoverUrlSmall());
        newAlbumRow.setContentDescription(album.getAlbumTitle());

        artistTableScrollView.addView(newAlbumRow);
    }



    private ArrayList<String> getAlbumsFromFile() {
        ArrayList<String> albums = new ArrayList<String>();
        AssetManager assetManager = getAssets();
        InputStream is = null;
        try {

            is = assetManager.open("preloadArtistAlbums.csv");
            InputStreamReader isReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isReader);
            String line;

            while ((line = reader.readLine()) != null) {
                albums.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return albums;

    }

    private class FetchArtistsTask extends
            AsyncTask<List<String>, Void, ArrayList<ArtistInfo>> {

        @Override
        protected ArrayList<ArtistInfo> doInBackground(List<String>... params) {
            // TODO Auto-generated method stub
            StringTokenizer stringToke = null;
            List<String> albumList = params[0];
            ArrayList<ArtistInfo> artistInfo = new ArrayList<ArtistInfo>();
//	ArtistInfo albumObjects = new ArtistInfo();
            for (String albums : albumList) {
                ArtistInfo artist = new ArtistInfo();
                AlbumInfo album = new AlbumInfo();
                stringToke = new StringTokenizer(albums, ",");
                album.setAlbumTitle(stringToke.nextToken());
                artist.setArtistName(stringToke.nextToken());
                album.setAlbumCoverUrlMedium(stringToke.nextToken());
                Log.d(LOGTAG, "getView.position=" + album.getAlbumCoverUrlMedium());
                artist.addAlbumToList(album);
                artistInfo.add(artist);
            }

            return artistInfo;
        }

        protected void onPostExecute(ArrayList<ArtistInfo> albumObjects) {

            listAdapter.setList(albumObjects);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @SuppressLint("InflateParams")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        final View dialogView = getLayoutInflater().inflate(
                R.layout.search_dialog_text_entry, null);
//		artistName = (EditText) findViewById(R.id.search_artist_edit);
        switch (item.getItemId()) {
            case R.id.action_search:
                new AlertDialog.Builder(MainActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setView(dialogView)
                        .setTitle(R.string.alert_dialog_search)
                        .setNegativeButton(R.string.alert_dialog_cancel, null)
                        .setPositiveButton(R.string.alert_dialog_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        final EditText userInput = (EditText) dialogView
                                                .findViewById(R.id.search_artist_edit);
                                        artistSearchName =  userInput.getText().toString();
                                        if (!artistSearchName.equalsIgnoreCase("")){
                                            ArtistInfo artist = new ArtistInfo();
                                            artist.setArtistName(artistSearchName);
                                            Intent myIntent = new Intent(MainActivity.this,
                                                    FoundArtistListActivity.class);
                                            myIntent.putExtra("artistInfo", artist);
                                            startActivity(myIntent);
                                            Toast.makeText(MainActivity.this,
                                                    R.string.alert_dialog_todo,
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this,
                                                    R.string.enter_artist_toast,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).show();
                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onPause() {
        super.onPause();
        if (fetchDataTask != null) {
            fetchDataTask.cancel(true);
            fetchDataTask = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


//	public void onAddButtonClick(View view) {
    // addButton.setVisibility(View.GONE);
    // libraryHeader.setVisibility(View.GONE);
    // artistName.setVisibility(View.VISIBLE);
    // artistName.setVisibility(View.VISIBLE);
    // searchButton.setVisibility(View.VISIBLE);
//		artistName.setText("hot water music");
//		albumTitle.setText("exister");
//		startFetchdata();
//	}

//	public void onSearchButtonClick(View view) {
    // artistName.setVisibility(View.GONE);
    // artistName.setVisibility(View.GONE);
    // searchButton.setVisibility(View.GONE);
//		startFetchdata();

//	}

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
            progressBar.setVisibility(View.VISIBLE);
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

    class CustomListAdapter extends BaseAdapter {
        private Context context;
        private List<ArtistInfo> list;
        private LayoutInflater layoutInflater;

        CustomListAdapter(Context context, List<ArtistInfo> list) {
            this.context = context;
            this.list = list;
        }

        public void setList(List<ArtistInfo> list) {
            this.list = list;
        }

        public void addToList(ArtistInfo item) {
            list.add(item);
        }

        @Override
        public int getCount() {
            return ((list == null) ? 0 : list.size());
        }

        @Override
        public Object getItem(int position) {
            // In theory we should not be called if getCount() returned 0;
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            int position;
            TextView artistName;
            TextView albumName;
            ImageView imageCover;
//			TextSwitcher stockPrice;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (DEBUG) {
                Log.d(LOGTAG, "getView.position=" + position);
                Log.d(LOGTAG, "getView.convertView=" + convertView);
            }
            if (list == null) {
                // In theory it should not happen but handle this in some
                // graceful way.
                // Returning null will not produce graceful results.
                list = new ArrayList<ArtistInfo>();
                ArrayList<AlbumInfo> albumList = new ArrayList<AlbumInfo>();

                ArtistInfo artistInfo = new ArtistInfo();
                artistInfo.setArtistName("John Frusciante");
                artistInfo.setArtistID("258115");
                artistInfo.setArtistAlbums(null);
                list.add(artistInfo);
            }
            // You can find this ViewHolder idiom described in detail in this
            // talk:
            // http://www.youtube.com/watch?v=N6YdwzAvwOA&feature=related
            ViewHolder holder = null;

            if (convertView != null)
                holder = (ViewHolder) convertView.getTag();
            if (holder == null) // not the right view
                convertView = null;
            if (convertView == null) {
                convertView = (LinearLayout) getLayoutInflator().inflate(R.layout.record_info_entry, null);
                holder = new ViewHolder();

                holder.artistName = (TextView) convertView.findViewById(R.id.albumArtistTextView);
                holder.albumName = (TextView) convertView.findViewById(R.id.albumTitleTextView);
                holder.imageCover = (ImageView) convertView.findViewById(R.id.coverArtImageView);
                Animation in = AnimationUtils.loadAnimation(MainActivity.this,
                        android.R.anim.slide_in_left);
                Animation out = AnimationUtils.loadAnimation(MainActivity.this,
                        android.R.anim.slide_out_right);
//				holder.stockPrice.setAnimation(in);
//				holder.stockPrice.setAnimation(out);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            ArtistInfo item = (ArtistInfo) getItem(position);
            holder.artistName.setText(item.getArtistName());
            holder.albumName.setText(item.getArtistName());
//			holder.imageCover.set;
            return convertView;
        }

        private LayoutInflater getLayoutInflator() {
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            return layoutInflater;
        }
    }


    private ArrayList<String> getLibraryFromFile() {
        ArrayList<String> library = new ArrayList<String>();
        AssetManager assetManager = getAssets();
        InputStream is = null;
        try {

            is = assetManager.open("preload_artist_albums.csv");
            InputStreamReader isReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isReader);
            String line;

            while ((line = reader.readLine()) != null) {
                library.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return library;

    }

    private void startFetchdata() {
        if (fetchDataTask != null)
            return;
        fetchDataTask = new FetchDataTask();
        fetchDataTask.execute(lastFmUrlPrefix, artistSearchName.replace(" ", "%20"));

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

        @Override
        protected void onPreExecute() {
            if (DEBUG)
                Log.d(LOGTAG, "**** onPreExecute() STARTING");
            progressBar.setVisibility(View.VISIBLE);
        }

        // Artist & album Url
        // http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=
        // dc5fa846a22b158c42612c953c1e2576&artist=Cher&album=Believe

        // Artist get albums URL
        // http://ws.audioscrobbler.com/2.0/?method=artist.gettopalbums&artist=cher&api_key=
        // dc5fa846a22b158c42612c953c1e2576

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
            if (artistInfo == null) {
                // onTaskCompleted(false);
                artistListHeader.setText("Null Value Dummy");
                progressBar.setVisibility(View.GONE);
                return;
            }
            progressBar.setVisibility(View.GONE);
            artistListHeader.setText("Artist: "
                    + artistInfo.getArtistName()
                    + "\nArtist ID: "
                    + artistInfo.getArtistID()
                    + " \n# Albums: "
                    + artistInfo.getArtistAlbums().size()
                    + "\nTitle: "
                    + artistInfo.getArtistAlbums().get(1).getAlbumTitle()
                    + "\nURL: "
                    + artistInfo.getArtistAlbums().get(1)
                    .getAlbumCoverUrlSmall());
        }
    }

    public View makeView() {
        // TODO Auto-generated method stub
        return null;
    }

}
