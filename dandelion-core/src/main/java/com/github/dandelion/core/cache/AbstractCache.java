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
package com.github.dandelion.core.cache;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;

/**
 * <p>
 * Abstract base class for all implemenations of {@link Cache}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public abstract class AbstractCache implements Cache {

	protected Context context;
	private AtomicLong getCount;
	private AtomicLong putCount;
	private AtomicLong hitCount;
	private AtomicLong missCount;

	@Override
	public void initCache(Context context) {
		this.context = context;
		this.getCount = new AtomicLong(0);
		this.putCount = new AtomicLong(0);
		this.hitCount = new AtomicLong(0);
		this.missCount = new AtomicLong(0);
	}

	/**
	 * @return the {@link Logger} bound to the actual implementation.
	 */
	protected abstract Logger getLogger();

	@Override
	public Set<Asset> get(String cacheKey) {

		this.getCount.incrementAndGet();
		Set<Asset> assets = doGet(cacheKey);

		if (assets == null) {
			this.missCount.incrementAndGet();
			getLogger().trace("Cache miss for key \"{}\"", cacheKey);
			return null;
		}

		this.hitCount.incrementAndGet();
		getLogger().trace("Cache hit for key \"{}\"", cacheKey);
		return assets;
	}

	protected abstract Set<Asset> doGet(String cacheKey);

	@Override
	public void put(String cacheKey, Set<Asset> a) {

		this.putCount.incrementAndGet();
		int newSize = doPut(cacheKey, a);
		getLogger().trace("Added cache entry for key \"{}\". New size is {}.", cacheKey, newSize);
	}

	protected abstract int doPut(String cacheKey, Set<Asset> a);

	@Override
	public void clear() {
		getLogger().trace("Clearing cache");
		doClear();
	}

	protected abstract void doClear();

	public AtomicLong getGetCount() {
		return getCount;
	}

	public AtomicLong getPutCount() {
		return putCount;
	}

	public AtomicLong getHitCount() {
		return hitCount;
	}

	public AtomicLong getMissCount() {
		return missCount;
	}
}
