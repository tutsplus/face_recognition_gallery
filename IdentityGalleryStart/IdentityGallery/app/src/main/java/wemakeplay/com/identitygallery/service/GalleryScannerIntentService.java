package wemakeplay.com.identitygallery.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import wemakeplay.com.identitygallery.data.IdentityGalleryContract;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class GalleryScannerIntentService extends IntentService {

    public static final String TAG = GalleryScannerIntentService.class.getSimpleName();

    private static final String ACTION_SCAN = "wemakeplay.com.identitygallery.service.action.SCAN";

    /**
     * Starts this service to perform action Scan.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionScan(Context context) {
        Intent intent = new Intent(context, GalleryScannerIntentService.class);
        intent.setAction(ACTION_SCAN);
        context.startService(intent);
    }

    public GalleryScannerIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SCAN.equals(action)) {
                handleActionScan();
            }
        }
    }

    /**
     * Handle action Scan in the provided background thread
     */
    private void handleActionScan() {
        try {
            new GalleryScanner(this.getApplicationContext(), getLastPhotoAddedDate()).scan();
        } catch(UnsupportedOperationException e){

        }
    }

    private long getLastPhotoAddedDate(){
        Cursor cursor = getContentResolver().query(
                IdentityGalleryContract.PhotoEntity.CONTENT_URI,
                new String[]{IdentityGalleryContract.PhotoEntity.COLUMN_DATE_ADDED},
                null,
                null,
                String.format("%s DESC", IdentityGalleryContract.PhotoEntity.COLUMN_DATE_ADDED)
        );

        long lastPhotoAdded = 0L;

        if(cursor.moveToFirst()){
            lastPhotoAdded = cursor.getLong(cursor.getColumnIndex(IdentityGalleryContract.PhotoEntity.COLUMN_DATE_ADDED));
        }
        cursor.close();

        return lastPhotoAdded;
    }
}
