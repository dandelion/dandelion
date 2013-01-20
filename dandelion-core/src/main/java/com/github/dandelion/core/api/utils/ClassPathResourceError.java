package com.github.dandelion.core.api.utils;

import com.github.dandelion.core.api.DandelionError;

/**
 * Possible Errors for 'ClassPath Resource'
 */
public enum ClassPathResourceError implements DandelionError {
    UNABLE_TO_LOCATION_RESOURCE_ON_DISK(1),
    UNKNOWN_ENCODING(2),
    UNABLE_TO_OBTAIN_INPUTSTREAM_FOR_RESOURCE(3),
    UNABLE_TO_LOAD_RESOURCE(4),
    UNABLE_TO_LOAD_RESOURCE_WITH_ENCODING(5);

    private final int number;

    private ClassPathResourceError(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }
}
