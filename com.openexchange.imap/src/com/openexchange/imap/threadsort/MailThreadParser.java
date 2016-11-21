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
 *    trademarks of the OX Software GmbH. group of companies.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;
import com.openexchange.java.BoolReference;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailThread;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * {@link MailThreadParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailThreadParser {

    /** The mail representing a non-existing parent */
    private static final IDMailMessage NON_EXISTING_PARENT = new IDMailMessage();

    private static final MailThreadParser INSTANCE = new MailThreadParser();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MailThreadParser getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link MailThreadParser}.
     */
    private MailThreadParser() {
        super();
    }

    /**
     * Parses the specified unified THREAD=REFERENCES response.
     *
     * @param unifiedResult The unified THREAD=REFERENCES response
     * @param fullName The full name
     * @param numMsgs Approximate number of queried messages for sizing
     * @return The parse result
     * @throws OXException If parsing fails
     */
    public ParseResult parseUnifiedResponse(String unifiedResult, String fullName, int numMsgs) throws OXException {
        List<MailThread> mailThreads = new ArrayList<>(numMsgs);
        TLongObjectMap<MailMessage> messages = new TLongObjectHashMap<>(numMsgs);
        BoolReference hasNonExistingParent = new BoolReference(false);
        parseUnifiedResponse(unifiedResult, fullName, hasNonExistingParent, mailThreads, messages);
        if (hasNonExistingParent.getValue()) {
            mailThreads = pullUpFirst(mailThreads);
        }
        return new ParseResult(mailThreads, messages);
    }

    private void parseUnifiedResponse(String unifiedResult, String fullName, BoolReference hasNonExistingParent, List<MailThread> mailThreads, TLongObjectMap<MailMessage> messages) throws OXException {
        int length = unifiedResult.length();
        char c0 = unifiedResult.charAt(0);
        if (c0 == '{') {
            // Entering a thread
            int pos = unifiedResult.indexOf('}');
            IDMailMessage parent = new IDMailMessage(unifiedResult.substring(1, pos), fullName);
            messages.put(parent.getUid(), parent);
            MailThread current = new MailThread(parent);
            mailThreads.add(current);

            pos++;
            if (pos < length) {
                if (unifiedResult.charAt(pos) != ' ') {
                    throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "Unexpected character '" + unifiedResult.charAt(pos) + "' in thread list \"" + unifiedResult + "\"");
                }

                // Child messages available
                List<MailThread> children = new LinkedList<>();
                parseUnifiedResponse(unifiedResult.substring(pos + 1), fullName, hasNonExistingParent, children, messages);
                current.addChildren(children);
            }
        } else if (c0 == '(') {
            // Parse list of threads.
            int pos = 0;
            do {
                int closingParenthesis = findMatchingParenthesis(unifiedResult.substring(pos));
                if (closingParenthesis == -1) {
                    throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "No closing parenthesis at position " + pos + " in thread list \"" + unifiedResult + "\"");
                }

                String subList = unifiedResult.substring(pos + 1, pos + closingParenthesis);
                if (subList.charAt(0) == '(') {
                    // A thread without a parent; e.g. "((8)(9))"
                    hasNonExistingParent.setValue(true);
                    MailThread nonExistingParent = new MailThread(NON_EXISTING_PARENT);
                    mailThreads.add(nonExistingParent);
                    List<MailThread> children = new LinkedList<>();
                    parseUnifiedResponse(subList, fullName, hasNonExistingParent, children, messages);
                    nonExistingParent.addChildren(children);
                } else {
                    // A regular thread with a parent; e.g. "(6 (8)(9))"
                    List<MailThread> children = new LinkedList<>();
                    parseUnifiedResponse(subList, fullName, hasNonExistingParent, children, messages);
                    mailThreads.addAll(children);
                }
                pos += closingParenthesis + 1;
            } while (pos < length);
        } else {
            throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "Unexpected start character '" + unifiedResult.charAt(0) + "' in thread list \"" + unifiedResult + "\"");
        }
    }

    private int findMatchingParenthesis(String unifiedResult) {
        int length = unifiedResult.length();
        int openingParentheses = 0;
        int pos = 0;
        do {
            char actual = unifiedResult.charAt(pos);
            if (actual == '(') {
                openingParentheses++;
            } else if (actual == ')') {
                openingParentheses--;
            }
            pos++;
        } while ((openingParentheses > 0) && (pos < length));
        return pos - 1;
    }

    /**
     * Pulls-up first tree node from given tree nodes list.
     *
     * @param threads The tree nodes list
     * @return The tree nodes list with first tree node pulled-up
     */
    private static List<MailThread> pullUpFirst(final List<MailThread> threads) {
        int size = threads.size();
        List<MailThread> newthreads = new ArrayList<MailThread>(size);
        for (int i = 0; i < size; i++) {
            MailThread cur = threads.get(i);
            if (NON_EXISTING_PARENT == cur.getParent()) {
                List<MailThread> children = cur.getChildren();
                cur = children.remove(0);
                newthreads.add(cur);
                cur.addChildren(children);
            } else {
                newthreads.add(cur);
            }
        }
        return newthreads;
    }

    // ---------------------------------------------------------------------------------------------------------

    /** A parse result */
    public static final class ParseResult {

        private final List<MailThread> mailThreads;
        private final TLongObjectMap<MailMessage> messages;

        /**
         * Initializes a new {@link ParseResult}.
         */
        ParseResult(List<MailThread> mailThreads, TLongObjectMap<MailMessage> messages) {
            super();
            this.mailThreads = mailThreads;
            this.messages = messages;
        }

        /**
         * Gets the mail threads
         *
         * @return The mailThreads
         */
        public List<MailThread> getMailThreads() {
            return mailThreads;
        }

        /**
         * Gets the messages
         *
         * @return The messages
         */
        public TLongObjectMap<MailMessage> getMessages() {
            return messages;
        }
    }

}
