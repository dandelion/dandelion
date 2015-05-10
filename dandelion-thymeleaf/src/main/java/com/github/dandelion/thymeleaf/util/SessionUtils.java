/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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
package com.github.dandelion.thymeleaf.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.web.RequestFlashData;
import com.github.dandelion.core.web.WebConstants;

/**
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class SessionUtils {

   public static void saveRequest(HttpServletRequest request, String requestKey, RequestFlashData requestData) {
      getSessionAttributes(request).put(requestKey, requestData);
   }

   public static RequestFlashData getRequestData(HttpServletRequest request) {
      String requestKey = AssetUtils.extractRequestKeyFromRequest(request);
      RequestFlashData rd = getSessionAttributes(request).get(requestKey);
      return rd;
   }

   public static void removeAttribute(HttpServletRequest request) {
      String requestKey = AssetUtils.extractRequestKeyFromRequest(request);
      getSessionAttributes(request).remove(requestKey);
   }

   @SuppressWarnings("unchecked")
   public static Map<String, RequestFlashData> getSessionAttributes(HttpServletRequest request) {
      Map<String, RequestFlashData> dandelionAttr = (Map<String, RequestFlashData>) request.getSession().getAttribute(
            WebConstants.DANDELION_SESSION_REQUESTATTRS);

      if (dandelionAttr == null) {
         dandelionAttr = new HashMap<String, RequestFlashData>();
         request.getSession(false).setAttribute(WebConstants.DANDELION_SESSION_REQUESTATTRS, dandelionAttr);
      }

      return dandelionAttr;
   }

   public static void cleanSessionAttributes(HttpServletRequest request) {
      Iterator<Map.Entry<String, RequestFlashData>> iterator = getSessionAttributes(request).entrySet().iterator();
      while (iterator.hasNext()) {
         Map.Entry<String, RequestFlashData> entry = iterator.next();
         if (entry.getValue().isExpired()) {
            iterator.remove();
         }
      }
   }

   public static void removeSessionAttributes(HttpServletRequest request) {
      HttpSession session = request.getSession(false);
      if (session != null) {
         session.removeAttribute(WebConstants.DANDELION_SESSION_REQUESTATTRS);
      }
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private SessionUtils() {
      throw new AssertionError();
   }
}
