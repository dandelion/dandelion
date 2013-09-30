package com.github.dandelion.core.asset.web;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class CharResponseWrapper extends HttpServletResponseWrapper {
	private CharArrayWriter output;

	public String toString() {
		return output.toString();
	}

	public CharResponseWrapper(HttpServletResponse response) {
		super(response);
		output = new CharArrayWriter();
	}

	public PrintWriter getWriter() {
		return new PrintWriter(output);
	}
}
