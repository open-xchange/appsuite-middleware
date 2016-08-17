/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
