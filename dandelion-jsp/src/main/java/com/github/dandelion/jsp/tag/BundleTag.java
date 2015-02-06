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
 * JSP tag for manipulating the current assets by adding or excluding bundles
 * from it.
 * 
 * <p>
 * Usage :
 * 
 * <pre>
 * &lt;dandelion:bundle includes="..." excludes="..." /&gt;
 * </pre>
 */
public class BundleTag extends TagSupport {

   private static final long serialVersionUID = -417156851675582892L;

   /**
    * Tag attributes
    */
   // Bundles to include in the current request
   private String includes;

   // Bundles to exclude from the current request
   private String excludes;

   public int doEndTag() throws JspException {
      AssetRequestContext.get(pageContext.getRequest()).addBundles(includes).excludeBundles(excludes);
      return EVAL_PAGE;
   }

   public void setIncludes(String includes) {
      this.includes = includes;
   }

   public void setExcludes(String excludes) {
      this.excludes = excludes;
   }
}