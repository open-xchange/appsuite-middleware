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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link PublicationTargetDiscoveryServiceTrackerCustomizer} - The {@link ServiceTrackerCustomizer customizer} for
 * {@link PublicationTargetDiscoveryService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PublicationTargetDiscoveryServiceTrackerCustomizer implements ServiceTrackerCustomizer<PublicationTargetDiscoveryService,PublicationTargetDiscoveryService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PublicationTargetDiscoveryServiceTrackerCustomizer.class);

    private final BundleContext context;

    private final int ranking;

    /**
     * Initializes a new {@link PublicationTargetDiscoveryServiceTrackerCustomizer}.
     *
     * @param context The bundle context
     */
    public PublicationTargetDiscoveryServiceTrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
        ranking = -1;
    }

    @Override
    public PublicationTargetDiscoveryService addingService(final ServiceReference<PublicationTargetDiscoveryService> reference) {
        final int refRanking = getServiceReferenceRanking(reference);
        if (refRanking <= ranking) {
            // Nothing to track if ranking is less than or equal to current ranking
            return null;
        }
        final PublicationTargetDiscoveryService addedService = context.getService(reference);
        ServerServiceRegistry.getInstance().addService(PublicationTargetDiscoveryService.class, addedService);
        return addedService;
    }

    @Override
    public void modifiedService(final ServiceReference<PublicationTargetDiscoveryService> reference, final PublicationTargetDiscoveryService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<PublicationTargetDiscoveryService> reference, final PublicationTargetDiscoveryService service) {
        if (null != service) {
            try {
                ServerServiceRegistry.getInstance().removeService(PublicationTargetDiscoveryService.class);
            } finally {
                context.ungetService(reference);
            }
        }
    }

    private static int getServiceReferenceRanking(final ServiceReference<PublicationTargetDiscoveryService> reference) {
        final Object property = reference.getProperty(org.osgi.framework.Constants.SERVICE_RANKING);
        if (property == null) {
            return 0;
        }
        if (Integer.class.isInstance(property)) {
            return ((Integer) property).intValue();
        }
        try {
            return Integer.parseInt(property.toString());
        } catch (final NumberFormatException e) {
            LOG.error("Service ranking cannot be parsed to an integer: {}", property, e);
            return 0;
        }
    }

}
