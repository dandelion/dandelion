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
package com.github.dandelion.core.asset.processor.support;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.dandelion.core.util.PathUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Rewrites URLs in CSS files according to the new relative location of the CSS
 * file
 * </p>
 * <p>
 * Part of this code has been kindly borrowed and adapted from Ibrahim Chaehoi
 * (Jawr Project).
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.1
 */
public class CssUrlRewriter {

   /** The URL separator */
   private static final String URL_SEPARATOR = "/";

   /** The URL regexp pattern */
   public static String URL_REGEXP =
   // 'url('
   "url\\("
   // and any number of whitespaces
         + "\\s*"
         // any sequence of characters not starting with 'data:', 'mhtml:',
         // or 'cid:', except an unescaped ')'
         + "(?!(\"|')?(data|mhtml|cid):)(((\\\\\\))|[^)])*)"
         // Any number of whitespaces, then ')'
         + "\\s*\\)";

   /** The url pattern */
   public static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP, Pattern.CASE_INSENSITIVE);

   /** The URL separator pattern */
   private static final Pattern URL_SEPARATOR_PATTERN = Pattern.compile("([^/]*)/");

   /** The pattern to go to the root */
   private static final String ROOT_REPLACE_PATTERN = "../";

   /** The context path */
   protected String contextPath;

   public CssUrlRewriter() {
   }

   /**
    * Sets the context path.
    * 
    * @param contextPath
    *           the contextPath to set
    */
   public void setContextPath(String contextPath) {
      if (StringUtils.isNotBlank(contextPath)) {
         if (contextPath.charAt(0) != '/') {
            contextPath = '/' + contextPath;
         }
         if (contextPath.charAt(contextPath.length() - 1) != '/') {
            contextPath = contextPath + '/';
         }
         this.contextPath = contextPath;
      }
      else {
         this.contextPath = null;
      }
   }

   /**
    * <p>
    * Rewrites all URLs present in the originalCssContent using the newCssPath.
    * 
    * @param originalCssPath
    *           the original CSS path.
    * @param newCssPath
    *           the new CSS path.
    * @param originalCssContent
    *           the original CSS content.
    * @return the new CSS content with URLs rewritten.
    * @throws IOException
    */
   public StringBuffer rewriteUrl(String originalCssPath, String newCssPath, String originalCssContent) {

      Matcher matcher = URL_PATTERN.matcher(originalCssContent);
      StringBuffer sb = new StringBuffer();
      while (matcher.find()) {

         String url = getUrlPath(matcher.group(), originalCssPath, newCssPath);
         matcher.appendReplacement(sb, adaptReplacementToMatcher(url));
      }
      matcher.appendTail(sb);
      return sb;
   }

   /**
    * Transform a matched url so it points to the proper relative path with
    * respect to the given path.
    * 
    * @param matchedUrl
    *           the matched URL
    * @param newCssPath
    *           the full bundle path
    * @param status
    *           the bundle processing status
    * @return the image URL path
    * @throws IOException
    *            if an IO exception occurs
    */
   protected String getUrlPath(String matchedUrl, String originalPath, String newCssPath) {

      String url = matchedUrl.substring(matchedUrl.indexOf('(') + 1, matchedUrl.lastIndexOf(')')).trim();

      // To keep quotes as they are, first they are checked and removed.
      String quoteStr = "";
      if (url.startsWith("'") || url.startsWith("\"")) {
         quoteStr = url.charAt(0) + "";
         url = url.substring(1, url.length() - 1);
      }

      // Check if the URL is absolute, but in the application itself
      if (StringUtils.isNotBlank(contextPath) && url.startsWith(contextPath)) {
         String rootRelativePath = PathUtils.getRootRelativePath(originalPath);
         url = rootRelativePath + url.substring(contextPath.length());
      }

      // Check if the URL is absolute, if it is return it as is.
      int firstSlash = url.indexOf('/');
      if (0 == firstSlash || (firstSlash != -1 && url.charAt(++firstSlash) == '/')) {
         StringBuffer sb = new StringBuffer("url(");
         sb.append(quoteStr).append(url).append(quoteStr).append(")");
         return sb.toString();
      }

      if (url.startsWith(URL_SEPARATOR)) {
         url = url.substring(1, url.length());
      }
      else if (url.startsWith("./")) {
         url = url.substring(2, url.length());
      }

      String imgUrl = getRewrittenImagePath(originalPath, newCssPath, url);

      // Start rendering the result, starting by the initial quote, if any.
      String finalUrl = "url(" + quoteStr + imgUrl + quoteStr + ")";
      Matcher urlMatcher = URL_PATTERN.matcher(finalUrl);
      if (urlMatcher.find()) { // Normalize only if a real URL
         finalUrl = PathUtils.normalizePath(finalUrl);
      }

      return finalUrl;
   }

   /**
    * Returns the rewritten image path
    * 
    * @param originalCssPath
    *           the original Css path
    * @param newCssPath
    *           the new Css path
    * @param url
    *           the image URL
    * @return the rewritten image path
    * @throws IOException
    *            if an IOException occurs
    */
   protected String getRewrittenImagePath(String originalCssPath, String newCssPath, String url) {

      // Here we generate the full path of the CSS image
      // to be able to define the relative path from the full bundle path
      String fullImgPath = PathUtils.concatWebPath(originalCssPath, url);

      String imgUrl = PathUtils.getRelativeWebPath(PathUtils.getParentPath(newCssPath), fullImgPath);

      return imgUrl;
   }

   public String adaptReplacementToMatcher(String replacement) {
      // Double the backslashes, so they are left as they are after
      // replacement.
      String result = replacement.replaceAll("\\\\", "\\\\\\\\");
      // Add backslashes after dollar signs
      result = result.replaceAll("\\$", "\\\\\\$");
      return result;
   }

   /**
    * Returns the relative path of an url to go back to the root. For example :
    * if the url path is defined as "/cssServletPath/css/myStyle.css" ->
    * "../../"
    * 
    * @param url
    *           the requested url
    * @return the relative path of an url to go back to the root.
    */
   public String getRootRelativePath(String url) {

      Matcher matcher = URL_SEPARATOR_PATTERN.matcher(url);
      StringBuffer result = new StringBuffer();
      boolean first = true;
      while (matcher.find()) {
         if (first) {
            matcher.appendReplacement(result, "");
            first = false;
         }
         else {
            matcher.appendReplacement(result, ROOT_REPLACE_PATTERN);
         }
      }

      return result.toString();
   }
}
