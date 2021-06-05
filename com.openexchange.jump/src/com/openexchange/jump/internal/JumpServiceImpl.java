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

package com.openexchange.jump.internal;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.jump.Endpoint;
import com.openexchange.jump.JumpExceptionCodes;
import com.openexchange.jump.JumpService;

/**
 * {@link JumpServiceImpl} - The jump service implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JumpServiceImpl extends ServiceTracker<Endpoint, Endpoint> implements JumpService {

    private final ConcurrentMap<String, Endpoint> endpoints;
    @SuppressWarnings("hiding")
    private final BundleContext context;

    /**
     * Initializes a new {@link JumpServiceImpl}.
     *
     * @param endpoints The configured end-points
     */
    public JumpServiceImpl(final Map<String, Endpoint> endpoints, final BundleContext context) {
        super(context, Endpoint.class, null);
        this.context = context;
        this.endpoints = new ConcurrentHashMap<String, Endpoint>(endpoints);
    }

    @Override
    public Endpoint addingService(final ServiceReference<Endpoint> reference) {
        final Endpoint endpoint = context.getService(reference);
        if (null == endpoints.putIfAbsent(endpoint.getSystemName(), endpoint)) {
            return endpoint;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<Endpoint> reference, final Endpoint endpoint) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<Endpoint> reference, final Endpoint endpoint) {
        endpoints.remove(endpoint.getSystemName());
        context.ungetService(reference);
    }

    // -------------------------------------------------------------------------------------------- //

    @Override
    public Map<String, Endpoint> getEndpoints() throws OXException {
        return Collections.unmodifiableMap(endpoints);
    }

    @Override
    public Endpoint getEndpoint(final String systemName) throws OXException {
        return null == systemName ? null : endpoints.get(systemName);
    }

    @Override
    public Endpoint requireEndpoint(final String systemName) throws OXException {
        if (null == systemName) {
            throw JumpExceptionCodes.NO_SUCH_ENDPOINT.create("<unknown>");
        }
        final Endpoint endpoint = endpoints.get(systemName);
        if (null == endpoint) {
            throw JumpExceptionCodes.NO_SUCH_ENDPOINT.create(systemName);
        }
        return endpoint;
    }

}
