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

package com.github.dandelion.jsp.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.github.dandelion.core.web.AssetRequestContext;

/**
 * <p>
 * JSP tag for manipulating the asset stack by excluding assets from it.
 * 
 * <p>
 * Usage :
 * 
 * <pre>
 * &lt;dandelion:asset jsExcludes="..." /&gt;
 * </pre>
 * 
 * or:
 * 
 * <pre>
 * &lt;dandelion:asset cssExcludes="..." /&gt;
 * </pre>
 */
public class AssetTag extends TagSupport {

   private static final long serialVersionUID = -417156851675582892L;

   /**
    * Tag attributes
    */
   // Assets to exclude from the current request
   private String jsExcludes;
   private String cssExcludes;

   public int doEndTag() throws JspException {
      AssetRequestContext.get(pageContext.getRequest()).excludeJs(jsExcludes);
      AssetRequestContext.get(pageContext.getRequest()).excludeCss(cssExcludes);
      return EVAL_PAGE;
   }

   public void setJsExcludes(String jsExcludes) {
      this.jsExcludes = jsExcludes;
   }

   public void setCssExcludes(String cssExcludes) {
      this.cssExcludes = cssExcludes;
   }
}