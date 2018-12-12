package com.android.approjects.musicfx;

import android.os.AsyncTask;

public class MusicRetrieverTask extends AsyncTask<Void, Void, Void> {
    /**
     * Asynchronous task that prepares a MusicRetriever. This asynchronous task essentially calls
     * {@link MusicRetriever#prepare()} on a {@link MusicRetriever}, which may take some time to
     * run. Upon finishing, it notifies the indicated {@MusicRetrieverPreparedListener}.
     */
    MusicRetriever mRetriever;
    PreparedListener mListener;

    public MusicRetrieverTask(MusicRetriever retriever, PreparedListener listener) {
        mRetriever = retriever;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mRetriever.prepare();
        return null;
    }

    // 加载完成之后需要回调
    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.onMusicRetrieverPrepared();
    }

    public interface PreparedListener {
        public void onMusicRetrieverPrepared();
    }
}
