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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.oauth.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;

/**
 * {@link MetaDataRegistry}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MetaDataRegistry {

    private static volatile MetaDataRegistry instance;

    /**
     * Gets the registry instance.
     * 
     * @return The instance
     */
    public static MetaDataRegistry getInstance() {
        MetaDataRegistry tmp = instance;
        if (null == tmp) {
            synchronized (MetaDataRegistry.class) {
                tmp = instance;
                if (null == tmp) {
                    instance = tmp = new MetaDataRegistry();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the instance.
     */
    public static void releaseInstance() {
        synchronized (MetaDataRegistry.class) {
            final MetaDataRegistry tmp = instance;
            if (null != tmp) {
                tmp.stop();
                tmp.map.clear();
                instance = null;
            }
        }
    }

    private final ConcurrentMap<String, OAuthServiceMetaData> map;

    private ServiceTracker tracker;

    /**
     * Initializes a new {@link MetaDataRegistry}.
     */
    public MetaDataRegistry() {
        super();
        map = new ConcurrentHashMap<String, OAuthServiceMetaData>();
    }

    public List<OAuthServiceMetaData> getAllServices() {
        return new ArrayList<OAuthServiceMetaData>(map.values());
    }

    public OAuthServiceMetaData getService(final String id) throws OAuthException {
        final OAuthServiceMetaData service = map.get(id);
        if (null == service) {
            throw OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA.create(id);
        }
        return service;
    }

    public boolean containsService(final String id) {
        return null == id ? false : map.containsKey(id);
    }

    /**
     * Starts the tracker.
     * 
     * @param context The bundle context
     */
    void start(final BundleContext context) {
        if (null == tracker) {
            tracker = new ServiceTracker(context, OAuthServiceMetaData.class.getName(), new Customizer(map, context));
            tracker.open();
        }
    }

    /**
     * Stops the tracker.
     */
    private void stop() {
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
    }

}
