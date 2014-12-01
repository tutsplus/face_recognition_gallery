package wemakeplay.com.identitygallery.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class IdentityGalleryProvider extends ContentProvider {

    public static final String TAG = IdentityGalleryProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private IdentityGalleryDatabase mOpenHelper;

    private static final int PHOTO = 100;

    private static final int IDENTITY = 200;

    private static final int PHOTO_IDENTITIES = 300;

    private static final int PHOTO_WITH_REFERENCE = 500;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = IdentityGalleryContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, IdentityGalleryContract.PATH_PHOTO, PHOTO);

        matcher.addURI(authority, IdentityGalleryContract.PATH_IDENTITY, IDENTITY);

        matcher.addURI(authority, IdentityGalleryContract.PATH_PHOTO_IDENTITY, PHOTO_IDENTITIES);

        matcher.addURI(authority, IdentityGalleryContract.PATH_PHOTO + "/*", PHOTO_WITH_REFERENCE);

        return matcher;
    }

    public IdentityGalleryProvider() {
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new IdentityGalleryDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PHOTO:
                return IdentityGalleryContract.PhotoEntity.CONTENT_TYPE;

            case IDENTITY:
                return IdentityGalleryContract.IdentityEntity.CONTENT_TYPE;

            case PHOTO_IDENTITIES:
                return IdentityGalleryContract.PhotoIdentityEntity.CONTENT_TYPE;

            case PHOTO_WITH_REFERENCE:
                return IdentityGalleryContract.PhotoEntity.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {

            case PHOTO: {
                retCursor = queryPhoto(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case IDENTITY: {
                retCursor = queryIdentity(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case PHOTO_IDENTITIES: {
                retCursor = queryPhotoIdentities(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case PHOTO_WITH_REFERENCE: {
                retCursor = queryPhotoWithReference(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor queryPhoto(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                IdentityGalleryContract.PhotoEntity.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor queryIdentity(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                IdentityGalleryContract.IdentityEntity.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor queryPhotoIdentities(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                IdentityGalleryContract.PhotoIdentityEntity.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor queryPhotoWithReference(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        long referencePhotoId = -1;
        try{
            referencePhotoId = Long.parseLong(
                    IdentityGalleryContract.PhotoEntity.getReferenceIdFromUri(uri)
            );
        } catch(NumberFormatException e){
            return queryPhoto(uri, projection, selection, selectionArgs, sortOrder);
        }

        // find all photos that have identities that are in the referenced photo referencePhotoId (inclusive)
        final String SQL =
                "SELECT DISTINCT photo._id, photo.media_id, photo.title, photo.date_added, photo.date_taken, photo.height, photo.width, photo.lat, photo.lon, photo.uri " +
                        " FROM photo " +
                        " JOIN photo_identity ON photo_identity.photo_id = photo._id " +
                        " WHERE photo_identity.identity_id IN (" +
                        " SELECT photo_identity_b.identity_id " +
                        " FROM photo_identity AS photo_identity_b " +
                        " WHERE photo_identity_b.photo_id = ?)" +
                        " ORDER BY photo.date_added DESC";

        return mOpenHelper.getReadableDatabase().rawQuery(
                SQL,
                new String[]{Long.toString(referencePhotoId)}
        );
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PHOTO:
                rowsDeleted = db.delete(IdentityGalleryContract.PhotoEntity.TABLE_NAME, selection, selectionArgs);
                break;
            case IDENTITY:
                rowsDeleted = db.delete(IdentityGalleryContract.IdentityEntity.TABLE_NAME, selection, selectionArgs);
                break;
            case PHOTO_IDENTITIES:
                rowsDeleted = db.delete(IdentityGalleryContract.PhotoIdentityEntity.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PHOTO: {
                long _id = db.insert(IdentityGalleryContract.PhotoEntity.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = IdentityGalleryContract.PhotoEntity.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case IDENTITY: {
                long _id = db.insert(IdentityGalleryContract.IdentityEntity.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = IdentityGalleryContract.IdentityEntity.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PHOTO_IDENTITIES: {
                long _id = db.insert(IdentityGalleryContract.PhotoIdentityEntity.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = IdentityGalleryContract.PhotoIdentityEntity.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PHOTO:
                rowsUpdated = db.update(IdentityGalleryContract.PhotoEntity.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case IDENTITY:
                rowsUpdated = db.update(IdentityGalleryContract.IdentityEntity.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case PHOTO_IDENTITIES:
                rowsUpdated = db.update(IdentityGalleryContract.PhotoIdentityEntity.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
