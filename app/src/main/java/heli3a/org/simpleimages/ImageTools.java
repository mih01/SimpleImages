package heli3a.org.simpleimages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

/**
 * Created by Hoenisch on 07.07.2015.
 */
public class ImageTools {

    public static Bitmap loadBitmap(Context c, int id, String path) throws IOException {
        Bitmap b = MediaStore.Images.Media.getBitmap(c.getContentResolver(), getImageUri(id));
        if (b != null && path != null && new File(path).exists()) {
            return rotate(b, path);
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
}
