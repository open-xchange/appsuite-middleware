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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPException;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.tools.Collections.SmartIntArray;
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
    public static int[] getSeqNumsFromThreadResponse(final String threadResponse) {
        final char[] chars = threadResponse.toCharArray();
        final SmartIntArray sia = new SmartIntArray();
        final StringBuilder sb = new StringBuilder(8);
        int i = 0;
        while (i < chars.length) {
            char c = chars[i++];
            while (Character.isDigit(c)) {
                sb.append(c);
                c = chars[i++];
            }
            if (sb.length() > 0) {
                sia.append(Integer.parseInt(sb.toString()));
                sb.setLength(0);
            }
        }
        return sia.toArray();
    }

    // private static final Pattern PATTERN_THREAD_RESP = Pattern.compile("[0-9]+");

    /**
     * Creates a newly allocated array of <code>javax.mail.Message</code> objects only filled with message's sequence number.
     * 
     * @return An array of <code>javax.mail.Message</code> objects only filled with message's sequence number.
     */
    public static ExtendedMimeMessage[] getMessagesFromThreadResponse(final String folderFullname, final char separator, final String threadResponse) {
        final char[] chars = threadResponse.toCharArray();
        final List<ExtendedMimeMessage> tmp = new ArrayList<ExtendedMimeMessage>();
        final StringBuilder sb = new StringBuilder(8);
        int i = 0;
        while (i < chars.length) {
            char c = chars[i++];
            while (Character.isDigit(c)) {
                sb.append(c);
                c = chars[i++];
            }
            if (sb.length() > 0) {
                tmp.add(new ExtendedMimeMessage(folderFullname, separator, Integer.parseInt(sb.toString())));
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
    public static List<ThreadSortNode> parseThreadResponse(final String threadResponse) throws IMAPException {
        /*
         * Now parse the odd THREAD response string.
         */
        List<ThreadSortNode> pulledUp = null;
        if ((threadResponse.indexOf('(') != -1) && (threadResponse.indexOf(')') != -1)) {
            ThreadSortParser tp = new ThreadSortParser();
            tp.parse(threadResponse.substring(threadResponse.indexOf('('), threadResponse.lastIndexOf(')') + 1));
            pulledUp = ThreadSortParser.pullUpFirst(tp.getParsedList());
            tp = null;
        }
        return pulledUp;
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
        final Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Response[] r;
                {
                    final String commandStart = "THREAD REFERENCES UTF-8 ";
                    r = p.command(
                        new StringBuilder(commandStart.length() + sortRange.length()).append(commandStart).append(sortRange).toString(),
                        null);
                }
                final Response response = r[r.length - 1];
                String retval = null;
                try {
                    if (response.isOK()) { // command successful
                        final String threadStr = "THREAD";
                        for (int i = 0, len = r.length; i < len; i++) {
                            if (!(r[i] instanceof IMAPResponse)) {
                                continue;
                            }
                            final IMAPResponse ir = (IMAPResponse) r[i];
                            if (ir.keyEquals(threadStr)) {
                                retval = ir.toString();
                            }
                            r[i] = null;
                        }
                    } else {
                        throw new ProtocolException("IMAP server does not support THREAD command");
                    }
                } finally {
                    p.notifyResponseHandlers(r);
                    p.handleResult(response);
                }
                return retval;
            }
        });
        return (String) val;
    }

}
