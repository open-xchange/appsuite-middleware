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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.dataobjects.MailMessage;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;


/**
 * {@link Conversations} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Conversations {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(Conversations.class);

    /**
     * Initializes a new {@link Conversations}.
     */
    private Conversations() {
        super();
    }

    static final FetchProfile FETCH_PROFILE_CONVERSATION;
    static {
        final FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add("References");
        fp.add("Message-Id");
        fp.add("In-Reply-To");
        FETCH_PROFILE_CONVERSATION = fp;
    }

    /**
     * Retrieves <b><small>UNFOLDED</small></b> conversations for specified IMAP folder.
     * 
     * @param imapFolder The IMAP folder
     * @param limit The limit
     * @return The unfolded conversations
     * @throws MessagingException If a messaging error occurs
     */
    @SuppressWarnings("unchecked")
    public static List<Conversation> conversationsFor(final IMAPFolder imapFolder, final int limit) throws MessagingException{
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return Collections.<Conversation> emptyList();
        }
        final org.apache.commons.logging.Log log = LOG;
        return (List<Conversation>) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                final String command;
                final Response[] r;
                {
                    com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128).append("FETCH ");
                    if (1 == messageCount) {
                        sb.append("1");
                    } else {
                        if (limit < 0 || limit >= messageCount) {
                            sb.append("1:*");
                        } else {
                            sb.append(messageCount - limit + 1).append(':').append('*');
                        }
                    }
                    sb.append(" (").append(getFetchCommand(protocol.isREV1(), FETCH_PROFILE_CONVERSATION, false)).append(')');
                    command = sb.toString();
                    sb = null;
                    final long start = System.currentTimeMillis();
                    r = protocol.command(command, null);
                    final long dur = System.currentTimeMillis() - start;
                    if (log.isInfoEnabled()) {
                        log.info('"' + command + "\" for \"" + imapFolder.getFullName() + "\" (" + imapFolder.getStore().toString() + ") took " + dur + "msec.");
                    }
                    mailInterfaceMonitor.addUseTime(dur);
                }
                final int len = r.length - 1;
                final Response response = r[len];
                if (response.isOK()) {
                    try {
                        final List<MailMessage> mails = new ArrayList<MailMessage>(messageCount);
                        final String fullName = imapFolder.getFullName();
                        final char sep = imapFolder.getSeparator();
                        final String sFetch = "FETCH";
                        for (int j = 0; j < len; j++) {
                            if (sFetch.equals(((IMAPResponse) r[j]).getKey())) {
                                final MailMessage message = handleFetchRespone((FetchResponse) r[j], fullName, sep);
                                {
                                    final String inReplyTo = message.getFirstHeader("In-Reply-To");
                                    if (!isEmpty(inReplyTo)) {
                                        final String references = message.getFirstHeader("References");
                                        if (!isEmpty(references) && references.indexOf(inReplyTo) < 0) {
                                            message.setHeader("References", new StringAllocator(references).append(' ').append(inReplyTo).toString());
                                        }
                                    }
                                }
                                mails.add(message);
                                r[j] = null;
                            }
                        }
                        // Handle remaining responses
                        protocol.notifyResponseHandlers(r);
                        final List<Conversation> conversations = new ArrayList<Conversation>(mails.size());
                        for (final MailMessage mail : mails) {
                            conversations.add(new Conversation(mail));
                        }
                        return conversations;
                    } catch (final MessagingException e) {
                        throw new ProtocolException(e.getMessage(), e);
                    } catch (final OXException e) {
                        throw new ProtocolException(e.getMessage(), e);
                    }
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return Collections.<Conversation> emptyList();
                    }
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        response.toString() + " (" + imapFolder.getStore().toString() + ")"));
                } else if (response.isNO()) {
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        response.toString() + " (" + imapFolder.getStore().toString() + ")"));
                } else {
                    protocol.handleResult(response);
                }
                return Collections.<Conversation> emptyList();
            }
        }));
    }

    /**
     * Folds specified conversations.
     * 
     * @param toFold The conversations to fold
     * @return The folded conversations
     */
    public static List<Conversation> fold(final List<Conversation> toFold) {
        while (true) {
            Conversation[] pair = null;
            for (final Iterator<Conversation> it1 = toFold.iterator(); null == pair && it1.hasNext();) {
                final Conversation conversation = it1.next();
                for (final Conversation other : toFold) {
                    if (conversation != other) {
                        if (conversation.references(other) || conversation.isReferencedBy(other)) {
                            it1.remove();
                            pair = new Conversation[] { conversation, other};
                            break;
                        }
                    }
                }
            }
            if (null == pair) {
                // No further pair found
                return toFold;
            }
            final Conversation join = new Conversation(pair[0]).join(pair[1]);
            toFold.add(0, join);
        }
    }

    /** Checks for an empty string */
    static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
