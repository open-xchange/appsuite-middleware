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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;

/**
 * Service tracker for transport providers
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportProviderServiceTracker implements ServiceTrackerCustomizer<TransportProvider,TransportProvider> {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TransportProviderServiceTracker.class));

    private final BundleContext context;

    /**
     * Initializes a new {@link TransportProviderServiceTracker}
     */
    public TransportProviderServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public TransportProvider addingService(final ServiceReference<TransportProvider> reference) {
        final TransportProvider addedService = context.getService(reference);
        if (null == addedService) {
            LOG.warn("Added service is null!", new Throwable());
        }
        final Object protocol = reference.getProperty("protocol");
        if (null == protocol) {
            LOG.error("Missing protocol in mail provider service: " + addedService.getClass().getName());
            context.ungetService(reference);
            return null;
        }
        try {
            /*
             * TODO: Clarify if proxy object is reasonable or if service itself should be registered
             */
            if (TransportProviderRegistry.registerTransportProvider(protocol.toString(), addedService)) {
                LOG.info(new StringBuilder(64).append("Transport provider for protocol '").append(protocol.toString()).append(
                    "' successfully registered"));
            } else {
                LOG.warn(new StringBuilder(64).append("Transport provider for protocol '").append(protocol.toString()).append(
                    "' could not be added.").append("Another provider which supports the protocol has already been registered."));
                context.ungetService(reference);
                return null;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            context.ungetService(reference);
            return null;
        }
        return addedService;
    }

    @Override
    public void modifiedService(final ServiceReference<TransportProvider> reference, final TransportProvider service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<TransportProvider> reference, final TransportProvider service) {
        if (null != service) {
            try {
                try {
                    final TransportProvider provider = service;
                    TransportProviderRegistry.unregisterTransportProvider(provider);
                    LOG.info(new StringBuilder(64).append("Transport provider for protocol '").append(provider.getProtocol().toString()).append(
                        "' successfully unregistered"));
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            } finally {
                /*
                 * TODO: Necessary?
                 */
                context.ungetService(reference);
            }
        }
    }

}
