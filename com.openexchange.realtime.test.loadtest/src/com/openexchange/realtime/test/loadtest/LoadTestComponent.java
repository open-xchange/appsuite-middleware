
package com.openexchange.realtime.test.loadtest;

import java.util.concurrent.TimeUnit;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.packet.ID;
import com.openexchange.server.ServiceLookup;

/**
 * Component that initializes the {@link LoadTestDispatcher} as required.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class LoadTestComponent implements Component {

    /**
     * The service
     */
    private final ServiceLookup services;

    /**
     * Initializes a new {@link LoadTestComponent}.
     * 
     * @param ServiceLookup with the services
     */
    public LoadTestComponent(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Create the room, say synthetic.loadTest://room1
     */
    @Override
    public ComponentHandle create(final ID id) {
        return new LoadTestDispatcher(id);
    }

    /**
     * Now we can address rooms as synthetic.loadTest://room1 (for example
     */
    @Override
    public String getId() {
        return "loadTest";
    }

    /**
     * Automatically shut down a room after five minutes of idling.
     */
    @Override
    public EvictionPolicy getEvictionPolicy() {
        return new Timeout(5, TimeUnit.MINUTES);
    }

}
