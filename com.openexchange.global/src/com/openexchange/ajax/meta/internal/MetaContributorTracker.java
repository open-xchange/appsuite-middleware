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

package com.openexchange.ajax.meta.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.meta.MetaContributor;
import com.openexchange.ajax.meta.MetaContributorRegistry;


/**
 * {@link MetaContributorTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class MetaContributorTracker extends ServiceTracker<MetaContributor, MetaContributorReference> implements MetaContributorRegistry {

    /** List of all handlers with topic of "*" */
    private final List<MetaContributorReference> globalWildcard;

    /** Map key is topic prefix of partial wildcard */
    private final ConcurrentMap<String, List<MetaContributorReference>> partialWildcard;

    /** Map key is topic name */
    private final ConcurrentMap<String, List<MetaContributorReference>> topicName;

    /**
     * Initializes a new {@link MetaContributorTracker}.
     *
     * @param context The bundle context
     */
    public MetaContributorTracker(final BundleContext context) {
        super(context, MetaContributor.class, null);
        globalWildcard = new CopyOnWriteArrayList<MetaContributorReference>();
        partialWildcard = new ConcurrentHashMap<String, List<MetaContributorReference>>();
        topicName = new ConcurrentHashMap<String, List<MetaContributorReference>>();
    }

    @Override
    public MetaContributorReference addingService(final ServiceReference<MetaContributor> reference) {
        final MetaContributorReference wrapper = new MetaContributorReference(reference, context);
        synchronized (this) {
            if (wrapper.init()) {
                bucket(wrapper);
            }
        }
        return wrapper;
    }

    @Override
    public void modifiedService(final ServiceReference<MetaContributor> reference, final MetaContributorReference service) {
        synchronized (this) {
            unbucket(service);
            if (service.init()) {
                bucket(service);
                return;
            }
        }
        // needs to be called outside sync region
        service.flush();
    }

    @Override
    public void removedService(final ServiceReference<MetaContributor> reference, final MetaContributorReference service) {
        synchronized (this) {
            unbucket(service);
        }
        // needs to be called outside sync region
        service.flush();
    }

    /**
     * Place the wrapper into the appropriate buckets.
     * This is a performance optimization for event delivery.
     *
     * @param wrapper The wrapper to place in buckets.
     * @GuardedBy this
     */
    private void bucket(final MetaContributorReference wrapper) {
        final String[] topics = wrapper.getTopics();
        final int length = (topics == null) ? 0 : topics.length;
        for (int i = 0; i < length; i++) {
            final String topic = topics[i];
            // global wildcard
            if (topic.equals("*")) {
                globalWildcard.add(wrapper);
            }
            // partial wildcard
            else if (topic.endsWith("/*")) {
                final String key = topic.substring(0, topic.length() - 2); // Strip off "/*" from the end
                List<MetaContributorReference> wrappers = partialWildcard.get(key);
                if (wrappers == null) {
                    wrappers = new ArrayList<MetaContributorReference>();
                    partialWildcard.put(key, wrappers);
                }
                wrappers.add(wrapper);
            }
            // simple topic name
            else {
                List<MetaContributorReference> wrappers = topicName.get(topic);
                if (wrappers == null) {
                    wrappers = new ArrayList<MetaContributorReference>();
                    topicName.put(topic, wrappers);
                }
                wrappers.add(wrapper);
            }
        }
    }

    /**
     * Remove the wrapper from the buckets.
     *
     * @param wrapper The wrapper to remove from the buckets.
     * @GuardedBy this
     */
    private void unbucket(final MetaContributorReference wrapper) {
        final String[] topics = wrapper.getTopics();
        final int length = (topics == null) ? 0 : topics.length;
        for (int i = 0; i < length; i++) {
            final String topic = topics[i];
            // global wilcard
            if (topic.equals("*")) { //$NON-NLS-1$
                globalWildcard.remove(wrapper);
            }
            // partial wildcard
            else if (topic.endsWith("/*")) { //$NON-NLS-1$
                final String key = topic.substring(0, topic.length() - 2); // Strip off "/*" from the end
                final List<MetaContributorReference> wrappers = partialWildcard.get(key);
                if (wrappers != null) {
                    wrappers.remove(wrapper);
                    if (wrappers.isEmpty()) {
                        partialWildcard.remove(key);
                    }
                }
            }
            // simple topic name
            else {
                final List<MetaContributorReference> wrappers = topicName.get(topic);
                if (wrappers != null) {
                    wrappers.remove(wrapper);
                    if (wrappers.isEmpty()) {
                        topicName.remove(topic);
                    }
                }
            }
        }
    }

    /**
     * Return the set of contributors which subscribes to the topic.
     * A set is used to ensure a contributor is not called for a topic more than once.
     *
     * @param topic
     * @return a set of contributors
     */
    public Set<MetaContributor> getContributors(final String topic) {
        // Use a set to remove duplicates
        final Set<MetaContributor> handlers = new HashSet<MetaContributor>(6);

        // Add the "*" handlers
        handlers.addAll(globalWildcard);

        // Add the handlers with partial matches
        if (!partialWildcard.isEmpty()) {
            int index = topic.lastIndexOf('/');
            while (index >= 0) {
                final String subTopic = topic.substring(0, index);
                final List<MetaContributorReference> wrappers = partialWildcard.get(subTopic);
                if (wrappers != null) {
                    handlers.addAll(wrappers);
                }
                // Strip the last level from the topic. For example, org/osgi/framework becomes org/osgi.
                // Wildcard topics are inserted into the map with the "/*" stripped off.
                index = subTopic.lastIndexOf('/');
            }
        }

        // Add the handlers for matching topic names
        final List<MetaContributorReference> wrappers = topicName.get(topic);
        if (wrappers != null) {
            handlers.addAll(wrappers);
        }

        return handlers;
    }

    @Override
    public Set<MetaContributor> getMetaContributors(String topic) {
        return getContributors(topic);
    }

}
