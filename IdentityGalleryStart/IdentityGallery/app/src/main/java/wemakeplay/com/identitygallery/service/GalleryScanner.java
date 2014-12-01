package wemakeplay.com.identitygallery.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import wemakeplay.com.identitygallery.data.IdentityGalleryContract;

/**
 * Created by josh on 30/11/14.
 */
public class GalleryScanner {

    final String[] mMediaProjection = new String[] { MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE };

    Context mContext;
    long mLastPhotoAddedDate = 0;

    int mTotalRecords = 0;

    public GalleryScanner(Context context, long lastPhotoAddedDate){
        mContext = context;
        mLastPhotoAddedDate = lastPhotoAddedDate;
    }

    public void scan(){
        Cursor externalCursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mMediaProjection,
                MediaStore.Images.Media.DATE_ADDED + " > ?",
                new String[] { Long.toString(mLastPhotoAddedDate) },
                MediaStore.Images.Media.DATE_ADDED + " ASC");

        Cursor internalCursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI, mMediaProjection,
                MediaStore.Images.Media.DATE_ADDED + " > ?",
                new String[] { Long.toString(mLastPhotoAddedDate) },
                MediaStore.Images.Media.DATE_ADDED + " ASC");

        mTotalRecords = externalCursor.getCount() + internalCursor.getCount();

        if(externalCursor.moveToFirst()) {
            do{
                ContentValues contentValues = createPhotoValuesFromCursor(externalCursor, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // check if photo already exists
                Cursor photoCursor = mContext.getContentResolver().query(
                        IdentityGalleryContract.PhotoEntity.CONTENT_URI,
                        null,
                        IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID + " = ?",
                        new String[]{contentValues.getAsInteger(IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID).toString()},
                        null
                );
                Uri contentUri = null;
                if (photoCursor == null || photoCursor.getCount() <= 0) {
                    contentUri = mContext.getContentResolver().insert(IdentityGalleryContract.PhotoEntity.CONTENT_URI, contentValues);
                } else {
                    // assume it has already been processed
                    continue;
                }
                photoCursor.close();
            } while(externalCursor.moveToNext());
        }
        externalCursor.close();

        if(internalCursor.moveToFirst()) {
            do{
                ContentValues contentValues = createPhotoValuesFromCursor(internalCursor, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                // check if photo already exists
                Cursor photoCursor = mContext.getContentResolver().query(
                        IdentityGalleryContract.PhotoEntity.CONTENT_URI,
                        null,
                        IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID + " = ?",
                        new String[]{contentValues.getAsInteger(IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID).toString()},
                        null
                );
                Uri contentUri = null;
                if (photoCursor.getCount() <= 0) {
                    contentUri = mContext.getContentResolver().insert(IdentityGalleryContract.PhotoEntity.CONTENT_URI, contentValues);
                } else { // assume it has already been processed
                    continue;
                }
                photoCursor.close();
            }while (internalCursor.moveToNext());
        }
        internalCursor.close();
    }

    private ContentValues createPhotoValuesFromCursor(Cursor cursor, Uri queryUri) {
        ContentValues value = new ContentValues();
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_DATE_ADDED, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)) * 1000L);
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_DATE_TAKEN, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)) * 1000L);
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_HEIGHT, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)));
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_WIDTH, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)));
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_LAT, cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)));
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_LON, cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)));
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
        Uri uri = Uri.withAppendedPath(queryUri, Long.toString(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))));
        value.put(IdentityGalleryContract.PhotoEntity.COLUMN_URI, uri.toString());

        return value;
    }
}
