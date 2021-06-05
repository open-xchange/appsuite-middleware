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

package com.openexchange.passwordchange.history;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link PasswordChangeRecorder} - Provides methods to retrieve and record password changes.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface PasswordChangeRecorder {

    /**
     * List the current data stored in the database
     *
     * @param userID The ID of the user to list the password changes for
     * @param contextID The context ID of the user
     * @return {@link List} of all available password change events (~ the history)
     * @throws OXException If password change events cannot be returned
     */
    List<PasswordChangeInfo> listPasswordChanges(int userID, int contextID) throws OXException;

    /**
     * List the current data stored in the database
     *
     * @param userID The ID of the user to list the password changes for
     * @param contextID The context ID of the user
     * @param fieldNames The field names that should be sorted with the corresponding {@link SortOrder}. Caller has to make sure that the order of elements is predictable.
     * @return {@link List} of all available password change events (~ the history)
     * @throws OXException If password change events cannot be returned
     */
    List<PasswordChangeInfo> listPasswordChanges(int userID, int contextID, Map<SortField, SortOrder> fieldNames) throws OXException;

    /**
     * Adds a new set of information to the database
     *
     * @param userID The ID of the user to track the password changes for
     * @param contextID The context ID of the user
     * @param info The {@link PasswordChangeInfo} to be added
     */
    void trackPasswordChange(int userID, int contextID, PasswordChangeInfo info) throws OXException;

    /**
     * Clears the PasswordChange informations for a specific user
     *
     * @param userID The ID of the user to clear recorded password changes for
     * @param contextID The context ID of the user
     * @param limit The limit of entries to store in the DB. If current entries exceed the limitation the oldest
     *            entries get deleted. If set to <code>0</code> all entries will be deleted
     */
    void clear(int userID, int contextID, int limit) throws OXException;

    /**
     * Get the name the {@link PasswordChangeRecorder} should be registered to
     *
     * @return The name of the implementation
     */
    String getSymbolicName();
}
