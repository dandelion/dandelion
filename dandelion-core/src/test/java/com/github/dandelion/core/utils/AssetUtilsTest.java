package com.github.dandelion.core.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.util.AssetUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetUtilsTest {

   @Test
   public void should_return_a_filtered_set_when_filtering_by_type() {

      Set<Asset> assets = new LinkedHashSet<Asset>();
      assets.add(new Asset("name1", "1.0.0", AssetType.js));
      assets.add(new Asset("name2", "1.0.0", AssetType.js));
      assets.add(new Asset("name3", "1.0.0", AssetType.css));

      assertThat(AssetUtils.filtersByType(assets, AssetType.css)).extracting("name").contains("name3");
   }

   @Test
   public void should_return_a_filtered_set_when_filtering_by_asset_name() {

      Set<Asset> assets = new LinkedHashSet<Asset>();
      assets.add(new Asset("name1", "1.0.0", AssetType.js));
      assets.add(new Asset("name2", "1.0.0", AssetType.js));
      assets.add(new Asset("name3", "1.0.0", AssetType.js));

      assertThat(AssetUtils.filtersByName(assets, new String[] { "name2" })).extracting("name").contains("name1",
            "name3");
   }

   @Test
   public void should_return_the_same_set_when_filtering_by_asset_name() {

      Set<Asset> assets = new LinkedHashSet<Asset>();
      assets.add(new Asset("name1", "1.0.0", AssetType.js));
      assets.add(new Asset("name2", "1.0.0", AssetType.js));
      assets.add(new Asset("name3", "1.0.0", AssetType.js));

      assertThat(AssetUtils.filtersByName(assets, new String[] { "name5" })).extracting("name").contains("name1",
            "name2", "name3");
   }

   @Test
   public void should_return_a_filtered_set_when_filtering_by_position() {

      Set<Asset> asus = new LinkedHashSet<Asset>();
      asus.add(new Asset("name1", "1.0.0", AssetType.js, AssetDomPosition.body));
      asus.add(new Asset("name2", "1.0.0", AssetType.js, AssetDomPosition.body));
      asus.add(new Asset("name3", "1.0.0", AssetType.js, AssetDomPosition.head));

      assertThat(AssetUtils.filtersByDomPosition(asus, AssetDomPosition.head)).extracting("name").contains("name3");
   }

   @Test
   public void should_return_a_filtered_set_when_filtering_by_position_with_default_dom() {

      Set<Asset> asus = new LinkedHashSet<Asset>();
      asus.add(new Asset("name1", "1.0.0", AssetType.js));
      asus.add(new Asset("name2", "1.0.0", AssetType.js));
      asus.add(new Asset("name3", "1.0.0", AssetType.css));

      assertThat(AssetUtils.filtersByDomPosition(asus, AssetDomPosition.body)).extracting("name").contains("name1",
            "name2");
   }

   @Test
   public void should_return_the_asset_extension() {

      assertThat(AssetUtils.getExtension("jquery.js")).isEqualTo("js");
      assertThat(AssetUtils.getExtension("dataTables.jquery.js")).isEqualTo("js");
      assertThat(AssetUtils.getExtension("jquery")).isNull();
      assertThat(AssetUtils.getExtension("")).isNull();
   }
}
