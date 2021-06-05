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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.converters.cover.CoverExtractor;
import com.openexchange.ajax.requesthandler.converters.cover.CoverExtractorRegistry;
import com.openexchange.ajax.requesthandler.converters.cover.Mp3CoverExtractor;
import com.openexchange.exception.OXException;

/**
 * {@link OSGiCoverExtractorRegistry} - An OSGi-based {@link CoverExtractorRegistry}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiCoverExtractorRegistry implements CoverExtractorRegistry, ServiceTrackerCustomizer<CoverExtractor, CoverExtractor> {

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<CoverExtractor, Object> map;
    private final BundleContext context;

    /**
     * Initializes a new {@link OSGiCoverExtractorRegistry}.
     */
    public OSGiCoverExtractorRegistry(final BundleContext context) {
        super();
        this.context = context;
        map = new ConcurrentHashMap<CoverExtractor, Object>();
        // Add default extractor
        map.put(new Mp3CoverExtractor(), PRESENT);
    }

    @Override
    public Collection<CoverExtractor> getExtractors() throws OXException {
        return Collections.unmodifiableCollection(map.keySet());
    }

    @Override
    public CoverExtractor addingService(final ServiceReference<CoverExtractor> reference) {
        final CoverExtractor coverExtractor = context.getService(reference);
        if (null == map.putIfAbsent(coverExtractor, PRESENT)) {
            return coverExtractor;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<CoverExtractor> reference, final CoverExtractor service) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<CoverExtractor> reference, final CoverExtractor service) {
        map.remove(service);
        context.ungetService(reference);
    }

}
