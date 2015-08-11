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
package com.github.dandelion.core.html;

/**
 * <p>
 * Super interface for all HTML tags.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.1.1
 */
public interface HtmlTag {

   public static final char CLASS_SEPARATOR = ' ';
   public static final char STYLE_SEPARATOR = ';';

   public static final String IF_OPENING_CONDITION = "<!--[if ";
   public static final String IF_CLOSING_CONDITION = "]>\n";
   public static final String ENDIF_CONDITION = "\n<![endif]-->";

   public static final String ATTR_ID = "id";
   public static final String ATTR_STYLE = "style";
   public static final String ATTR_CLASS = "class";
   public static final String ATTR_REL = "rel";
   public static final String ATTR_SRC = "src";
   public static final String ATTR_HREF = "href";

   /**
    * <p>
    * Returns a {@link StringBuilder} containing the HTML code used to render
    * the tag in a browser.
    * </p>
    * 
    * @return the HTML code corresponding to this tag.
    */
   public StringBuilder toHtml();
}
