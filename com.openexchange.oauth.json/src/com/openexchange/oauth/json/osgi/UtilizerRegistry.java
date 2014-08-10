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

package com.openexchange.oauth.json.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthUtilizerCreator;

/**
 * {@link UtilizerRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public final class UtilizerRegistry implements ServiceTrackerCustomizer<OAuthUtilizerCreator, OAuthUtilizerCreator> {

    private static volatile UtilizerRegistry instance;

    /**
     * Initializes the registry instance
     *
     * @param context The associated bundle context
     * @return The newly created instance
     */
    public static UtilizerRegistry initInstance(BundleContext context) {
        UtilizerRegistry tmp = new UtilizerRegistry(context);
        instance = tmp;
        return tmp;
    }

    /**
     * Unsets the instance.
     */
    public static void freeInstance() {
        instance = null;
    }

    /**
     * Gets the registry instance
     *
     * @return The instance or <code>null</code>
     */
    public static UtilizerRegistry getInstance() {
        return instance;
    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    private final BundleContext context;
    private final ConcurrentMap<API, Queue<OAuthUtilizerCreator>> map;

    /**
     * Initializes a new {@link UtilizerRegistry}.
     */
    private UtilizerRegistry(BundleContext context) {
        super();
        this.context = context;
        map = new ConcurrentHashMap<API, Queue<OAuthUtilizerCreator>>(8);
    }

    /**
     * Gets the currently registered creators.
     *
     * @return The creators
     */
    public Collection<OAuthUtilizerCreator> getCreatorsFor(API oauthApi) {
        return Collections.unmodifiableCollection(map.get(oauthApi));
    }

    @Override
    public OAuthUtilizerCreator addingService(ServiceReference<OAuthUtilizerCreator> reference) {
        OAuthUtilizerCreator creator = context.getService(reference);

        API api = creator.getApplicableApi();
        Queue<OAuthUtilizerCreator> queue = map.get(api);
        if (null == queue) {
            Queue<OAuthUtilizerCreator> newqueue = new ConcurrentLinkedQueue<OAuthUtilizerCreator>();
            queue = map.putIfAbsent(api, newqueue);
            if (null == queue) {
                queue = newqueue;
            }
        }

        if (queue.offer(creator)) {
            return creator;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<OAuthUtilizerCreator> reference, OAuthUtilizerCreator creator) {
        // Nope
    }

    @Override
    public void removedService(ServiceReference<OAuthUtilizerCreator> reference, OAuthUtilizerCreator creator) {
        Queue<OAuthUtilizerCreator> queue = map.get(creator.getApplicableApi());
        if (null != queue) {
            queue.remove(creator);
        }
        context.ungetService(reference);
    }

}
