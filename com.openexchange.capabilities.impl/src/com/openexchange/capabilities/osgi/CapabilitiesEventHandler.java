package com.openexchange.capabilities.osgi;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link CapabilitiesEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class CapabilitiesEventHandler implements EventHandler {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CapabilitiesEventHandler.class);

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link CapabilitiesEventHandler}.
     */
    public CapabilitiesEventHandler(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
            Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
            if (null != contextId) {
                Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                if (null != userId) {
                    final CacheService cacheService = serviceLookup.getService(CacheService.class);
                    if (null != cacheService) {
                        try {
                            final Cache cache = cacheService.getCache("Capabilities");
                            cache.localRemoveFromGroup(userId, contextId.toString());
                        } catch (final Exception x) {
                            // Ignore
                        }

                        try {
                            final Cache cache = cacheService.getCache("CapabilitiesUser");
                            cache.localRemoveFromGroup(userId, contextId.toString());
                        } catch (final Exception x) {
                            // Ignore
                        }
                    }

                    LOGGER.info("Cleared capabilities caches for user {} in context {} as last active session was dropped.", userId, contextId);
                }
            }
        }
    }

}
