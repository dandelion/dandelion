/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.storage.BundleStorage;

/**
 * <p>
 * Collection of utilities to ease working with relative paths.
 * 
 * <p>
 * Part of this code has been kindly borrowed and adapted from both Jawr and
 * commons-io projects.
 * 
 * @author Thibault Duchateau
 * @since 0.10.1
 */
public final class PathUtils {

   /** The URL separator */
   public static final String URL_SEPARATOR = "/";

   /** The URL separator character. */
   public static final char URL_SEPARATOR_CHAR = '/';

   /** The comma separator */
   public static final String COMMA_SEPARATOR = ",";

   /** The URL separator pattern */
   private static final Pattern URL_SEPARATOR_PATTERN = Pattern.compile("([^/]*)/");

   /** The pattern to go to the root */
   private static final String ROOT_REPLACE_PATTERN = "../";

   /**
    * Normalizes a path and adds a separator at its start.
    * 
    * @param path
    * @return
    */
   public static String asPath(String path) {
      return (URL_SEPARATOR + normalizePath(path));
   }

   /**
    * Normalizes a path and adds a separator at its start and its end.
    * 
    * @param path
    *           the path
    * @return the normalized path
    */
   public static String asDirPath(String path) {
      return (URL_SEPARATOR + normalizePath(path) + URL_SEPARATOR);
   }

   /**
    * <p>
    * Removes leading and trailing separators from a path, and removes double
    * separators (// is replaced by /).
    * 
    * <pre>
    * PathUtils.normalizePath("//some/path")   = "some/path"
    * PathUtils.normalizePath("//some/path/")  = "some/path"
    * PathUtils.normalizePath("/some//path/")  = "some/path"
    * </pre>
    * 
    * @param path
    *           the path to normalize.
    * @return the normalized path.
    */
   public static final String normalizePath(String path) {
      String normalizedPath = path.replaceAll("//", URL_SEPARATOR);
      StringTokenizer tk = new StringTokenizer(normalizedPath, URL_SEPARATOR);
      StringBuffer sb = new StringBuffer();
      while (tk.hasMoreTokens()) {
         sb.append(tk.nextToken());
         if (tk.hasMoreTokens())
            sb.append(URL_SEPARATOR);
      }
      return sb.toString();

   }

   /**
    * <p>
    * Determines the parent path of a filename or a directory.
    * 
    * <pre>
    * PathUtils.getParentPath(null)                          = ""
    * PathUtils.getParentPath("")                            = ""
    * PathUtils.getParentPath("/")                           = "/"
    * PathUtils.getParentPath("/folder/sub/")                = "/folder/"
    * PathUtils.getParentPath("/folder/sub")                 = "/folder/"
    * PathUtils.getParentPath("/folder/sub")                 = "/folder/"
    * PathUtils.getParentPath("/folder/sub/sub2/")           = "/folder/sub/"
    * PathUtils.getParentPath("/folder/sub/sub2/file.txt")   = "/folder/sub/sub2/"
    * </pre>
    * 
    * @param path
    *           the path
    * @return the parent path.
    */
   public static String getParentPath(String path) {

      String parentPath = null;
      if (StringUtils.isBlank(path)) {
         parentPath = "";
      }
      else {

         parentPath = path;
         if (parentPath.length() > 1 && parentPath.endsWith(URL_SEPARATOR)) {
            parentPath = parentPath.substring(0, parentPath.length() - 2);
         }
         int index = parentPath.lastIndexOf(URL_SEPARATOR);
         if (index > 0) {
            return parentPath.substring(0, index + 1);
         }
         else {
            parentPath = URL_SEPARATOR;
         }
      }

      return parentPath;
   }

