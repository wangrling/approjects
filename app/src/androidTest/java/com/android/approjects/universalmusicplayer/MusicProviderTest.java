package com.android.approjects.universalmusicplayer;

import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;

import com.android.approjects.universalmusicplayer.model.MusicProvider;
import com.android.approjects.universalmusicplayer.utils.SimpleMusicProviderSource;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Android instrumentation unit tests for {@link MusicProvider} and related classes.
 */
public class MusicProviderTest {

    private MusicProvider provider;

    @Before
    public void setupMusicProvider() throws InterruptedException {
        SimpleMusicProviderSource source = new SimpleMusicProviderSource();
        source.add("Music 1", "Album 1", "Smith Singer", "Genre 1",
                "https://examplemusic.com/music1.mp3", "https://icons.com/album1.png",
                1, 3, 3200);
        source.add("Music 2", "Album 1", "Joe Singer", "Genre 1",
                "https://examplemusic.com/music2.mp3", "https://icons.com/album1.png", 2, 3, 3300);
        source.add("Music 3", "Album 1", "John Singer", "Genre 1",
                "https://examplemusic.com/music3.mp3", "https://icons.com/album1.png", 3, 3, 3400);
        source.add("Romantic Song 1", "Album 2", "Joe Singer", "Genre 2",
                "https://examplemusic.com/music4.mp3", "https://icons.com/album2.png", 1, 2, 4200);
        source.add("Romantic Song 2", "Album 2", "Joe Singer", "Genre 2",
                "https://examplemusic.com/music5.mp3", "https://icons.com/album2.png", 2, 2, 4200);

        provider = TestSetupHelper.setupMusicProvider(source);
    }

    @Test
    public void testGetGenres() throws Exception {
        Iterable<String> genres = provider.getGenres();
        ArrayList<String> list = new ArrayList<>();

        for (String genre : genres) {
            list.add(genre);
        }

        // "Genres 1", "Genres 2".
        assertEquals(2, list.size());

        Collections.sort(list);
        assertEquals(Arrays.asList(new String[] {"Genre 1", "Genre 2"}), list);
    }

    @Test
    public void testGetMusicsByGenre() {
        int count = 0;
        for (MediaMetadataCompat metadata : provider.getMusicsByGenre("Genre 1")) {
            String genre = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            assertEquals("Genre 1", genre);
            count++;
        }

        assertEquals(3, count);
    }

    @Test
    public void testGetMusicByInvalidGenre() {
        assertFalse(provider.getMusicsByGenre("ABC").iterator().hasNext());
    }

    @Test
    public void testSearchBySongTitle() {
        int count = 0;
        for (MediaMetadataCompat metadata : provider.searchMusicBySongTitle("Romantic")) {
            String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            assertTrue(title.contains("Romantic"));
            count++;
        }

        // "Romantic Song 1", "Romantic Song 2".
        assertEquals(2, count);
    }

    @Test
    public void testSearchByInvalidSongTitle() {
        assertFalse(provider.searchMusicBySongTitle("XYZ").iterator().hasNext());
    }

    @Test
    public void testSearchMusicByAlbum() {
        int count = 0;
        for (MediaMetadataCompat metadata : provider.searchMusicByAlbum("Album")) {
            String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            assertTrue(album.contains("Album"));
            count++;
        }

        assertEquals(5, count);
    }

    @Test
    public void testSearchMusicByInvalidAlbum() {
        assertFalse(provider.searchMusicByAlbum("XYZ").iterator().hasNext());
    }

    @Test
    public void testSearchMusicByArtist() throws Exception {
        int count = 0;
        for (MediaMetadataCompat metadata : provider.searchMusicByArtist("Joe")) {
            String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            assertTrue(title.contains("Joe"));
            count++;
        }

        assertEquals(3, count);
    }

    @Test
    public void testSearchMusicByInvalidArtist() throws Exception {
        assertFalse(provider.searchMusicByArtist("XYZ").iterator().hasNext());
    }

    @Test
    public void testUpdateMusicArt() {
        Bitmap bIcon = Bitmap.createBitmap(2, 2, Bitmap.Config.ALPHA_8);
        Bitmap bArt = Bitmap.createBitmap(2, 2, Bitmap.Config.ALPHA_8);

        MediaMetadataCompat metadata = provider.getShuffledMusic().iterator().next();

        // 唯一标识符
        // KEY_MEDIA_ID
        String musicId = metadata.getDescription().getMediaId();

        assertNotEquals(bArt, metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
        assertNotEquals(bIcon, metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON));


        provider.updateMusicArt(musicId, bArt, bIcon);

        MediaMetadataCompat newMetadata = provider.getMusic(musicId);
        assertEquals(bArt, newMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
        assertEquals(bIcon, newMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON));
    }
}
