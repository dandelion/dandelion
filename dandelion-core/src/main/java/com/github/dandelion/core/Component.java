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
package com.github.dandelion.core;

import com.github.dandelion.core.bundle.loader.BundleLoader;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.web.handler.debug.DebugMenu;

/**
 * <p>
 * Interface for all Dandelion components.
 * </p>
 * <p>
 * A component must define:
 * </p>
 * <ul>
 * <li>a logical <b>name</b> that is mainly used for logging purpose. It is also
 * used to indicate whether the component must be used in standalone mode thanks
 * to the {@link DandelionConfig#COMPONENTS_STANDALONE} configuration option</li>
 * <li>a <b>bundle loader</b>, implementing the {@link BundleLoader} interface,
 * that will tell Dandelion where to scan for bundles in the classpath</li>
 * <li>(optional) a <b>debug menu</b>, implementing the {@link DebugMenu}
 * interface, that will be automatically displayed in the Dandelion debugger</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public interface Component {

   /**
    * @return the component name.
    */
   String getName();

   /**
    * <p>
    * The {@link BundleLoader} used by the component.
    * </p>
    * 
    * @param context
    *           The Dandelion context.
    * @return the bundle loader.
    */
   BundleLoader getBundleLoader(Context context);

   /**
    * @return the debug menu of the component.
    */
   DebugMenu getDebugMenu();
}
