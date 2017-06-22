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

package com.openexchange.passwordchange.history.events;

import java.sql.Timestamp;
import java.util.List;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.exeption.PasswordChangeHistoryException;
import com.openexchange.passwordchange.history.osgi.Services;
import com.openexchange.passwordchange.history.registry.PasswordChangeTrackerRegistry;
import com.openexchange.passwordchange.history.tracker.PasswordChangeInfo;
import com.openexchange.passwordchange.history.tracker.PasswordChangeTracker;

/**
 * {@link PasswordChangeUtility}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class PasswordChangeUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeUtility.class);

    private static final String ENABLED = "com.openexchange.passwordchange.history";
    private static final String TRACKER = "com.openexchange.passwordchange.tracker";

    /**
     * Calls the according tracker for a user if the feature is available and saves the data
     * 
     * @param contextID The context of the user
     * @param userID The ID representing the user. For this user the password change will be recorded
     * @param ipAddress The IP address if available
     * @param source The calling resource. See {@link PasswordChangeInfo#APPSUITE}, {@link PasswordChangeInfo#PROVISIONING} or {@link PasswordChangeInfo#UNKOWN}
     */
    public static void recordChange(int contextID, int userID, final String ipAddress, final String source) {
        try {
            PasswordChangeTracker tracker = loadTracker(contextID, userID);
            final Timestamp current = new Timestamp(System.currentTimeMillis());
            PasswordChangeInfo info = new PasswordChangeInfo() {

                @Override
                public Timestamp lastModified() {
                    return current;
                }

                @Override
                public String modifiedBy() {
                    return source;
                }

                @Override
                public String modifyOrigin() {
                    return ipAddress;
                }

            };
            tracker.trackPasswordChange(userID, contextID, info);
        } catch (Exception e) {
            // IF this happens some property won't be there ..
            LOG.debug("Error while tracking password change for user {} in context {}", userID, contextID);
        }
    }

    /**
     * Clears the password change history for a specific user
     * 
     * @param contextID The context of the user
     * @param userID The ID of the user
     * @param limit see {@link PasswordChangeTracker#clear(int, int, int)}
     */
    public static void clearFor(int contextID, int userID, int limit) {
        try {
            PasswordChangeTracker tracker = loadTracker(contextID, userID);
            tracker.clear(userID, contextID, limit);
        } catch (Exception e) {
            LOG.warn("Error while deleting password change history for user {} in context {}", userID, contextID);
        }
    }

    private static PasswordChangeTracker loadTracker(int contextID, int userID) throws OXException {
        // Load config and get according tracker
        ConfigViewFactory casscade = Services.getService(ConfigViewFactory.class);
        if (null == casscade) {
            LOG.warn("Could not get config.");
            throw PasswordChangeHistoryException.MISSING_SERVICE.create("ConfigCasscade");
        }
        ConfigView view;
        view = casscade.getView(userID, contextID);
        boolean enabled = view.get(ENABLED, Boolean.class);
        if (false == enabled) {
            throw PasswordChangeHistoryException.DISABLED.create(userID, contextID);
        }
        // If empty, password change history is not wanted after all 
        String symbolicTrackerName = view.get(TRACKER, String.class);
        if (null == symbolicTrackerName || symbolicTrackerName.isEmpty()) {
            LOG.debug("No PasswordChangeTracker found .. No tracking wanted ");
            throw PasswordChangeHistoryException.MISSING_CONFIGURATION.create(userID, contextID);
        }
        // Load registry to get the fitting tracker
        PasswordChangeTrackerRegistry trackerRegistry = Services.getService(PasswordChangeTrackerRegistry.class);
        if (null == trackerRegistry) {
            LOG.debug("Could not get PasswordChangeTrackerRegistry");
            throw PasswordChangeHistoryException.MISSING_SERVICE.create("PasswordChangeTrackerRegistry");
        }
        PasswordChangeTracker tracker = trackerRegistry.getTracker(symbolicTrackerName);
        if (null == tracker) {
            // If no tracker available, there should be no tracking
            LOG.debug("Could not load {} for user {} in context {}", symbolicTrackerName, userID, contextID);
            throw PasswordChangeHistoryException.MISSING_TRACKER.create(symbolicTrackerName);
        }
        return tracker;
    }
}
