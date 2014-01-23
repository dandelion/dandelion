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
package com.github.dandelion.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;

import javax.servlet.http.HttpServletRequest;

public final class ResourceUtils {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(ResourceUtils.class);

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final String PROTOCOL_RELATIVE_PREFIX = "//";

    public static InputStream getFileFromClasspath(String pathToFile) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathToFile);
    }

    public static String getFileContentFromClasspath(String pathToFile, boolean neverFail) {
        try {
            InputStream in = getFileFromClasspath(pathToFile);
            return getContentFromInputStream(in);
        } catch (IOException e) {
            DandelionException de = DandelionException.wrap(e, ResourceError.CONTENT_CANT_BE_READ_FROM_INPUTSTREAM)
                    .set("path", pathToFile);
            if(neverFail) {
                LOG.debug(de.getLocalizedMessage(), de);
                return "";
            }
            throw de;
        }
    }

    public static String getContentFromInputStream(InputStream input) throws IOException {
        StringWriter sw = new StringWriter();
        InputStreamReader in = new InputStreamReader(input);

        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        while (-1 != (n = in.read(buffer))) {
            sw.write(buffer, 0, n);
        }

        return sw.toString();
    }

    public static String getContentFromUrl(HttpServletRequest request, String url, boolean neverFail) {
        try {
            String realUrl = url;
            if(realUrl.startsWith(PROTOCOL_RELATIVE_PREFIX)) {
                realUrl = request.isSecure()?"https:":"http:"+ realUrl;
            }
            URL urlLocation = new URL(realUrl);
            return ResourceUtils.getContentFromInputStream(urlLocation.openStream());
        } catch(IOException e) {
            DandelionException de = DandelionException.wrap(e, ResourceError.CONTENT_CANT_BE_READ_FROM_URL)
                    .set("url", url);
            if(neverFail) {
                LOG.debug(de.getLocalizedMessage(), de);
                return "";
            }
            throw de;
        }
    }
}
