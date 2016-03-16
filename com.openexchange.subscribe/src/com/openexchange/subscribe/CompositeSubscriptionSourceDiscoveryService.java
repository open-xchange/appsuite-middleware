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

package com.openexchange.subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.ConcurrentList;
import com.openexchange.subscribe.helpers.FilteredSubscriptionSourceDiscoveryService;


/**
 * {@link CompositeSubscriptionSourceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeSubscriptionSourceDiscoveryService implements SubscriptionSourceDiscoveryService {

    private final List<SubscriptionSourceDiscoveryService> services = new ConcurrentList<SubscriptionSourceDiscoveryService>();

    @Override
    public SubscriptionSource getSource(final String identifier) {
        SubscriptionSource current = null;
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            if(subDiscoverer.knowsSource(identifier)) {
                final SubscriptionSource source = subDiscoverer.getSource(identifier);
                if(current == null || current.getPriority() < source.getPriority()) {
                    current = source;
                }
            }
        }
        return current;
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        final Map<String, SubscriptionSource> allSources = new HashMap<String, SubscriptionSource>();
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            final List<SubscriptionSource> sources = subDiscoverer.getSources(folderModule);
            for (final SubscriptionSource subscriptionSource : sources) {
                final SubscriptionSource previousSource = allSources.get(subscriptionSource.getId());
                if(previousSource == null || previousSource.getPriority() < subscriptionSource.getPriority()) {
                    allSources.put(subscriptionSource.getId(), subscriptionSource);
                }
            }
        }
        final List<SubscriptionSource> sources = new ArrayList<SubscriptionSource>(allSources.values());
        Collections.sort(sources, new Comparator<SubscriptionSource>() {

            @Override
            public int compare(final SubscriptionSource o1, final SubscriptionSource o2) {
                if(o1.getDisplayName() != null && o2.getDisplayName() != null) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                } else {
                    return o1.getDisplayName() == null ? -1 : 1;
                }
            }

        });
        return sources;
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return getSources(-1);
    }

    @Override
    public boolean knowsSource(final String identifier) {
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            if(subDiscoverer.knowsSource(identifier)) {
                return true;
            }
        }
        return false;
    }

    public void addSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        services.add(service);
    }

    public void removeSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        services.remove(service);
    }

    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) throws OXException {
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            final SubscriptionSource source = subDiscoverer.getSource(context, subscriptionId);
            if(source != null) {
                return source;
            }
        }
        return null;
    }

    public void clear() {
        services.clear();
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return new FilteredSubscriptionSourceDiscoveryService(user, context, this);
    }

}
