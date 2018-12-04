package com.android.approjects.universalmusicplayer.model;

import android.support.v4.media.MediaMetadataCompat;

import com.android.approjects.universalmusicplayer.utils.LogHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * json的数据形式
 * {"music" : [
 *        { "title" : "Jazz in Paris",
 * 	  "album" : "Jazz & Blues",
 * 	  "artist" : "Media Right Productions",
 * 	  "genre" : "Jazz & Blues",
 * 	  "source" : "Jazz_In_Paris.mp3",
 * 	  "image" : "album_art.jpg",
 * 	  "trackNumber" : 1,
 * 	  "totalTrackCount" : 6,
 * 	  "duration" : 103,
 * 	  "site" : "https://www.youtube.com/audiolibrary/music"
 *    },
 *    { "title" : "The Messenger",
 * 	  "album" : "Jazz & Blues",
 * 	  "artist" : "Silent Partner",
 * 	  "genre" : "Jazz & Blues",
 * 	  "source" : "The_Messenger.mp3",
 * 	  "image" : "album_art.jpg",
 * 	  "trackNumber" : 2,
 * 	  "totalTrackCount" : 6,
 * 	  "duration" : 132,
 * 	  "site" : "https://www.youtube.com/audiolibrary/music"
 *    }
 * ]}
 */

public class RemoteJSONSource implements MusicProviderSource {

    protected static final String CATALOG_URL =
            "http://storage.googleapis.com/automotive-media/music.json";

    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        try {
            int slashPos = CATALOG_URL.lastIndexOf('/');
            String path = CATALOG_URL.substring(0, slashPos + 1);
            JSONObject jsonObj = fetchJSONFromUrl(CATALOG_URL);

            ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();

            if (jsonObj != null) {

                JSONArray jsonTracks = jsonObj.getJSONArray(JSON_MUSIC);

                for (int j = 0; j < jsonTracks.length(); j++) {
                    tracks.add(buildFromJSON(jsonTracks.getJSONObject(j), path));
                }
            }

            return tracks.iterator();
        } catch (JSONException e) {
            LogHelper.e("Could not retrieve music list.");
            throw new RuntimeException("Could not retrieve music list", e);
        }
    }

    private MediaMetadataCompat buildFromJSON(JSONObject json, String basePath) throws JSONException {
        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms

        LogHelper.d("Found music track: " + json);

        // Media is stored relative to JSON file
        if (!source.startsWith("http")) {
            source = basePath + source;
        }

        if (!iconUrl.startsWith("http")) {
            iconUrl = basePath + iconUrl;
        }

        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        //noinspection ResourceType
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }

    /**
     * Download a JSON file from a server, parse the content and return the JSON
     * object.
     *
     * @return result JSONObject containing the parsed representation.
     */

    // 把music.json文本读取出来
    private JSONObject fetchJSONFromUrl(String urlString) throws JSONException {
        BufferedReader reader = null;

        try {
            URLConnection urlConnection = new URL(urlString).openConnection();

            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return new JSONObject(sb.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            LogHelper.e("Failed to parse the json for media list.");
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}