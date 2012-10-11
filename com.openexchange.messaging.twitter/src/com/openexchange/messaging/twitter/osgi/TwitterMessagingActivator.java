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

package com.openexchange.messaging.twitter.osgi;

import static com.openexchange.messaging.twitter.services.TwitterMessagingServiceRegistry.getServiceRegistry;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.html.HtmlService;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.twitter.TwitterMessagingService;
import com.openexchange.messaging.twitter.TwitterOAuthAccountDeleteListener;
import com.openexchange.messaging.twitter.session.TwitterEventHandler;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthAccountInvalidationListener;
import com.openexchange.oauth.OAuthService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.twitter.TwitterService;

/**
 * {@link TwitterMessagingActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingActivator extends HousekeepingActivator {

    private WhiteboardSecretService secretService;

    /**
     * Initializes a new {@link TwitterMessagingActivator}.
     */
    public TwitterMessagingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { TwitterService.class, SessiondService.class, HtmlService.class, OAuthService.class, SecretService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log logger = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TwitterMessagingActivator.class));
        if (logger.isInfoEnabled()) {
            logger.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log logger = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TwitterMessagingActivator.class));
        if (logger.isWarnEnabled()) {
            logger.warn("Absent service: " + clazz.getName());
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
                secretService = new WhiteboardSecretService(context);
                secretService.open();
                registry.addService(SecretService.class, secretService);
            }

            registerService(MessagingService.class, new TwitterMessagingService(), null);
            /*
             * Register event handler to detect removed sessions
             */
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, new TwitterEventHandler(), serviceProperties);
            registerService(OAuthAccountDeleteListener.class, new TwitterOAuthAccountDeleteListener(), null);
            registerService(OAuthAccountInvalidationListener.class, new TwitterOAuthAccountDeleteListener(), null);
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TwitterMessagingActivator.class)).error(
                e.getMessage(),
                e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            if (secretService != null) {
                secretService.close();
            }
            cleanUp();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TwitterMessagingActivator.class)).error(
                e.getMessage(),
                e);
            throw e;
        }
    }

}
