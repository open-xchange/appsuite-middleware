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

package com.openexchange.passwordchange.history.impl.events;

import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.history.PasswordChangeHandlerRegistryService;
import com.openexchange.passwordchange.history.PasswordChangeHistoryException;
import com.openexchange.passwordchange.history.PasswordChangeInfo;
import com.openexchange.passwordchange.history.PasswordHistoryHandler;
import com.openexchange.passwordchange.history.impl.PasswordChangeInfoImpl;
import com.openexchange.server.ServiceLookup;

/**
 * {@link PasswordChangeHelper} - Utility class to save or clear password changes history.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHelper.class);

    /**
     *
     * Initializes a new {@link PasswordChangeHelper}.
     *
     * @param service The {@link ServiceLookup} to get services from
     * @param registry The {@link PasswordChangeHandlerRegistryService} to get the {@link PasswordHistoryHandler} from
     */
    private PasswordChangeHelper(PasswordChangeHandlerRegistryService registry) {
        super();
    }

    /**
     * Calls the according tracker for a user if the feature is available and saves the data
     *
     * @param contextId The context of the user
     * @param userId The ID representing the user. For this user the password change will be recorded
     * @param ipAddress The IP address if available
     * @param client The calling resource. E.g. {@link PasswordChangeInfo#PROVISIONING}
     * @param registry The handler registry
     */
    public static void recordChangeSafe(int contextId, int userId, String ipAddress, String client, PasswordChangeHandlerRegistryService registry) {
        try {
            PasswordHistoryHandler handler = registry.getHandlerForUser(userId, contextId);
            PasswordChangeInfo info = new PasswordChangeInfoImpl(System.currentTimeMillis(), client, ipAddress);
            handler.trackPasswordChange(userId, contextId, info);
        } catch (OXException e) {
            if (PasswordChangeHistoryException.DENIED_FOR_GUESTS.equals(e) || PasswordChangeHistoryException.DISABLED.equals(e)) {
                LOG.debug("No password change recording for user {} in context {}", userId, contextId, e);
            } else {
                LOG.error("Failed password change recording for user {} in context {}", userId, contextId, e);
            }
        } catch (Exception e) {
            LOG.error("Failed password change recording for user {} in context {}", userId, contextId, e);
        }
    }

    /**
     * Clears the password change history for a specific user
     *
     * @param contextId The context of the user
     * @param userId The ID of the user
     * @param limit see {@link PasswordHistoryHandler#clear(int, int, int)}
     */
    public static void clearSafeFor(int contextId, int userId, int limit, PasswordChangeHandlerRegistryService registry) {
        try {
            PasswordHistoryHandler handler = registry.getHandlerForUser(userId, contextId);
            handler.clear(userId, contextId, limit);
        } catch (Exception e) {
            LOG.debug("Error while deleting password change history for user {} in context {}. Reason: {}", userId, contextId, e.getMessage(), e);
        }
    }

}
