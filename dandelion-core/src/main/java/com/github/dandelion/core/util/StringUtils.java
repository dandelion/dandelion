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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <p>
 * Collection of utilities to ease working with {@link String}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 */
public final class StringUtils {

   private static final String[] ESCAPES;

   static {
      int size = '>' + 1; // '>' is the largest escaped value
      ESCAPES = new String[size];
      ESCAPES['<'] = "&lt;";
      ESCAPES['>'] = "&gt;";
      ESCAPES['&'] = "&amp;";
      ESCAPES['\''] = "&#039;";
      ESCAPES['"'] = "&#034;";
   }

   /**
    * <p>
    * Checks if a String is whitespace, empty ("") or null.
    * </p>
    * 
    * <pre>
    * StringUtils.isBlank(null)      = true
    * StringUtils.isBlank("")        = true
    * StringUtils.isBlank(" ")       = true
    * StringUtils.isBlank("bob")     = false
    * StringUtils.isBlank("  bob  ") = false
    * </pre>
    * 
    * @param str
    *           the String to check, may be null
    * @return <code>true</code> if the String is null, empty or whitespace
    * @since 2.0
    */
   public static boolean isBlank(String str) {
      int strLen;
      if (str == null || (strLen = str.length()) == 0) {
         return true;
      }
      for (int i = 0; i < strLen; i++) {
         if (!Character.isWhitespace(str.charAt(i))) {
            return false;
         }
      }
      return true;
   }

   /**
    * <p>
    * Checks if a String is not empty (""), not null and not whitespace only.
    * </p>
    * 
    * <pre>
    * StringUtils.isNotBlank(null)      = false
    * StringUtils.isNotBlank("")        = false
    * StringUtils.isNotBlank(" ")       = false
    * StringUtils.isNotBlank("bob")     = true
    * StringUtils.isNotBlank("  bob  ") = true
    * </pre>
    * 
    * @param str
    *           the String to check, may be null
    * @return <code>true</code> if the String is not empty and not null and not
    *         whitespace
    * @since 2.0
    */
   public static boolean isNotBlank(String str) {
      return !StringUtils.isBlank(str);
   }

   /**
    * <p>
    * Capitalizes a String changing the first letter to title case as per
    * {@link Character#toTitleCase(char)}. No other letters are changed.
    * </p>
    * 
    * <pre>
    * StringUtils.capitalize(null)  = null
    * StringUtils.capitalize("")    = ""
    * StringUtils.capitalize("cat") = "Cat"
    * StringUtils.capitalize("cAt") = "CAt"
    * </pre>
    * 
    * @param str
    *           the String to capitalize, may be null
    * @return the capitalized String, <code>null</code> if null String input
    * @see #uncapitalize(String)
    * @since 2.0
    */
   public static String capitalize(String str) {
      if (str == null) {
         return null;
      }
      StringBuilder result = new StringBuilder(str.toString());
      if (result.length() > 0) {
         result.setCharAt(0, Character.toTitleCase(result.charAt(0)));
      }
      return result.toString();

   }

   /**
    * <p>
    * Uncapitalizes a String changing the first letter to title case as per
    * {@link Character#toLowerCase(char)}. No other letters are changed.
    * </p>
    * 
    * <pre>
    * StringUtils.uncapitalize(null)  = null
    * StringUtils.uncapitalize("")    = ""
    * StringUtils.uncapitalize("Cat") = "cat"
    * StringUtils.uncapitalize("CAT") = "cAT"
    * </pre>
    * 
    * @param str
    *           the String to uncapitalize, may be null
    * @return the uncapitalized String, <code>null</code> if null String input
    * @see #capitalize(String)
    * @since 2.0
    */
   public static String uncapitalize(String str) {
      if (str == null) {
         return null;
      }
      StringBuilder result = new StringBuilder(str);

      if (result.length() > 0) {
         result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
      }

      return result.toString();
   }

   public static String join(Object[] target, String separator) {

      final StringBuilder sb = new StringBuilder();
      if (target.length > 0) {
         sb.append(target[0]);
         for (int i = 1; i < target.length; i++) {
            sb.append(separator);
            sb.append(target[i]);
         }
      }
      return sb.toString();
   }

   public static String join(Iterable<?> target, String separator) {

      StringBuilder sb = new StringBuilder();
      Iterator<?> it = target.iterator();
      if (it.hasNext()) {
         sb.append(it.next());
         while (it.hasNext()) {
            sb.append(separator);
            sb.append(it.next());
         }
      }
      return sb.toString();

   }

