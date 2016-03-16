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
