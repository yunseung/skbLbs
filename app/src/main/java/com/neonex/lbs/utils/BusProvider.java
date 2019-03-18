package com.neonex.lbs.utils;

import com.squareup.otto.Bus;

/**
 * Created by macpro on 2018. 2. 7..
 */

public final class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instance.
    }
}
