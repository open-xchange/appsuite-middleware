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

package com.openexchange.quota;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;


/**
 * A {@link QuotaProvider} must be implemented by every module that wants
 * to contribute to the general {@link QuotaService}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
@Service
public interface QuotaProvider {

    /**
     * Gets the id of the corresponding module. A modules id must always be
     * unique.
     *
     * @return The modules id; never <code>null</code>.
     */
    String getModuleID();

    /**
     * Gets the modules name that may be displayed within a client application.
     * The name should be localizable, that means it should be a valid key for
     * the I18nService.
     *
     * @return The name, never <code>null</code>.
     */
    String getDisplayName();

    /**
     * Gets the quota and usage for a session-specific user and a given account.
     *
     * @param session The session, never <code>null</code>.
     * @param accountID The id of a possible account for the user within this module,
     *  never <code>null</code>.
     * @return The quota and usage, never <code>null</code>.
     * @throws OXException If no account was found for the given id, {@link QuotaExceptionCodes#UNKNOWN_ACCOUNT}
     * is thrown. Other exception codes denote occurred errors while calculating quota and usage.
     */
    AccountQuota getFor(Session session, String accountID) throws OXException;

    /**
     * Gets the quota and usage for a session-specific user, folder and a given account.
     *
     * @param session The session, never <code>null</code>.
     * @param accountID The id of a possible account for the user within this module,
     *  never <code>null</code>.
     * @param folderId The folderId to get the quota for
     * @return The quota and usage, never <code>null</code>.
     * @throws OXException If no account was found for the given id, {@link QuotaExceptionCodes#UNKNOWN_ACCOUNT}
     * is thrown. Other exception codes denote occurred errors while calculating quota and usage.
     */
    default AccountQuota getFor(Session session, String accountID, String folderId) throws OXException {
        return getFor(session, accountID);
    }

    /**
     * Gets the quota and usage for all accounts for the session-specific
     * user within this module.
     *
     * @param session The session, never <code>null</code>.
     * @return A list of quotas and usages. Never <code>null</code> but possibly
     *  empty, if no account exists.
     * @throws OXException If an error occurs while calculating quota and usage.
     */
    AccountQuotas getFor(Session session) throws OXException;
}
