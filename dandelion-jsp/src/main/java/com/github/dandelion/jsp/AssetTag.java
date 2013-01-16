package com.github.dandelion.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class AssetTag extends TagSupport {
	
	private static final long serialVersionUID = -8609753777149757623L;

	private String asset;
	
	public int doStartTag() throws JspException {

		return SKIP_BODY;
	}
	
	public int doEndTag() throws JspException {
		
		return EVAL_PAGE;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}
}