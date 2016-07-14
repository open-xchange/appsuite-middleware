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
                DefaultCredentials credentials = new DefaultCredentials();
                credentials.setContextId(contextId);
                credentials.setUserId(userId);
                credentials.setPassword(newPassword);
                credentials.setLogin(loginName);

                // Store credentials
                credentialStorage.storeCredentials(credentials);
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
