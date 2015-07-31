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
package com.github.dandelion.core.html;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Abstract superclass for all HTML tags.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public abstract class AbstractHtmlTag implements HtmlTag {

   /**
    * Plain old HTML <code>id</code> attribute.
    */
   protected String id;

   /**
    * Plain old HTML <code>class</code> attribute.
    */
   protected StringBuilder cssClass;

   /**
    * Plain old HTML <code>style</code> attribute.
    */
   protected StringBuilder cssStyle;

   /**
    * Dynamic native HTML attributes.
    */
   protected Map<String, String> dynamicAttributes;

   protected Map<String, String> attributes;
   private String[] attributesOnlyName;

   /**
    * Tag label.
    */
   protected String tag;

   @Override
   public StringBuilder toHtml() {
      StringBuilder html = new StringBuilder();
      html.append(getHtmlOpeningTag());
      html.append(getHtmlClosingTag());
      return html;
   }

   protected StringBuilder getHtmlOpeningTag() {
      StringBuilder html = new StringBuilder();
      html.append('<');
      html.append(this.tag);
      html.append(getHtmlAttributes());
      html.append(getDynamicHtmlAttributes());
      html.append('>');
      return html;
   }

   protected StringBuilder getHtmlAttributes() {
      StringBuilder html = new StringBuilder();
      html.append(writeAttribute(ATTR_ID, this.id));
      html.append(writeAttribute(ATTR_CLASS, this.cssClass));
      html.append(writeAttribute(ATTR_STYLE, this.cssStyle));
      return html;
   }

   protected StringBuilder getDynamicHtmlAttributes() {

      // If no dynamicAttributes set, return empty StringBuilder
      if (this.dynamicAttributes == null) {
         return new StringBuilder();
      }
      StringBuilder html = new StringBuilder();
      for (Map.Entry<String, String> attribute : this.dynamicAttributes.entrySet()) {
         html.append(writeAttribute(attribute.getKey(), attribute.getValue()));
      }
      return html;
   }

   protected static StringBuilder writeAttribute(String name, Object data) {
      StringBuilder html = new StringBuilder();
      if (data != null) {
         html.append(' ');
         html.append(name);
         html.append("=\"");
         html.append(data.toString());
         html.append('"');
      }
      return html;
   }

   protected StringBuilder getHtmlClosingTag() {
      StringBuilder html = new StringBuilder();
      html.append("</");
      html.append(this.tag);
      html.append('>');
      return html;
   }

   public String getTag() {
      return tag;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public StringBuilder getCssClass() {
      return cssClass;
   }

   public void setCssClass(StringBuilder cssClass) {
      this.cssClass = cssClass;
   }

   public StringBuilder getCssStyle() {
      return cssStyle;
   }

   public void setCssStyle(StringBuilder cssStyle) {
      this.cssStyle = cssStyle;
   }

   public Map<String, String> getDynamicAttributes() {
      return dynamicAttributes;
   }

   public String getDynamicAttributeValue(String attributeName) {
      if (this.dynamicAttributes != null) {
         return this.dynamicAttributes.get(attributeName);
      }
      return null;
   }

   public void setDynamicAttributes(Map<String, String> dynamicAttributes) {
      this.dynamicAttributes = dynamicAttributes;
   }

   public void addDynamicAttribute(String name, String value) {
      if (this.dynamicAttributes == null) {
         this.dynamicAttributes = new HashMap<String, String>();
      }
      this.dynamicAttributes.put(name, value);
   }

   public void removeDynamicAttribute(String attributeName) {
      if (this.dynamicAttributes != null) {
         this.dynamicAttributes.remove(attributeName);
      }
   }

   public void addCssClass(String cssClass) {
      if (this.cssClass == null) {
         this.cssClass = new StringBuilder();
      }
      else {
         this.cssClass.append(CLASS_SEPARATOR);
      }
      this.cssClass.append(cssClass);
   }

   public void addCssStyle(String cssStyle) {
      if (this.cssStyle == null) {
         this.cssStyle = new StringBuilder();
      }
      else {
         this.cssStyle.append(STYLE_SEPARATOR);
      }
      this.cssStyle.append(cssStyle);
   }

   public void addAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
   }

   public void addAttributesOnlyName(String... attributesOnlyName) {
      this.attributesOnlyName = attributesOnlyName;
   }

   protected String attributesToHtml() {
      StringBuilder html = new StringBuilder();
      if (this.attributes != null) {
         for (Map.Entry<String, String> attribute : this.attributes.entrySet()) {
            html.append(" ");
            html.append(attribute.getKey());
            html.append("=\"");
            html.append(attribute.getValue());
            html.append("\"");
         }
      }
      return html.toString();
   }

   public String attributesOnlyNameToHtml() {
      StringBuilder html = new StringBuilder();
      if (this.attributesOnlyName != null) {
         for (String attribute : this.attributesOnlyName) {
            html.append(" ");
            html.append(attribute);
         }
      }
      return html.toString();
   }
}
