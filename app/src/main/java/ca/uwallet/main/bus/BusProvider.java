package ca.uwallet.main.bus;

import com.squareup.otto.Bus;

/**
 * Created by gabriel on 6/14/14.
 */
public class BusProvider {
    public static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {

    }
}
