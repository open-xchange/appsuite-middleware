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

package com.openexchange.mail;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link MailFetchListener} - A listener invoked right before and after fetching mails allowing to modify and/or enhance mails.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface MailFetchListener {

    /**
     * Invoked when mails are fetched from {@link MailMessageCache} to test whether this listener is satisfied with the information already available in cached mails.
     *
     * @param mailsFromCache The mails fetched from cache
     * @param fetchArguments The fetch arguments
     * @param session The user's session
     * @return <code>true</code> if satisfied; otherwise <code>false</code>
     * @throws OXException If acceptance cannot be checked
     */
    boolean accept(MailMessage[] mailsFromCache, MailFetchArguments fetchArguments, Session session) throws OXException;

    /**
     * Invoked prior to fetching mails from mail back-end and allows this listener to add its needed fields and/or header names (if any)
     *
     * @param fetchArguments The fetch arguments
     * @param mailAccess The user's session
     * @param state The state, which lets individual listeners store stuff
     * @return The mail attributation
     * @throws OXException If attributation fails
     */
    MailAttributation onBeforeFetch(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException;

    /**
     * Invoked after mails are fetched and allows to modify and/or enhance them.
     *
     * @param mails The fetched mails
     * @param cacheable Whether specified mails are supposed to be cached
     * @param mailAccess The user's session
     * @param state The state, which was passed to {@link #onBeforeFetch(MailFetchArguments, MailAccess, Map) onBeforeFetch} invocation
     * @return The listener's result
     * @throws OXException If an aborting error occurs; acts in the same way as returning {@link MailFetchListenerResult#deny(OXException)}
     */
    MailFetchListenerResult onAfterFetch(MailMessage[] mails, boolean cacheable, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException;

    /**
     * Invoked when a single mail has been fully fetched from storage.
     *
     * @param mail The fetched mail
     * @param mailAccess The user's session
     * @return The resulting mail
     * @throws OXException If an aborting error occurs
     */
    MailMessage onSingleMailFetch(MailMessage mail, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException;

}
