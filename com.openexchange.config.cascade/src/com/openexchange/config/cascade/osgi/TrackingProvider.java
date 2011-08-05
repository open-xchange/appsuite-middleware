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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.exception.OXException;


/**
 * {@link TrackingProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TrackingProvider implements ConfigProviderService {

    private final ServiceTracker tracker;

    /**
     * Initializes a new {@link TrackingProvider}.
     * @param serverProviders
     */
    public TrackingProvider(ServiceTracker providers) {
        super();
        this.tracker = providers;
    }

    @Override
    public BasicProperty get(final String property, int context, int user) throws OXException {
        ServiceReference[] serviceReferences = tracker.getServiceReferences();
        if(serviceReferences == null) {
            serviceReferences = new ServiceReference[0];
        }
        Arrays.sort(serviceReferences, new Comparator<ServiceReference>() {

            @Override
            public int compare(ServiceReference o1, ServiceReference o2) {
                Comparable p1 = (Comparable) o1.getProperty("priority");
                Comparable p2 = (Comparable) o2.getProperty("priority");
                if(p1 == null && p2 == null) {
                    return 0;
                }
                if(p1 == null) {
                    return -1;
                }

                if(p2 == null) {
                    return 1;
                }
                return p1.compareTo(p2);
            }

        });

        BasicProperty first = null;
        for (ServiceReference ref : serviceReferences) {
            ConfigProviderService delegate = (ConfigProviderService) tracker.getService(ref);
            BasicProperty prop = delegate.get(property, context, user);
            if (first == null) {
                first = prop;
            }
            if(prop.isDefined()) {
                return prop;
            }
        }
        if(first == null) {
            first = new BasicProperty() {

                @Override
                public String get() throws OXException {
                    return null;
                }

                @Override
                public String get(String metadataName) throws OXException {
                    return null;
                }

                @Override
                public boolean isDefined() throws OXException {
                    return false;
                }

                @Override
                public void set(String value) throws OXException {
                    throw new UnsupportedOperationException("Can't save setting "+property+". No ConfigProvider is specified for this value");
                }

                @Override
                public void set(String metadataName, String value) throws OXException {
                    throw new UnsupportedOperationException("Can't save metadata "+metadataName+" on property "+property+". No ConfigProvider is specified for this value");
                }

                @Override
                public List<String> getMetadataNames() throws OXException {
                    return Collections.emptyList();
                }
            };
        }
        return first;
    }

    @Override
    public Collection<String> getAllPropertyNames(int context, int user) throws OXException {
        Object[] services = tracker.getServices();
        if(services == null) {
            return Collections.emptyList();
        }
        Set<String> allNames = new HashSet<String>();

        for (Object object : services) {
            ConfigProviderService configProvider = (ConfigProviderService) object;
            Collection<String> names = configProvider.getAllPropertyNames(context, user);
            allNames.addAll(names);

        }
        return allNames;
    }

}
