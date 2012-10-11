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

package com.openexchange.imap.threadsort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;

/**
 * {@link ThreadSortParser} - Parses an IMAP server's thread-sort string.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ThreadSortParser {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ThreadSortParser.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private final List<ThreadSortNode> threads;

    /**
     * Initializes a new {@link ThreadSortParser}.
     */
    ThreadSortParser() {
        threads = new LinkedList<ThreadSortNode>();
    }

    /**
     * Parses specified IMAP server's thread-sort string.
     *
     * @param threadList The thread-sort string.
     * @throws OXException If parsing thread-sort string fails.
     */
    void parse(final String threadList) throws OXException {
        parse(threadList, threads);
    }

    private void parse(final String threadList, final List<ThreadSortNode> recthreads) throws OXException {
        if (DEBUG) {
            LOG.debug(new StringBuilder("Start parse: ").append(threadList).toString());
        }
        final int length = threadList.length();
        if (threadList.charAt(0) == '{') {
            // Now in a thread the thread starts normally with a number.
            final MessageId message = getMessageID(threadList);
            if (DEBUG) {
                LOG.debug(new StringBuilder("Found message: ").append(message).toString());
            }
            final ThreadSortNode actual = new ThreadSortNode(message, -1L);
            recthreads.add(actual);
            // Now thread ends or answers are there.
            final int messageIDLength = message.getSlen();
            if ((length > messageIDLength) && (threadList.charAt(messageIDLength) == ' ')) {
                if (DEBUG) {
                    LOG.debug("Parsing child threads.");
                }
                final List<ThreadSortNode> childThreads = new ArrayList<ThreadSortNode>();
                parse(threadList.substring(messageIDLength + 1), childThreads);
                actual.addChildren(childThreads);
            } else if (length > messageIDLength) {
                throw IMAPException.create(
                    IMAPException.Code.THREAD_SORT_PARSING_ERROR,
                    "Found unexpected character: " + threadList.charAt(messageIDLength));
            }
        } else if (threadList.charAt(0) == '(') {
            if (DEBUG) {
                LOG.debug("Parsing list.");
            }
            // Parse list of threads.
            int pos = 0;
            do {
                if (DEBUG) {
                    LOG.debug(new StringBuilder("Position: ").append(pos).toString());
                }
                final int closingBracket = findMatchingBracket(threadList.substring(pos));
                if (closingBracket == -1) {
                    throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "Closing parenthesis not found.");
                }
                if (DEBUG) {
                    LOG.debug(new StringBuilder("Closing bracket: ").append((pos + closingBracket)).toString());
                }
                final String subList = threadList.substring(pos + 1, pos + closingBracket);
                if (subList.charAt(0) == '(') {
                    if (DEBUG) {
                        LOG.debug("Parsing childs of thread with no parent.");
                    }
                    final ThreadSortNode emptyParent = new ThreadSortNode(MessageId.DUMMY, -1L);
                    recthreads.add(emptyParent);
                    final List<ThreadSortNode> childThreads = new ArrayList<ThreadSortNode>();
                    parse(subList, childThreads);
                    emptyParent.addChildren(childThreads);
                } else {
                    final List<ThreadSortNode> childThreads = new ArrayList<ThreadSortNode>();
                    parse(subList, childThreads);
                    recthreads.addAll(childThreads);
                }
                pos += closingBracket + 1;
            } while (pos < length);
            if (DEBUG) {
                LOG.debug(new StringBuilder("List: ").append(recthreads).toString());
            }
        } else {
            throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "Found unexpected character: " + threadList.charAt(0));
        }
    }

    private MessageId getMessageID(final String threadList) {
        if (DEBUG) {
            LOG.debug(new StringBuilder("Parsing messageID: ").append(threadList).toString());
        }
        final MessageId messageId = MessageId.valueOf(threadList, 0, threadList.indexOf('}') + 1);
        if (DEBUG) {
            LOG.debug(new StringBuilder("Parsed number: ").append(messageId).toString());
        }
        return messageId;
    }

    private int findMatchingBracket(final String threadList) {
        int openingBrackets = 0;
        int pos = 0;
        if (DEBUG) {
            LOG.debug(new StringBuilder("findMatchingBracket: ").append(threadList).toString());
        }
        do {
            final char actual = threadList.charAt(pos);
            if (actual == '(') {
                openingBrackets++;
            } else if (actual == ')') {
                openingBrackets--;
            }
            if (DEBUG) {
                LOG.debug(new StringBuilder("Char: ").append(actual).append(" Pos ").append(pos).toString());
            }
            pos++;
        } while ((openingBrackets > 0) && (pos < threadList.length()));
        pos--;
        if (DEBUG) {
            LOG.debug(new StringBuilder("Found: ").append(pos).toString());
        }
        return pos;
    }

    /**
     * Get parsed tree nodes.
     */
    List<ThreadSortNode> getParsedList() {
        return threads;
    }

    /**
     * Pulls-up first tree node from given tree nodes list.
     *
     * @param threads The tree nodes list
     * @return The tree nodes list with first tree node pulled-up
     */
    static List<ThreadSortNode> pullUpFirst(final List<ThreadSortNode> threads) {
        final List<ThreadSortNode> newthreads = new LinkedList<ThreadSortNode>();
        final int size = threads.size();
        for (int i = 0; i < size; i++) {
            ThreadSortNode actual = threads.get(i);
            if (MessageId.DUMMY == actual.msgId) {
                final List<ThreadSortNode> childs = actual.getChilds();
                actual = childs.remove(0);
                newthreads.add(actual);
                actual.addChildren(childs);
            } else {
                newthreads.add(actual);
            }
        }
        return newthreads;
    }

}
