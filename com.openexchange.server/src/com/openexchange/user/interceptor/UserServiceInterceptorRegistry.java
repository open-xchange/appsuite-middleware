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

package com.openexchange.user.interceptor;

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
