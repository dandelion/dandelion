package com.github.dandelion.jsp.tag;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.github.dandelion.core.html.LinkTag;
import com.github.dandelion.core.html.ScriptTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetsJsonLoader;
import com.github.dandelion.core.asset.AssetsStorage;

/**
 * <p>
 * JSP tag in charge of generating necessary HTML <code>script</code> and
 * <code>link</code> tags.
 * 
 * <p>
 * Usage :
 * 
 * <pre>
 * &lt;dandelion:asset scopes="..." /&gt;
 * </pre>
 * 
 * @author Thibault Duchateau
 */
public class AssetTag extends TagSupport {

	private static final long serialVersionUID = -8609753777149757623L;
	private static final Logger LOG = LoggerFactory.getLogger(AssetTag.class);
		
	private String scopes;

	public int doStartTag() throws JspException {

		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {

		AssetsJsonLoader loader = new AssetsJsonLoader();
		loader.loadAssets();
		
		// The stream to fill
		JspWriter out = pageContext.getOut();

		if (scopes != null && !"".equals(scopes)) {

			// Mode bourrin/test : j'utilise directement l'AssetStorage
			List<Asset> assets = AssetsStorage.assetsFor(scopes);
			System.out.println("scope = " + scopes + ", assets = " + assets);

			try {
				for (Asset asset : assets) {
					switch (asset.getType()) {
					case css:
						out.println(new LinkTag(asset.getRemote()).toHtml());
						break;
					case img:
						break;
					case js:
						out.println(new ScriptTag(asset.getRemote()).toHtml());
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

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scope) {
		this.scopes = scope;
	}
}