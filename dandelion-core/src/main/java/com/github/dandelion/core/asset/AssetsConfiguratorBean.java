package com.github.dandelion.core.asset;

import javax.annotation.PostConstruct;

/**
 * Public API of Assets Configurator accessible as a Bean
 */
public class AssetsConfiguratorBean {
    private AssetsConfigurator configurator;

    public AssetsConfiguratorBean() {
    }

    @PostConstruct
    void init() {
        configurator = AssetsConfigurator.getInstance();
    }
}
