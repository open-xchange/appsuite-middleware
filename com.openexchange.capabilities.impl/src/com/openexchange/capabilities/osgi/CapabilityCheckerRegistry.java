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

package com.openexchange.capabilities.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentList;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CapabilityCheckerRegistry} - A registry for CapabilityChecker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CapabilityCheckerRegistry extends ServiceTracker<CapabilityChecker, CapabilityChecker> {

    /** The name for optional <code>"capabilities"</code> property */
    private static final String PROPERTY_CAPABILITIES = CapabilityChecker.PROPERTY_CAPABILITIES;

    /** The special list for all capabilities */
    private static final List<String> ALL = Collections.singletonList("*");

    private final ConcurrentMap<String, List<CapabilityChecker>> map;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link CapabilityCheckerRegistry}.
     *
     * @param context The bundle context
     */
    public CapabilityCheckerRegistry(final BundleContext context, final ServiceLookup services) {
        super(context, CapabilityChecker.class, null);
        this.services = services;
        map = new ConcurrentHashMap<String, List<CapabilityChecker>>(256, 0.9f, 1);
    }

    private Cache optCache() {
        final CacheService service = services.getOptionalService(CacheService.class);
        if (null == service) {
            return null;
        }
        try {
            return service.getCache("Capabilities");
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(CapabilityCheckerRegistry.class).error("", e);
            return null;
        }
    }

    @Override
    public CapabilityChecker addingService(ServiceReference<CapabilityChecker> reference) {
        final CapabilityChecker checker = context.getService(reference);

        final List<String> caps;
        {
            final String csv = (String) reference.getProperty(PROPERTY_CAPABILITIES);
            if (com.openexchange.java.Strings.isEmpty(csv)) {
                caps = ALL;
            } else {
                final String[] splits = Strings.splitByComma(csv);
                final int length = splits.length;
                if (1 == length) {
                    caps = Collections.singletonList(com.openexchange.java.Strings.toLowerCase(splits[0]));
                } else {
                    final List<String> l = new ArrayList<String>(length);
                    for (int i = 0; i < length; i++) {
                        l.add(com.openexchange.java.Strings.toLowerCase(splits[i]));
                    }
                    caps = l;
                }
            }
        }

        for (final String cap : caps) {
            List<CapabilityChecker> list = map.get(cap);
            if (null == list) {
                final List<CapabilityChecker> newList = new ConcurrentList<CapabilityChecker>();
                list = map.putIfAbsent(cap, newList);
                if (null == list) {
                    list = newList;
                }
            }
            list.add(checker);
        }

        final Cache optCache = optCache();
        if (null != optCache) {
            try {
                optCache.clear();
            } catch (Exception e) {
                // ignore
            }
        }

        return checker;
    }

    @Override
    public void modifiedService(org.osgi.framework.ServiceReference<CapabilityChecker> reference, CapabilityChecker service) {
        // Ignore
    }

    @Override
    public void remove(ServiceReference<CapabilityChecker> reference) {
        final CapabilityChecker checker = context.getService(reference);

        final List<String> caps;
        {
            final String csv = (String) reference.getProperty(PROPERTY_CAPABILITIES);
            if (null == csv) {
                caps = ALL;
            } else {
                final String[] splits = Strings.splitByComma(csv);
                final int length = splits.length;
                final List<String> l = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    l.add(com.openexchange.java.Strings.toLowerCase(splits[i]));
                }
                caps = l;
            }
        }

        for (final String cap : caps) {
            final List<CapabilityChecker> list = map.get(cap);
            if (null != list) {
                list.remove(checker);
            }
        }

        final Cache optCache = optCache();
        if (null != optCache) {
            try {
                optCache.clear();
            } catch (Exception e) {
                // ignore
            }
        }

        context.ungetService(reference);
    }

    /**
     * Gets a snapshot of currently available checkers.
     *
     * @return The available checkers
     */
    public Map<String, List<CapabilityChecker>> getCheckers() {
        return map;
    }
}