   /**
    * <p>
    * Concatenates a filename to a base web path. If the base path doesn't end
    * with "/", it will consider as base path the parent folder of the base path
    * passed as parameter.
    * 
    * <pre>
    * PathUtils.concatWebPath("", null)                                            = null
    * PathUtils.concatWebPath(null, "")                                            = null
    * PathUtils.concatWebPath(null, null)                                          = null
    * PathUtils.concatWebPath("", "")                                              = ""
    * PathUtils.concatWebPath(null, "name")                                        = null
    * PathUtils.concatWebPath(null, "/name")                                       = "/name"
    * PathUtils.concatWebPath("", "name")                                          = "name"
    * PathUtils.concatWebPath("", "/name")                                         = "/name"
    * PathUtils.concatWebPath("/css/folder/subfolder/", null)                      = null
    * PathUtils.concatWebPath("/css/folder/subfolder/", "images/img.png")          = "/css/folder/subfolder/images/img.png"
    * PathUtils.concatWebPath("/css/folder/subfolder/style.css", "images/img.png") = "/css/folder/subfolder/images/img.png"
    * PathUtils.concatWebPath("/css/folder/", "../images/img.png")                 = "/css/images/img.png"
    * PathUtils.concatWebPath("/css/folder/", "../../images/img.png")              = "/images/img.png"
    * PathUtils.concatWebPath("/css/folder/style.css", "../images/img.png")        = "/css/images/img.png"
    * </pre>
    * 
    * @param basePath
    *           the base path
    * @param fullFilenameToAdd
    *           the file name to add
    * @return the concatenated path, or null if invalid
    */
   public static String concatWebPath(String basePath, String fullFilenameToAdd) {

      if (fullFilenameToAdd == null || basePath == null
            && (fullFilenameToAdd.length() == 0 || fullFilenameToAdd.charAt(0) != URL_SEPARATOR_CHAR)) {
         return null;
      }

      if (basePath == null) {
         basePath = "";
      }
      // If the basePath is pointing to a file, set the base path to the
      // parent directory
      if (basePath.length() > 1 && basePath.charAt(basePath.length() - 1) != '/') {
         basePath = getParentPath(basePath);
      }

      int len = basePath.length();
      String fullPath = null;
      if (len == 0) {
         return doNormalizeIgnoreOtherSeparator(fullFilenameToAdd, true);
      }

      char ch = basePath.charAt(len - 1);
      if (ch == URL_SEPARATOR_CHAR) {
         fullPath = basePath + fullFilenameToAdd;
      }
      else {
         fullPath = basePath + '/' + fullFilenameToAdd;
      }

      return doNormalizeIgnoreOtherSeparator(fullPath, true);

   }

   /**
    * <p>
    * Calculates the relative path between two pathes. on a web
    * 
    * <pre>
    * PathUtils.getRelativeWebPath(null, null)                                                                    = ""
    * PathUtils.getRelativeWebPath(null, "")                                                                      = ""
    * PathUtils.getRelativeWebPath("", null)                                                                      = ""
    * PathUtils.getRelativeWebPath("", "")                                                                        = ""
    * PathUtils.getRelativeWebPath(null, "http://dandelion.github.io/")                                           = ""
    * PathUtils.getRelativeWebPath("", "http://dandelion.github.io/")                                             = ""
    * PathUtils.getRelativeWebPath("http://dandelion.github.io/", null)                                           = ""
    * PathUtils.getRelativeWebPath("http://dandelion.github.io/", "")                                             = ""
    * PathUtils.getRelativeWebPath("http://dandelion.github.io/", "http://dandelion.github.io/folder/index.html") = "folder/index.html"
    * PathUtils.getRelativeWebPath("http://dandelion.github.io/folder/index.html", "http://dandelion.github.io/") = "../../"
    * </pre>
    * 
    * @param oldPath
    * @param newPath
    * @return a relative web path from <code>oldPath</code>.
    */
   public static final String getRelativeWebPath(final String oldPath, final String newPath) {
      if (StringUtils.isBlank(oldPath) || StringUtils.isBlank(newPath)) {
         return "";
      }

      String resultPath = buildRelativePath(newPath, oldPath, '/');

      if (newPath.endsWith("/") && !resultPath.endsWith("/")) {
         return resultPath + "/";
      }

      return resultPath;
   }

