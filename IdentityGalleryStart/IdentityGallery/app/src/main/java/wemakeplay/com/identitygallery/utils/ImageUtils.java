package wemakeplay.com.identitygallery.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by josh on 30/11/14.
 */
public final class ImageUtils {

    public static Bitmap getMiniThumbnailForImage(Context context, long imageId){
        return MediaStore.Images.Thumbnails.getThumbnail(
                context.getContentResolver(),
                imageId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null);
    }

    public static Bitmap getImage(Context context, Uri uri) throws FileNotFoundException, IOException {
        return ImageUtils.getBitmap(context.getContentResolver(), uri);
    }

    public static final Bitmap getBitmap(ContentResolver cr, Uri url) throws FileNotFoundException, IOException {
        InputStream input = cr.openInputStream(url);
        byte[] raw = ImageUtils.toByteArray(input, -1, true);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.length, options);
        input.close();
        return bitmap;
    }

    public static byte[] toByteArray(InputStream is, int length, boolean readAll)
            throws IOException {
        byte[] output = {};
        if (length == -1) length = Integer.MAX_VALUE;
        int pos = 0;
        while (pos < length) {
            int bytesToRead;
            if (pos >= output.length) { // Only expand when there's no room
                bytesToRead = Math.min(length - pos, output.length + 1024);
                if (output.length < pos + bytesToRead) {
                    output = Arrays.copyOf(output, pos + bytesToRead);
                }
            } else {
                bytesToRead = output.length - pos;
            }
            int cc = is.read(output, pos, bytesToRead);
            if (cc < 0) {
                if (readAll && length != Integer.MAX_VALUE) {
                    throw new EOFException("Detect premature EOF");
                } else {
                    if (output.length != pos) {
                        output = Arrays.copyOf(output, pos);
                    }
                    break;
                }
            }
            pos += cc;
        }
        return output;
    }

}
