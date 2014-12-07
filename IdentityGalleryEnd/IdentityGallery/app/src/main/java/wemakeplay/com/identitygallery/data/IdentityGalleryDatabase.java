package wemakeplay.com.identitygallery.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by josh on 27/11/14.
 */
public class IdentityGalleryDatabase extends SQLiteOpenHelper {

    public static final String TAG = IdentityGalleryDatabase.class.getSimpleName();

    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "identity_gallery.db";

    public IdentityGalleryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Log.d(TAG, "Database created: " + context.getDatabasePath(DATABASE_NAME));
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_IDENTITY_TABLE = "CREATE TABLE " + IdentityGalleryContract.IdentityEntity.TABLE_NAME + " (" +
                IdentityGalleryContract.IdentityEntity._ID + " INTEGER PRIMARY KEY," +
                IdentityGalleryContract.IdentityEntity.COLUMN_PERSON_ID + " INTEGER UNIQUE NOT NULL, " +
                IdentityGalleryContract.IdentityEntity.COLUMN_LABEL + " TEXT);";

        final String SQL_CREATE_PHOTO_TABLE = "CREATE TABLE " + IdentityGalleryContract.PhotoEntity.TABLE_NAME + " (" +
                IdentityGalleryContract.PhotoEntity._ID + " INTEGER PRIMARY KEY," +
                IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID + " INTEGER UNIQUE NOT NULL, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_TITLE + " TEXT, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_DATE_ADDED + " INTEGER, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_DATE_TAKEN + " INTEGER, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_HEIGHT + " INTEGER, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_WIDTH + " INTEGER, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_LAT + " REAL, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_LON + " REAL, " +
                IdentityGalleryContract.PhotoEntity.COLUMN_URI + " TEXT" +
                ");";

        final String SQL_CREATE_FACE_DETAILS_TABLE = "CREATE TABLE " + IdentityGalleryContract.PhotoIdentityEntity.TABLE_NAME + " (" +
                IdentityGalleryContract.PhotoIdentityEntity._ID + " INTEGER PRIMARY KEY," +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_SMILE + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_LEFT_EYE_BLINK + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RIGHT_EYE_BLINK + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_YAW + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PITCH + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_FACE_ROLL + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_HORIZONTAL_GAZE + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_VERTICAL_GAZE + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_GAZE_POINT_X + " REAL, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_GAZE_POINT_Y + " REAL, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_LEFT + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_TOP + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_RIGHT + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_BOTTOM + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_IDENTITY_ID + " INTEGER, " +
                IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PHOTO_ID + "  INTEGER NOT NULL, " +
                " FOREIGN KEY (" + IdentityGalleryContract.PhotoIdentityEntity.COLUMN_IDENTITY_ID + ") REFERENCES " +
                IdentityGalleryContract.IdentityEntity.TABLE_NAME + " (" + IdentityGalleryContract.IdentityEntity._ID + "), " +
                " FOREIGN KEY (" + IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PHOTO_ID + ") REFERENCES " +
                IdentityGalleryContract.PhotoEntity.TABLE_NAME + " (" + IdentityGalleryContract.PhotoEntity._ID + ")" +
                ");";

        Log.d(TAG, SQL_CREATE_FACE_DETAILS_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_IDENTITY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PHOTO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FACE_DETAILS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IdentityGalleryContract.IdentityEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IdentityGalleryContract.PhotoEntity.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IdentityGalleryContract.PhotoIdentityEntity.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
