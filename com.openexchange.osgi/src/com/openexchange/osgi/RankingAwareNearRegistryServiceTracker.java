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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.osgi.util.RankedService;

/**
 * {@link RankingAwareNearRegistryServiceTracker} - A {@link NearRegistryServiceTracker} that sorts tracked services by their ranking
 * (highest ranking first).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RankingAwareNearRegistryServiceTracker<S> extends ServiceTracker<S, S> implements ServiceListing<S> {

    private final SortableConcurrentList<RankedService<S>> services;

    /**
     * Initializes a new {@link RankingAwareNearRegistryServiceTracker}.
     *
     * @param context The bundle context
     * @param clazz The service's class
     */
    public RankingAwareNearRegistryServiceTracker(final BundleContext context, final Class<S> clazz) {
        super(context, clazz, null);
        services = new SortableConcurrentList<RankedService<S>>();
    }

    /**
     * Gets the rank-wise sorted service list
     *
     * @return The rank-wise sorted service list
     */
    @Override
    public List<S> getServiceList() {
        final List<S> ret = new ArrayList<S>(services.size());
        for (final RankedService<S> rs : services) {
            ret.add(rs.service);
        }
        return ret;
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        final S service = context.getService(reference);
        final int ranking = getRanking(reference);
        final RankedService<S> rankedService = new RankedService<S>(service, ranking);
        if (services.add(rankedService)) {
            services.sort();
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        services.remove(new RankedService<S>(service, getRanking(reference)));
        services.sort();
        context.ungetService(reference);
    }

    private static <S> int getRanking(final ServiceReference<S> reference) {
        int ranking = 0;
        {
            final Object oRanking = reference.getProperty(Constants.SERVICE_RANKING);
            if (null != oRanking) {
                try {
                    ranking = Integer.parseInt(oRanking.toString().trim());
                } catch (final NumberFormatException e) {
                    ranking = 0;
                }
            }
        }
        return ranking;
    }

}
