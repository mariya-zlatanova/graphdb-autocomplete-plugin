package com.ontotext.trree.plugin.autocomplete;

/**
 * Created by desislava on 14/12/15.
 */
public enum IndexStatus {
    READY,
    READY_CONFIG,
    ERROR,
    NONE,
    BUILDING,
    CANCELED;

    static boolean statusIsOk(IndexStatus status) {
        return status == READY || status == READY_CONFIG;
    }
}
