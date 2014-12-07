package wemakeplay.com.identitygallery.service;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessingConstants;

import java.io.IOException;
import java.util.Arrays;

import wemakeplay.com.identitygallery.data.IdentityGalleryContract;
import wemakeplay.com.identitygallery.utils.ImageUtils;

/**
 * Created by josh on 30/11/14.
 */
public class GalleryScanner {

    static final String TAG = GalleryScanner.class.getSimpleName();

    static final String KEY_IDENTITY_ALBUM = "identity_album";

    static final String[] mMediaProjection = new String[] { MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE };

    Context mContext;
    long mLastPhotoAddedDate = 0;

    int mTotalRecords = 0;
    FacialProcessing mFacialProcessing = null;
    int mConfidenceThreshold = 57;

    public GalleryScanner(Context context, long lastPhotoAddedDate){
        mContext = context;
        mLastPhotoAddedDate = lastPhotoAddedDate;
    }

    public void scan() throws UnsupportedOperationException{
        initFacialProcessing();

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

                // hook method
                processImage(contentValues, contentUri);
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

                // hook method
                processImage(contentValues, contentUri);
            }while (internalCursor.moveToNext());
        }
        internalCursor.close();

        deinitFacialProcessing();
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

    private void initFacialProcessing() throws UnsupportedOperationException{
        if( !FacialProcessing.isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_PROCESSING) ||
                !FacialProcessing.isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_RECOGNITION)){
            throw new UnsupportedOperationException("Facial Processing or Recognition is not supported on this device");
        }

        mFacialProcessing = FacialProcessing.getInstance();				// Calling the Facial Processing Constructor.

        if(mFacialProcessing != null){
            // Recognition confidence is the confidence value with which the Facial Recognition engine identifies a given face.
            // The setRecognitionConfidence method tells the face engine to ignore any face data matched with a confidence value below the set threshold.
            mFacialProcessing.setRecognitionConfidence(mConfidenceThreshold);
            // FP_MODE_STILL for static images and FP_MODE_VIDEO using live camera preview and will constantly process frames
            mFacialProcessing.setProcessingMode(FacialProcessing.FP_MODES.FP_MODE_STILL);
            loadAlbum();
        } else{
            throw new UnsupportedOperationException("An instance is already in use");
        }
    }

    private void loadAlbum(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(TAG, 0);
        String arrayOfString = sharedPreferences.getString(KEY_IDENTITY_ALBUM, null);

        byte[] albumArray = null;
        if (arrayOfString != null) {
            String[] splitStringArray = arrayOfString.substring(1,
                    arrayOfString.length() - 1).split(", ");

            albumArray = new byte[splitStringArray.length];
            for (int i = 0; i < splitStringArray.length; i++) {
                albumArray[i] = Byte.parseByte(splitStringArray[i]);
            }
            mFacialProcessing.deserializeRecognitionAlbum(albumArray);
        }
    }

    private void processImage(ContentValues contentValues, Uri contentUri){
        long photoRowId = ContentUris.parseId(contentUri);

        String uriAsString = contentValues.getAsString(IdentityGalleryContract.PhotoEntity.COLUMN_URI);
        Uri uri = Uri.parse(uriAsString);
        Bitmap bitmap = null;
        try{
            bitmap = ImageUtils.getImage(mContext, uri);
        } catch(IOException e){
            Log.e(TAG, "process - " + e.toString());
            return;
        }

        if(bitmap != null){
            if( !mFacialProcessing.setBitmap(bitmap)){
                return;
            }

            int numFaces = mFacialProcessing.getNumFaces();

            if(numFaces > 0){
                FaceData[] faceDataArray = mFacialProcessing.getFaceData();

                if( faceDataArray == null){
                    Log.w(TAG, contentUri.toString() + " has been returned a NULL FaceDataArray");
                    return;
                }

                for(int i=0; i<faceDataArray.length; i++) {
                    FaceData faceData = faceDataArray[i];
                    if(faceData == null){
                        continue;
                    }

                    int personId = faceData.getPersonId();
                    if(personId == FacialProcessingConstants.FP_PERSON_NOT_REGISTERED){
                        personId = mFacialProcessing.addPerson(i);
                    } else{
                        if(mFacialProcessing.updatePerson(personId, i) != FacialProcessingConstants.FP_SUCCESS){
                            // TODO handle error
                        }
                    }

                    long identityRowId = getOrInsertPerson(personId);

                    int smileValue = faceData.getSmileValue();
                    int leftEyeBlink = faceData.getLeftEyeBlink();
                    int rightEyeBlink = faceData.getRightEyeBlink();
                    int roll = faceData.getRoll();
                    PointF gazePointValue = faceData.getEyeGazePoint();
                    int pitch = faceData.getPitch();
                    int yaw = faceData.getYaw();
                    int horizontalGaze = faceData.getEyeHorizontalGazeAngle();
                    int verticalGaze = faceData.getEyeVerticalGazeAngle();
                    Rect faceRect = faceData.rect;

                    insertNewPhotoIdentityRecord(photoRowId, identityRowId,
                            gazePointValue, horizontalGaze, verticalGaze,
                            leftEyeBlink, rightEyeBlink,
                            pitch, yaw, roll,
                            smileValue, faceRect);
                }
            }
        }
    }

    private long insertNewPhotoIdentityRecord(long photoRowId, long identityRowId,
                                              PointF gazePointValue, int horizontalGaze, int verticalGaze,
                                              int leftEyeBlink, int rightEyeBlink,
                                              int pitch, int yaw, int roll,
                                              int smileValue, Rect faceRect){
        ContentValues photoIdentityValues = new ContentValues();

        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PHOTO_ID, photoRowId);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_IDENTITY_ID, identityRowId);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_GAZE_POINT_X, gazePointValue.x);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_GAZE_POINT_Y, gazePointValue.y);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_HORIZONTAL_GAZE, horizontalGaze);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_VERTICAL_GAZE, verticalGaze);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_LEFT_EYE_BLINK, leftEyeBlink);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RIGHT_EYE_BLINK, rightEyeBlink);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PITCH, pitch);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_YAW, yaw);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_FACE_ROLL, roll);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_SMILE, smileValue);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_LEFT, faceRect.left);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_TOP, faceRect.top);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_RIGHT, faceRect.right);
        photoIdentityValues.put(IdentityGalleryContract.PhotoIdentityEntity.COLUMN_RECT_BOTTOM, faceRect.bottom);

        Uri photoIdentityUri = mContext.getContentResolver().insert(IdentityGalleryContract.PhotoIdentityEntity.CONTENT_URI, photoIdentityValues);

        return ContentUris.parseId(photoIdentityUri);
    }

    /**
     * query for the identity Id for a specific person, if exists, otherwise insert a new identity
     * returning the Id
     * @param personId
     * @return
     */
    private long getOrInsertPerson(int personId){
        Cursor cursor = mContext.getContentResolver().query(
                IdentityGalleryContract.IdentityEntity.CONTENT_URI,
                new String[]{IdentityGalleryContract.IdentityEntity._ID},
                IdentityGalleryContract.IdentityEntity.COLUMN_PERSON_ID + " = ?",
                new String[]{Integer.toString(personId)},
                null
        );

        long identityRowId = 0L;

        if(cursor == null || cursor.getCount() <= 0){ // doesn't exist so create a new record
            ContentValues identityValues = new ContentValues();
            identityValues.put(IdentityGalleryContract.IdentityEntity.COLUMN_PERSON_ID, personId);
            identityValues.put(IdentityGalleryContract.IdentityEntity.COLUMN_LABEL, "undefined");
            Uri identityUri = mContext.getContentResolver().insert(IdentityGalleryContract.IdentityEntity.CONTENT_URI, identityValues);
            identityRowId = ContentUris.parseId(identityUri);
        } else{ // already exists
            if(cursor.moveToFirst()) {
                identityRowId = cursor.getLong(cursor.getColumnIndex(IdentityGalleryContract.IdentityEntity._ID));
            }
        }

        cursor.close();

        return identityRowId;
    }

    private void deinitFacialProcessing(){
        if(mFacialProcessing != null){
            saveAlbum();
            mFacialProcessing.release();
            mFacialProcessing = null;
        }
    }

    private void saveAlbum(){
        byte[] albumBuffer = mFacialProcessing.serializeRecogntionAlbum();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(TAG, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IDENTITY_ALBUM, Arrays.toString(albumBuffer));
        editor.commit();
    }
}
