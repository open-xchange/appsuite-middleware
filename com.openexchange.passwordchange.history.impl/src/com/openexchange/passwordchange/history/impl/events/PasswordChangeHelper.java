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

package com.openexchange.passwordchange.history.impl.events;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.history.PasswordChangeClients;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.PasswordChangeRecorderException;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.impl.PasswordChangeInfoImpl;

/**
 * {@link PasswordChangeHelper} - Utility class to save or clear password changes history.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHelper.class);

    /**
     * Initializes a new {@link PasswordChangeHelper}.
     */
    private PasswordChangeHelper() {
        super();
    }

    /**
     * Calls the according tracker for a user if the feature is available and saves the data
     *
     * @param contextId The context of the user
     * @param userId The ID representing the user. For this user the password change will be recorded
     * @param ipAddress The IP address if available
     * @param client The calling resource. E.g. {@link PasswordChangeClients#PROVISIONING}
     * @param registry The recorder registry
     */
    public static void recordChangeSafe(int contextId, int userId, String ipAddress, String client, PasswordChangeRecorderRegistryService registry) {
        try {
            PasswordChangeRecorder recorder = registry.getRecorderForUser(userId, contextId);
            recorder.trackPasswordChange(userId, contextId, new PasswordChangeInfoImpl(System.currentTimeMillis(), client, ipAddress));
        } catch (OXException e) {
            if (PasswordChangeRecorderException.DENIED_FOR_GUESTS.equals(e) || PasswordChangeRecorderException.DISABLED.equals(e)) {
                LOG.debug("No password change recording for user {} in context {}", I(userId), I(contextId), e);
            } else {
                LOG.error("Failed password change recording for user {} in context {}", I(userId), I(contextId), e);
            }
        } catch (Exception e) {
            LOG.error("Failed password change recording for user {} in context {}", I(userId), I(contextId), e);
        }
    }

    /**
     * Clears the password change history for a specific user
     *
     * @param contextId The context of the user
     * @param userId The ID of the user
     * @param limit see {@link PasswordChangeRecorder#clear(int, int, int)}
     * @param registry The recorder registry
     */
    public static void clearSafeFor(int contextId, int userId, int limit, PasswordChangeRecorderRegistryService registry) {
        try {
            PasswordChangeRecorder recorder = registry.getRecorderForUser(userId, contextId);
            recorder.clear(userId, contextId, limit);
        } catch (OXException e) {
            if (PasswordChangeRecorderException.DENIED_FOR_GUESTS.equals(e) || PasswordChangeRecorderException.DISABLED.equals(e)) {
                LOG.debug("No password change recording for user {} in context {}", I(userId), I(contextId), e);
            } else {
                LOG.error("Error while deleting password change history for user {} in context {}.", I(userId), I(contextId), e);
            }
        } catch (Exception e) {
            LOG.error("Error while deleting password change history for user {} in context {}.", I(userId), I(contextId), e);
        }
    }

}
