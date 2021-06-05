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

package com.openexchange.mail.cache;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;

/**
 * {@link IMailAccessCache} - A very volatile cache for already connected instances of {@link MailAccess}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailAccessCache {

    /**
     * Removes and returns a mail access from cache.
     *
     * @param session The session
     * @param accountId The account ID
     * @return An active instance of {@link MailAccess} or <code>null</code>
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> removeMailAccess(Session session, int accountId);

    /**
     * Puts given mail access into cache if none user-bound connection is already contained in cache.
     *
     * @param session The session
     * @param accountId The account ID
     * @param mailAccess The mail access to put into cache
     * @return <code>true</code> if mail access could be successfully cached; otherwise <code>false</code>
     */
    public boolean putMailAccess(Session session, int accountId, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess);

    /**
     * Checks if cache already holds a user-bound mail access for specified account.
     *
     * @param session The session
     * @param accountId The account ID
     * @return <code>true</code> if a user-bound mail access is already present in cache; otherwise <code>false</code>
     */
    public boolean containsMailAccess(Session session, int accountId);

    /**
     * Clears the cache entries kept for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If clearing user entries fails
     */
    public void clearUserEntries(int userId, int contextId) throws OXException;

    /**
     * Gets the number of cached, user-bound mail accesses for specified account.
     *
     * @param session The session
     * @param accountId The account ID
     * @return The number of cached, user-bound mail accesses for specified account
     */
    public int numberOfMailAccesses(Session session, int accountId) throws OXException;

    /**
     * Closes this cache.
     */
    public void close();

}
