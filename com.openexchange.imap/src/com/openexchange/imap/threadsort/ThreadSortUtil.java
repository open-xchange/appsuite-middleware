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

package com.openexchange.imap.threadsort;

import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadSortMailMessage;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.utils.MailMessageComparator;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * {@link ThreadSortUtil} - Utilities for thread-sort.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadSortUtil {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadSortUtil.class);

    /**
     * Prevent instantiation
     */
    private ThreadSortUtil() {
        super();
    }

    /**
     * Creates a newly allocated array of <code>int</code> filled with message's sequence number.
     *
     * @param threadResponse The thread response string; e.g.<br>
     *            <code>&quot;&#042;&nbsp;THREAD&nbsp;(1&nbsp;(2)(3)(4)(5))(6)(7)(8)((9)(10)(11)(12)(13)(14)(15)(16)(17)(18)(19))&quot;</code>
     * @return A newly allocated array of <code>int</code> filled with message's sequence number
     */
    public static List<MessageInfo> getSeqNumsFromThreadResponse(final String threadResponse) {
        if (null == threadResponse) {
            return Collections.emptyList();
        }
        //final char[] chars = threadResponse.toCharArray();
        final List<MessageInfo> list = new LinkedList<MessageInfo>();
        int start = threadResponse.indexOf('{', 0);
        if (start < 0) {
            return Collections.emptyList();
        }
        int end;
        do {
            end = threadResponse.indexOf('}', start + 2);
            if (end >= 0) {
                list.add(MessageInfo.valueOf(threadResponse, start, end - start));
                start = threadResponse.indexOf('{', end + 1);
            } else {
                start = threadResponse.indexOf('{', start + 2);
            }
        } while (start >= 0);
        return list;
    }

    /**
     * Gets the message identifiers from given thread list.
     *
     * @param threadList
     * @return The message identifiers
     */
    public static List<MessageInfo> fromThreadResponse(final List<ThreadSortNode> threadList) {
        if (null == threadList || threadList.isEmpty()) {
            return Collections.emptyList();
        }
        final List<MessageInfo> list = new LinkedList<MessageInfo>();
        recThreadResponse(threadList, list);
        return list;
    }

    private static void recThreadResponse(final List<ThreadSortNode> threadList, final List<MessageInfo> list) {
        for (final ThreadSortNode threadSortNode : threadList) {
            list.add(threadSortNode.msgInfo);
            final List<ThreadSortNode> childs = threadSortNode.getChilds();
            if (null != childs) {
                recThreadResponse(childs, list);
            }
        }
    }

    /**
     * Extracts sequence numbers from specified thread list
     *
     * @param threadList The thread list
     * @return The extracted sequence numbers
     */
    public static TIntList extractSeqNumsAsList(final List<ThreadSortNode> threadList) {
        if (null == threadList || threadList.isEmpty()) {
            return new TIntArrayList(0);
        }
        final int initialCapacity = threadList.size() << 1;
        final TIntList l = new TIntArrayList(initialCapacity);
        recSeqNumsAsList(threadList, l, initialCapacity);
        return l;
    }

    private static void recSeqNumsAsList(final List<ThreadSortNode> threadList, final TIntList l, final int initialCapacity) {
        for (final ThreadSortNode threadSortNode : threadList) {
            l.add(threadSortNode.msgInfo.getMessageNumber());
            final List<ThreadSortNode> childs = threadSortNode.getChilds();
            if (null != childs) {
                recSeqNumsAsList(childs, l, initialCapacity);
            }
        }
    }

    /**
     * Extracts sequence numbers from specified thread list
     *
     * @param threadList The thread list
     * @return The extracted sequence numbers; &lt;full-name&gt; &lt;=&gt; &lt;sequence-numbers&gt;
     */
    public static Map<String, TIntList> extractSeqNumsAsMap(final List<ThreadSortNode> threadList) {
        if (null == threadList || threadList.isEmpty()) {
            return Collections.emptyMap();
        }
        final int initialCapacity = threadList.size() << 1;
        final Map<String, TIntList> m = new HashMap<String, TIntList>(initialCapacity);
        recSeqNumsAsMap(threadList, m, initialCapacity);
        return m;
    }

    private static void recSeqNumsAsMap(final List<ThreadSortNode> threadList, final Map<String, TIntList> m, final int initialCapacity) {
        for (final ThreadSortNode threadSortNode : threadList) {
            final MessageInfo messageId = threadSortNode.msgInfo;
            final String fn = messageId.getFullName();
            TIntList list = m.get(fn);
            if (null == list) {
                list = new TIntArrayList(initialCapacity);
                m.put(fn, list);
            }
            list.add(messageId.getMessageNumber());
            final List<ThreadSortNode> childs = threadSortNode.getChilds();
            if (null != childs) {
                recSeqNumsAsMap(childs, m, initialCapacity);
            }
        }
    }

    private static final char DIGIT_START = '\u0030';

    private static final char DIGIT_END = '\u0039';

    /**
     * Determines if the specified character is a ISO-LATIN-1 digit.
     * <p>
     * '\u0030' through '\u0039', ISO-LATIN-1 digits ('0' through '9');
     *
     * @param c The character to check for a digit
     * @return <code>true</code> if character is a ISO-LATIN-1 digit; otherwise <code>false</code>
     */
    private static boolean isDigit(final char c) {
        return c >= DIGIT_START && c <= DIGIT_END;
    }

    // private static final Pattern PATTERN_THREAD_RESP = Pattern.compile("[0-9]+");

    /**
     * Creates a newly allocated array of <code>javax.mail.Message</code> objects only filled with message's sequence number.
     *
     * @return An array of <code>javax.mail.Message</code> objects only filled with message's sequence number.
     */
    public static ExtendedMimeMessage[] getMessagesFromThreadResponse(final String folderFullname, final String threadResponse) {
        final List<ExtendedMimeMessage> tmp = new ArrayList<ExtendedMimeMessage>();
        final StringBuilder sb = new StringBuilder(8);
        final int length = threadResponse.length();
        int i = 0;
        while (i < length) {
            char c = threadResponse.charAt(i++);
            while (Character.isDigit(c)) {
                sb.append(c);
                c = threadResponse.charAt(i++);
            }
            if (sb.length() > 0) {
                tmp.add(new ExtendedMimeMessage(folderFullname, Integer.parseInt(sb.toString())));
                sb.setLength(0);
            }
        }
        return tmp.toArray(new ExtendedMimeMessage[tmp.size()]);
        /*-
         * Formerly:
         *

        final Pattern PATTERN_THREAD_RESP = Pattern.compile("[0-9]+");
        final Matcher m = PATTERN_THREAD_RESP.matcher(threadResponse);
        if (m.find()) {
            final List<ExtendedMimeMessage> tmp = new ArrayList<ExtendedMimeMessage>();
            do {
                tmp.add(new ExtendedMimeMessage(folderFullname, separator, Integer.parseInt(m.group())));
            } while (m.find());
            return tmp.toArray(new ExtendedMimeMessage[tmp.size()]);
        }
        return null;
         */
    }

    /**
     * Parses specified thread-sort string.
     *
     * @return Parsed thread-sort string in a structured data type
     */
    public static List<ThreadSortNode> parseThreadResponse(final String threadResponse) throws OXException {
        /*
         * Now parse the odd THREAD response string.
         */
        final int openPos = threadResponse.indexOf('(');
        final int endPos = threadResponse.lastIndexOf(')');
        if ((openPos < 0) || (endPos < 0)) {
            return null;
        }
        final ThreadSortParser tp = new ThreadSortParser();
        tp.parse(threadResponse.substring(openPos, endPos + 1));
        return ThreadSortParser.pullUpFirst(tp.getParsedList());
    }

    /**
     * Executes THREAD command with given arguments.
     *
     * @param imapFolder The IMAP folder on which THREAD command shall be executed
     * @param sortRange The THREAD command argument specifying the sort range; e.g. <code>&quot;ALL&quot;</code> or
     *            <code>&quot;12,13,14,24&quot;</code>
     * @return The thread-sort string.
     * @throws MessagingException If a messaging error occurs
     */
    public static String getThreadResponse(final IMAPFolder imapFolder, final String sortRange) throws MessagingException {
        final org.slf4j.Logger log = LOG;
        final Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String command = new StringBuilder("THREAD REFERENCES UTF-8 ").append(sortRange).toString();
                final Response[] r;
                {
                    final long start = System.currentTimeMillis();
                    r = p.command(command, null);
                    final long dur = System.currentTimeMillis() - start;
                    log.info("\"{}\" for \"{}\" ({}) took {}msec.", command, imapFolder.getFullName(), imapFolder.getStore(), Long.valueOf(dur));
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
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new ProtocolException(new StringBuilder("IMAP server does not support THREAD command: ").append(
                        response.toString()).toString());
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new ProtocolException(new StringBuilder("IMAP server does not support THREAD command: ").append(
                        response.toString()).toString());
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return toUnifiedThreadResponse(retval);
            }
        });
        return (String) val;
    }

    private static final Pattern PATTERN_NUM = Pattern.compile("[0-9]+");

    static String toUnifiedThreadResponse(final String resp) {
        final Matcher matcher = PATTERN_NUM.matcher(resp);
        final StringBuffer sb = new StringBuffer(resp.length() << 1);
        final StringBuilder tmp = new StringBuilder(8);
        while (matcher.find()) {
            if (tmp.length() > 0) {
                tmp.setLength(0);
            }
            matcher.appendReplacement(sb, tmp.append('{').append(matcher.group()).append('}').toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Outputs specified structured list to given string builder.
     *
     * @param structuredList The structured list
     * @param sb The string builder to output to
     */
    public static void outputList(final List<ThreadSortMailMessage> structuredList, final StringBuilder sb) {
        outputList(structuredList, "", sb);
    }

    private static void outputList(final List<ThreadSortMailMessage> structuredList, final String prefix, final StringBuilder sb) {
        if (null == structuredList || structuredList.isEmpty()) {
            return;
        }
        for (final ThreadSortMailMessage threadSortMailMessage : structuredList) {
            sb.append(prefix).append(threadSortMailMessage.getMailId()).append(" (").append(threadSortMailMessage.getThreadLevel()).append(
                ')').append('\n');
            final List<ThreadSortMailMessage> structuredSubList = threadSortMailMessage.getChildMessages();
            outputList(structuredSubList, prefix + "  ", sb);
        }
    }

    /**
     * Converts specified structured list to a flat list.
     *
     * @param structuredList The structured list
     * @param flatList The flat list to fill
     */
    public static void toFlatList(final List<ThreadSortMailMessage> structuredList, final List<MailMessage> flatList) {
        if (null == structuredList || structuredList.isEmpty()) {
            return;
        }
        for (final ThreadSortMailMessage tsmm : structuredList) {
            flatList.add(tsmm.getOriginalMessage());
            final List<ThreadSortMailMessage> children = tsmm.getChildMessages();
            toFlatList(children, flatList);
        }
    }

    /**
     * Converts specified structured list to simplified structure.
     *
     * @param structuredList The structured list to convert
     * @param comparator The comparator to use to sort child messages
     */
    public static List<List<MailMessage>> toSimplifiedStructure(final List<ThreadSortMailMessage> structuredList, final MailMessageComparator comparator) {
        final List<List<MailMessage>> retval = new ArrayList<List<MailMessage>>(structuredList.size());
        for (final ThreadSortMailMessage root : structuredList) {
            // Create flat list
            final LinkedList<MailMessage> flatList = new LinkedList<MailMessage>();
            flatList.add(root.getOriginalMessage());
            toFlatList0(root.getChildMessages(), flatList);
            // Sort list
            Collections.sort(flatList, comparator);
            retval.add(flatList);
        }
        return retval;
    }

    private static void toFlatList0(final List<ThreadSortMailMessage> structuredList, final List<MailMessage> flatList) {
        if (null == structuredList || structuredList.isEmpty()) {
            return;
        }
        for (final ThreadSortMailMessage tsmm : structuredList) {
            // Add to list
            flatList.add(tsmm.getOriginalMessage());
            // Recursive invocation
            toFlatList0(tsmm.getChildMessages(), flatList);
        }
    }

    /**
     * Generates a structured list from specified mails.
     *
     * @param threadList The thread list
     * @param map The map providing mails by sequence number
     * @return A structured list reflecting thread-order structure
     */
    public static List<ThreadSortMailMessage> toThreadSortStructure(final List<ThreadSortNode> threadList, final Map<MessageInfo, MailMessage> map) {
        final List<ThreadSortMailMessage> list = new ArrayList<ThreadSortMailMessage>(threadList.size());
        for (final ThreadSortNode node : threadList) {
            final MailMessage rootMail = map.get(node.msgInfo);
            if (null != rootMail) {
                rootMail.setThreadLevel(0);
                final ThreadSortMailMessage tsmm = new ThreadSortMailMessage(rootMail);
                list.add(tsmm);
                final List<ThreadSortNode> subnodes = node.getChilds();
                if (null != subnodes && !subnodes.isEmpty()) {
                    processSubnodes(subnodes, 1, tsmm, map);
                }
            }
        }
        return list;
    }

    private static void processSubnodes(final List<ThreadSortNode> nodes, final int level, final ThreadSortMailMessage parent, final Map<MessageInfo, MailMessage> map) {
        for (final ThreadSortNode node : nodes) {
            final ThreadSortMailMessage tsmm = tsmmFor(map.get(node.msgInfo), level);
            if (null != tsmm) {
                parent.addChildMessage(tsmm);

                final List<ThreadSortNode> subnodes = node.getChilds();
                if (null != subnodes && !subnodes.isEmpty()) {
                    processSubnodes(subnodes, level + 1, tsmm, map);
                }
            }
        }
    }

    private static ThreadSortMailMessage tsmmFor(final MailMessage mail, final int level) {
        if (null == mail) {
            return null;
        }
        mail.setThreadLevel(level);
        return new ThreadSortMailMessage(mail);
    }

    /**
     * Generates a structured list from specified mails.
     *
     * @param mails The mails with thread level applied
     * @return A structured list reflecting thread-order structure
     */
    public static List<ThreadSortMailMessage> toThreadSortStructure(final MailMessage[] mails) {
        final List<ThreadSortMailMessage> list = new ArrayList<ThreadSortMailMessage>(mails.length);
        int i = 0;
        while (i < mails.length) {
            final MailMessage mail = mails[i];
            if (0 == mail.getThreadLevel()) {
                final ThreadSortMailMessage tsmm = new ThreadSortMailMessage(mail);
                list.add(tsmm);
                i++;
                final List<ThreadSortMailMessage> sublist = new ArrayList<ThreadSortMailMessage>();
                i = gatherChildren(mails, i, 1, sublist);
                tsmm.addChildMessages(sublist);
            }
        }
        return list;
    }

    private static int gatherChildren(final MailMessage[] mails, final int index, final int level, final List<ThreadSortMailMessage> newList) {
        boolean b = true;
        int i = index;
        while (b && i < mails.length) {
            final MailMessage mail = mails[i];
            final int mailLevel = mail.getThreadLevel();
            if (mailLevel > level) {
                if (mailLevel != level + 1) {
                    LOG.warn("Unexpected thread level! Expected={}, Actual={}", (level + 1), mailLevel);
                }
                final ThreadSortMailMessage parent = newList.get(newList.size() - 1);
                final List<ThreadSortMailMessage> sublist = new ArrayList<ThreadSortMailMessage>();
                i = gatherChildren(mails, i, mailLevel, sublist);
                parent.addChildMessages(sublist);
            } else if (mailLevel == level) {
                newList.add(new ThreadSortMailMessage(mail));
                i++;
            } else {
                // Mail's thread level is lower than given one
                b = false;
            }
        }
        return i;
    }
}
