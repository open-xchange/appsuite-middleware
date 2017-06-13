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

package com.openexchange.passwordchange.history.tracker;

import java.sql.Timestamp;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.passwordchange.history.osgi.Services;
import com.openexchange.session.Session;

/**
 * {@link PasswordChangeEventListener}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeEventListener implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeEventListener.class);

    private static final String TOPIC = "com/openexchange/passwordchange";
    private static final String ENABLED = "com.openexchange.passwordchange.history";
    private static final String LIMIT = "com.openexchange.passwordchange.limit";
    private static final String TRACKER = "com.openexchange.passwordchange.tracker";

    /**
     * Initializes a new {@link PasswordChangeEventListener}.
     */
    public PasswordChangeEventListener() {
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
    public void handleEvent(Event event) {
        if (false == TOPIC.equals(event.getTopic())) {
            return;
        }
        // Read values
        int contextID = (int) event.getProperty("com.openexchange.passwordchange.contextId");
        int userID = (int) event.getProperty("com.openexchange.passwordchange.userId");
        Session session = (Session) event.getProperty("com.openexchange.passwordchange.session");
        String ipAdderess = String.valueOf(event.getProperty(("com.openexchange.passwordchange.ipAddress")));

        // Read from ConfigCasscade if history should be saved
        ConfigViewFactory casscade = Services.getService(ConfigViewFactory.class);
        if (null == casscade) {
            LOG.warn("Could not get config.");
            return;
        }
        ConfigView view;
        try {
            view = casscade.getView(userID, contextID);
            boolean enabled = view.get(ENABLED, Boolean.class);
            if (false == enabled) {
                return;
            }
            // Integer limit = view.get(LIMIT, Integer.class);
            // If empty, password change history is not wanted after all 
            String symbolicTrackerName = view.get(TRACKER, String.class);
            if (null == symbolicTrackerName || symbolicTrackerName.isEmpty()) {
                LOG.debug("No PasswordChangeTracker found .. No tracking wanted ");
                return;
            }
            // Load registry to get the fitting tracker
            PasswordChangeTrackerRegistry trackerRegistry = Services.getOptionalService(PasswordChangeTrackerRegistry.class);
            if (null == trackerRegistry) {
                LOG.debug("Could not get PasswordChangeTrackerRegistry");
                return;
            }
            PasswordChangeTracker tracker = trackerRegistry.getTracker(symbolicTrackerName);
            if (null == tracker) {
                LOG.error("Could not load {} for user {} in context {}", symbolicTrackerName, userID, contextID);
                return;
            }
            // XXX app suite correct?
            tracker.trackPasswordChange(session, new PasswordChangeInfoImpl(new Timestamp(System.currentTimeMillis()), PasswordChangeInfo.APPSUITE, ipAdderess));
        } catch (Exception e) {
            // IF this happens some property won't be there ..
            LOG.warn("Error while tracking password change for user {} in context {}", userID, contextID);
            return;
        }
    }

    private class PasswordChangeInfoImpl implements PasswordChangeInfo {

        private final Timestamp lastModified;
        private final String modifiedBy;
        private final String modifiedOrigin;

        /**
         * 
         * Initializes a new {@link PasswordChangeInfoImpl}.
         * 
         * @param lastModified The {@link Timestamp} the password got changed on
         * @param modifiedBy The client the password got changed from
         * @param modifiedOrigin The IP-address of the client or <code>null</code>
         */
        public PasswordChangeInfoImpl(Timestamp lastModified, String modifiedBy, String modifiedOrigin) {
            super();
            this.lastModified = lastModified;
            if (APPSUITE.equals(modifiedBy)) {
                this.modifiedBy = APPSUITE;
            } else if (PROVISIONING.equals(modifiedBy)) {
                this.modifiedBy = PROVISIONING;
            } else {
                this.modifiedBy = UNKOWN;
            }
            this.modifiedOrigin = modifiedOrigin;
        }

        @Override
        public Timestamp lastModified() {
            return lastModified;
        }

        @Override
        public String modifiedBy() {
            return modifiedBy;
        }

        @Override
        public String modifyOrigin() {
            return modifiedOrigin;
        }
    }
}
