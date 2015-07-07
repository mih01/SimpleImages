package heli3a.org.simpleimages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;


public class ImageActivity extends AppCompatActivity {

    public static final String TAG = "ImageActivity";

    private static final String ARGS_ID = "id";

    private static final String ARGS_PATH = "path";

    private ImageView mImageView;

    private int mImageId;

    private String mImagePath;

    public static void newInstance(Activity p, int req, MainActivity.MediaStoreImageData data) {
        Intent i = new Intent(p, ImageActivity.class);
        i.putExtra(ARGS_ID, data.id);
        i.putExtra(ARGS_PATH, data.path);
        p.startActivityForResult(i, req);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mImageView = (ImageView) findViewById(R.id.imageView1);

        mImageId = getIntent().getExtras().getInt(ARGS_ID);
        mImagePath = getIntent().getExtras().getString(ARGS_PATH);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ImageLoader(mImageId, mImagePath).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                share(mImageId);
                return true;
            case R.id.action_tag:
                createDescriptionDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createDescriptionDialog() {

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this).setTitle("Beschreibung")
                .setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateDescription(mImageId, editText.getText().toString());
            }
        }).setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setView(editText).show();
    }

    private Uri getImageUri(int id) {
        return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
    }

    private void updateDescription(int imageId, String val) {
        if (val != null && val.trim().length() > 0) {
            final Uri uri = getImageUri(imageId);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DESCRIPTION, val);
            getContentResolver().update(uri, values, null, null);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }
    }

    private void share(int imageId) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("image/jpeg");
        i.putExtra(Intent.EXTRA_STREAM, getImageUri(imageId));
        startActivity(Intent.createChooser(i, getString(R.string.share_image)));
    }

    private class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

        private int mId;
        private String mPath;

        public ImageLoader(int id, String path) {
            mId = id;
            mPath = path;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return ImageTools.loadBitmap(ImageActivity.this, mId, mPath);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (ImageActivity.this.mImageView != null) {
                ImageActivity.this.mImageView.setImageBitmap(bitmap);
            }
        }
    }
}
