package heli3a.org.simpleimages;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import heli3a.org.simpleimages.views.CustomImageView;


public class ImageActivity extends AppCompatActivity {

    public static final String TAG = "ImageActivity";

    private static final String ARGS_ID = "id";

    private static final String ARGS_PATH = "path";

    private CustomImageView mImageView;

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

        mImageView = (CustomImageView) findViewById(R.id.imageView1);
        if (mImageView != null) {
            mImageView.setOnSizeChangedListener(new CustomImageView.OnSizeChangedListener() {
                @Override
                public void onSizeChanged(int w, int h, int oldw, int oldh) {
                    if (mImageView != null) {
                        mImageView.setVisibility(View.INVISIBLE);
                        ImageTools.recycleImageView(mImageView);
                        new ImageLoader(mImagePath, mImageView.getWidth(), mImageView.getHeight()).execute();
                    }
                }
            });
        }

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

        // TODO: screen rotation
        new AlertDialog.Builder(this).setTitle(R.string.add_tag)
                .setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateDescription(mImageId, editText.getText().toString());
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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

    // http://developer.android.com/training/sharing/send.html
    private void share(int imageId) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("image/jpeg");
            i.putExtra(Intent.EXTRA_STREAM, getImageUri(imageId));
            startActivity(Intent.createChooser(i, getString(R.string.share_image)));
        } catch (Exception e) {
            final String msg = e.getLocalizedMessage();
            if (msg != null && msg.length() > 0) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

        private ProgressDialog mDialog;

        private String mPath;

        private int width = 0;
        private int height = 0;

        public ImageLoader(String path, int width, int height) {
            this.mPath = path;
            this.width = width;
            this.height = height;
            this.mDialog = new ProgressDialog(ImageActivity.this);
        }

        @Override
        protected void onPreExecute() {
            mDialog.setMessage(getString(R.string.load_image));
            mDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return ImageTools.loadBitmap(ImageActivity.this, mPath, width, height);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (mDialog != null) {
                mDialog.dismiss();
            }

            if (ImageActivity.this.mImageView != null && !isCancelled()) {
                ImageActivity.this.mImageView.setImageBitmap(bitmap);
                ImageActivity.this.mImageView.setVisibility(View.VISIBLE);

                ImageTools.animate(ImageActivity.this.mImageView);
            }
        }
    }
}
