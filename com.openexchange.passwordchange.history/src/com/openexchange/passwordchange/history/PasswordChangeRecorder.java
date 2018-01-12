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

package com.openexchange.passwordchange.history;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.history.PasswordChangeInfo;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;

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
