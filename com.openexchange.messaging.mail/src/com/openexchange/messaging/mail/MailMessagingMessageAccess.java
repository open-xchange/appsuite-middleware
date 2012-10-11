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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.messaging.generic.AttachmentFinderHandler;
import com.openexchange.messaging.generic.MessageParser;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MailMessagingMessageAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public final class MailMessagingMessageAccess implements MessagingMessageAccess {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailMessagingMessageAccess.class));

    private final IMailMessageStorage messageStorage;

    private final MailLogicTools logicTools;

    private final int accountId;

    private final Session session;

    /**
     * Initializes a new {@link MailMessagingMessageAccess}.
     *
     * @param messageStorage The mail message storage
     * @param logicTools The logic tools
     * @param accountId The account ID
     * @param session The session providing user data
     */
    public MailMessagingMessageAccess(final IMailMessageStorage messageStorage, final MailLogicTools logicTools, final int accountId, final Session session) {
        super();
        this.messageStorage = messageStorage;
        this.logicTools = logicTools;
        this.accountId = accountId;
        this.session = session;
    }

    @Override
    public void appendMessages(final String folder, final MessagingMessage[] messages) throws OXException {
        try {
            final MailMessage[] mails = new MailMessage[messages.length];
            final UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192);
            for (int i = 0; i < mails.length; i++) {
                out.reset();
                messages[i].writeTo(out);
                mails[i] = MimeMessageConverter.convertMessage(out.toByteArray());
            }
            messageStorage.appendMessages(folder, mails);
        } catch (final OXException e) {
            throw e;
        } catch (final IOException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MessagingPart getAttachment(final String folder, final String messageId, final String sectionId) throws OXException {
        final AttachmentFinderHandler handler = new AttachmentFinderHandler(sectionId);
        new MessageParser().parseMessage(getMessage(folder, messageId, true), handler);
        final MessagingPart part = handler.getMessagingPart();
        if (null == part) {
            throw MessagingExceptionCodes.ATTACHMENT_NOT_FOUND.create(sectionId, messageId, folder);
        }
        return part;
    }

    @Override
    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        try {
            final String[] ids = messageStorage.copyMessages(sourceFolder, destFolder, messageIds, fast);
            return fast ? Collections.<String> emptyList() : Arrays.asList(ids);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws OXException {
        try {
            messageStorage.deleteMessages(folder, messageIds, hardDelete);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws OXException {
        try {
            final MailMessage[] mails =
                messageStorage.getAllMessages(folder, from(indexRange), fromSort(sortField), from(order), from(fields));
            final List<MessagingMessage> list = new ArrayList<MessagingMessage>();
            for (final MailMessage mail : mails) {
                list.add(from(mail));
            }
            return list;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws OXException {
        try {
            return from(messageStorage.getMessage(folder, id, !peek));
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws OXException {
        try {
            final MailMessage[] mails = messageStorage.getMessages(folder, messageIds, from(fields));
            final List<MessagingMessage> list = new ArrayList<MessagingMessage>();
            for (final MailMessage mail : mails) {
                list.add(from(mail));
            }
            return list;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        try {
            final String[] ids = messageStorage.moveMessages(sourceFolder, destFolder, messageIds, fast);
            return fast ? Collections.<String> emptyList() : Arrays.asList(ids);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public MessagingMessage perform(final String folder, final String id, final String action) throws OXException {
        try {
            if (MailConstants.TYPE_FORWARD.equalsIgnoreCase(action)) {
                final MailMessage fowardMessage =
                    logicTools.getFowardMessage(new MailMessage[] { messageStorage.getMessage(folder, id, false) });
                return from(fowardMessage);
            } else if (MailConstants.TYPE_REPLY.equalsIgnoreCase(action)) {
                final MailMessage replyMessage = logicTools.getReplyMessage(messageStorage.getMessage(folder, id, false), false);
                return from(replyMessage);
            } else if (MailConstants.TYPE_REPLY_ALL.equalsIgnoreCase(action)) {
                final MailMessage replyMessage = logicTools.getReplyMessage(messageStorage.getMessage(folder, id, false), true);
                return from(replyMessage);
            } else {
                throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
            }
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public MessagingMessage perform(final String action) throws OXException {
        /*
         * No supported actions for this perform() method
         */
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public MessagingMessage perform(final MessagingMessage message, final String action) throws OXException {
        /*
         * No supported actions for this perform() method
         */
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public List<MessagingMessage> searchMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws OXException {
        try {
            final MailMessage[] mails =
                messageStorage.searchMessages(folder, from(indexRange), fromSort(sortField), from(order), from(searchTerm), from(fields));
            final List<MessagingMessage> list = new ArrayList<MessagingMessage>();
            for (final MailMessage mail : mails) {
                list.add(from(mail));
            }
            return list;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void updateMessage(final MessagingMessage message, final MessagingField[] fields) throws OXException {
        try {
            final EnumSet<MessagingField> set = EnumSet.copyOf(Arrays.asList(fields));
            if (set.contains(MessagingField.COLOR_LABEL)) {
                messageStorage.updateMessageColorLabel(message.getFolder(), new String[] { message.getId() }, message.getColorLabel());
            }
            if (set.contains(MessagingField.FLAGS)) {
                messageStorage.updateMessageFlags(message.getFolder(), new String[] { message.getId() }, message.getFlags(), true);
            }
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws OXException {
        try {
            return new MailBinaryContent(messageStorage.getAttachment(folder, id, referenceId));
        } catch (final OXException e) {
            throw e;
        }
    }

    private static MailField[] from(final MessagingField[] fields) {
        final MailField[] ret = new MailField[fields.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = from(fields[i]);
        }
        return ret;
    }

    private static MailField from(final MessagingField messagingField) {
        switch (messagingField) {
        case ACCOUNT_NAME:
            return MailField.ACCOUNT_NAME;
        case BCC:
            return MailField.BCC;
        case BODY:
            return MailField.BODY;
        case CC:
            return MailField.CC;
        case COLOR_LABEL:
            return MailField.COLOR_LABEL;
        case CONTENT_TYPE:
            return MailField.CONTENT_TYPE;
        case DISPOSITION_NOTIFICATION_TO:
            return MailField.DISPOSITION_NOTIFICATION_TO;
        case FLAGS:
            return MailField.FLAGS;
        case FOLDER_ID:
            return MailField.FOLDER_ID;
        case FROM:
            return MailField.FROM;
        case FULL:
            return MailField.FULL;
        case HEADERS:
            return MailField.HEADERS;
        case ID:
            return MailField.ID;
        case PICTURE:
            return MailField.HEADERS;
        case PRIORITY:
            return MailField.PRIORITY;
        case RECEIVED_DATE:
            return MailField.RECEIVED_DATE;
        case SENT_DATE:
            return MailField.SENT_DATE;
        case SIZE:
            return MailField.SIZE;
        case SUBJECT:
            return MailField.SUBJECT;
        case THREAD_LEVEL:
            return MailField.THREAD_LEVEL;
        case TO:
            return MailField.TO;
        default:
            return null;
        }
    }

    private static com.openexchange.mail.OrderDirection from(final OrderDirection order) {
        if(order == null) {
            return null;
        }
        switch (order) {
        case ASC:
            return com.openexchange.mail.OrderDirection.ASC;
        case DESC:
            return com.openexchange.mail.OrderDirection.DESC;
        default:
            return null;
        }
    }

    private static MailSortField fromSort(final MessagingField sortField) {
        if(sortField == null) {
            return null;
        }
        switch (sortField) {
        case ACCOUNT_NAME:
            return MailSortField.ACCOUNT_NAME;
        case CC:
            return MailSortField.CC;
        case COLOR_LABEL:
            return MailSortField.COLOR_LABEL;
        case FLAGS:
            return MailSortField.FLAG_SEEN;
        case FROM:
            return MailSortField.FROM;
        case RECEIVED_DATE:
            return MailSortField.RECEIVED_DATE;
        case SENT_DATE:
            return MailSortField.SENT_DATE;
        case SIZE:
            return MailSortField.SIZE;
        case SUBJECT:
            return MailSortField.SUBJECT;
        case TO:
            return MailSortField.TO;
        default:
            return null;
        }
    }

    private static com.openexchange.mail.IndexRange from(final IndexRange indexRange) {
        if (null == indexRange) {
            return null;
        }
        return new com.openexchange.mail.IndexRange(indexRange.start, indexRange.end);
    }

    private static MessagingMessage from(final MailMessage mailMessage) {
        return new MailMessagingMessage(mailMessage);
    }

    private com.openexchange.mail.search.SearchTerm<?> from(final SearchTerm<?> searchTerm) {
        // TODO Auto-generated method stub
        return null;
    }

}
