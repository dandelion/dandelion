package com.github.dandelion.core.asset.web;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class AssetFilterResponseWrapper extends HttpServletResponseWrapper {
    protected AssetFilterServletOutputStream stream;
    protected PrintWriter writer = null;

    public AssetFilterResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public AssetFilterServletOutputStream createOutputStream() throws IOException {
        return new AssetFilterServletOutputStream();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called for this response");
        }

        if (stream == null) {
            stream = createOutputStream();
        }

        return stream;
    }

    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }

        if (stream != null) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }

        stream = createOutputStream();
        writer = new PrintWriter(stream);

        return writer;
    }

    public String getWrappedContent() {
        return stream.toString();
    }
}