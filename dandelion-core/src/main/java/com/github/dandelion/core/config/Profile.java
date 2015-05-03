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
package com.github.dandelion.core.config;

import java.util.Arrays;

import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * A {@link Profile} is typically used to activate a set of configurations which
 * may vary depending on the target environment (e.g. dev, qa, prod).
 * </p>
 * 
 * <p>
 * A {@link Profile} is activated using the {@value #DANDELION_PROFILE_ACTIVE}
 * system property and will tell Dandelion to load the configuration file using
 * the following syntax: {@code dandelion_[activeProfile].properties}.
 * </p>
 * 
 * <p>
 * Note that only one profile can be activated at a time.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 * @see StandardConfigurationLoader
 */
public class Profile {

   public static final String DANDELION_PROFILE_ACTIVE = "dandelion.profile.active";
   public static final String DEFAULT_DEV_PROFILE = "dev";
   public static final String DEFAULT_PROD_PROFILE = "prod";
   public static final String[] DEV_ALIASES = new String[] { "", "dev", "development" };
   public static final String[] PROD_ALIASES = new String[] { "prod", "production" };

   /**
    * <p>
    * Retrieves the current active profile.
    * </p>
    * 
    * <p>
    * Some words are reserved and allow Dandelion to use a preconfigured set of
    * configurations:
    * </p>
    * <ul>
    * <li>"" (empty or null), "dev" or "development": all these words will lead
    * to activate the "dev" profile, and thus to use all default development
    * values of each {@link DandelionConfig} entry.</li>
    * <li>"prod" or "production": these words will lead to activate the "prod"
    * profile, and thus to use all default production values of each
    * {@link DandelionConfig} entry.</li>
    * </ul>
    * 
    * @return <ul>
    *         <li>{@value #DEFAULT_DEV_PROFILE} if the
    *         {@value #DANDELION_PROFILE_ACTIVE} system property is set to "",
    *         "dev" or "development"</li>
    *         <li>{@value #DEFAULT_PROD_PROFILE} if the
    *         {@value #DANDELION_PROFILE_ACTIVE} system property is set to
    *         "prod" or "production"</li>
    *         <li>otherwise the active profile, trimmed.</li>
    *         </ul>
    */
   public static String getActiveProfile() {
      String activeProfile = System.getProperty(DANDELION_PROFILE_ACTIVE);
      if (StringUtils.isBlank(activeProfile) || Arrays.asList(DEV_ALIASES).contains(activeProfile)) {
         return DEFAULT_DEV_PROFILE;
      }
      else if (StringUtils.isNotBlank(activeProfile) && Arrays.asList(PROD_ALIASES).contains(activeProfile)) {
         return DEFAULT_PROD_PROFILE;
      }

      return activeProfile.trim();
   }

   /**
    * <p>
    * Retrieves the current and untouched active profile.
    * </p>
    * 
    * @return the raw active profile, but at least trimmed if not null and
    *         empty.
    */
   public static String getActiveRawProfile() {
      String activeProfile = System.getProperty(DANDELION_PROFILE_ACTIVE);
      return StringUtils.isNotBlank(activeProfile) ? activeProfile.trim() : activeProfile;
   }
}
