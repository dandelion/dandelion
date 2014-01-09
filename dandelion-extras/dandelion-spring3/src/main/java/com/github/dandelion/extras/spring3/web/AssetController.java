package com.github.dandelion.extras.spring3.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.github.dandelion.core.asset.web.HtmlUtil;
import com.github.dandelion.core.asset.web.data.AssetContent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
	@RequestMapping(value = "{assetKey:.+}")
	public ResponseEntity<String> renderAsset(@PathVariable String assetKey, HttpServletResponse response) throws IOException {
        AssetContent assetContent = HtmlUtil.getAssetContent(assetKey);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(assetContent.getContentType()));
        headers.setCacheControl(HtmlUtil.getCacheControl());
        headers.setContentLength(assetContent.getContent().getBytes().length);
		return new ResponseEntity<String>(assetContent.getContent(), headers, HttpStatus.OK);
	}
}
