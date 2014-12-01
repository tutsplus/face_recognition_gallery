package wemakeplay.com.identitygallery.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by josh on 27/11/14.
 */
public class IdentityGalleryContract {

    public static final String TAG = IdentityGalleryContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.wmp.identitygallery.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PHOTO = "photo";

    public static final String PATH_PHOTO_IDENTITY = "photo_identity";

    public static final String PATH_IDENTITY = "identity";

    public static final class IdentityEntity implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_IDENTITY).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_IDENTITY;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_IDENTITY;

        public static final String TABLE_NAME = "identity";

        public static final String COLUMN_PERSON_ID = "person_id";

        public static final String COLUMN_LABEL = "label";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class PhotoEntity implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO;

        public static final String TABLE_NAME = "photo";

        public static final String COLUMN_MEDIA_ID = "media_id";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_DATE_TAKEN = "date_taken";

        public static final String COLUMN_DATE_ADDED = "date_added";

        public static final String COLUMN_WIDTH = "width";

        public static final String COLUMN_HEIGHT = "height";

        public static final String COLUMN_LAT = "lat";

        public static final String COLUMN_LON = "lon";

        public static final String COLUMN_URI = "uri";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUriWithReferencePhoto(long referencePhotoId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(referencePhotoId)).build();
        }

        public static String getReferenceIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class PhotoIdentityEntity implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTO_IDENTITY).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO_IDENTITY;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO_IDENTITY;

        public static final String TABLE_NAME = "photo_identity";

        public static final String COLUMN_SMILE = "smile";

        public static final String COLUMN_LEFT_EYE_BLINK = "leye_blink";

        public static final String COLUMN_RIGHT_EYE_BLINK = "reye_blink";

        public static final String COLUMN_YAW = "yaw";

        public static final String COLUMN_PITCH = "pitch";

        public static final String COLUMN_FACE_ROLL = "face_roll";

        public static final String COLUMN_HORIZONTAL_GAZE = "horizontal_gaze";

        public static final String COLUMN_VERTICAL_GAZE = "vertical_gaze";

        public static final String COLUMN_GAZE_POINT_X = "gaze_point_x";

        public static final String COLUMN_GAZE_POINT_Y = "gaze_point_y";

        public static final String COLUMN_RECT_LEFT = "rect_left";

        public static final String COLUMN_RECT_TOP = "rect_top";

        public static final String COLUMN_RECT_RIGHT = "rect_right";

        public static final String COLUMN_RECT_BOTTOM = "rect_bottom";

        public static final String COLUMN_IDENTITY_ID = "identity_id";

        public static final String COLUMN_PHOTO_ID = "photo_id";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