   /**
    * <p>
    * Returns the relative path of an url to go back to the root.
    * 
    * <pre>
    * PathUtils.getRootRelativePath(null)                             = ""
    * PathUtils.getRootRelativePath("")                               = ""
    * PathUtils.getRootRelativePath("/assets/some.css")               = "../"
    * PathUtils.getRootRelativePath("/assets/css/some.css")           = "../../"
    * PathUtils.getRootRelativePath("/assets/css/subfolder/some.css") = "../../../"
    * </pre>
    * 
    * @param url
    *           the requested url
    * @return the relative path of an url to go back to the root.
    */
   public static String getRootRelativePath(String url) {

      StringBuffer retval = new StringBuffer();
      if (StringUtils.isNotBlank(url)) {
         Matcher matcher = URL_SEPARATOR_PATTERN.matcher(url);
         boolean first = true;
         while (matcher.find()) {
            if (first) {
               matcher.appendReplacement(retval, "");
               first = false;
            }
            else {
               matcher.appendReplacement(retval, ROOT_REPLACE_PATTERN);
            }
         }
      }

      return retval.toString();
   }

   /**
    * Retrieves the file path of this URL, with any trailing slashes removed.
    * 
    * @param url
    *           The URL to get the file path for.
    * @return The file path.
    */
   public static String toFilePath(URL url) {
      String filePath;

      try {
         filePath = URLDecoder.decode(url.getPath(), "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
         throw new IllegalStateException("Can never happen", e);
      }

      if (filePath.endsWith("/")) {
         filePath = filePath.substring(0, filePath.length() - 1);
      }

      return filePath;
   }

   /**
    * <p>
    * Extract the a lower-cased {@link Asset} name using its configured
    * location.
    * </p>
    * 
    * <pre>
    * AssetUtils.extractName("/assets/js/jquery.js")  = "jquery"
    * AssetUtils.extractName("JQUERY.js")             = "jquery"
    * AssetUtils.extractName("jQuery")                = "jquery"
    * </pre>
    * 
    * @param location
    *           The location of the asset.
    * @return the asset name to be used in the {@link BundleStorage}.
    */
   public static String extractLowerCasedName(String location) {

      String assetName = null;
      String tmpAssetName;

      if (location != null && location.contains("/")) {
         // Get last token, this after the last "/"
         tmpAssetName = location.substring(location.lastIndexOf('/') + 1, location.length());
      }
      else if (location != null && location.contains("\\")) {
         // Get last token, this after the last "\"
         tmpAssetName = location.substring(location.lastIndexOf('\\') + 1, location.length());
      }
      else {
         tmpAssetName = location;
      }

      // Remove the extension
      if (tmpAssetName.contains(".")) {
         assetName = tmpAssetName.substring(0, tmpAssetName.lastIndexOf('.'));
      }

      return assetName.toLowerCase();
   }

   /**
    * Internal method to perform the normalization.
    * 
    * @param filename
    *           the filename
    * @param keepSeparator
    *           true to keep the final separator
    * @return the normalized filename
    */
   private static String doNormalizeIgnoreOtherSeparator(String filename, boolean keepSeparator) {

      int size = filename.length();
      if (size == 0) {
         return filename;
      }
      int prefix = 0;

      char[] array = new char[size + 2]; // +1 for possible extra slash, +2
      // for arraycopy
      filename.getChars(0, filename.length(), array, 0);

      // add extra separator on the end to simplify code below
      boolean lastIsDirectory = true;
      if (array[size - 1] != URL_SEPARATOR_CHAR) {
         array[size++] = URL_SEPARATOR_CHAR;
         lastIsDirectory = false;
      }

      // adjoining slashes
      for (int i = prefix + 1; i < size; i++) {
         if (array[i] == URL_SEPARATOR_CHAR && array[i - 1] == URL_SEPARATOR_CHAR) {
            System.arraycopy(array, i, array, i - 1, size - i);
            size--;
            i--;
         }
      }

      // dot slash
      for (int i = prefix + 1; i < size; i++) {
         if (array[i] == URL_SEPARATOR_CHAR && array[i - 1] == '.'
               && (i == prefix + 1 || array[i - 2] == URL_SEPARATOR_CHAR)) {
            if (i == size - 1) {
               lastIsDirectory = true;
            }
            System.arraycopy(array, i + 1, array, i - 1, size - i);
            size -= 2;
            i--;
         }
      }

      // double dot slash
      outer: for (int i = prefix + 2; i < size; i++) {
         if (array[i] == URL_SEPARATOR_CHAR && array[i - 1] == '.' && array[i - 2] == '.'
               && (i == prefix + 2 || array[i - 3] == URL_SEPARATOR_CHAR)) {
            if (i == prefix + 2) {
               return null;
            }
            if (i == size - 1) {
               lastIsDirectory = true;
            }
            int j;
            for (j = i - 4; j >= prefix; j--) {
               if (array[j] == URL_SEPARATOR_CHAR) {
                  // remove b/../ from a/b/../c
                  System.arraycopy(array, i + 1, array, j + 1, size - i);
                  size -= (i - j);
                  i = j + 1;
                  continue outer;
               }
            }
            // remove a/../ from a/../c
            System.arraycopy(array, i + 1, array, prefix, size - i);
            size -= (i + 1 - prefix);
            i = prefix + 1;
         }
      }

      if (size <= 0) { // should never be less than 0
         return "";
      }
      if (size <= prefix) { // should never be less than prefix
         return new String(array, 0, size);
      }
      if (lastIsDirectory && keepSeparator) {
         return new String(array, 0, size); // keep trailing separator
      }
      return new String(array, 0, size - 1); // lose trailing separator
   }

   private static final String buildRelativePath(String toPath, String fromPath, final char separatorChar) {
      // use tokeniser to traverse paths and for lazy checking
      StringTokenizer toTokeniser = new StringTokenizer(toPath, String.valueOf(separatorChar));
      StringTokenizer fromTokeniser = new StringTokenizer(fromPath, String.valueOf(separatorChar));

      int count = 0;

      // walk along the to path looking for divergence from the from path
      while (toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens()) {
         if (separatorChar == '\\') {
            if (!fromTokeniser.nextToken().equalsIgnoreCase(toTokeniser.nextToken())) {
               break;
            }
         }
         else {
            if (!fromTokeniser.nextToken().equals(toTokeniser.nextToken())) {
               break;
            }
         }

         count++;
      }

      // reinitialise the tokenisers to count positions to retrieve the
      // gobbled token

      toTokeniser = new StringTokenizer(toPath, String.valueOf(separatorChar));
      fromTokeniser = new StringTokenizer(fromPath, String.valueOf(separatorChar));

      while (count-- > 0) {
         fromTokeniser.nextToken();
         toTokeniser.nextToken();
      }

      StringBuffer relativePath = new StringBuffer();

      // add back refs for the rest of from location.
      while (fromTokeniser.hasMoreTokens()) {
         fromTokeniser.nextToken();

         relativePath.append("..");

         if (fromTokeniser.hasMoreTokens()) {
            relativePath.append(separatorChar);
         }
      }

      if (relativePath.length() != 0 && toTokeniser.hasMoreTokens()) {
         relativePath.append(separatorChar);
      }

      // add fwd fills for whatevers left of newPath.
      while (toTokeniser.hasMoreTokens()) {
         relativePath.append(toTokeniser.nextToken());

         if (toTokeniser.hasMoreTokens()) {
            relativePath.append(separatorChar);
         }
      }
      return relativePath.toString();
   }

   /**
    * Prevents instantiation.
    */
   private PathUtils() {
   }
}