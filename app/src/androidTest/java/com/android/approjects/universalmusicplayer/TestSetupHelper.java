package com.android.approjects.universalmusicplayer;

import com.android.approjects.universalmusicplayer.model.MusicProvider;
import com.android.approjects.universalmusicplayer.model.MusicProviderSource;

import java.util.concurrent.CountDownLatch;

public class TestSetupHelper {

    public static MusicProvider setupMusicProvider(MusicProviderSource source) throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);

        MusicProvider provider = new MusicProvider(source);

        provider.retrieveMediaAsync(new MusicProvider.Callback() {
            @Override
            public void onMusicCatalogReady(boolean success) {
                signal.countDown();
            }
        });

        signal.await();

        return provider;
    }
}
