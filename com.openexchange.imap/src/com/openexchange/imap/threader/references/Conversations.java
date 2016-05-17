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

package com.openexchange.imap.threader.references;

import static com.openexchange.imap.command.MailMessageFetchIMAPCommand.getFetchCommand;
import static com.openexchange.imap.command.MailMessageFetchIMAPCommand.handleFetchRespone;
import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPServerInfo;
import com.openexchange.imap.command.MailMessageFetchIMAPCommand;
import com.openexchange.imap.threadsort.MessageInfo;
import com.openexchange.imap.threadsort.ThreadSortNode;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.utils.MimeStorageUtility;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;


/**
 * {@link Conversations} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Conversations {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Conversations.class);

    /**
     * Initializes a new {@link Conversations}.
     */
    private Conversations() {
        super();
    }

    static final FetchProfile FETCH_PROFILE_CONVERSATION_BY_HEADERS;
    static final FetchProfile FETCH_PROFILE_CONVERSATION_BY_ENVELOPE;
    static {
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add("References");
        fp.add("Message-Id");
        //fp.add("In-Reply-To");
        FETCH_PROFILE_CONVERSATION_BY_HEADERS = fp;
        fp = new FetchProfile();
        fp.add("References");
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add(MailMessageFetchIMAPCommand.ENVELOPE_ONLY);
        FETCH_PROFILE_CONVERSATION_BY_ENVELOPE = fp;
    }

    /**
     * Gets the <i>"by envelope"</i> fetch profile including specified fields.
     *
     * @param fields The fields to add
     * @return The <i>"by envelope"</i> fetch profile
     */
    public static FetchProfile getFetchProfileConversationByEnvelope(MailField... fields) {
        FetchProfile fp = new FetchProfile();
        fp.add("References");
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add(MailMessageFetchIMAPCommand.ENVELOPE_ONLY);
        if (null != fields) {
            for (MailField field : fields) {
                if (!MimeStorageUtility.isEnvelopeField(field)) {
                    MimeStorageUtility.addFetchItem(fp, field);
                }
            }
        }
        return fp;
    }

    /**
     * Gets the <i>"by headers"</i> fetch profile including specified fields.
     *
     * @param fields The fields to add
     * @return The <i>"by headers"</i> fetch profile
     */
    public static FetchProfile getFetchProfileConversationByHeaders(MailField... fields) {
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add("References");
        fp.add("Message-Id");
        if (null != fields) {
            for (MailField field : fields) {
                if (MailField.RECEIVED_DATE.equals(field)) {
                    fp.add(MailMessageFetchIMAPCommand.INTERNALDATE);
                } else {
                    MimeStorageUtility.addFetchItem(fp, field);
                }
            }
        }
        return fp;
    }

    /**
     * Checks specified {@link FetchProfile} to contain needed items/headers for building up conversations.
     *
     * @param fetchProfile The fetch profile to check
     * @param byEnvelope <code>true</code> to use ENVELOPE fetch item; other <code>false</code> to use single headers (<i>"References"</i>, <i>"In-Reply-To"</i>, and <i>"Message-Id"</i>)
     * @return The fetch profile ready for building up conversations
     */
    public static FetchProfile checkFetchProfile(final FetchProfile fetchProfile, final boolean byEnvelope) {
        // Add 'References' to FetchProfile if absent
        {
            boolean found = false;
            final String hdrReferences = MessageHeaders.HDR_REFERENCES;
            final String[] headerNames = fetchProfile.getHeaderNames();
            for (int i = 0; !found && i < headerNames.length; i++) {
                if (hdrReferences.equalsIgnoreCase(headerNames[i])) {
                    found = true;
                }
            }
            if (!found) {
                fetchProfile.add(hdrReferences);
            }
        }
        // Add UID item to FetchProfile if absent
        {
            boolean found = false;
            final Item uid = UIDFolder.FetchProfileItem.UID;
            final Item[] items = fetchProfile.getItems();
            for (int i = 0; !found && i < items.length; i++) {
                final Item cur = items[i];
                if (uid == cur) {
                    found = true;
                }
            }
            if (!found) {
                fetchProfile.add(uid);
            }
        }
        // ------ Either by-envelope or by headers ------
        if (byEnvelope) {
            boolean found = false;
            final Item envelope = FetchProfile.Item.ENVELOPE;
            final Item envelopeOnly = MailMessageFetchIMAPCommand.ENVELOPE_ONLY;
            final Item[] items = fetchProfile.getItems();
            for (int i = 0; !found && i < items.length; i++) {
                final Item cur = items[i];
                if (envelope == cur || envelopeOnly == cur) {
                    found = true;
                }
            }
            if (!found) {
                fetchProfile.add(envelopeOnly);
            }
        } else {
            // Add 'In-Reply-To' to FetchProfile if absent
            /*-
             *
            {
                boolean found = false;
                final Item envelope = FetchProfile.Item.ENVELOPE;
                final Item envelopeOnly = MailMessageFetchIMAPCommand.ENVELOPE_ONLY;
                final Item[] items = fetchProfile.getItems();
                for (int i = 0; !found && i < items.length; i++) {
                    final Item cur = items[i];
                    if (envelope == cur || envelopeOnly == cur) {
                        found = true;
                    }
                }
                if (!found) {
                    final String hdrInReplyTo = MessageHeaders.HDR_IN_REPLY_TO;
                    final String[] headerNames = fetchProfile.getHeaderNames();
                    for (int i = 0; !found && i < headerNames.length; i++) {
                        if (hdrInReplyTo.equalsIgnoreCase(headerNames[i])) {
                            found = true;
                        }
                    }
                    if (!found) {
                        fetchProfile.add(hdrInReplyTo);
                    }
                }
            }
             */
            // Add 'Message-Id' to FetchProfile if absent
            {
                boolean found = false;
                final Item envelope = FetchProfile.Item.ENVELOPE;
                final Item envelopeOnly = MailMessageFetchIMAPCommand.ENVELOPE_ONLY;
                final Item[] items = fetchProfile.getItems();
                for (int i = 0; !found && i < items.length; i++) {
                    final Item cur = items[i];
                    if (envelope == cur || envelopeOnly == cur) {
                        found = true;
                    }
                }
                if (!found) {
                    final String hdrMessageId = MessageHeaders.HDR_MESSAGE_ID;
                    final String[] headerNames = fetchProfile.getHeaderNames();
                    for (int i = 0; !found && i < headerNames.length; i++) {
                        if (hdrMessageId.equalsIgnoreCase(headerNames[i])) {
                            found = true;
                        }
                    }
                    if (!found) {
                        fetchProfile.add(envelopeOnly);
                    }
                }
            }
        }
        return fetchProfile;
    }

    /**
     * Retrieves <b><small>UNFOLDED</small></b> conversations for specified IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @param lookAhead The limit
     * @param order The order direction that controls which chunk (oldest vs. most recent) to select
     * @param fetchProfile The fetch profile
     * @param serverInfo The IMAP server information
     * @param byEnvelope Whether to build-up using ENVELOPE; otherwise <code>false</code>
     * @return The unfolded conversations
     * @throws MessagingException If a messaging error occurs
     */
    public static List<Conversation> conversationsFor(IMAPFolder imapFolder, int lookAhead, OrderDirection order, FetchProfile fetchProfile, IMAPServerInfo serverInfo, boolean byEnvelope) throws MessagingException {
        final List<MailMessage> messages = messagesFor(imapFolder, lookAhead, order, fetchProfile, serverInfo, byEnvelope);
        if (null == messages || messages.isEmpty()) {
            return Collections.<Conversation> emptyList();
        }
        final List<Conversation> conversations = new ArrayList<Conversation>(messages.size());
        for (final MailMessage message : messages) {
            conversations.add(new Conversation(message));
        }
        return conversations;
    }

    /**
     * Retrieves messages for specified IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @param lookAhead The limit
     * @param order The order direction that controls which chunk (oldest vs. most recent) to select
     * @param fetchProfile The fetch profile
     * @param serverInfo The IMAP server information
     * @param byEnvelope Whether to build-up using ENVELOPE; otherwise <code>false</code>
     * @return The messages with conversation information (References, In-Reply-To, Message-Id)
     * @throws MessagingException If a messaging error occurs
     */
    @SuppressWarnings("unchecked")
    public static List<MailMessage> messagesFor(final IMAPFolder imapFolder, final int lookAhead, final OrderDirection order, final FetchProfile fetchProfile, final IMAPServerInfo serverInfo, final boolean byEnvelope) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return Collections.<MailMessage> emptyList();
        }
        final org.slf4j.Logger log = LOG;
        return (List<MailMessage>) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                final String command;
                final Response[] r;
                {
                    StringBuilder sb = new StringBuilder(128).append("FETCH ");
                    if (1 == messageCount) {
                        sb.append("1");
                    } else {
                        if (lookAhead < 0 || lookAhead >= messageCount) {
                            sb.append("1:*");
                        } else {
                            if (OrderDirection.DESC.equals(order)) {
                                sb.append(messageCount - lookAhead + 1).append(':').append('*');
                            } else {
                                sb.append(1).append(':').append(lookAhead);
                            }
                        }
                    }
                    final FetchProfile fp = null == fetchProfile ? (byEnvelope ? FETCH_PROFILE_CONVERSATION_BY_ENVELOPE : FETCH_PROFILE_CONVERSATION_BY_HEADERS) : checkFetchProfile(fetchProfile, byEnvelope);
                    sb.append(" (").append(getFetchCommand(protocol.isREV1(), fp, false, serverInfo)).append(')');
                    command = sb.toString();
                    sb = null;
                    // Execute command
                    final long start = System.currentTimeMillis();
                    r = protocol.command(command, null);
                    final long dur = System.currentTimeMillis() - start;
                    log.debug("\"{}\" for \"{}\" ({}) took {}msec.", command, imapFolder.getFullName(), imapFolder.getStore(), Long.valueOf(dur));
                    mailInterfaceMonitor.addUseTime(dur);
                }
                final int len = r.length - 1;
                final Response response = r[len];
                if (response.isOK()) {
                    try {
                        final List<MailMessage> mails = new ArrayList<MailMessage>(messageCount);
                        final String fullName = imapFolder.getFullName();
                        final String sInReplyTo = "In-Reply-To";
                        final String sReferences = "References";
                        for (int j = 0; j < len; j++) {
                            if (r[j] instanceof FetchResponse) {
                                final MailMessage message = handleFetchRespone((FetchResponse) r[j], fullName);
                                final String references = message.getFirstHeader(sReferences);
                                if (null == references) {
                                    final String inReplyTo = message.getFirstHeader(sInReplyTo);
                                    if (null != inReplyTo) {
                                        message.setHeader(sReferences, inReplyTo);
                                    }
                                }
                                mails.add(message);
                                r[j] = null;
                            }
                        }
                        // Handle remaining responses
                        protocol.notifyResponseHandlers(r);
                        return mails;
                    } catch (final MessagingException e) {
                        throw new ProtocolException(e.getMessage(), e);
                    } catch (final OXException e) {
                        throw new ProtocolException(e.getMessage(), e);
                    }
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return Collections.<Conversation> emptyList();
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    protocol.handleResult(response);
                }
                return Collections.<MailMessage> emptyList();
            }
        }));
    }

    /**
     * Transforms conversations to list of <tt>ThreadSortNode</tt>s.
     *
     * @param conversations The conversations to transform
     * @return The resulting list of <tt>ThreadSortNode</tt>s
     */
    public static List<ThreadSortNode> toNodeList(final List<Conversation> conversations) {
        if (null == conversations) {
            return Collections.emptyList();
        }
        final List<ThreadSortNode> list = new ArrayList<ThreadSortNode>(conversations.size());
        for (final Conversation conversation : conversations) {
            final List<MailMessage> messages = conversation.getMessages();
            final ThreadSortNode root = toThreadSortNode((IDMailMessage) messages.remove(0));
            root.addChildren(toThreadSortNodes(messages));
            list.add(root);
        }
        return list;
    }

    private static ThreadSortNode toThreadSortNode(final IDMailMessage message) {
        return new ThreadSortNode(new MessageInfo(message.getSeqnum()).setFullName(message.getFolder()), message.getUid());
    }

    private static List<ThreadSortNode> toThreadSortNodes(final List<MailMessage> messages) {
        final List<ThreadSortNode> ret = new ArrayList<ThreadSortNode>(messages.size());
        for (final MailMessage message : messages) {
            ret.add(toThreadSortNode((IDMailMessage) message));
        }
        return ret;
    }

    /**
     * Folds specified conversations.
     *
     * @param toFold The conversations to fold
     * @return The folded conversations
     */
    public static List<Conversation> fold(final List<Conversation> toFold) {
        Iterator<Conversation> iter = toFold.iterator();
        int i = 0;
        while (iter.hasNext()) {
            foldInto(iter.next(), iter);
            iter = toFold.listIterator(++i);
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
