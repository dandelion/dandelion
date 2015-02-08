package com.github.dandelion.core.utils;

import org.junit.Test;

import com.github.dandelion.core.util.PathUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class PathUtilsTest {

   @Test
   public void should_return_normalized_path() {
      assertThat(PathUtils.normalizePath("//some/path")).isEqualTo("some/path");
      assertThat(PathUtils.normalizePath("//some/path/")).isEqualTo("some/path");
      assertThat(PathUtils.normalizePath("/some//path/")).isEqualTo("some/path");
   }

   @Test
   public void should_return_parent_path() {
      assertThat(PathUtils.getParentPath(null)).isEqualTo("");
      assertThat(PathUtils.getParentPath("")).isEqualTo("");
      assertThat(PathUtils.getParentPath("/")).isEqualTo("/");
      assertThat(PathUtils.getParentPath("/folder/sub/")).isEqualTo("/folder/");
      assertThat(PathUtils.getParentPath("/folder/sub")).isEqualTo("/folder/");
      assertThat(PathUtils.getParentPath("/folder/sub")).isEqualTo("/folder/");
      assertThat(PathUtils.getParentPath("/folder/sub/sub2/")).isEqualTo("/folder/sub/");
      assertThat(PathUtils.getParentPath("/folder/sub/sub2/file.text")).isEqualTo("/folder/sub/sub2/");
   }

   @Test
   public void should_return_relative_web_path() {
      assertThat(PathUtils.getRelativeWebPath(null, null)).isEqualTo("");
      assertThat(PathUtils.getRelativeWebPath(null, "")).isEqualTo("");
      assertThat(PathUtils.getRelativeWebPath("", null)).isEqualTo("");
      assertThat(PathUtils.getRelativeWebPath("", "")).isEqualTo("");
      assertThat(PathUtils.getRelativeWebPath(null, "http://dandelion.github.io/")).isEqualTo("");
      assertThat(PathUtils.getRelativeWebPath("", "http://dandelion.github.io/")).isEqualTo("");
      assertThat(PathUtils.getRelativeWebPath("http://dandelion.github.io/", null)).isEqualTo("");
      assertThat(PathUtils.getRelativeWebPath("http://dandelion.github.io/", "")).isEqualTo("");
      assertThat(
            PathUtils.getRelativeWebPath("http://dandelion.github.io/", "http://dandelion.github.io/folder/index.html"))
            .isEqualTo("folder/index.html");
      assertThat(
            PathUtils.getRelativeWebPath("http://dandelion.github.io/folder/index.html", "http://dandelion.github.io/"))
            .isEqualTo("../../");
   }

   @Test
   public void should_return_root_relative_web_path() {
      assertThat(PathUtils.getRootRelativePath(null)).isEqualTo("");
      assertThat(PathUtils.getRootRelativePath("")).isEqualTo("");
      assertThat(PathUtils.getRootRelativePath("/assets/some.css")).isEqualTo("../");
      assertThat(PathUtils.getRootRelativePath("/assets/css/some.css")).isEqualTo("../../");
      assertThat(PathUtils.getRootRelativePath("/assets/css/subfolder/some.css")).isEqualTo("../../../");
   }

   @Test
   public void should_return_concatenated_web_path() {
      assertThat(PathUtils.concatWebPath("", null)).isEqualTo(null);
      assertThat(PathUtils.concatWebPath(null, "")).isEqualTo(null);
      assertThat(PathUtils.concatWebPath(null, null)).isEqualTo(null);
      assertThat(PathUtils.concatWebPath("", "")).isEqualTo("");
      assertThat(PathUtils.concatWebPath(null, "name")).isEqualTo(null);
      assertThat(PathUtils.concatWebPath(null, "/name")).isEqualTo("/name");
      assertThat(PathUtils.concatWebPath("", "name")).isEqualTo("name");
      assertThat(PathUtils.concatWebPath("", "/name")).isEqualTo("/name");
      assertThat(PathUtils.concatWebPath("/css/folder/subfolder/", null)).isEqualTo(null);
      assertThat(PathUtils.concatWebPath("/css/folder/subfolder/", "images/img.png")).isEqualTo(
            "/css/folder/subfolder/images/img.png");
      assertThat(PathUtils.concatWebPath("/css/folder/subfolder/style.css", "images/img.png")).isEqualTo(
            "/css/folder/subfolder/images/img.png");
      assertThat(PathUtils.concatWebPath("/css/folder/", "../images/img.png")).isEqualTo("/css/images/img.png");
      assertThat(PathUtils.concatWebPath("/css/folder/", "../../images/img.png")).isEqualTo("/images/img.png");
      assertThat(PathUtils.concatWebPath("/css/folder/style.css", "../images/img.png"))
            .isEqualTo("/css/images/img.png");
   }

   @Test
   public void should_extract_the_asset_name() {

      String location = "/assets/js/jquery.js";
      assertThat(PathUtils.extractLowerCasedName(location)).isEqualTo("jquery");

      location = "jquery.js";
      assertThat(PathUtils.extractLowerCasedName(location)).isEqualTo("jquery");

      location = "jquery.js";
      assertThat(PathUtils.extractLowerCasedName(location)).isEqualTo("jquery");
   }
}
