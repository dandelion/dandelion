package com.github.dandelion.jsp.tag;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.api.asset.Asset;
import com.github.dandelion.core.api.tag.HtmlLink;
import com.github.dandelion.core.api.tag.HtmlScript;
import com.github.dandelion.core.asset.AssetStorage;

/**
 * <p>
 * JSP tag in charge of generating necessary HTML <code>script</code> and
 * <code>link</code> tags.
 * 
 * <p>
 * Usage :
 * 
 * <pre>
 * &lt;dandelion:asset scope="..." /&gt;
 * </pre>
 * 
 * @author Thibault Duchateau
 */
public class AssetTag extends TagSupport {

	private static final long serialVersionUID = -8609753777149757623L;
	private static final Logger LOG = LoggerFactory.getLogger(AssetTag.class);
		
	private String scope;

	public int doStartTag() throws JspException {

		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {

		// The stream to fill
		JspWriter out = pageContext.getOut();

		if (scope != null && !"".equals(scope)) {

			// Mode bourrin/test : j'utilise directement l'AssetStorage
			List<Asset> assets = AssetStorage.assetsFor(scope);
			System.out.println("scope = " + scope + ", assets = " + assets);

			try {
				for (Asset asset : assets) {
					switch (asset.getType()) {
					case css:
						out.println(new HtmlLink(asset.getLocal()).toHtml());
						break;
					case img:
						break;
					case js:
						out.println(new HtmlScript(asset.getLocal()).toHtml());
						break;
					default:
						break;
					}
				}
			} catch (IOException e) {
				throw new JspException(e);
			}
		}
		else{
			LOG.error("The scope attribute is required !");
			throw new JspException("The scope attribute is required !");
		}

		return EVAL_PAGE;
	}

	// Boring stuff

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}