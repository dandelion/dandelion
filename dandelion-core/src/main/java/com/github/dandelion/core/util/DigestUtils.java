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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.github.dandelion.core.DandelionException;

/**
 * <p>
 * Utilities for calculating digests.
 * </p>
 * <p>
 * Part of this code has been kindly borrowed and adapted from the Spring
 * Framework.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class DigestUtils {

   private static final String MD5_ALGORITHM_NAME = "MD5";
   private static final String SHA1_ALGORITHM_NAME = "SHA1";

   private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
         'f' };

   public static String md5Digest(String string) {

      Validate.notBlank(string, "The string to get the hash from cannot be blank");
      char[] encodedDigest;
      try {
         byte[] digest = digest(MD5_ALGORITHM_NAME, string.getBytes("UTF-8"));
         encodedDigest = encodeHex(digest);
      }
      catch (UnsupportedEncodingException e) {
         throw new DandelionException("Unable to get a MD5 hash of " + string, e);
      }

      return new String(encodedDigest);
   }

   /**
    * Calculate the MD5 digest of the given bytes.
    * 
    * @param bytes
    *           the bytes to calculate the digest over
    * @return the digest
    */
   public static byte[] md5Digest(byte[] bytes) {
      return digest(MD5_ALGORITHM_NAME, bytes);
   }

   /**
    * Calculate the MD5 digest of the given bytes.
    * 
    * @param bytes
    *           the bytes to calculate the digest over
    * @return the digest
    */
   public static byte[] sha1Digest(byte[] bytes) {
      return digest(SHA1_ALGORITHM_NAME, bytes);
   }

   /**
    * Creates a new {@link MessageDigest} with the given algorithm. Necessary
    * because {@code MessageDigest} is not thread-safe.
    */
   private static MessageDigest getDigest(String algorithm) {
      try {
         return MessageDigest.getInstance(algorithm);
      }
      catch (NoSuchAlgorithmException e) {
         throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", e);
      }
   }

   private static byte[] digest(String algorithm, byte[] bytes) {
      return getDigest(algorithm).digest(bytes);
   }

   private static char[] encodeHex(byte[] bytes) {
      char chars[] = new char[32];
      for (int i = 0; i < chars.length; i = i + 2) {
         byte b = bytes[i / 2];
         chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
         chars[i + 1] = HEX_CHARS[b & 0xf];
      }
      return chars;
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private DigestUtils() {
      throw new AssertionError();
   }
}
