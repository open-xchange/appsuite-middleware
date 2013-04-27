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

package com.openexchange.config.cascade.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.exception.OXException;

/**
 * {@link TrackingProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Refactored to use a <code>PriorityBlockingQueue</code>
 */
public class TrackingProvider extends ServiceTracker<ConfigProviderService, ConfigProviderService> implements ConfigProviderService {

    /**
     * Creates an appropriate filter expression for specified scope.
     *
     * @param scope The scope
     * @param context The bundle context
     * @return The filter expression
     */
    public static Filter createFilter(final String scope, final BundleContext context) {
        try {
            return context.createFilter("(& (objectclass="+ConfigProviderService.class.getName()+") (scope="+scope+"))");
        } catch (final InvalidSyntaxException e) {
            com.openexchange.log.Log.loggerFor(TrackingProvider.class).fatal(e.getMessage(), e);
        }
        return null;
    }

    private final PriorityBlockingQueue<Element> queue;

    /**
     * Initializes a new {@link TrackingProvider}.
     */
    public TrackingProvider(final String scope, final BundleContext context) {
        super(context, createFilter(scope, context), null);
        // PriorityBlockingQueue
        this.queue = new PriorityBlockingQueue<Element>(11, new Comparator<Element>() {

            @Override
            public int compare(final Element o1, final Element o2) {
                final Comparable<Object> p1 = o1.comparable;
                final Comparable<Object> p2 = o2.comparable;
                return null == p1 ? (null == p2 ? 0 : -1) : (null == p2 ? 1 : p1.compareTo(p2));
            }
        });
    }

    @Override
    public ConfigProviderService addingService(final ServiceReference<ConfigProviderService> reference) {
        final ConfigProviderService service = context.getService(reference);
        @SuppressWarnings("unchecked") final Comparable<Object> comparable = (Comparable<Object>) reference.getProperty("priority");
        if (queue.offer(new Element(service, comparable))) {
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<ConfigProviderService> reference, final ConfigProviderService service) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<ConfigProviderService> reference, final ConfigProviderService service) {
        queue.remove(new Element(service, null));
        context.ungetService(reference);
    }

    @Override
    public BasicProperty get(final String property, final int contextId, final int userId) throws OXException {
        BasicProperty first = null;
        for (final Element e : queue) {
            final BasicProperty prop = e.configProviderService.get(property, contextId, userId);
            if (prop.isDefined()) {
                return prop;
            }
            if (first == null) {
                first = prop;
            }
        }
        if (first != null) {
            return first;
        }
        // Return empty property
        return new EmptyBasicProperty(property);
    }

    @Override
    public Collection<String> getAllPropertyNames(final int contextId, final int userId) throws OXException {
        final Set<String> allNames = new HashSet<String>();
        for (final Element e : queue) {
            allNames.addAll(e.configProviderService.getAllPropertyNames(contextId, userId));
        }
        return allNames;
    }

    // -------------------------------------------------------------------------------------------------------------- //

    /**
     * A comparable queue element providing look-up of {@link ConfigProviderService} through its {@link #equals(Object)} method.
     */
    private static final class Element implements Comparable<Element> {

        final Comparable<Object> comparable;
        final ConfigProviderService configProviderService;

        Element(final ConfigProviderService configProviderService, final Comparable<Object> comparable) {
            super();
            this.configProviderService = configProviderService;
            this.comparable = comparable;
        }

        @Override
        public int compareTo(final Element o) {
            final Comparable<Object> p1 = this.comparable;
            final Comparable<Object> p2 = o.comparable;
            return null == p1 ? (null == p2 ? 0 : -1) : (null == p2 ? 1 : p1.compareTo(p2));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((configProviderService == null) ? 0 : configProviderService.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Element)) {
                return false;
            }
            final Element other = (Element) obj;
            if (configProviderService == null) {
                if (other.configProviderService != null) {
                    return false;
                }
            } else if (configProviderService != other.configProviderService) {
                return false;
            }
            return true;
        }
    }

    private static final class EmptyBasicProperty implements BasicProperty {

        private final String property;

        EmptyBasicProperty(final String property) {
            super();
            this.property = property;
        }

        @Override
        public String get() throws OXException {
            return null;
        }

        @Override
        public String get(final String metadataName) throws OXException {
            return null;
        }

        @Override
        public boolean isDefined() throws OXException {
            return false;
        }

        @Override
        public void set(final String value) throws OXException {
            throw new UnsupportedOperationException("Can't save setting " + property + ". No ConfigProvider is specified for this value");
        }

        @Override
        public void set(final String metadataName, final String value) throws OXException {
            throw new UnsupportedOperationException(
                "Can't save metadata " + metadataName + " on property " + property + ". No ConfigProvider is specified for this value");
        }

        @Override
        public List<String> getMetadataNames() throws OXException {
            return Collections.emptyList();
        }
    }
}
