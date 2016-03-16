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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link HigherRankedObservator} - An observator for possibly appearing higher-ranked service(s).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @see HigherRankedObservatorCallback
 */
public class HigherRankedObservator<S> implements ServiceTrackerCustomizer<S, S> {

    /**
     * The callback for {@link HigherRankedObservator observator}.
     */
    public static interface HigherRankedObservatorCallback<S> {

        /**
         * Invoked if first service appeared with a ranking higher than observator's one.
         *
         * @param reference The service reference
         * @param service The service
         */
        void onFirstHigherRankedAvailable(final ServiceReference<S> reference, final S service);

        /**
         * Invoked if last service disappeared with a ranking higher than observator's one.
         *
         * @param reference The service reference
         * @param service The service
         */
        void onLastHigherRankedDisappeared(final ServiceReference<S> reference, final S service);
    }

    private final AtomicInteger higherRankedCounter;

    private final List<HigherRankedObservatorCallback<S>> callbacks;

    private final int myRanking;

    private final BundleContext context;

    /**
     * Initializes a new {@link HigherRankedObservator}.
     *
     * @param ranking This observator's ranking
     * @param context The bundle context to acquire higher-ranked service(s)
     */
    public HigherRankedObservator(final int ranking, final BundleContext context) {
        super();
        callbacks = new CopyOnWriteArrayList<HigherRankedObservatorCallback<S>>();
        this.context = context;
        higherRankedCounter = new AtomicInteger(0);
        this.myRanking = ranking;
    }

    /**
     * Adds specified callback to this observator.
     *
     * @param callback The callback to add
     * @return This observator with callback added
     */
    public HigherRankedObservator<S> addCallback(final HigherRankedObservatorCallback<S> callback) {
        if (null != callback) {
            callbacks.add(callback);
        }
        return this;
    }

    /**
     * Removes specified callback from this observator.
     *
     * @param callback The callback to remove
     * @return This observator with callback removed
     */
    public HigherRankedObservator<S> removeCallback(final HigherRankedObservatorCallback<S> callback) {
        if (null != callback) {
            callbacks.remove(callback);
        }
        return this;
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        if ((getRanking(reference) > myRanking) && (1 == higherRankedCounter.incrementAndGet())) {
            final S service = context.getService(reference);
            for (final HigherRankedObservatorCallback<S> callback : callbacks) {
                callback.onFirstHigherRankedAvailable(reference, service);
            }
            return service;
        }
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<S> reference, final S service) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        if (null == service) {
            return;
        }
        if ((getRanking(reference) > myRanking) && (0 == higherRankedCounter.decrementAndGet())) {
            try {
                for (final HigherRankedObservatorCallback<S> callback : callbacks) {
                    callback.onLastHigherRankedDisappeared(reference, service);
                }
            } finally {
                context.ungetService(reference);
            }
        }
    }

    private int getRanking(final ServiceReference<S> reference) {
        final Object property = reference.getProperty(Constants.SERVICE_RANKING);
        if (null == property) {
            return 0;
        }
        return ((Integer) property).intValue();
    }

}
