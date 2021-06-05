/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oauth.impl.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;

/**
 * {@link OSGiMetaDataRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiMetaDataRegistry implements OAuthServiceMetaDataRegistry {

    private static volatile OSGiMetaDataRegistry instance;

    public static void initialize() {
        instance = new OSGiMetaDataRegistry();
    }

    /**
     * Gets the registry instance.
     *
     * @return The instance
     */
    public static OSGiMetaDataRegistry getInstance() {
        return instance;
    }

    /**
     * Releases the instance.
     */
    public static void releaseInstance() {
        synchronized (OSGiMetaDataRegistry.class) {
            final OSGiMetaDataRegistry tmp = instance;
            if (null != tmp) {
                tmp.stop();
                tmp.map.clear();
                instance = null;
            }
        }
    }

    final ConcurrentMap<String, OAuthServiceMetaData> map;

    private ServiceTracker<OAuthServiceMetaData,OAuthServiceMetaData> tracker;

    /**
     * Initializes a new {@link OSGiMetaDataRegistry}.
     */
    public OSGiMetaDataRegistry() {
        super();
        map = new ConcurrentHashMap<String, OAuthServiceMetaData>();
    }

    @Override
    public List<OAuthServiceMetaData> getAllServices(final int user, final int contextId) throws OXException {
        final java.util.List<OAuthServiceMetaData> retval = new ArrayList<OAuthServiceMetaData>(map.values().size());
        for (final OAuthServiceMetaData metadata : map.values()) {
            if (metadata.isEnabled(user, contextId)) {
                retval.add(metadata);
            }
        }
        return retval;
    }

    @Override
    public OAuthServiceMetaData getService(final String id, final int user, final int contextId) throws OXException {
        final OAuthServiceMetaData service = map.get(id);
        if (null == service) {
            throw OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA.create(id);
        }

        if (!service.isEnabled(user, contextId)) {
            throw OAuthExceptionCodes.DISABLED_OAUTH_SERVICE_META_DATA.create(service.getDisplayName(), I(user), I(contextId));
        }

        return service;
    }

    @Override
    public boolean containsService(final String id, final int user, final int contextId) throws OXException {
        if (id == null) {
            return false;
        }

        final OAuthServiceMetaData service = map.get(id);
        if (null == service) {
            return false;
        }

        return service.isEnabled(user, contextId);
    }

    /**
     * Starts the tracker.
     *
     * @param context The bundle context
     */
    void start(final BundleContext context) {
        if (null == tracker) {
            tracker = new ServiceTracker<OAuthServiceMetaData,OAuthServiceMetaData>(context, OAuthServiceMetaData.class, new Customizer(context));
            tracker.open();
        }
    }

    /**
     * Stops the tracker.
     */
    private void stop() {
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
    }

    private final class Customizer implements ServiceTrackerCustomizer<OAuthServiceMetaData,OAuthServiceMetaData> {

        private final BundleContext context;

        /**
         * Initializes a new {@link Customizer}.
         */
        public Customizer(final BundleContext context) {
            super();
            this.context = context;
        }

        @Override
        public OAuthServiceMetaData addingService(final ServiceReference<OAuthServiceMetaData> reference) {
            final OAuthServiceMetaData service = context.getService(reference);
            {
                final OAuthServiceMetaData addMe = service;
                if (null == map.putIfAbsent(addMe.getId(), addMe)) {
                    return service;
                }
                final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OSGiMetaDataRegistry.Customizer.class);
                logger.warn("OAuth service meta data {} could not be added to registry. Another service meta data is already registered with identifier: {}", addMe.getDisplayName(), addMe.getId());
            }
            /*
             * Adding to registry failed
             */
            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(final ServiceReference<OAuthServiceMetaData> reference, final OAuthServiceMetaData service) {
            // Nothing to do
        }

        @Override
        public void removedService(final ServiceReference<OAuthServiceMetaData> reference, final OAuthServiceMetaData service) {
            if (null != service) {
                try {
                    final OAuthServiceMetaData removeMe = service;
                    map.remove(removeMe.getId());
                } finally {
                    context.ungetService(reference);
                }
            }
        }

    }

}
