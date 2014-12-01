package wemakeplay.com.identitygallery.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

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

}
