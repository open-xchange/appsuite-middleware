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

package com.openexchange.imap.threadsort;

import static com.openexchange.java.Autoboxing.I;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadSortParser.class);

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
    void parse(String threadList) throws OXException {
        parse(threadList, threads);
    }

    private void parse(String threadList, List<ThreadSortNode> recthreads) throws OXException {
        LOG.debug("Start parse: {}", threadList);
        final int length = threadList.length();
        if (threadList.charAt(0) == '{') {
            // Now in a thread the thread starts normally with a number.
            final MessageInfo message = getMessageID(threadList);
                LOG.debug("Found message: {}", message);
            final ThreadSortNode actual = new ThreadSortNode(message, -1L);
            recthreads.add(actual);
            // Now thread ends or answers are there.
            final int messageIDLength = message.getSlen();
            if ((length > messageIDLength) && (threadList.charAt(messageIDLength) == ' ')) {
                    LOG.debug("Parsing child threads.");
                final List<ThreadSortNode> childThreads = new ArrayList<ThreadSortNode>();
                parse(threadList.substring(messageIDLength + 1), childThreads);
                actual.addChildren(childThreads);
            } else if (length > messageIDLength) {
                throw IMAPException.create(
                    IMAPException.Code.THREAD_SORT_PARSING_ERROR,
                    "Found unexpected character: " + threadList.charAt(messageIDLength));
            }
        } else if (threadList.charAt(0) == '(') {
                LOG.debug("Parsing list.");
            // Parse list of threads.
            int pos = 0;
            do {
                LOG.debug("Position: {}", I(pos));
                final int closingBracket = findMatchingBracket(threadList.substring(pos));
                if (closingBracket == -1) {
                    throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "Closing parenthesis not found.");
                }
                    LOG.debug("Closing bracket: {}{}", I(pos), I(closingBracket));
                final String subList = threadList.substring(pos + 1, pos + closingBracket);
                if (subList.charAt(0) == '(') {
                        LOG.debug("Parsing childs of thread with no parent.");
                    final ThreadSortNode emptyParent = new ThreadSortNode(MessageInfo.DUMMY, -1L);
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
                LOG.debug("List: {}", recthreads);
        } else {
            throw IMAPException.create(IMAPException.Code.THREAD_SORT_PARSING_ERROR, "Found unexpected character: " + threadList.charAt(0));
        }
    }

    private MessageInfo getMessageID(String threadList) {
        return MessageInfo.valueOf(threadList, 0, threadList.indexOf('}') + 1);
    }

    private int findMatchingBracket(String threadList) {
        int openingBrackets = 0;
        int pos = 0;
        do {
            final char actual = threadList.charAt(pos);
            if (actual == '(') {
                openingBrackets++;
            } else if (actual == ')') {
                openingBrackets--;
            }
            pos++;
        } while ((openingBrackets > 0) && (pos < threadList.length()));
        pos--;
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
    static List<ThreadSortNode> pullUpFirst(List<ThreadSortNode> threads) {
        final List<ThreadSortNode> newthreads = new LinkedList<ThreadSortNode>();
        final int size = threads.size();
        for (int i = 0; i < size; i++) {
            ThreadSortNode actual = threads.get(i);
            if (MessageInfo.DUMMY == actual.msgInfo) {
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