   /**
    * <p>
    * Checks if the String contains any character in the given set of
    * characters.
    * </p>
    * 
    * <p>
    * A <code>null</code> String will return <code>false</code>. A
    * <code>null</code> or zero length search array will return
    * <code>false</code>.
    * </p>
    * 
    * <pre>
    * StringUtils.containsAny(null, *)                = false
    * StringUtils.containsAny("", *)                  = false
    * StringUtils.containsAny(*, null)                = false
    * StringUtils.containsAny(*, [])                  = false
    * StringUtils.containsAny("zzabyycdxx",['z','a']) = true
    * StringUtils.containsAny("zzabyycdxx",['b','y']) = true
    * StringUtils.containsAny("aba", ['z'])           = false
    * </pre>
    * 
    * @param str
    *           the String to check, may be null
    * @param searchChars
    *           the chars to search for, may be null
    * @return the <code>true</code> if any of the chars are found,
    *         <code>false</code> if no match or null input
    * @since 2.4
    */
   public static boolean containsAny(String str, char[] searchChars) {
      int csLength = str.length();
      int searchLength = searchChars.length;
      for (int i = 0; i < csLength; i++) {
         char ch = str.charAt(i);
         for (int j = 0; j < searchLength; j++) {
            if (searchChars[j] == ch) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Trim <i>all</i> whitespace from the given String: leading, trailing, and
    * inbetween characters.
    * 
    * @param str
    *           the String to check
    * @return the trimmed String
    * @see java.lang.Character#isWhitespace
    */
   public static String trimAllWhitespace(String str) {
      if (!hasLength(str)) {
         return str;
      }
      StringBuilder sb = new StringBuilder(str);
      int index = 0;
      while (sb.length() > index) {
         if (Character.isWhitespace(sb.charAt(index))) {
            sb.deleteCharAt(index);
         }
         else {
            index++;
         }
      }
      return sb.toString();
   }

   /**
    * Check that the given CharSequence is neither {@code null} nor of length 0.
    * Note: Will return {@code true} for a CharSequence that purely consists of
    * whitespace.
    * <p>
    * 
    * <pre>
    * StringUtils.hasLength(null) = false
    * StringUtils.hasLength("") = false
    * StringUtils.hasLength(" ") = true
    * StringUtils.hasLength("Hello") = true
    * </pre>
    * 
    * @param str
    *           the CharSequence to check (may be {@code null})
    * @return {@code true} if the CharSequence is not null and has length
    */
   public static boolean hasLength(CharSequence str) {
      return (str != null && str.length() > 0);
   }

   /**
    * Check that the given String is neither {@code null} nor of length 0. Note:
    * Will return {@code true} for a String that purely consists of whitespace.
    * 
    * @param str
    *           the String to check (may be {@code null})
    * @return {@code true} if the String is not null and has length
    * @see #hasLength(CharSequence)
    */
   public static boolean hasLength(String str) {
      return hasLength((CharSequence) str);
   }

   /**
    * <p>
    * Escapes the characters in a <code>String</code> using XML entities.
    * 
    * @param str
    *           the <code>String</code> to escape, may be null
    * @return a new escaped <code>String</code>, <code>null</code> if null
    *         string input
    */
   public static String escape(String src) {

      if (src == null) {
         return src;
      }

      // First pass to determine the length of the buffer so we only allocate
      // once
      int length = 0;
      for (int i = 0; i < src.length(); i++) {
         char c = src.charAt(i);
         String escape = getEscape(c);
         if (escape != null) {
            length += escape.length();
         }
         else {
            length += 1;
         }
      }

      // Skip copy if no escaping is needed
      if (length == src.length()) {
         return src;
      }

      // Second pass to build the escaped string
      StringBuilder buf = new StringBuilder(length);
      for (int i = 0; i < src.length(); i++) {
         char c = src.charAt(i);
         String escape = getEscape(c);
         if (escape != null) {
            buf.append(escape);
         }
         else {
            buf.append(c);
         }
      }
      return buf.toString();
   }

   /**
    * <p>
    * Escapes the characters in a {@code String} using XML entities only if the
    * passed boolean is {@code true}.
    * 
    * @param str
    *           the {@code String} to escape.
    * @return a new escaped {@code String} if {@code shouldEscape} is set to
    *         {@code true}, an unchanged {@code String} otherwise.
    */
   public static String escape(boolean shouldEscape, String src) {
      if (shouldEscape) {
         return escape(src);
      }
      return src;
   }

   private static String getEscape(char c) {
      if (c < ESCAPES.length) {
         return ESCAPES[c];
      }
      else {
         return null;
      }
   }

   /**
    * <p>
    * Counts how many times the substring appears in the larger string.
    * </p>
    * 
    * <p>
    * A {@code null} or empty ("") String input returns {@code 0}.
    * </p>
    * 
    * <pre>
    * StringUtils.countMatches(null, *)       = 0
    * StringUtils.countMatches("", *)         = 0
    * StringUtils.countMatches("abba", null)  = 0
    * StringUtils.countMatches("abba", "")    = 0
    * StringUtils.countMatches("abba", "a")   = 2
    * StringUtils.countMatches("abba", "ab")  = 1
    * StringUtils.countMatches("abba", "xxx") = 0
    * </pre>
    * 
    * @param str
    *           the CharSequence to check, may be null
    * @param sub
    *           the substring to count, may be null
    * @return the number of occurrences, 0 if either CharSequence is
    *         {@code null}
    */
   public static int countMatches(CharSequence str, CharSequence sub) {
      if ((str == null || str.length() == 0) || (sub == null || sub.length() == 0)) {
         return 0;
      }
      int count = 0;
      int idx = 0;
      while ((idx = indexOf(str, sub, idx)) != -1) {
         count++;
         idx += sub.length();
      }
      return count;
   }

   /**
    * Used by the indexOf(CharSequence methods) as a green implementation of
    * indexOf.
    * 
    * @param cs
    *           the {@code CharSequence} to be processed
    * @param searchChar
    *           the {@code CharSequence} to be searched for
    * @param start
    *           the start index
    * @return the index where the search sequence was found
    */
   public static int indexOf(CharSequence cs, CharSequence searchChar, int start) {
      return cs.toString().indexOf(searchChar.toString(), start);
   }

   /**
    * <p>
    * First convert the supplied {@link Properties} into a {@link Map} and then
    * call {@link #substitute(String, Map)}.
    * </p>
    * 
    * @param source
    *           the string containing possible variable references
    * @param variablesAndValues
    *           the {@link Map} to use to resolve the variables' values.
    * @return The updated source {@link String}.
    * @throws IllegalArgumentException
    *            <ul>
    *            <li>if the source {@link String} is null</li>
    *            <li>if the {@link Map} of variables is null</li>
    *            <li>
    *            or if the source {@link String} references a variable which
    *            doesn't exist in the variable {@link Map}.</li>
    *            </ul>
    */
   public static String substitute(String source, Properties variablesAndValues) {

      Validate.notNull(source, "The source cannot be null");
      Validate.notNull(variablesAndValues, "The Properties cannot be null");

      Map<String, String> map = new HashMap<String, String>();
      for (String key : variablesAndValues.stringPropertyNames()) {
         map.put(key, variablesAndValues.getProperty(key));
      }

      return substitute(source, map);
   }

   /**
    * <p>
    * Substitute all variable referenced in the specified {@code source} with
    * the syntax {@code %VARIABLE_NAME%}.
    * </p>
    * 
    * <p>
    * All variables (and associated values) must be supplied in a {@link Map}
    * where all keys are the variable names and values are their associated
    * value.
    * </p>
    * 
    * <p>
    * To include a literal "%" character in the source String, just double it.
    * </p>
    * 
    * @param source
    *           the string containing possible variable references
    * @param variablesAndValues
    *           the {@link Map} to use to resolve the variables' values.
    * @return The updated source {@link String}.
    * @throws IllegalArgumentException
    *            <ul>
    *            <li>if the source {@link String} is null</li>
    *            <li>if the {@link Map} of variables is null</li>
    *            <li>
    *            or if the source {@link String} references a variable which
    *            doesn't exist in the variable {@link Map}.</li>
    *            </ul>
    */
   public static String substitute(String source, Map<String, String> variablesAndValues) {

      Validate.notNull(source, "The source cannot be null");
      Validate.notNull(variablesAndValues, "The Properties cannot be null");

      StringBuilder result = new StringBuilder();
      int len = source.length();
      char prev = '\0';
      StringBuilder var = new StringBuilder();
      boolean inVar = false;
      boolean syntaxError = false;
      char ch[];

      ch = source.toCharArray();
      for (int i = 0; i < len; i++) {
         char c = ch[i];

         if (c == '%') {
            if (inVar) {
               if (prev == '%') {
                  // Doubled "%". Insert one literal "%".

                  inVar = false;
                  result.append('%');
               }
               else {
                  // End of variable reference. If the variable name
                  // is syntactically incorrect, just store the
                  // entire original sequence in the result string.

                  String varName = var.toString();
                  if (syntaxError) {
                     result.append('%' + varName + '%');
                  }

                  else {
                     if (!variablesAndValues.containsKey(varName)) {
                        StringBuilder error = new StringBuilder("The supplied set of variables doesn't contain");
                        error.append(" a variable named \"");
                        error.append(varName);
                        error.append("\"");
                        throw new IllegalArgumentException(error.toString());
                     }
                     else {
                        String value = variablesAndValues.get(varName);
                        result.append(value == null ? "" : value);
                     }
                  }

                  var.setLength(0);
                  inVar = false;
                  syntaxError = false;
                  prev = '\0'; // prevent match on trailing "%"
               }
            }
            else {
               // Possible start of a new variable.
               inVar = true;
               prev = c;
            }
         }

         else {
            // Not a '%'
            if (inVar) {
               var.append(c);
            }
            else {
               result.append(c);
            }
            prev = c;
         }
      }

      if (inVar) {
         // Never saw the trailing "%" for the last variable reference.
         // Transfer the characters buffered in 'var' into the result,
         // without modification.

         result.append('%');
         result.append(var.toString());
         syntaxError = true;
      }

      return result.toString();
   }

   /**
    * Tokenize the given String into a String array via a StringTokenizer. Trims
    * tokens and omits empty tokens.
    * <p>
    * The given delimiters string is supposed to consist of any number of
    * delimiter characters. Each of those characters can be used to separate
    * tokens. A delimiter is always a single character; for multi-character
    * delimiters, consider using {@code delimitedListToStringArray}
    * 
    * @param str
    *           the String to tokenize
    * @param delimiters
    *           the delimiter characters, assembled as String (each of those
    *           characters is individually considered as delimiter).
    * @return an array of the tokens
    * @see java.util.StringTokenizer
    * @see String#trim()
    * @see #delimitedListToStringArray
    */
   public static String[] tokenizeToStringArray(String str, String delimiters) {
      return tokenizeToStringArray(str, delimiters, true, true);
   }

   /**
    * Tokenize the given String into a String array via a StringTokenizer.
    * <p>
    * The given delimiters string is supposed to consist of any number of
    * delimiter characters. Each of those characters can be used to separate
    * tokens. A delimiter is always a single character; for multi-character
    * delimiters, consider using {@code delimitedListToStringArray}
    * 
    * @param str
    *           the String to tokenize
    * @param delimiters
    *           the delimiter characters, assembled as String (each of those
    *           characters is individually considered as delimiter)
    * @param trimTokens
    *           trim the tokens via String's {@code trim}
    * @param ignoreEmptyTokens
    *           omit empty tokens from the result array (only applies to tokens
    *           that are empty after trimming; StringTokenizer will not consider
    *           subsequent delimiters as token in the first place).
    * @return an array of the tokens ({@code null} if the input String was
    *         {@code null})
    * @see java.util.StringTokenizer
    * @see String#trim()
    * @see #delimitedListToStringArray
    */
   public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
         boolean ignoreEmptyTokens) {

      if (str == null) {
         return null;
      }
      StringTokenizer st = new StringTokenizer(str, delimiters);
      List<String> tokens = new ArrayList<String>();
      while (st.hasMoreTokens()) {
         String token = st.nextToken();
         if (trimTokens) {
            token = token.trim();
         }
         if (!ignoreEmptyTokens || token.length() > 0) {
            tokens.add(token);
         }
      }
      return toStringArray(tokens);
   }

   /**
    * Copy the given Collection into a String array. The Collection must contain
    * String elements only.
    * 
    * @param collection
    *           the Collection to copy
    * @return the String array ({@code null} if the passed-in Collection was
    *         {@code null})
    */
   public static String[] toStringArray(Collection<String> collection) {
      if (collection == null) {
         return null;
      }
      return collection.toArray(new String[collection.size()]);
   }

   /**
    * Check whether the given CharSequence has actual text. More specifically,
    * returns {@code true} if the string not {@code null}, its length is greater
    * than 0, and it contains at least one non-whitespace character.
    * <p>
    * 
    * <pre class="code">
    * StringUtils.hasText(null) = false
    * StringUtils.hasText("") = false
    * StringUtils.hasText(" ") = false
    * StringUtils.hasText("12345") = true
    * StringUtils.hasText(" 12345 ") = true
    * </pre>
    * 
    * @param str
    *           the CharSequence to check (may be {@code null})
    * @return {@code true} if the CharSequence is not {@code null}, its length
    *         is greater than 0, and it does not contain whitespace only
    * @see Character#isWhitespace
    */
   public static boolean hasText(CharSequence str) {
      if (!hasLength(str)) {
         return false;
      }
      int strLen = str.length();
      for (int i = 0; i < strLen; i++) {
         if (!Character.isWhitespace(str.charAt(i))) {
            return true;
         }
      }
      return false;
   }

   /**
    * Check whether the given String has actual text. More specifically, returns
    * {@code true} if the string not {@code null}, its length is greater than 0,
    * and it contains at least one non-whitespace character.
    * 
    * @param str
    *           the String to check (may be {@code null})
    * @return {@code true} if the String is not {@code null}, its length is
    *         greater than 0, and it does not contain whitespace only
    * @see #hasText(CharSequence)
    */
   public static boolean hasText(String str) {
      return hasText((CharSequence) str);
   }

   public static String getTestString(String str) {
      return str.replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "");
   }
}