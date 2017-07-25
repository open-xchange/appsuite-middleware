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

package com.openexchange.osgi;

import static com.openexchange.osgi.util.RankedService.getRanking;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.util.RankedService;

/**
 * {@link RankingAwareRegistryServiceTrackerCustomizer} can be used to remember discovered services in an {@link AbstractServiceRegistry}, while the one with highest ranking gets injected.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RankingAwareRegistryServiceTrackerCustomizer<S> implements ServiceTrackerCustomizer<S, S> {

    /** The bundle context */
    private final BundleContext context;

    /** The service registry to add the tracked service to */
    private final AbstractServiceRegistry registry;

    /** The class of the service to track */
    private final Class<S> serviceClass;

    private final List<RankedService<S>> candidates;
    private RankedService<S> current;

    /**
     * Initializes a new {@link RankingAwareRegistryServiceTrackerCustomizer}.
     *
     * @param context The bundle context
     * @param registry The registry
     * @param clazz The service class to track
     */
    public RankingAwareRegistryServiceTrackerCustomizer(final BundleContext context, final AbstractServiceRegistry registry, final Class<S> clazz) {
        super();
        this.context = context;
        this.registry = registry;
        this.serviceClass = clazz;
        candidates = new ArrayList<RankedService<S>>(4);
    }

    @Override
    public synchronized S addingService(final ServiceReference<S> reference) {
        S service = context.getService(reference);
        if (serviceClass.isInstance(service)) {
            RankedService<S> rankedService = new RankedService<S>(service, getRanking(service, reference, 0));
            if (false == candidates.contains(rankedService) && candidates.add(rankedService)) {
                Collections.sort(candidates);
                if (null == current || current.ranking < rankedService.ranking) {
                    // Not set before OR has higher ranking than current
                    current = rankedService;
                    registry.addService(serviceClass, service);
                }
                return service;
            }
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<S> reference, final S service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(final ServiceReference<S> reference, final S service) {
        if (null == service) {
            return;
        }

        if (candidates.remove(new RankedService<S>(service, getRanking(service, reference, 0)))) {
            if (candidates.isEmpty()) {
                // No other candidate available
                current = null;
                registry.removeService(serviceClass);
            } else {
                RankedService<S> next = candidates.get(0);
                if (null == current || current.service == service) {
                    // Replace service in registry
                    current = next;
                    registry.addService(serviceClass, next.service);
                }
            }
            context.ungetService(reference);
        }
    }

}
