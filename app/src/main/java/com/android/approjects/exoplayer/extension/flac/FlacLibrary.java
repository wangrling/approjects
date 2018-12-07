package com.android.approjects.exoplayer.extension.flac;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.util.LibraryLoader;

/**
 * Configures and queries the underlying native library.
 */

public final class FlacLibrary {

    static {
        ExoPlayerLibraryInfo.registerModule("goog.exo.flac");
    }

    private static final LibraryLoader LOADER = new LibraryLoader("flacJNI");

    private FlacLibrary() {

    }

    /**
     * Override the names of the Flac native libraries. If an application wishes to call this method,
     * it must do so before calling any other method defined by this class, and before instantiating
     * any {@link LibflacAudioRenderer} and {@link FlacExtractor} instances.
     *
     * @param libraries The names of the Flac native libraries.
     */
    public static void setLibraries(String... libraries) {
        LOADER.setLibraries(libraries);
    }

    /**
     * Return whether the underlying library is available, loading it if necessary.
     */
    // System.loadLibrary("flacJNI");
    public static boolean isAvailable() {
        return LOADER.isAvailable();
    }
}
