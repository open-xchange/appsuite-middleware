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

package com.openexchange.mail.threader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link Conversations} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Conversations {

    /**
     * Initializes a new {@link Conversations}.
     */
    private Conversations() {
        super();
    }

    private static final MailField[] FIELDS_HEADERS = new MailField[] { MailField.ID, MailField.HEADERS };
    private static final MailSortField RECEIVED_DATE = MailSortField.RECEIVED_DATE;
    private static final OrderDirection ASC = OrderDirection.ASC;

    /**
     * Retrieves <b><small>UNFOLDED</small></b> conversations for specified folder.
     *
     * @param fullName The folder full name
     * @param limit The limit
     * @param mailFields The mail fields
     * @param messageStorage The message storage
     * @return The unfolded conversations
     * @throws OXException If a messaging error occurs
     */
    public static List<Conversation> conversationsFor(final String fullName, final int limit, final MailFields mailFields, final IMailMessageStorage messageStorage) throws OXException {
        final IndexRange indexRange = limit > 0 ? new IndexRange(0, limit) : null;
        mailFields.addAll(FIELDS_HEADERS);
        final MailMessage[] messages = messageStorage.searchMessages(fullName, indexRange, RECEIVED_DATE, ASC, null, mailFields.toArray());
        final String sInReplyTo = "In-Reply-To";
        final String sReferences = "References";
        final int length = messages.length;
        final List<Conversation> conversations = new ArrayList<Conversation>(length);
        for (int i = 0; i < length; i++) {
            final MailMessage message = messages[i];
            if (null != message) {
                message.setFolder(fullName);
                final String references = message.getFirstHeader(sReferences);
                if (null == references) {
                    final String inReplyTo = message.getFirstHeader(sInReplyTo);
                    if (null != inReplyTo) {
                        message.setHeader(sReferences, inReplyTo);
                    }
                }
                conversations.add(new Conversation(message));
            }
        }
        return conversations;
    }

    /**
     * Retrieves messages for specified folder.
     *
     * @param fullName The folder full name
     * @param limit The limit
     * @param mailFields The mail fields
     * @param messageStorage The message storage
     * @return The unfolded conversations
     * @throws OXException If a messaging error occurs
     */
    public static List<MailMessage> messagesFor(final String fullName, final int limit, final MailFields mailFields, final IMailMessageStorage messageStorage) throws OXException {
        final IndexRange indexRange = limit > 0 ? new IndexRange(0, limit) : null;
        mailFields.addAll(FIELDS_HEADERS);
        final MailMessage[] messages = messageStorage.searchMessages(fullName, indexRange, RECEIVED_DATE, ASC, null, mailFields.toArray());
        final String sInReplyTo = "In-Reply-To";
        final String sReferences = "References";
        final int length = messages.length;
        final List<MailMessage> retval = new ArrayList<MailMessage>(length);
        for (int i = 0; i < length; i++) {
            final MailMessage message = messages[i];
            if (null != message) {
                message.setFolder(fullName);
                final String references = message.getFirstHeader(sReferences);
                if (null == references) {
                    final String inReplyTo = message.getFirstHeader(sInReplyTo);
                    if (null != inReplyTo) {
                        message.setHeader(sReferences, inReplyTo);
                    }
                }
                retval.add(message);
            }
        }
        return retval;
    }

    /**
     * Folds specified conversations.
     *
     * @param toFold The conversations to fold
     * @return The folded conversations
     */
    public static List<Conversation> fold(final List<Conversation> toFold) {
        int lastProcessed = -1;
        Iterator<Conversation> iter = toFold.iterator();
        int i = 0;
        while (iter.hasNext()) {
            if (i > lastProcessed) {
                foldInto(iter.next(), iter);
                lastProcessed = i;
                iter = toFold.iterator();
                i = 0;
            } else {
                // Consume iterator until proper position reached
                iter.next();
                i++;
            }
        }
        return toFold;
    }

    private static void foldInto(final Conversation conversation, final Iterator<Conversation> iter) {
        while (iter.hasNext()) {
            final Conversation other = iter.next();
            if (conversation.referencesOrIsReferencedBy(other)) {
                iter.remove();
                conversation.join(other);
            }
        }
    }

}
