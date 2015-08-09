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
package com.github.dandelion.core.asset.processor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;

/**
 * <p>
 * Factory that produces a processor pipeline for each asset.
 * </p>
 * <p>
 * Each pipeline is composed of an ordered list of {@link AssetProcessor} that
 * will be used to process the asset.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 2.0.0
 */
public class AssetProcessorPipelineFactory {

   private static final Logger LOG = LoggerFactory.getLogger(AssetProcessorPipelineFactory.class);

   /**
    * The Dandelion context.
    */
   private final Context context;

   /**
    * <p>
    * Constructor.
    * </p>
    * 
    * @param context
    *           The Dandelion context.
    */
   public AssetProcessorPipelineFactory(Context context) {
      this.context = context;
   }

   /**
    * <p>
    * Resolves the asset pipeline for the provided asset.
    * </p>
    * <p>
    * Processors configured in bundles take precedence over the ones defined in
    * the global properties file.
    * </p>
    * 
    * @param asset
    *           The asset to be processed.
    * @return an asset pipeline.
    */
   public List<AssetProcessor> resolveProcessorPipeline(Asset asset) {
      if (asset.getProcessors() != null) {
         if (asset.getProcessors().length > 0) {
            return getCustomPipeline(asset);
         }
         else {
            LOG.warn("The requested processor list is empty");
            return Collections.emptyList();
         }
      }
      else {
         return getDefaultPipeline(asset);
      }
   }

   /**
    * <p>
    * Builds a custom pipeline based on the processors defined in the asset
    * definition from the bundle.
    * </p>
    * 
    * @param asset
    *           The asset to be processed.
    * @return a custom pipeline for the provided asset.
    */
   private List<AssetProcessor> getCustomPipeline(Asset asset) {
      List<AssetProcessor> retval = new ArrayList<AssetProcessor>();

      for (String processorString : asset.getProcessors()) {
         String normalizedName = processorString.toLowerCase().trim();

         AssetProcessor processor = null;

         if (context.getProcessorsMap().containsKey(normalizedName)) {
            processor = context.getProcessorsMap().get(processorString.toLowerCase().trim());
         }
         else {
            throw new DandelionException(
                  "\"" + normalizedName + "\" doesn't exist among the available processors. Please correct the asset \""
                        + asset.getName() + "\" of the bundle \"" + asset.getBundle() + "\" before continuing.");
         }

         Annotation annotation = processor.getClass().getAnnotation(CompatibleAssetType.class);
         CompatibleAssetType compatibleAssetType = (CompatibleAssetType) annotation;
         List<AssetType> compatibleAssetTypes = Arrays.asList(compatibleAssetType.types());
         if (compatibleAssetTypes.contains(asset.getType())) {
            retval.add(processor);
         }
         else {
            LOG.warn("The processor \"" + processor.getName() + "\" is not compatible with the asset: " + asset.toLog()
                  + ". It will be ignored.");
         }
      }
      return retval;
   }

   /**
    * <p>
    * Builds a pipeline based on the default configuration from the global
    * properties file.
    * </p>
    * 
    * @param asset
    *           The asset to be processed.
    * @return a default pipeline for the provided asset.
    */
   private List<AssetProcessor> getDefaultPipeline(Asset asset) {
      List<AssetProcessor> retval = new ArrayList<AssetProcessor>();
      List<String> defaultProcessors = null;

      switch (asset.getType()) {
      case css:
         defaultProcessors = context.getConfiguration().getAssetCssProcessors();
         break;
      case js:
         defaultProcessors = context.getConfiguration().getAssetJsProcessors();
         break;
      case less:
         defaultProcessors = context.getConfiguration().getAssetLessProcessors();
         break;
      default:
         defaultProcessors = Collections.emptyList();
         break;
      }

      for (String processorString : defaultProcessors) {
         retval.add(context.getProcessorsMap().get(processorString.toLowerCase().trim()));
      }

      return retval;
   }
}
