package com.android.approjects.soundrecorder.util;

import java.io.File;

public class FileUtils {
    public static final int NOT_FOUND = -1;
    public static final int SAVE_FILE_START_INDEX = 1;

    public static String getLastFileName(File file, boolean withExtension) {
        if (file == null) {
            return null;
        }
        return getLastFileName(file.getName(), withExtension);
    }

    public static String getLastFileName(String fileName, boolean withExtension) {
        if (fileName == null) {
            return null;
        }

        if (!withExtension) {
            int dotIndex = fileName.lastIndexOf(".");
            int pathSegmentIndex = fileName.lastIndexOf(File.separator) + 1;
            if (dotIndex == NOT_FOUND) {
                dotIndex = fileName.length();
            }
            if (pathSegmentIndex == NOT_FOUND) {
                pathSegmentIndex = 0;
            }
            fileName = fileName.substring(pathSegmentIndex, dotIndex);
        }

        return fileName;
    }

    public static String getFileExtension(File file, boolean withDot) {
        if (file == null) {
            return null;
        }

        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == NOT_FOUND) {
            return null;
        }

        if (withDot) {
            return fileName.substring(dotIndex, fileName.length());
        }
        return fileName.substring(dotIndex + 1, fileName.length());
    }


    public static File renameFile(File file, String newName) {
        if (file == null) {
            return null;
        }

        String filePath = file.getAbsolutePath();
        String folderPath = file.getParent();
        String extension = filePath.substring(filePath.lastIndexOf("."),
                filePath.length());
        File newFile = new File(folderPath, newName + extension);
        if (file.renameTo(newFile)) {
            return newFile;
        }

        return file;
    }

    public static boolean exists(File file) {
        return file != null && file.exists();
    }


}
