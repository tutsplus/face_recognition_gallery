package wemakeplay.com.identitygallery;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import wemakeplay.com.identitygallery.data.IdentityGalleryContract;

/**
 * Created by josh on 29/11/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String TAG = TestProvider.class.getSimpleName();

    final Random mRandom = new Random(System.currentTimeMillis());
    final List<Long> mPhotoIdList = new Vector<Long>();
    final List<Long> mIdentityIdList = new Vector<Long>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteAllRecords();
        super.tearDown();
    }

    public void test_insert_photos(){
        final int COUNT = 10;

        for(int i=0; i<COUNT; i++){
            ContentValues values = getContentValuesForPhoto();

            Uri photoUri = mContext.getContentResolver().insert(IdentityGalleryContract.PhotoEntity.CONTENT_URI, values);
            long photoRowId = ContentUris.parseId(photoUri);

            if(photoRowId != -1){
                mPhotoIdList.add(photoRowId);
            }
        }
    }

    public void test_insert_identities() {
        final int COUNT = 10;

        for(int i=0; i<COUNT; i++){
            ContentValues values = getContentValuesForIdentity(mRandom.nextInt(9999));

            Uri identityUri = mContext.getContentResolver().insert(IdentityGalleryContract.IdentityEntity.CONTENT_URI, values);
            long identityRowId = ContentUris.parseId(identityUri);

            if(identityRowId != -1){
                mIdentityIdList.add(identityRowId);
            }
        }

        assertTrue(mIdentityIdList.size() == COUNT);
    }

    public void test_insert_photoidentities() {
        test_insert_photos();
        test_insert_identities();

        final int COUNT = 10;

        for(int i=0; i<COUNT; i++){
            Long photoId = mPhotoIdList.get(mRandom.nextInt(mPhotoIdList.size()));
            Long identityId = mPhotoIdList.get(mRandom.nextInt(mIdentityIdList.size()));

            ContentValues values = getContentValuesForPhotoIdentity(identityId, photoId);

            Uri photoIdentityUri = mContext.getContentResolver().insert(IdentityGalleryContract.PhotoIdentityEntity.CONTENT_URI, values);
            long photoIdentityRowId = ContentUris.parseId(photoIdentityUri);

            assertTrue(photoIdentityRowId != -1);
        }
    }

    private void insertDummyData(){
        test_insert_photoidentities();
    }

    public void test_query_photos() {
        insertDummyData();

        Cursor cursor = mContext.getContentResolver().query(
                IdentityGalleryContract.PhotoEntity.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();

        assertTrue(count == mPhotoIdList.size());
    }

    public void test_query_identities() {
        insertDummyData();

        Cursor cursor = mContext.getContentResolver().query(
                IdentityGalleryContract.IdentityEntity.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();

        assertTrue(count == mIdentityIdList.size());
    }

    public void test_query_photoidentities() {
        insertDummyData();

        Cursor cursor = mContext.getContentResolver().query(
                IdentityGalleryContract.PhotoIdentityEntity.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();

        assertTrue(count > 0);
    }

    public void test_query_photo_with_reference() {
        insertDummyData();

        Cursor cursor = mContext.getContentResolver().query(
                IdentityGalleryContract.PhotoIdentityEntity.CONTENT_URI,
                new String[]{IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PHOTO_ID},
                null,
                null,
                null
        );

        long photoId = -1;

        if(cursor.moveToFirst()){
            photoId = cursor.getLong(cursor.getColumnIndex(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PHOTO_ID));
        }

        cursor.close();

        if(photoId == -1){
            fail();
        }

        cursor = mContext.getContentResolver().query(
                IdentityGalleryContract.PhotoEntity.buildUriWithReferencePhoto(photoId),
                null,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();

        assertTrue(count >= 1);
    }

    public void test_update_identitylabel() {
        insertDummyData();

        final int COUNT = 5;
        final String[] RANDOM_NAMES = new String[]{"Sam", "Molly", "Milli", "Gio", "George", "Tom", "Mike", "Kelly"};

        for(int i=0; i<COUNT; i++){
            Long identityId = mPhotoIdList.get(mRandom.nextInt(mIdentityIdList.size()));
            ContentValues values = new ContentValues();
            values.put(
                    IdentityGalleryContract.IdentityEntity.COLUMN_LABEL, RANDOM_NAMES[mRandom.nextInt(RANDOM_NAMES.length)]);

            assertTrue(mContext.getContentResolver().update(
                    IdentityGalleryContract.IdentityEntity.CONTENT_URI,
                    values,
                    String.format("%s = ?", IdentityGalleryContract.IdentityEntity._ID),
                    new String[]{Long.toString(identityId)}) == 1);
        }
    }

    private ContentValues getContentValuesForPhotoIdentity(long identityId, long photoId){
        ContentValues values = new ContentValues();

        values.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PHOTO_ID, photoId);
        values.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_IDENTITY_ID, identityId);
        values.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_SMILE, 100);

        return values;
    }

    private ContentValues getContentValuesForIdentity(long personId){
        ContentValues values = new ContentValues();

        values.put(IdentityGalleryContract.IdentityEntity.COLUMN_LABEL, "person");
        values.put(IdentityGalleryContract.IdentityEntity.COLUMN_PERSON_ID, personId);

        return values;
    }

    private ContentValues getContentValuesForPhoto(){
        ContentValues values = new ContentValues();

        values.put(IdentityGalleryContract.PhotoEntity.COLUMN_WIDTH, 200);
        values.put(IdentityGalleryContract.PhotoEntity.COLUMN_HEIGHT, 200);
        values.put(IdentityGalleryContract.PhotoEntity.COLUMN_DATE_ADDED, System.currentTimeMillis());
        values.put(IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID, 100 + mRandom.nextInt(99999));
        values.put(IdentityGalleryContract.PhotoEntity.COLUMN_URI, "/image/filename.png");
        values.put(IdentityGalleryContract.PhotoEntity.COLUMN_TITLE, "photo");

        return values;
    }

    private void deleteAllRecords(){
        mContext.getContentResolver().delete(
                IdentityGalleryContract.PhotoIdentityEntity.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                IdentityGalleryContract.IdentityEntity.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                IdentityGalleryContract.PhotoEntity.CONTENT_URI,
                null,
                null
        );
    }
}
