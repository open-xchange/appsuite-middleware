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
    public static List<Conversation> conversationsFor(String fullName, int limit, MailFields mailFields, IMailMessageStorage messageStorage) throws OXException {
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
    public static List<MailMessage> messagesFor(String fullName, int limit, MailFields mailFields, IMailMessageStorage messageStorage) throws OXException {
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
    public static List<Conversation> fold(List<Conversation> toFold) {
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

    private static void foldInto(Conversation conversation, Iterator<Conversation> iter) {
        while (iter.hasNext()) {
            final Conversation other = iter.next();
            if (conversation.referencesOrIsReferencedBy(other)) {
                iter.remove();
                conversation.join(other);
            }
        }
    }

}
