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

package com.openexchange.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A registry for tracked {@link UserServiceInterceptor interceptors}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class UserServiceInterceptorRegistry implements ServiceTrackerCustomizer<UserServiceInterceptor, UserServiceInterceptor> {

    private final List<UserServiceInterceptor> interceptors;
    private final Comparator<UserServiceInterceptor> comparator;
    private final BundleContext context;

    /**
     * Initializes a new {@link UserServiceInterceptorRegistry}.
     *
     * @param context The bundle context
     */
    public UserServiceInterceptorRegistry(BundleContext context) {
        super();
        this.context = context;
        interceptors = new LinkedList<UserServiceInterceptor>();
        comparator = new Comparator<UserServiceInterceptor>() {

            @Override
            public int compare(UserServiceInterceptor s1, UserServiceInterceptor s2) {
                return s2.getRanking() - s1.getRanking();
            }
        };
    }

    @Override
    public UserServiceInterceptor addingService(ServiceReference<UserServiceInterceptor> reference) {
        UserServiceInterceptor service = context.getService(reference);
        addInterceptor(service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<UserServiceInterceptor> reference, UserServiceInterceptor service) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<UserServiceInterceptor> reference, UserServiceInterceptor service) {
        removeInterceptor(service);
        context.ungetService(reference);
    }

    /**
     * Gets the tracked, rank-wise sorted interceptors.
     *
     * @return The interceptors
     */
    public synchronized List<UserServiceInterceptor> getInterceptors() {
        return new ArrayList<UserServiceInterceptor>(interceptors);
    }

    /**
     * Adds given interceptor.
     *
     * @param interceptor The interceptor to add
     */
    synchronized void addInterceptor(UserServiceInterceptor interceptor) {
        interceptors.add(interceptor);
        Collections.sort(interceptors, comparator);
    }

    /**
     * Removes given interceptor.
     *
     * @param interceptor The interceptor to remove
     */
    synchronized void removeInterceptor(UserServiceInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

}
