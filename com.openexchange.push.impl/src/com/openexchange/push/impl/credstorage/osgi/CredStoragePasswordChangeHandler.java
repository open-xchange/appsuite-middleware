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

package com.openexchange.push.impl.credstorage.osgi;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.credstorage.CredentialStorageProvider;
import com.openexchange.push.credstorage.DefaultCredentials;
import com.openexchange.session.Session;


/**
 * {@link CredStoragePasswordChangeHandler} - Handles events for topic <i>"com/openexchange/passwordchange"</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CredStoragePasswordChangeHandler implements EventHandler {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CredStoragePasswordChangeHandler.class);

    private static final String TOPIC = "com/openexchange/passwordchange";

    /**
     * Initializes a new {@link CredStoragePasswordChangeHandler}.
     */
    public CredStoragePasswordChangeHandler() {
        super();
    }

    /**
     * Gets the topic of interest.
     *
     * @return The topic
     */
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public void handleEvent(final Event event) {
        if (TOPIC.equals(event.getTopic())) {
            // Acquire service
            CredentialStorageProvider provider = CredStorageServices.optService(CredentialStorageProvider.class);
            if (null == provider) {
                return;
            }

            int contextId = 0;
            int userId = 0;
            try {
                CredentialStorage credentialStorage = provider.getCredentialStorage();
                if (null == credentialStorage) {
                    return;
                }

                // Check new password
                String newPassword = (String) event.getProperty("com.openexchange.passwordchange.newPassword");
                if (null == newPassword) {
                    // Nothing to do...
                    return;
                }

                // Check associated login name
                String loginName = null;
                {
                    Session session = (Session) event.getProperty("com.openexchange.passwordchange.session");
                    if (null != session) {
                        loginName = session.getLoginName();
                    }
                }
                if (null == loginName) {
                    // Nothing to do...
                    return;
                }

                // Context & user identifier
                contextId = ((Integer) event.getProperty("com.openexchange.passwordchange.contextId")).intValue();
                userId = ((Integer) event.getProperty("com.openexchange.passwordchange.userId")).intValue();

                // Create credentials
                DefaultCredentials.Builder credentials = DefaultCredentials.builder();
                credentials.withContextId(contextId);
                credentials.withUserId(userId);
                credentials.withPassword(newPassword);
                credentials.withLogin(loginName);

                // Store credentials
                credentialStorage.storeCredentials(credentials.build());
            } catch (Exception e) {
                if (userId > 0 && contextId > 0) {
                    LOGGER.warn("Failed to update changed password for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId), e);
                } else {
                    Object obj = new Object() {

                        @Override
                        public String toString() {
                            return stringFor(event);
                        }
                    };
                    LOGGER.warn("Failed to update changed password for event {}", obj, e);
                }
            }
        }
    }

    /**
     * Builds a string for given event
     *
     * @param event The event
     * @return The describing string
     */
    String stringFor(Event event) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("topic=").append(event.getTopic());

        String[] names = event.getPropertyNames();
        if (null != names) {
            for (String name : names) {
                sb.append(' ').append(name).append('=');
                Object property = event.getProperty(name);
                if (null != property) {
                    sb.append(property);
                }
            }
        }

        return sb.toString();
    }

}
