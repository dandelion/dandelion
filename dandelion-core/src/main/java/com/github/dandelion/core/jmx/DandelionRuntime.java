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
package com.github.dandelion.core.jmx;

import javax.servlet.FilterConfig;

import com.github.dandelion.core.Beta;
import com.github.dandelion.core.Context;

/**
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
@Beta
public class DandelionRuntime implements DandelionRuntimeMBean {

	private Context context;
	
	public DandelionRuntime(Context context, FilterConfig filterConfig){
		this.context = context;
	}
	
	/**
	 * Blabla
	 */
	@Override
	public void reloadBundles() {
		// TODO Auto-generated method stub
		System.out.println("RELOADING!!!");
		context.initBundleStorage();
		System.out.println("Context reloaded");
	}

	@Override
	public void clearAllCache() {
		System.out.println("Clearing all cache");
		context.getAssetCache().clearAll();
		System.out.println("All caches cleared");
	}

	@Override
	public void clearAssetCache() {
		// TODO Auto-generated method stub
		
	}
	
	
}
