package com.github.dandelion.core.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.dandelion.core.component.Component;
import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;

public class AssetManager {

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Recuperation de tous les fichier *.json present dans le classpath sous le repertoire "dandelion".
	 */
	public void loadAssets() {

		// Get current classloader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

		List<Component> components = new ArrayList<Component>();

		try {
			System.out.println("=================================");
			ClassPathResource[] resources = new ClassPathScanner().scanForResources("dandelion", "", "json");
			for(ClassPathResource resource : resources){
				System.out.println("Location = " + resource.getLocation());
				System.out.println("Ressource = " + resource.getFilename());
			}
			
			for (ClassPathResource resource : resources) {

				// Get default file as stream
				InputStream configFileStream = classLoader.getResourceAsStream(resource.getLocation());

				Component component = mapper.readValue(configFileStream, Component.class);
				System.out.println("Component = " + component);

				components.add(component);
			}

			// TODO gerer les conflits de version : si 2 librairies sont
			// trouvees avec le meme nom : prendre la 1ere version qui vient et
			// logguer
			checkConflicts(components);

		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void checkConflicts(List<Component> components){
		// TODO
	}
}
