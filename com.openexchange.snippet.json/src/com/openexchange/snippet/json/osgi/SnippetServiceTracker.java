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

package com.openexchange.snippet.json.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.snippet.SnippetService;


/**
 * {@link SnippetServiceTracker} - The ranking-aware service tracker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SnippetServiceTracker implements ServiceTrackerCustomizer<SnippetService, SnippetService> {

    private final BundleContext context;
    private final ConcurrentPriorityQueue<RankedService<SnippetService>> queue;

    /**
     * Initializes a new {@link SnippetServiceTracker}.
     */
    public SnippetServiceTracker(final BundleContext context) {
        super();
        this.context = context;
        queue = new ConcurrentPriorityQueue<RankedService<SnippetService>>();
    }

    /**
     * Gets currently available, highest ranked snippet service.
     *
     * @return The snippet service or <code>null</code> (if none available).
     */
    public SnippetService getSnippetService() {
        final RankedService<SnippetService> rankedService = queue.peek();
        return null == rankedService ? null : rankedService.service;
    }

    @Override
    public SnippetService addingService(final ServiceReference<SnippetService> reference) {
        final SnippetService service = context.getService(reference);
        queue.offer(new RankedService<SnippetService>(service, getRanking(reference)));
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<SnippetService> reference, final SnippetService service) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<SnippetService> reference, final SnippetService service) {
        queue.remove(new RankedService<SnippetService>(service, getRanking(reference)));
        context.ungetService(reference);
    }

    private int getRanking(final ServiceReference<SnippetService> reference) {
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
