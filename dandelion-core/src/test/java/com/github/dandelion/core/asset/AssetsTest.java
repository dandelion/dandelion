package com.github.dandelion.core.asset;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.asset.web.AssetRequestContext;

public class AssetsTest {
	
	private static MockHttpServletRequest request;
	private static AssetRequestContext context;
	private static final String CONTEXT_PATH = "/contextPath";
	
	@BeforeClass
	public static void setup() {
		request = new MockHttpServletRequest();
		request.setContextPath(CONTEXT_PATH);
		context = AssetRequestContext.get(request);
	}

	@Test
	public void should_return_false_for_a_bundle_without_asset(){
		context.addBundles("bundle1");
		assertThat(Assets.existsAssetsFor(request)).isFalse();
		context.clear();
	}
	
	@Test
	public void should_return_true_for_a_bundle_with_assets(){
		context.addBundles("bundle2");
		assertThat(Assets.existsAssetsFor(request)).isTrue();
		context.clear();
	}
	
	/**
	 * Since assets.locations=webapp,cdn, "webapp" locations are selected first
	 * if they exist in each asset.
	 */
	@Test
	public void should_return_all_processed_assets_by_bundle_name() {
		
		Set<Asset> assetsB1 = Assets.assetsFor(request, "bundle1");
		assertThat(assetsB1).isEmpty();

		Set<Asset> assetsB2 = Assets.assetsFor(request, "bundle2");
		assertThat(assetsB2).hasSize(1);
		for(Asset aB2 : assetsB2){
			assertThat(aB2.getLocation()).startsWith(CONTEXT_PATH);
		}
		
		Set<Asset> assetsB3 = Assets.assetsFor(request, "bundle3");
		assertThat(assetsB3).hasSize(2);
		for(Asset aB3 : assetsB3){
			assertThat(aB3.getLocation()).startsWith(CONTEXT_PATH);
		}
	}
	
	@Test
	public void should_return_all_processed_assets_by_bundle_names() {
		Set<Asset> assets = Assets.assetsFor(request, null, "bundle1", "bundle2", "bundle3");
		assertThat(assets).hasSize(3);
	}
	
	
//	@Test
//	public void should_contains_assets() {
//		assertThat(Assets.isEmpty()).isFalse();
//	}
//
//	@Test
//	public void should_load_default_json() {
//		assertThat(Assets.assetsFor()).hasSize(1);
//	}
//
//	@Test
//	public void should_load_the_assets_locations() {
//		assertThat(Assets.getAssetLocations()).contains("remote");
//	}
//
//	@Test
//	public void should_be_the_remote_url_for_all_assets() {
//		List<Asset> assets = Assets.prepareAssetsFor(request, new String[] { "default", "detachedBundle", "plugin1",
//				"plugin2" }, new String[0]);
//		assertThat(assets).hasSize(6);
//		for (Asset asset : assets) {
//			assertThat(asset.getLocations().values()).contains("remoteURL");
//		}
//	}
//
//	@Test
//	public void should_contains_assets_for_bundle() {
//		assertThat(
//				Assets.existsAssetsFor(new String[] { "plugin1", "plugin2", "plugin1addon2", "plugin3addon" },
//						new String[0])).isTrue();
//	}
//
//	@Test
//	public void should_exclude_assets_by_name() {
//		List<Asset> assets = Assets.assetsFor("detachedBundle");
//		assertThat(Assets.excludeByName(assets, "asset3addon")).hasSize(1);
//		assertThat(Assets.excludeByName(assets, "asset1")).hasSize(0);
//		assertThat(Assets.excludeByName(assets, "asset1.css")).hasSize(1);
//	}
//
//	@Test
//	public void should_filter_assets_by_type() {
//		List<Asset> assets = Assets.assetsFor("plugin1", "plugin2", "plugin1addon2", "plugin3addon");
//		assertThat(assets).hasSize(7);
//		assertThat(Assets.filterByType(assets, AssetType.css)).hasSize(3);
//		assertThat(Assets.filterByType(assets, AssetType.js)).hasSize(4);
//	}
//
//	@Test
//	public void should_filter_assets_by_dom() {
//		List<Asset> assets = Assets.assetsFor("plugin1", "plugin2", "plugin1addon2", "plugin3addon");
//		assertThat(assets).hasSize(7);
//		assertThat(Assets.filterByDOMPosition(assets, AssetDOMPosition.head)).hasSize(3);
//		assertThat(Assets.filterByDOMPosition(assets, AssetDOMPosition.body)).hasSize(4);
//	}
//
//	@Test
//	public void should_manage_unknown_location() {
//		List<Asset> assets = Assets.prepareAssetsFor(request, new String[] { "unknown_location" }, new String[0]);
//		assertThat(assets).hasSize(2);
//		for (Asset asset : assets) {
//			assertThat(asset.getLocations().values()).hasSize(1).contains("URL");
//		}
//	}
//
//	@Test
//	public void should_respect_locations_order() {
//		List<Asset> assets = Assets.prepareAssetsFor(request, new String[] { "locations_order" }, new String[0]);
//		assertThat(assets).hasSize(3);
//		for (Asset asset : assets) {
//			assertThat(asset.getLocations().values()).contains("otherURL");
//		}
//	}
}
