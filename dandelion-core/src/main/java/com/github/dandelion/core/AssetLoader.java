package com.github.dandelion.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.dandelion.api.Asset;
import com.github.dandelion.api.Component;
import com.github.dandelion.utils.ClasspathUtils;

public class AssetLoader {

	public static final String DEFAULT_CDN_JSON = "dandelion/cdn/";
	public static final String DEFAULT_RESOURCES_JSON = "dandelion/resources/";
	
	public static void main(String[] args){
		load();
	}
	
	// TODO : les fichiers *.json sont copies dans dandelion-core/src/main/resources mais devraient normalement etre
	// dans dandelion-datatables/src/main/resources
	public static void load(){
		
		// Get current classloader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		
		List<Asset> globalAssets = new ArrayList<Asset>();
		
		try {
			// Recuperation de tous les fichier *.json disponibles dans le classpath
			String[] configFilePaths = ClasspathUtils.getClasspathFileNamesWithExtension("json");
			
			for(String configFilePath : configFilePaths){
				
				// Get default file as stream
				InputStream propertiesStream = classLoader.getResourceAsStream(configFilePath);
				
				// TODO Parsing en Asset.java
				Component component = mapper.readValue(propertiesStream, Component.class);
				System.out.println("Component = " + component);
				
				// TODO Ajout de l'Asset.java a une liste
				
			}
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
