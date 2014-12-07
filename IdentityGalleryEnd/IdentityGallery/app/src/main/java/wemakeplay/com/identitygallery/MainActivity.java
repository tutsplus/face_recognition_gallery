package wemakeplay.com.identitygallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import wemakeplay.com.identitygallery.data.IdentityGalleryContract;
import wemakeplay.com.identitygallery.service.GalleryScannerIntentService;
import wemakeplay.com.identitygallery.utils.ImageUtils;


public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();

    GridView mGridView;
    GalleryCursorAdapter mGridAdapter;

    long mFilterPhotoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // kick off a scan
        GalleryScannerIntentService.startActionScan(this.getApplicationContext());

        getActionBar().setTitle(mFilterPhotoId == -1 ? "Gallery" : "Gallery (filtered)");

        mGridView = (GridView)findViewById(R.id.grid_view);

        mGridAdapter = new GalleryCursorAdapter(this, getGalleryCursor(), true);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(view.getTag() != null){
                    String photoId = (String)view.getTag();
                    updateFilterPhotoId(Long.parseLong(photoId));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_filter:
                updateFilterPhotoId(-1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mFilterPhotoId != -1)
            getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private void updateFilterPhotoId(long id){
        if(mFilterPhotoId == id)
            return;

        if(!isValid(id))
            return;

        mFilterPhotoId = id;

        getActionBar().setTitle(mFilterPhotoId == -1 ? "Gallery" : "Gallery (filtered)");

        updateCursor();
        invalidateOptionsMenu();
    }

    /** make sure this photo has detected identities **/
    private boolean isValid(long id){
        if(id == -1){
            return true;
        }
        
        Cursor cursor = getContentResolver().query(
                IdentityGalleryContract.PhotoIdentityEntity.CONTENT_URI,
                new String[]{IdentityGalleryContract.PhotoIdentityEntity._ID},
                String.format("%s = ?", IdentityGalleryContract.PhotoIdentityEntity.COLUMN_PHOTO_ID),
                new String[]{Long.toString(id)},
                null
        );

        int count = cursor.getCount();

        return count > 0;
    }

    private Cursor getGalleryCursor(){
        return getContentResolver().query(
                getGalleryUri(),
                null,
                null,
                null,
                String.format("%s DESC", IdentityGalleryContract.PhotoEntity.COLUMN_DATE_ADDED)
        );
    }

    private Uri getGalleryUri(){
        if(mFilterPhotoId < 0){
            return IdentityGalleryContract.PhotoEntity.CONTENT_URI;
        } else{
            return IdentityGalleryContract.PhotoEntity.buildUriWithReferencePhoto(mFilterPhotoId);
        }
    }

    private void updateCursor(){
        mGridAdapter.changeCursor(getGalleryCursor());
    }

    private class GalleryCursorAdapter extends CursorAdapter {
        private LayoutInflater mLayoutInflater;

        public GalleryCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return mLayoutInflater.inflate(R.layout.grid_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Bitmap bitmap = null;
            long mediaId = cursor.getLong(cursor.getColumnIndex(IdentityGalleryContract.PhotoEntity.COLUMN_MEDIA_ID));
            bitmap = ImageUtils.getMiniThumbnailForImage(MainActivity.this, mediaId);

            view.setTag(Long.toString(cursor.getLong(cursor.getColumnIndex(IdentityGalleryContract.PhotoEntity._ID))));
            ImageView imageView = (ImageView)view.findViewById(R.id.image_view);
            imageView.setImageBitmap(bitmap);
        }
    }
}
