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

package com.openexchange.mail.api.unified;

import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;

/**
 * {@link UnifiedViewService} - Provides a unified view across subscribed mail accounts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public interface UnifiedViewService {

    /**
     * Gets all messages for specified full name.
     *
     * @param fullName The unified view full name
     * @param fields The fields to fill
     * @param session The associated session
     * @return All messages
     * @throws OXException If messages cannot be returned
     */
    MailMessage[] allMessages(UnifiedFullName fullName, MailField[] fields, Session session) throws OXException;

    /**
     * Searches messages in denoted unified view.
     *
     * @param fullName The unified view full name
     * @param indexRange The optional index range
     * @param sortField The optional sort field; default is {@link MailSortField#RECEIVED_DATE}
     * @param order The optional order; default is {@link OrderDirection#DESC}
     * @param searchTerm The optional search term
     * @param fields The fields to fill
     * @param session The associated session
     * @return The resulting messages
     * @throws OXException If messages cannot be returned
     */
    MailMessage[] searchMessages(UnifiedFullName fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields, Session session) throws OXException;

    /**
     * Gets the messages associated with given identifiers.
     *
     * @param fullName The unified view full name
     * @param mailIds The identifiers
     * @param fields The fields to fill
     * @param session The associated session
     * @return The associated messages
     * @throws OXException If messages cannot be returned
     */
    MailMessage[] getMessages(UnifiedFullName fullName, String[] mailIds, MailField[] fields, Session session) throws OXException;

    /**
     * Gets the denoted message.
     *
     * @param fullName The unified view full name
     * @param mailId The identifier
     * @param markSeen Whether to mark as seen
     * @param session The associated session
     * @return The associated message
     * @throws OXException If the message cannot be returned
     */
    MailMessage getMessage(UnifiedFullName fullName, String mailId, boolean markSeen, Session session) throws OXException;

}
