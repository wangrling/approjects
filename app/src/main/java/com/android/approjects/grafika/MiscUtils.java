package com.android.approjects.grafika;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some handy utilities.
 */

public class MiscUtils {
    private static final String TAG = GrafikaActivity.TAG;

    private MiscUtils() {

    }

    /**
     * Obtains a list of files that live in the specified directory and match the glob pattern.
     */
    public static String[] getFiles(File dir, String glob) {
        String regex = globToRegex(glob);
        final Pattern pattern = Pattern.compile(regex);
        String[] result = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Matcher matcher = pattern.matcher(name);
                return ((Matcher) matcher).matches();
            }
        });

        Arrays.sort(result);

        return result;
    }

    /**
     * Converts a filename globbing pattern to a regular expression.
     * <p>
     * The regex is suitable for use by Matcher.matches(), which matches the entire string, so
     * we don't specify leading '^' or trailing '$'.
     */
    private static String globToRegex(String glob) {
        // Quick, overly-simplistic implementation -- just want to handle something simple
        // like "*.mp4".
        //
        // See e.g. http://stackoverflow.com/questions/1247772/ for a more thorough treatment.
        StringBuilder regex = new StringBuilder(glob.length());
        for (char ch : glob.toCharArray()) {
            switch (ch) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append('.');
                    break;
                case '.':
                    regex.append("\\.");
                    break;
                default:
                    regex.append(ch);
                    break;
            }
        }
        // regex.append('$');
        return regex.toString();
    }
}
