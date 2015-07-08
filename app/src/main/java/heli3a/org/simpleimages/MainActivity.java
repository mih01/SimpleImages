package heli3a.org.simpleimages;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import heli3a.org.simpleimages.views.CustomImageView;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private final int REQ_TAKE_IMAGE = 1;
    private final int REQ_SHOW_IMAGE = 2;

    private final int LOADER_ID_PHOTOS = 1;

    private SearchView mSearchView;

    private GridView mGridView;

    private File mPhoto;

    private CursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new ImageAdapter(this, null, true);

        mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view != null && view.getTag() != null && view.getTag() instanceof MediaStoreImageData) {
                    final MediaStoreImageData data = (MediaStoreImageData) view.getTag();
                    ImageActivity.newInstance(MainActivity.this, REQ_SHOW_IMAGE, data);
                }
            }
        });

        startLoader(LOADER_ID_PHOTOS, null, new ImageCallbacks());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        // SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(new SearchViewListener());
            mSearchView.setQueryHint(getString(R.string.search_hint));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_camera:
                takePicture();
                return true;
            case R.id.action_search:
                // onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startLoader(int id, Bundle b, LoaderManager.LoaderCallbacks<Cursor> cb) {
        final LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager.getLoader(id) == null) {
            loaderManager.initLoader(id, b, cb);
        } else {
            loaderManager.restartLoader(id, b, cb);
        }
    }

    private void takePicture() {
        mPhoto = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "SI_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            try {
                mPhoto = File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (IOException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (mPhoto != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhoto));
                startActivityForResult(takePictureIntent, REQ_TAKE_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode);
        switch (requestCode) {
            case REQ_TAKE_IMAGE:
                try {
                    if (resultCode == Activity.RESULT_OK && mPhoto != null && mPhoto.exists() && mPhoto.isFile()) {
                        Toast.makeText(this, getString(R.string.hint_picture_taken), Toast.LENGTH_LONG).show();
                        galleryAddPic(mPhoto);
                    }
                } finally {
                    mPhoto = null;
                }
        }
    }

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(f));
        this.sendBroadcast(mediaScanIntent);
    }

    private class ImageCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DESCRIPTION};

            String selection = null;
            String[] selectionArgs = null;
            if (args != null && args.containsKey(MediaStore.Images.Media.DESCRIPTION)) {
                final String pattern = args.getString(MediaStore.Images.Media.DESCRIPTION);
                if (pattern != null && pattern.trim().length() > 0) {
                    selection = MediaStore.Images.Media.DESCRIPTION + " like ?";
                    selectionArgs = new String[]{"%" + args.getString(MediaStore.Images.Media.DESCRIPTION) + "%"};
                }
            }

            // String sort = null;
            String sort = MediaStore.Images.Media.DATE_TAKEN + " DESC";

            CursorLoader cursorLoader = new CursorLoader(MainActivity.this,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
                    selectionArgs, sort);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mAdapter != null) {
                mAdapter.swapCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (mAdapter != null) {
                mAdapter.swapCursor(null);
            }
        }
    }

    private class ImageAdapter extends CursorAdapter {

        public ImageAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            final TextView textView = (TextView) view.findViewById(R.id.textView);
            if (textView != null) {
                String dsc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
                textView.setText(dsc == null || dsc.trim().length() == 0 ? "" : dsc);
            }

            final CustomImageView imageView = (CustomImageView) view.findViewById(R.id.imageView);
            ImageTools.recycleImageView(imageView);

            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            imageView.setTag(path); // !!! fuer den AsyncTask !!!
            new ThumbnailLoader(imageView, id).execute();

            view.setTag(new MediaStoreImageData(path, id)); // !!! fuer den GridView.onItemClickListener !!!
        }
    }

    private class SearchViewListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Bundle b = new Bundle();
            b.putString(MediaStore.Images.Media.DESCRIPTION, newText);
            startLoader(LOADER_ID_PHOTOS, b, new ImageCallbacks());
            return false;
        }
    }

    // look in http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
    private class ThumbnailLoader extends AsyncTask<Void, Void, Bitmap> {

        private final WeakReference<ImageView> mViewReference;
        private String mPath;
        private int mId = -1;

        public ThumbnailLoader(ImageView v, int id) {
            mViewReference = new WeakReference<ImageView>(v);
            v.setVisibility(View.INVISIBLE);
            mPath = v.getTag().toString();
            mId = id;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            try {
                if (mViewReference != null && mViewReference.get() != null && !isCancelled()) {
                    return ImageTools.loadThumbnail(MainActivity.this, mId, mPath);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (isCancelled()) {
                bitmap = null;
            }

            if (mViewReference != null && bitmap != null) {
                final ImageView imageView = mViewReference.get();
                if (imageView != null && mPath.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);

                    ImageTools.animate(imageView);
                }
            }
        }
    }

    public class MediaStoreImageData {
        public String path;
        public int id;

        public MediaStoreImageData(String path, int id) {
            this.path = path;
            this.id = id;
        }
    }
}
