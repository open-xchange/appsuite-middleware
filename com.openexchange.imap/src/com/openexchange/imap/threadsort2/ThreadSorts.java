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

package com.openexchange.imap.threadsort2;

import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.java.Strings.isDigit;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.MessagingException;
import javax.mail.search.SearchException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPServerInfo;
import com.openexchange.imap.command.MailMessageFillerIMAPCommand;
import com.openexchange.imap.threader.references.Conversation;
import com.openexchange.imap.util.WrappingProtocolException;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailField;
import com.openexchange.mail.PreviewMode;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.utils.MimeStorageUtility;
import com.openexchange.mail.search.SearchTerm;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.SearchSequence;

/**
 * {@link ThreadSorts}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadSorts {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadSorts.class);

    /**
     * Initializes a new {@link ThreadSorts}.
     */
    private ThreadSorts() {
        super();
    }

    /**
     * Executes THREAD command with given arguments.
     *
     * @param imapFolder The IMAP folder on which THREAD command shall be executed
     * @param sortRange The THREAD command argument specifying the sort range; e.g. <code>&quot;ALL&quot;</code> or <code>&quot;12,13,14,24&quot;</code>
     * @param uid <code>true</code> to perform a "UID THREAD" command; other wise <code>false</code>
     * @return The thread-sort string.
     * @throws MessagingException If a messaging error occurs
     */
    public static String getThreadResponse(IMAPFolder imapFolder, String sortRange, boolean uid, SearchTerm<?> searchTerm) throws MessagingException {
        final org.slf4j.Logger log = LOG;
        final Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                String cmd = uid ? "UID THREAD" : "THREAD";

                Argument args = new Argument().writeAtom("REFERENCES UTF-8");

                if (searchTerm != null) {
                    try {
                        args.append(new SearchSequence(p).generateSequence(searchTerm.getJavaMailSearchTerm(), "UTF-8"));
                    } catch (IOException ioex) {
                        // should never happen
                        throw new WrappingProtocolException("", new SearchException(ioex.toString()));
                    } catch (MessagingException e) {
                        throw new WrappingProtocolException("", e);
                    }
                }
                args.writeAtom(sortRange);
                final Response[] r;
                {
                    final long start = System.currentTimeMillis();
                    r = p.command(cmd, args);
                    final long dur = System.currentTimeMillis() - start;
                    log.debug("\"{}\" for \"{}\" ({}) took {}msec.", cmd, imapFolder.getFullName(), imapFolder.getStore(), Long.valueOf(dur));
                    mailInterfaceMonitor.addUseTime(dur);
                }
                final Response response = r[r.length - 1];
                String retval = null;
                if (response.isOK()) { // command successful
                    final String threadStr = "THREAD";
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals(threadStr)) {
                            retval = ir.toString();
                            r[i] = null;
                        }
                    }
                    p.notifyResponseHandlers(r);
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(cmd));
                    throw new ProtocolException(new StringBuilder("IMAP server does not support THREAD command: ").append(response.toString()).toString());
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(cmd));
                    throw new ProtocolException(new StringBuilder("IMAP server does not support THREAD command: ").append(response.toString()).toString());
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(cmd));
                    p.handleResult(response);
                }
                return retval;
            }
        });
        return (String) val;
    }

    /**
     * Parses the IMAP THREAD response to a conversation listing.
     * <p>
     * The {@link MailMessage} instances only have identifier and folder full name set.
     *
     * @param imapFolder The IMAP folder on which THREAD command shall be executed
     * @param sortRange The THREAD command argument specifying the sort range; e.g. <code>&quot;ALL&quot;</code> or <code>&quot;12,13,14,24&quot;</code>
     * @param isRev1 Flag for IMAPrev1
     * @param examineHasAttachmentUserFlags Whether to consider user flags to determine "has attachment(s)" flag
     * @param previewMode Whether target IMAP server supports any preview capability
     * @param serverInfo The IMAP server information
     * @param fields The optional fields to pre-fill
     * @return The conversation listing
     * @throws OXException If parsing fails
     * @throws MessagingException If acquiring the THREAD response fails
     */
    public static List<List<MailMessage>> getConversations(IMAPFolder imapFolder, String sortRange, boolean isRev1, boolean examineHasAttachmentUserFlags, PreviewMode previewMode, IMAPServerInfo serverInfo, SearchTerm<?> searchTerm, MailField... fields) throws OXException, MessagingException {
        if (null == fields || fields.length == 0 || (fields.length == 1 && MailField.RECEIVED_DATE.equals(fields[0]))) {
            return parseConversations(getThreadResponse(imapFolder, sortRange, true, searchTerm), imapFolder.getFullName());
        }

        // Its fetch profile...
        FetchProfile fp = new FetchProfile();
        for (MailField field : fields) {
            MimeStorageUtility.addFetchItem(fp, field, examineHasAttachmentUserFlags, previewMode);
        }
        return getConversations(imapFolder, sortRange, isRev1, fp, serverInfo, examineHasAttachmentUserFlags, previewMode, searchTerm);
    }

    /**
     * Parses the IMAP THREAD response to a conversation listing.
     * <p>
     * The {@link MailMessage} instances only have identifier and folder full name set.
     *
     * @param imapFolder The IMAP folder on which THREAD command shall be executed
     * @param sortRange The THREAD command argument specifying the sort range; e.g. <code>&quot;ALL&quot;</code> or <code>&quot;12,13,14,24&quot;</code>
     * @param isRev1 Flag for IMAPrev1
     * @param fetchProfile The optional fetch profile to pre-fill messages
     * @param serverInfo The IMAP server information
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @param previewMode Whether target IMAP server supports any preview capability
     * @return The conversation listing
     * @throws OXException If parsing fails
     * @throws MessagingException If acquiring the THREAD response fails
     */
    public static List<List<MailMessage>> getConversations(IMAPFolder imapFolder, String sortRange, boolean isRev1, FetchProfile fetchProfile, IMAPServerInfo serverInfo, boolean examineHasAttachmentUserFlags, PreviewMode previewMode, SearchTerm<?> searchTerm) throws OXException, MessagingException {
        if (null == fetchProfile) {
            return parseConversations(getThreadResponse(imapFolder, sortRange, true, searchTerm), imapFolder.getFullName());
        }

        List<List<MailMessage>> conversations = parseConversations(getThreadResponse(imapFolder, sortRange, true, searchTerm), imapFolder.getFullName());

        {
            // Turn conversations to a flat list
            List<MailMessage> msgs = new ArrayList<>(conversations.size() << 2);
            for (List<MailMessage> conversation : conversations) {
                for (MailMessage m : conversation) {
                    msgs.add(m);
                }
            }
            // Fill them
            new MailMessageFillerIMAPCommand(msgs, isRev1, fetchProfile, serverInfo, examineHasAttachmentUserFlags, previewMode, imapFolder).doCommand();
        }

        return conversations;
    }

    /**
     * Parses the IMAP THREAD response to a conversation listing.
     * <p>
     * The {@link MailMessage} instances only have identifier and folder full name set.
     *
     * @param imapFolder The IMAP folder on which THREAD command shall be executed
     * @param sortRange The THREAD command argument specifying the sort range; e.g. <code>&quot;ALL&quot;</code> or <code>&quot;12,13,14,24&quot;</code>
     * @param isRev1 Flag for IMAPrev1
     * @param fetchProfile The optional fetch profile to pre-fill messages
     * @param serverInfo The IMAP server information
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @param previewMode Whether target IMAP server supports any preview capability
     * @return The conversation listing
     * @throws OXException If parsing fails
     * @throws MessagingException If acquiring the THREAD response fails
     */
    public static List<Conversation> getConversationList(IMAPFolder imapFolder, String sortRange, boolean isRev1, FetchProfile fetchProfile, IMAPServerInfo serverInfo, boolean examineHasAttachmentUserFlags, PreviewMode previewMode, SearchTerm<?> searchTerm) throws OXException, MessagingException {
        List<List<MailMessage>> conversations = parseConversations(getThreadResponse(imapFolder, sortRange, true, searchTerm), imapFolder.getFullName());
        {
            // Turn conversations to a flat list
            List<MailMessage> msgs = new ArrayList<>(conversations.size() << 2);
            for (List<MailMessage> conversation : conversations) {
                for (MailMessage m : conversation) {
                    msgs.add(m);
                }
            }
            // Fill them
            new MailMessageFillerIMAPCommand(msgs, isRev1, fetchProfile, serverInfo, examineHasAttachmentUserFlags, previewMode, imapFolder).doCommand();
        }

        List<Conversation> retval = new ArrayList<>(conversations.size());
        for (List<MailMessage> conversationMessages : conversations) {
            retval.add(new Conversation(conversationMessages));
        }
        return retval;
    }

    /**
     * Parses the IMAP THREAD response to a conversation listing.
     * <p>
     * The {@link MailMessage} instances only have identifier and folder full name set.
     *
     * @param threadList The IMAP THREAD response
     * @param fullName The full name of the folder associated with the IMAP THREAD response
     * @return The conversation listing
     * @throws OXException If parsing fails
     */
    public static List<List<MailMessage>> parseConversations(String threadList, String fullName) throws OXException {
        List<List<MailMessage>> conversations = new LinkedList<>();

        int length = threadList.length();
        int off = threadList.indexOf('(');

        if (length == 0 || off == -1) {
            return conversations;
        }

        while (off < length) {
            char c = threadList.charAt(off);
            if (c != '(') {
                throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "Found unexpected character: " + c);
            }

            int end = findMatchingBracket(threadList, off + 1);
            end = end < 0 ? length : end + 1;

            // System.out.println(threadList.substring(off, end));

            @SuppressWarnings("unchecked") List<MailMessage> conversation = (List<MailMessage>) parseConversationList(threadList.substring(off, end), fullName);
            conversations.add(conversation);

            off = end;
        }

        return conversations;
    }

    private static int findMatchingBracket(String threadList, int off) {
        final int length = threadList.length();
        int openingBrackets = 1;
        int pos = off;
        do {
            final char c = threadList.charAt(pos++);
            if (c == '(') {
                openingBrackets++;
            } else if (c == ')') {
                openingBrackets--;
                if (openingBrackets <= 0) {
                    return pos - 1;
                }
            }
        } while (pos < length);
        return -1;
    }

    private static final Comparator<IDMailMessage> CONVERSATION_COMPARATOR = new Comparator<IDMailMessage>() {

        @Override
        public int compare(IDMailMessage msg1, IDMailMessage msg2) {
            return (int) (msg1.getUid() - msg2.getUid());
        }
    };

    private static List<? extends MailMessage> parseConversationList(String conversation, String fullName) {
        int length = conversation.length();
        int off = 0;

        StringBuilder digits = new StringBuilder(8);
        List<IDMailMessage> list = new LinkedList<>();

        while (off < length) {
            char c = conversation.charAt(off++);
            if (isDigit(c)) {
                digits.append(c);
            } else {
                if (digits.length() > 0) {
                    list.add(new IDMailMessage(digits.toString(), fullName));
                    digits.setLength(0);
                }
            }
        }

        if (digits.length() > 0) {
            list.add(new IDMailMessage(digits.toString(), fullName));
        }

        Collections.sort(list, CONVERSATION_COMPARATOR);

        return list;
    }

}
