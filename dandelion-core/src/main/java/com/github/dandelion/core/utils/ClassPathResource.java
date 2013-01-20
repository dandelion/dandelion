package com.github.dandelion.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import com.github.dandelion.core.api.DandelionException;
import com.github.dandelion.core.api.utils.ClassPathResourceError;

/**
 * A resource on the classpath.
 */
public class ClassPathResource implements Comparable<ClassPathResource> {
    /**
     * The location of the resource on the classpath.
     */
    private String location;

    /**
     * Creates a new ClassPathResource.
     *
     * @param location The location of the resource on the classpath.
     */
    public ClassPathResource(String location) {
        this.location = location;
    }

    /**
     * @return The location of the resource on the classpath.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Retrieves the location of this resource on disk.
     *
     * @return The location of this resource on disk.
     */
    public String getLocationOnDisk() {
        URL url = getUrl();
        if (url == null) {
            throw new DandelionException(ClassPathResourceError.UNABLE_TO_LOCATION_RESOURCE_ON_DISK).set("location", location);
        }
        try {
            return URLDecoder.decode(url.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw DandelionException.wrap(e, ClassPathResourceError.UNKNOWN_ENCODING).set("encoding", "UTF-8");
        }
    }

    /**
     * @return The url of this resource.
     */
    private URL getUrl() {
        return getClassLoader().getResource(location);
    }

    /**
     * @return The classloader to load the resource with.
     */
    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Loads this resource as a string.
     *
     * @param encoding The encoding to use.
     * @return The string contents of the resource.
     */
    public String loadAsString(String encoding) {
        try {
            InputStream inputStream = getClassLoader().getResourceAsStream(location);
            if (inputStream == null) {
                throw new DandelionException(ClassPathResourceError.UNABLE_TO_OBTAIN_INPUTSTREAM_FOR_RESOURCE).set("location", location);
            }
            Reader reader = new InputStreamReader(inputStream, Charset.forName(encoding));

            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw DandelionException.wrap(e, ClassPathResourceError.UNABLE_TO_LOAD_RESOURCE_WITH_ENCODING)
                    .set("location", location).set("encoding", encoding);
        }
    }

    /**
     * Loads this resource as a byte array.
     *
     * @return The contents of the resource.
     */
    public byte[] loadAsBytes() {
        try {
            InputStream inputStream = getClassLoader().getResourceAsStream(location);
            if (inputStream == null) {
                throw new DandelionException(ClassPathResourceError.UNABLE_TO_OBTAIN_INPUTSTREAM_FOR_RESOURCE).set("location", location);
            }
            return FileCopyUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            throw DandelionException.wrap(e, ClassPathResourceError.UNABLE_TO_LOAD_RESOURCE)
                    .set("location", location);
        }
    }

    /**
     * @return The filename of this resource, without the path.
     */
    public String getFilename() {
        return location.substring(location.lastIndexOf("/") + 1);
    }

    /**
     * Checks whether this resource exists.
     *
     * @return {@code true} if it exists, {@code false} if not.
     */
    public boolean exists() {
        return getUrl() != null;
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassPathResource that = (ClassPathResource) o;

        if (!location.equals(that.location)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    public int compareTo(ClassPathResource o) {
        return location.compareTo(o.location);
    }
}
