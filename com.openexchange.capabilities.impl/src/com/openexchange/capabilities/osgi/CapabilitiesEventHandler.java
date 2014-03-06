package com.openexchange.capabilities.osgi;

import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;

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
        if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
            handleDroppedSession((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
        } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
            @SuppressWarnings("unchecked")
            final Map<String, Session> map = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
            for (final Session session : map.values()) {
                handleDroppedSession(session);
            }
        }
    }

    private void handleDroppedSession(final Session session) {
        if (session.isTransient()) {
            return;
        }
        final SessiondService sessiondService = serviceLookup.getService(SessiondService.class);
        if (null != sessiondService) {
            final int contextId = session.getContextId();
            final int userId = session.getUserId();
            if (null == sessiondService.getAnyActiveSessionForUser(userId, contextId)) {
                final CacheService cacheService = serviceLookup.getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        final Cache cache = cacheService.getCache("Capabilities");
                        cache.removeFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
                    } catch (final Exception x) {
                        // Ignore
                    }

                    try {
                        final Cache cache = cacheService.getCache("CapabilitiesUser");
                        cache.removeFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
                    } catch (final Exception x) {
                        // Ignore
                    }
                }

                LOGGER.info("Cleared capabilities caches for user {} in context {} as last active session was dropped.", userId, contextId);
            }
        }
    }
}