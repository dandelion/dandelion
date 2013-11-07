package com.github.dandelion.core.asset.web.data;

public class AssetContent {
    private String content;
    private String contentType;

    public AssetContent(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }
}
