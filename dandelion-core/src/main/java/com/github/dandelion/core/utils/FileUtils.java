/*
 * [The "BSD licence"]
 * Copyright (c) 2014 Dandelion
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

import java.io.File;

/**
 * Utility for File manipulation.
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public final class FileUtils {
    /**
     * Check if a filePath start with the folderPath
     * @param filePath path of file
     * @param folderPath path of folder
     * @return <code>true</code> if the file is in the folder
     */
    public static boolean contains(String filePath, String folderPath) {
        return cleanBase(filePath).contains(cleanBase(folderPath));
    }

    /**
     * Get the name from file path
     * @param filePath file path
     * @return the name of the file path
     */
    public static String getName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    /**
     * Clean the path for cross-platform usage
     *
     * @param path path to clean
     * @return clean path
     */
    private static String cleanBase(String path) {
        return path.toLowerCase().replace("\\", "/");
    }

    /**
     * Prevent instantiation.
     */
    private FileUtils() {
    }
}
