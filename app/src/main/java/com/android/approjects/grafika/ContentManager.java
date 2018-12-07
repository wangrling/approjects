package com.android.approjects.grafika;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.approjects.R;

import java.io.File;
import java.util.ArrayList;

public class ContentManager {
    private static final String TAG = GrafikaActivity.TAG;

    // Enumerated content tags.  These are used as indices into the mContent ArrayList,
    // so don't make them sparse.
    // TODO: consider using String tags and a HashMap?  prepare() is currently fragile,
    //       depending on the movies being added in tag-order.  Could also just use a plain array.
    public static final int MOVIE_EIGHT_RECTS = 0;
    public static final int MOVIE_SLIDERS = 1;

    private static final int[] ALL_TAGS = new int[] {
            MOVIE_EIGHT_RECTS,
            MOVIE_SLIDERS
    };

    // Housekeeping.
    private static final Object sLock = new Object();
    private static ContentManager sInstance = null;

    private boolean mInitialized = false;
    private File mFileDir;

    private ArrayList<Content> mContent;

    /**
     * Returns the singleton instance.
     */
    public static ContentManager getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ContentManager();
            }
            return sInstance;
        }
    }

    private ContentManager() {

    }

    public static void initialize(Context context) {
        ContentManager mgr = getInstance();
        synchronized (sLock) {
            if (!mgr.mInitialized) {
                mgr.mFileDir = context.getFilesDir();
                mgr.mContent = new ArrayList<>();
                mgr.mInitialized = true;
            }
        }
    }

    /**
     * Returns true if all of the content has been created.
     * <p>
     * If this returns false, call createAll.
     */
    public boolean isContentCreated(@SuppressWarnings("unused") Context unused) {
        // Ideally this would probe each individual item to see if anything needs to be done,
        // and a subsequent "prepare" call would generate only the necessary items.  This
        // takes a much simpler approach and just checks to see if the files exist.  If the
        // content changes the user will need to force a regen (via a menu option) or wipe data.

        for (int i = 0; i < ALL_TAGS.length; i++) {
            File file = getPath(i);
            if (!file.canRead()) {
                Log.d(TAG, "Can't find readable " + file);
                return false;
            }
        }
        return true;
    }

    /**
     * Create all content, overwriting any existing entries.
     *
     * Call from main UI thread.
     */
    public void createAll(Activity caller) {
        prepareContent(caller, ALL_TAGS);
    }

    /**
     * Prepares the specified content.  For example, if the caller requires a movie that doesn't
     * exist, this will post a progress dialog and generate the movie.
     * <p>
     * Call from main UI thread.  This returns immediately.  Content generation continues
     * on a background thread.
     */
    public void prepareContent(Activity caller, int[] tags) {
        // Put up the progress dialog.
        AlertDialog.Builder builder = WorkDialog.create(caller, R.string.preparing_content);
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();

        // Generate content in async task.
        GenerateTask genTask = new GenerateTask(caller, dialog, tags);
        genTask.execute();
    }

    private void prepare(ProgressUpdater prog, int tag) {
        GenerateMovie movie;
        switch (tag) {
            case MOVIE_EIGHT_RECTS:
                movie = new MovieEightRects();
                movie.create(getPath(tag), prog);
                synchronized (mContent) {
                    mContent.add(tag, movie);
                }
        }
    }

    /**
     * Returns the filename for the tag.
     */
    private String getFileName(int tag) {
        switch (tag) {
            case MOVIE_EIGHT_RECTS:
                return "gen-eight-rects.mp4";
            case MOVIE_SLIDERS:
                return "gen-sliders.mp4";
            default:
                throw new RuntimeException("Unknown tag " + tag);
        }
    }

    /**
     * Returns the storage location for the specified item.
     */
    public File getPath(int tag) {
        return new File(mFileDir, getFileName(tag));
    }

    public interface ProgressUpdater {
        /**
         * Updates a progress meter.
         * @param percent Percent completed (0-100).
         */
        void updateProgress(int percent);

    }

    /**
     * Performs generation of content on an async task thread.
     */
    private static class GenerateTask extends AsyncTask<Void, Integer, Integer> implements
            ProgressUpdater {

        // accessed from UI thread.
        private final Context mContext;
        private final AlertDialog mPrepDialog;
        private final ProgressBar mProgressBar;

        // accessed from async thread.
        private int mCurrentIndex;

        // accessed from both.
        private final int[] mTags;
        private volatile RuntimeException mFailure;

        public GenerateTask(Context context, AlertDialog dialog, int[] tags) {
            mContext = context;
            mPrepDialog = dialog;
            mTags = tags;
            mProgressBar = (ProgressBar) mPrepDialog.findViewById(R.id.work_progress);
            mProgressBar.setMax(tags.length * 100);
        }

        @Override       // async task thread
        protected Integer doInBackground(Void... voids) {
            ContentManager contentManager = ContentManager.getInstance();

            Log.d(TAG, "onInBackground...");
            for (int i = 0; i < mTags.length; i++) {
                mCurrentIndex = i;
                updateProgress(0);
                try {
                    contentManager.prepare(this, mTags);
                } catch (RuntimeException re) {
                    mFailure = re;
                    break;
                }
                updateProgress(100);
            }
        }

        @Override
        public void updateProgress(int percent) {

        }
    }
}
