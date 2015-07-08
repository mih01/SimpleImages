package heli3a.org.simpleimages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

/**
 * Created by Hoenisch on 07.07.2015.
 */
public class ImageTools {

    public static Bitmap loadBitmap(Context c, String path, int reqWidth, int reqHeight) throws IOException {

        if (path != null && new File(path).exists()) {
            Bitmap b = ImageTools.decodeSampledBitmapFromFile(path, reqWidth, reqHeight);
            if (b != null && path != null && new File(path).exists()) {
                return rotate(b, path);
            }
        }
        return null;
    }

    public static Bitmap loadThumbnail(Context c, int id, String path) {
        Bitmap b = MediaStore.Images.Thumbnails.getThumbnail(c.getContentResolver(), id,
                MediaStore.Images.Thumbnails.MINI_KIND, null);
        if (b != null && path != null && new File(path).exists()) {
            return rotate(b, path);
        }
        return null;
    }

    private static Bitmap rotate(Bitmap b, String path) {

        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
        }

        if (rotate != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        }

        return b;
    }

    public static Uri getImageUri(int id) {
        return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
    }

    // http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void recycleImageView(ImageView imageView) {
        try {
            if (imageView != null && imageView.getDrawable() != null) {
                BitmapDrawable bd = ((BitmapDrawable) imageView.getDrawable());
                if (bd != null && bd.getBitmap() != null) {
                    bd.getBitmap().recycle();
                }
            }
        } catch (Exception e) {
        }
    }
}
