package com.android.approjects.universalmusicplayer.utils;

/**
 * Utility class to help on queue related tasks.
 */

public class MediaIDHelper {
    // Media IDs used on browseable items of MediaBrowser
    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__";
    public static final String MEDIA_ID_ROOT = "__ROOT__";

    public static final String MEDIA_ID_MUSICS_BY_GENRE = "__BY_GENRE__";
    public static final String MEDIA_ID_MUSICS_BY_SEARCH = "__BY_SEARCH__";

    private static final char CATEGORY_SEPARATOR = '/';
    private static final char LEAF_SEPARATOR = '|';
}
