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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Utility class used when dealing with SHA-1.
 * 
 * <p>
 * The {@link MessageDigest} for SHA1 is loaded at the first use.
 * 
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public final class Sha1Utils {

   private static final Logger LOG = LoggerFactory.getLogger(Sha1Utils.class);

   static MessageDigest mDigest;

   static {
      try {
         mDigest = MessageDigest.getInstance("SHA1");
      }
      catch (NoSuchAlgorithmException e) {
         LOG.error("SHA1 algorithm unknown, no generation");
      }

   }

   /**
    * <p>
    * Generates a SHA1 from the supplied input.
    * 
    * @param input
    *           The input from which the SHA1 is generated.
    * @param neverFail
    *           Whether the function should return something even if the
    *           {@link MessageDigest} algorithm can't be loaded.
    * @return a hash value from the {@code input}.
    */
   public static String generateSha1(String input, boolean neverFail) {
      if (mDigest == null) {
         if (neverFail) {
            LOG.error("SHA-1 can't be calculated for [{}]. Returning the unchanged input instead.", input);
            return input;
         }
         else {
            LOG.error("SHA-1 can't be calculated for [{}]. Returning null instead.", input);
            return null;
         }
      }

      byte[] result = mDigest.digest(input.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte aResult : result) {
         sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
      }

      return sb.toString();
   }
}
