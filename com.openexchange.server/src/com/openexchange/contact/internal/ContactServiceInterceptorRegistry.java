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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.contact.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.openexchange.contact.ContactServiceInterceptor;

/**
 * {@link ContactServiceInterceptorRegistry}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactServiceInterceptorRegistry implements ServiceTrackerCustomizer<ContactServiceInterceptor, ContactServiceInterceptor> {

    private final List<ContactServiceInterceptor> interceptors;
    private final Comparator<ContactServiceInterceptor> comparator;
    private final BundleContext context;

    /**
     * Initializes a new {@link ContactServiceInterceptorRegistry}.
     *
     * @param context The bundle context
     */
    public ContactServiceInterceptorRegistry(BundleContext context) {
        super();
        this.context = context;
        interceptors = new LinkedList<ContactServiceInterceptor>();
        comparator = new Comparator<ContactServiceInterceptor>() {

            @Override
            public int compare(ContactServiceInterceptor s1, ContactServiceInterceptor s2) {
                return s2.getRanking() - s1.getRanking();
            }
        };
    }

    @Override
    public ContactServiceInterceptor addingService(ServiceReference<ContactServiceInterceptor> reference) {
    	ContactServiceInterceptor service = context.getService(reference);
        addInterceptor(service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<ContactServiceInterceptor> reference, ContactServiceInterceptor service) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<ContactServiceInterceptor> reference, ContactServiceInterceptor service) {
        removeInterceptor(service);
        context.ungetService(reference);
    }

    /**
     * Gets the tracked, rank-wise sorted interceptors.
     *
     * @return The interceptors
     */
    public synchronized List<ContactServiceInterceptor> getInterceptors() {
        return new ArrayList<ContactServiceInterceptor>(interceptors);
    }

    /**
     * Adds given interceptor.
     *
     * @param interceptor The interceptor to add
     */
    synchronized void addInterceptor(ContactServiceInterceptor interceptor) {
        interceptors.add(interceptor);
        Collections.sort(interceptors, comparator);
    }

    /**
     * Removes given interceptor.
     *
     * @param interceptor The interceptor to remove
     */
    synchronized void removeInterceptor(ContactServiceInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

}
