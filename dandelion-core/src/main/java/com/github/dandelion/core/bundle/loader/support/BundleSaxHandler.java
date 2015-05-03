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
package com.github.dandelion.core.bundle.loader.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Custom SAX2 event handler in charge of parsing an XML-formatted bundle
 * definition.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class BundleSaxHandler extends DefaultHandler {

   public static final String EL_BUNDLE = "bundle";
   public static final String EL_NAME = "name";
   public static final String EL_ASSET = "asset";
   public static final String EL_DEPENDENCY = "dependency";
   public static final String EL_LOCATIONS = "locations";
   public static final String EL_LOCATION = "location";
   public static final String ATTR_NAME = "name";
   public static final String ATTR_VERSION = "version";
   public static final String ATTR_TYPE = "type";
   public static final String ATTR_CONDITION = "condition";
   public static final String ATTR_KEY = "key";

   public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
   public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
   public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

   private BundleStorageUnit bsu;
   private Set<AssetStorageUnit> asus;
   private AssetStorageUnit asu;
   private Map<String, String> locationMap;
   private String content = null;
   private String locationKey;

   public BundleSaxHandler() {
      this.asus = new HashSet<AssetStorageUnit>();
      this.locationMap = new HashMap<String, String>();
   }

   @Override
   public void startElement(String uri, String s1, String qName, Attributes attributes) throws SAXException {

      if (qName.equalsIgnoreCase(EL_BUNDLE)) {
         bsu = new BundleStorageUnit();
      }
      else if (qName.equalsIgnoreCase(EL_ASSET)) {
         asu = new AssetStorageUnit();
         asu.setName(attributes.getValue(ATTR_NAME));
         asu.setVersion(attributes.getValue(ATTR_VERSION));
         if (StringUtils.isNotBlank(attributes.getValue(ATTR_TYPE))) {
            asu.setType(AssetType.valueOf(attributes.getValue(ATTR_TYPE)));
         }
         if (StringUtils.isNotBlank(attributes.getValue(ATTR_CONDITION))) {
            asu.setCondition(attributes.getValue(ATTR_CONDITION));
         }
      }
      else if (qName.equalsIgnoreCase(EL_LOCATION)) {
         locationKey = attributes.getValue(ATTR_KEY);
      }
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {

      if (qName.equalsIgnoreCase(EL_NAME)) {
         bsu.setName(content);
      }
      else if (qName.equalsIgnoreCase(EL_DEPENDENCY)) {
         bsu.addDependency(content);
      }
      else if (qName.equalsIgnoreCase(EL_ASSET)) {
         asus.add(asu);
      }
      else if (qName.equalsIgnoreCase(EL_BUNDLE)) {
         bsu.setAssetStorageUnits(asus);
      }
      else if (qName.equalsIgnoreCase(EL_LOCATION)) {
         locationMap.put(locationKey, content);
      }
      else if (qName.equalsIgnoreCase(EL_LOCATIONS)) {
         asu.setLocations(locationMap);
      }
   }

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
      content = String.copyValueOf(ch, start, length).trim();
   }

   @Override
   public void warning(SAXParseException e) throws SAXException {
      throw e;
   }

   @Override
   public void error(SAXParseException e) throws SAXException {
      throw e;
   }

   @Override
   public void fatalError(SAXParseException e) throws SAXException {
      throw e;
   }

   public BundleStorageUnit getBsu() {
      return bsu;
   }
}
