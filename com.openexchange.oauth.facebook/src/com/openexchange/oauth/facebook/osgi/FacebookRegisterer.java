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

package com.openexchange.oauth.facebook.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.facebook.FacebookService;
import com.openexchange.oauth.facebook.FacebookServiceImpl;
import com.openexchange.oauth.facebook.OAuthServiceMetaDataFacebookImpl;


/**
 * {@link FacebookRegisterer}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FacebookRegisterer implements ServiceTrackerCustomizer<Object,Object> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookRegisterer.class));

    private final BundleContext context;
    private final Lock lock = new ReentrantLock();

    private ServiceRegistration<OAuthServiceMetaData> registration;
    private ServiceRegistration<FacebookService> registration2;
    private ConfigurationService configurationService;
    private OAuthService oAuthService;

    private DeferringURLService deferrer;


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
            if (obj instanceof ConfigurationService) {
                configurationService = (ConfigurationService) obj;
            }
            if (obj instanceof OAuthService) {
                oAuthService = (OAuthService) obj;
            }
            if (obj instanceof DeferringURLService) {
                deferrer = (DeferringURLService) obj;
            }
            needsRegistration = null != configurationService && null != oAuthService && deferrer != null && registration == null;
        } finally {
            lock.unlock();
        }
        if (needsRegistration) {
            LOG.info("Registering Facebook MetaData service.");
            LOG.info("Parameter com.openexchange.facebook.apiKey : " + configurationService.getProperty("com.openexchange.facebook.apiKey"));
            LOG.info("Parameter com.openexchange.facebook.secretKey :" + configurationService.getProperty("com.openexchange.facebook.secretKey"));
            final OAuthServiceMetaDataFacebookImpl facebookMetaDataService = new OAuthServiceMetaDataFacebookImpl(deferrer);
            registration = context.registerService(OAuthServiceMetaData.class,
                facebookMetaDataService, null);
            LOG.info("Registering Facebook service.");
            registration2 = context.registerService(FacebookService.class,
                new FacebookServiceImpl(oAuthService, facebookMetaDataService), null);
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
            if (service instanceof ConfigurationService) {
                configurationService = null;
            }
            if (service instanceof OAuthService) {
                oAuthService = null;
            }
            if (registration != null && (configurationService == null || oAuthService == null)) {
                unregister = registration;
                registration = null;
            }
        } finally {
            lock.unlock();
        }
        if (null != unregister) {
            LOG.info("Unregistering facebook metadata service.");
            unregister.unregister();
            if (registration2 != null){
                LOG.info("Unregistering facebook service.");
                registration2.unregister();
            }

        }
        context.ungetService(reference);
    }

}
