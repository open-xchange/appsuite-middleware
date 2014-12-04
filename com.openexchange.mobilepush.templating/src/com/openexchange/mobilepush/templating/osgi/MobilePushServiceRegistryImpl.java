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

package com.openexchange.mobilepush.templating.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.mobilepush.MobilePushExceptionCodes;
import com.openexchange.mobilepush.templating.MobilePushService;
import com.openexchange.mobilepush.templating.MobileNotifierServiceRegistry;

/**
 * {@link MobilePushServiceRegistryImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobilePushServiceRegistryImpl extends ServiceTracker<MobilePushService, MobilePushService> implements MobileNotifierServiceRegistry {

    final ConcurrentMap<String, MobilePushService> map;
    /**
     * Initializes a new {@link MobilePushServiceRegistryImpl}.
     */
    public MobilePushServiceRegistryImpl(BundleContext context) {
        super(context, MobilePushService.class, null);
        map = new ConcurrentHashMap<String, MobilePushService>();
    }

    @Override
    public MobilePushService getService(String provider, int uid, int cid) throws OXException {
        final MobilePushService service = map.get(provider);
        if (null == service) {
            throw MobilePushExceptionCodes.UNKNOWN_SERVICE.create(provider);
        } else if (!service.isEnabled(uid, cid)) {
            throw MobilePushExceptionCodes.UNKNOWN_SERVICE.create(provider);
        }

        return service;
    }

    @Override
    public List<MobilePushService> getAllServices(int uid, int cid) throws OXException {
        final java.util.List<MobilePushService> service = new ArrayList<MobilePushService>(map.values().size());
        for (final MobilePushService mobileNotifier : map.values()) {
            if (mobileNotifier.isEnabled(uid, cid)) {
                service.add(mobileNotifier);
            } else {
                throw MobilePushExceptionCodes.UNKNOWN_SERVICE.create(mobileNotifier.getFrontendName());
            }
        }
        return service;
    }

    @Override
    public MobilePushService addingService(final ServiceReference<MobilePushService> reference) {
        final MobilePushService addMe = context.getService(reference);
        if (null == map.putIfAbsent(addMe.getFrontendName(), addMe)) {
            return context.getService(reference);
        }
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MobileNotifierServiceRegistry.class);
        logger.warn(
            "MobileNotifier service could not be added to registry. Another notifier service is already registered with identifier: {}",
            addMe.getFrontendName());
        /*
         * Adding to registry failed
         */
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<MobilePushService> reference, final MobilePushService service) {
        if (null != service) {
            try {
                final MobilePushService removeMe = service;
                map.remove(removeMe.getFrontendName());
            } finally {
                context.ungetService(reference);
            }
        }
    }
}
