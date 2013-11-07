package com.github.dandelion.core.asset.web;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class AssetFilterResponseWrapper extends HttpServletResponseWrapper {
    ByteArrayOutputStream output;
    AssetFilterServletOutputStream filterOutput;
    PrintWriter pw;

    public AssetFilterResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (filterOutput==null){
            filterOutput = new AssetFilterServletOutputStream(output);
        }
        return filterOutput;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (filterOutput==null){
            filterOutput = new AssetFilterServletOutputStream(output);
        }
        if(pw==null){
            pw = new PrintWriter(filterOutput, true);
        }
        return pw;
    }

    public byte[] getDataStream(){
        return output.toByteArray();
    }
}