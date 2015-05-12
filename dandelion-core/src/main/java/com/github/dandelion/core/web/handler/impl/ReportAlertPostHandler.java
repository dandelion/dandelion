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
package com.github.dandelion.core.web.handler.impl;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.html.AbstractHtmlTag;
import com.github.dandelion.core.html.HtmlLink;
import com.github.dandelion.core.html.HtmlScript;
import com.github.dandelion.core.reporting.Alert;
import com.github.dandelion.core.reporting.ReportingType;
import com.github.dandelion.core.util.UrlUtils;
import com.github.dandelion.core.web.handler.AbstractHandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Post-filtering request handler in charge of reporting alerts.
 * </p>
 * <p>
 * Only applies:
 * </p>
 * <ul>
 * <li>on HTML responses</li>
 * <li>if the {@link DandelionConfig#TOOL_ALERT_REPORTING} configuration is
 * enabled</li>
 * </ul>
 * <p>
 * Depending on the {@link DandelionConfig#TOOL_ALERT_REPORTING_MODE}, Dandelion
 * will inject necessary assets to display an alert client-side.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class ReportAlertPostHandler extends AbstractHandlerChain {

   private static final Logger LOG = LoggerFactory.getLogger(DebuggerPostHandler.class);
   private static final String CLOSING_HEAD_TAG = "</head>";
   private static final String BASE_MSG_CONSOLE = "Dandelion has reported some alerts. See details by browsing the following URI: ";

   @Override
   protected Logger getLogger() {
      return LOG;
   }

   @Override
   public boolean isAfterChaining() {
      return true;
   }

   @Override
   public int getRank() {
      return 30;
   }

   @Override
   public boolean isApplicable(HandlerContext handlerContext) {

      HttpServletRequest request = handlerContext.getRequest();
      HttpServletResponse wrappedResponse = handlerContext.getResponse();

      boolean isToolAlertReportingEnabled = handlerContext.getContext().getConfiguration()
            .isToolAlertReportingEnabled();
      boolean isHtmlResponse = wrappedResponse.getContentType() != null
            && wrappedResponse.getContentType().contains("text/html");
      boolean notBrowsingDebugger = !request.getRequestURI().contains("ddl-debugger");

      if (isToolAlertReportingEnabled && isHtmlResponse && notBrowsingDebugger) {

         Set<Alert> alerts = new AssetQuery(handlerContext.getRequest(), handlerContext.getContext()).alerts();
         return alerts.size() > 0;
      }
      else {
         return false;
      }
   }

   @Override
   public boolean handle(HandlerContext handlerContext) {

      ReportingType reportingType = handlerContext.getContext().getConfiguration().getToolAlertReportingMode();

      // Convert the response to a String in order to perform easier
      // replacements
      String responseAsString = new String(handlerContext.getResponseAsBytes());

      switch (reportingType) {
      case ALL:
         responseAsString = reportAsNotification(responseAsString, handlerContext.getRequest());
         responseAsString = reportAsConsoleError(responseAsString, handlerContext.getRequest());
         break;
      case CONSOLE:
         responseAsString = reportAsConsoleError(responseAsString, handlerContext.getRequest());
         break;
      case NOTIFICATION:
         responseAsString = reportAsNotification(responseAsString, handlerContext.getRequest());
         break;
      case NONE:
         break;
      default:
         break;
      }

      // Once all assets injected, convert back to a byte array to
      // let other handler do their work
      String configuredEncoding = handlerContext.getContext().getConfiguration().getEncoding();
      byte[] updatedResponse;
      try {
         handlerContext.getResponse().setContentLength(responseAsString.getBytes(configuredEncoding).length);
         updatedResponse = responseAsString.getBytes(configuredEncoding);
      }
      catch (UnsupportedEncodingException e) {
         throw new DandelionException("Unable to encode the HTML page using the '" + configuredEncoding
               + "', which doesn't seem to be supported", e);
      }

      handlerContext.setResponseAsBytes(updatedResponse);
      
      return true;
   }

   private String reportAsNotification(String responseAsString, HttpServletRequest request) {

      StringBuilder alertMessage = new StringBuilder();
      alertMessage.append("Dandelion has reported some alerts. <br/>See <a href=\"");
      alertMessage.append(UrlUtils.getCurrentUri(request));
      alertMessage.append("?ddl-debug&ddl-debug-page=alerts");
      alertMessage.append("\">");
      alertMessage.append("details</a>");

      StringBuilder htmlHead = new StringBuilder();
      StringBuilder href = new StringBuilder(request.getContextPath());
      href.append("/ddl-debugger/css/pnotify.custom.min.css");
      AbstractHtmlTag tag = new HtmlLink(href.toString());
      htmlHead.append(tag.toHtml());
      htmlHead.append('\n');

      StringBuilder src = new StringBuilder(request.getContextPath());
      src.append("/ddl-debugger/js/jquery-1.11.1.min.js");
      tag = new HtmlScript(src.toString());
      htmlHead.append(tag.toHtml());
      htmlHead.append('\n');

      src = new StringBuilder(request.getContextPath());
      src.append("/ddl-debugger/js/pnotify.custom.min.js");
      tag = new HtmlScript(src.toString());
      htmlHead.append(tag.toHtml());
      htmlHead.append('\n');
      htmlHead.append("<script type=\"text/javascript\">");
      htmlHead.append("$(document).ready(function(){ ");
      htmlHead.append("   new PNotify({");
      htmlHead.append("     title: 'Dandelion',");
      htmlHead.append("     text: '").append(alertMessage).append("',");
      htmlHead.append("     type: 'error',");
      htmlHead.append("     icon: false,");
      htmlHead.append("     hide: false");
      htmlHead.append("   });");
      htmlHead.append("});");
      htmlHead.append("</script>");
      htmlHead.append('\n');
      htmlHead.append(CLOSING_HEAD_TAG);
      responseAsString = responseAsString.replace(CLOSING_HEAD_TAG, htmlHead);

      return responseAsString;
   }

   private String reportAsConsoleError(String responseAsString, HttpServletRequest request) {

      StringBuilder alertMessage = new StringBuilder(BASE_MSG_CONSOLE);
      alertMessage.append(UrlUtils.getCurrentUri(request));
      alertMessage.append("?ddl-debug&ddl-debug-page=alerts");

      StringBuilder console = new StringBuilder();
      console.append("<script type=\"text/javascript\">");
      console.append("throw new Error('").append(alertMessage).append("');");
      console.append("</script>");

      console.append('\n');
      console.append(CLOSING_HEAD_TAG);
      responseAsString = responseAsString.replace(CLOSING_HEAD_TAG, console);

      return responseAsString;
   }
}