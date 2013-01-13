package com.github.dandelion.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Collection of utility methods for working with URLs.
 */
public class UrlUtils {
    /**
     * Prevent instantiation.
     */
    private UrlUtils() {
        // Do nothing
    }

    /**
     * Retrieves the file path of this URL, with any trailing slashes removed.
     *
     * @param url The URL to get the file path for.
     * @return The file path.
     */
    public static String toFilePath(URL url) {
        String filePath;

        try {
            filePath = URLDecoder.decode(url.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Can never happen", e);
        }

        if (filePath.endsWith("/")) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }

        return filePath;
    }
}
