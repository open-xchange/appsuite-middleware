/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.messaging.twitter.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.html.HtmlService;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.twitter.TwitterMessagingService;
import com.openexchange.messaging.twitter.TwitterOAuthAccountDeleteListener;
import com.openexchange.messaging.twitter.session.TwitterEventHandler;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthAccountInvalidationListener;
import com.openexchange.oauth.OAuthService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.secret.SecretService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.twitter.TwitterService;

/**
 * {@link TwitterMessagingActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingActivator extends HousekeepingActivator {

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
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);
            registerService(MessagingService.class, new TwitterMessagingService(), null);
            /*
             * Register event handler to detect removed sessions
             */
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
            registerService(EventHandler.class, new TwitterEventHandler(), serviceProperties);
            registerService(OAuthAccountDeleteListener.class, new TwitterOAuthAccountDeleteListener(), null);
            registerService(OAuthAccountInvalidationListener.class, new TwitterOAuthAccountDeleteListener(), null);
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(TwitterMessagingActivator.class);
            logger.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            super.stopBundle();
            /*
             * Clear service registry
             */
            Services.setServiceLookup(null);
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(TwitterMessagingActivator.class);
            logger.error("", e);
            throw e;
        }
    }

}
