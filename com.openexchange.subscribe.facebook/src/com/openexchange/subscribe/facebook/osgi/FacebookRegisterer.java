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

package com.openexchange.subscribe.facebook.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.context.ContextService;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.facebook.FacebookService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.facebook.FacebookSubscribeService;
import com.openexchange.subscribe.facebook.groupware.FacebookSubscriptionsOAuthAccountDeleteListener;

/**
 * {@link FacebookRegisterer}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FacebookRegisterer implements ServiceTrackerCustomizer<Object,Object> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookRegisterer.class));

    private final BundleContext context;

    private final Lock lock = new ReentrantLock();

    private ServiceRegistration<SubscribeService> registration;
    private ServiceRegistration<OAuthAccountDeleteListener> registration2;

    private OAuthServiceMetaData facebookMetaData;

    private FacebookService facebookService;

    private ContextService contextService;

    public FacebookRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        lock.lock();
        try {
            if (obj instanceof OAuthServiceMetaData) {
                if ("com.openexchange.oauth.facebook".equals(((OAuthServiceMetaData) obj).getId())) {
                    facebookMetaData = (OAuthServiceMetaData) obj;
                }
            }
            if (obj instanceof FacebookService) {
                facebookService = (FacebookService) obj;
            }
            if (obj instanceof ContextService) {
                contextService = (ContextService) obj;
            }
            needsRegistration = null != facebookMetaData && null != facebookService && null != contextService && registration == null;
        } finally {
            lock.unlock();
        }
        if (needsRegistration) {
            LOG.info("Registering facebook subscribe service.");
            final FacebookSubscribeService facebookSubscribeService = new FacebookSubscribeService(facebookMetaData, facebookService);
            final FacebookSubscriptionsOAuthAccountDeleteListener deleteListener = new FacebookSubscriptionsOAuthAccountDeleteListener(facebookSubscribeService, contextService);
            registration = context.registerService(SubscribeService.class, facebookSubscribeService, null);
            registration2 = context.registerService(OAuthAccountDeleteListener.class, deleteListener, null);
        }
        return obj;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        ServiceRegistration<?> unregister = null;
        lock.lock();
        try {
            if (service instanceof OAuthServiceMetaData) {
                if ("com.openexchange.oauth.facebook".equals(((OAuthServiceMetaData) service).getId())) {
                    facebookMetaData = null;
                }
            }
            if (service instanceof FacebookService) {
                facebookService = null;
            }
            if (registration != null && (facebookMetaData == null || facebookService == null)) {
                unregister = registration;
                registration = null;

            }
        } finally {
            lock.unlock();
        }
        if (null != unregister) {
            LOG.info("Unregistering facebook subscribe service.");
            unregister.unregister();
            registration2.unregister();
        }
        context.ungetService(reference);
    }

}
