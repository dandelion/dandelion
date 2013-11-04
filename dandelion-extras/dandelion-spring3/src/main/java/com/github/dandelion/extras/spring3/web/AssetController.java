package com.github.dandelion.extras.spring3.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.dandelion.core.asset.web.HtmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.AssetsCacheSystem;
import com.github.dandelion.core.asset.web.AssetServlet;

/**
 * <p>
 * Controller used to render cached web resources instead of a concrete
 * implementation of {@link AssetServlet}.
 * <p>
 * Note that client applications must import the corresponding Spring config
 * file in order for this controller to be scanned. For example using:
 * 
 * <pre>
 * <code>&lt;import resource="classpath:dandelion.xml" /&gt;</code>
 * </pre>
 * 
 * @since 0.3.0
 */
@Controller
@RequestMapping(value = AssetServlet.DANDELION_ASSETS_URL, method = RequestMethod.GET)
public class AssetController {
	@RequestMapping(value = "{assetKey}")
	public @ResponseBody
    String renderAsset(@PathVariable(value = "assetKey") String assetKey, HttpServletResponse response) throws IOException {
        String[] content = HtmlUtil.getAssetContent(response, assetKey);
        response.setHeader("Cache-Control", HtmlUtil.getCacheControl());
        response.setContentType(content[1]);
		return content[0];
	}
}
