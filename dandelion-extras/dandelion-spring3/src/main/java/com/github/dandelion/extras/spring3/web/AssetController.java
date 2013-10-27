package com.github.dandelion.extras.spring3.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.AssetsCacheSystem;
import com.github.dandelion.core.asset.web.AssetServlet;

@Controller
@RequestMapping(value = AssetServlet.DANDELION_ASSETS_URL)
public class AssetController {

	// Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetController.class);

    @RequestMapping(method = RequestMethod.GET)
	public @ResponseBody String renderJs(HttpServletResponse response, HttpServletRequest request) throws IOException {

    	
		String cacheKey = AssetsCacheSystem.getCacheKeyFromRequest(request);

//        if (isDevModeEnabled() && !AssetsCacheSystem.checkCacheKey(cacheKey)) {
//        	throw new ServletException("The Dandelion assets should have been generated!");
//        }

        AssetType resourceType = AssetType.typeOfAsset(cacheKey);
        if (resourceType == null) {
        	LOG.debug("Unknown asset type from key {}", cacheKey);
//        	return;
        }

        String fileContent = AssetsCacheSystem.getCacheContent(cacheKey);
        if(fileContent == null) {
        	LOG.debug("Missing content from key {}", cacheKey);
//            return;
        }
        
        return fileContent;
//        response.setContentType(resourceType.getContentType());
//        response.setHeader("Cache-Control", "no-cache");
//        response.getWriter().write(fileContent);
	}
}
