/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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

/**
 * Plain old HTML <code>link</code> tag.
 * 
 * @author Thibault Duchateau
 */
public class LinkTag extends HtmlTag {

	/**
	 * Plain old HTML <code>href</code> attribute.
	 */
	private String href;
    /**
     * Plain old HTML <code>rel</code> attribute (by default 'stylesheet').
     */
    private String rel = "stylesheet";


    public LinkTag(){
	}
	
	public LinkTag(String href){
		this.href = href;
	}

    public LinkTag(String href, String rel){
        this(href);
        this.rel = rel;
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toHtml(){
		StringBuffer html = new StringBuffer();
		html.append("<link rel=\"");
        html.append(this.rel);
        html.append("\"");
		
		if(this.href != null){
			html.append(" href=\"");
			html.append(this.href);
			html.append("\"");
		}

        html.append(attributesToHtml());
        html.append(attributesOnlyNameToHtml());
		
		html.append("/>");
		
		return html.toString();
	}
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }
}