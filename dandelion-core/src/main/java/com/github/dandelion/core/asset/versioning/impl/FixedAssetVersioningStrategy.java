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
package com.github.dandelion.core.asset.versioning.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.versioning.AbstractAssetVersioningStrategy;
import com.github.dandelion.core.util.EnumUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Versioning strategy that relies on a fixed version for all non-vendor assets.
 * </p>
 * <p>
 * Some fixed version types already exist to ease the setup, such as
 * {@link FixedVersionType#DATE} in order to use a formatted date as version
 * number or {@link FixedVersionType#STRING}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class FixedAssetVersioningStrategy extends AbstractAssetVersioningStrategy {

   private static final Logger LOG = LoggerFactory.getLogger(FixedAssetVersioningStrategy.class);

   /**
    * The fixed version to be applied on all non-vendor assets.
    */
   private String fixedVersion;

   @Override
   public String getName() {
      return "fixed";
   }

   @Override
   public void init(Context context) {

      FixedVersionType fixedVersionType = null;

      try {
         fixedVersionType = FixedVersionType.valueOf(context.getConfiguration().getAssetFixedVersionType()
               .toUpperCase());
      }
      catch (IllegalArgumentException e) {
         StringBuilder error = new StringBuilder();
         error.append("'");
         error.append(context.getConfiguration().getAssetFixedVersionType());
         error.append("' is not a valid versioning type. Possible values are: ");
         error.append(EnumUtils.printPossibleValuesOf(FixedVersionType.class));
         throw new DandelionException(error.toString(), e);
      }

      switch (fixedVersionType) {
      case DATE:
         LOG.debug("Selected fixed version type: {}", FixedVersionType.DATE);
         DateFormat dateFormat = null;
         String desiredDateFormat = context.getConfiguration().getAssetFixedVersionDatePattern();

         LOG.debug("Selected date format: {}", desiredDateFormat);
         try {
            dateFormat = new SimpleDateFormat(desiredDateFormat);
            LOG.debug("Selected fixed version type option: Date format = {}", desiredDateFormat);
         }
         catch (IllegalArgumentException e) {
            throw new DandelionException("Wrong date pattern configured : " + desiredDateFormat, e);
         }

         Date date = null;
         String desiredDate = context.getConfiguration().getAssetFixedVersionValue();
         if (StringUtils.isNotBlank(desiredDate)) {
            try {
               date = dateFormat.parse(desiredDate);
            }
            catch (ParseException e) {
               throw new DandelionException("Unable to parse the desired date \"" + desiredDate + "\" in the format \""
                     + desiredDateFormat + "\"");
            }
         }
         else {
            date = GregorianCalendar.getInstance().getTime();
         }
         this.fixedVersion = dateFormat.format(date);
         break;
      case STRING:
         LOG.debug("Selected fixed version type: {}", FixedVersionType.STRING);
         this.fixedVersion = context.getConfiguration().getAssetFixedVersionValue();
         break;
      default:
         break;
      }
   }

   @Override
   public String getAssetVersion(Asset asset) {
      return this.fixedVersion;
   }
}
