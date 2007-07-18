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

package com.openexchange.imap;

import java.util.ArrayList;
import java.util.List;

/**
 * ThreadParser
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadParser {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ThreadParser.class);

	private final List<TreeNode> threads;

	public ThreadParser() {
		threads = new ArrayList<TreeNode>();
	}

	public void parse(final String threadList) throws Exception {
		parse(threadList, threads);
	}

	/**
	 * @param threadList
	 * @param threads
	 */
	private void parse(final String threadList, final List<TreeNode> recthreads) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder("Start parse: ").append(threadList).toString());
		}
		if (threadList.charAt(0) >= '0' && threadList.charAt(0) <= '9') {
			// Now in a thread the thread starts normally with a number.
			final int message = getMessageID(threadList);
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder("Found message: ").append(message).toString());
			}
			final TreeNode actual = new TreeNode(message);
			recthreads.add(actual);
			// Now thread ends or answers are there.
			final int messageIDLength = String.valueOf(message).length();
			if (threadList.length() > messageIDLength && threadList.charAt(messageIDLength) == ' ') {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Parsing child threads.");
				}
				final List<TreeNode> childThreads = new ArrayList<TreeNode>();
				parse(threadList.substring(messageIDLength + 1), childThreads);
				actual.addChilds(childThreads);
			} else if (threadList.length() > messageIDLength) {
				throw new Exception("Found not expected character: " + threadList.charAt(messageIDLength));
			}
		} else if (threadList.charAt(0) == '(') {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Parsing list.");
			}
			// Parse list of threads.
			int pos = 0;
			do {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder("Position: ").append(pos).toString());
				}
				final int closingBracket = findMatchingBracket(threadList.substring(pos));
				if (closingBracket == -1) {
					throw new Exception("Closing bracket not found.");
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder("Closing bracket: ").append((pos + closingBracket)).toString());
				}
				final String subList = threadList.substring(pos + 1, pos + closingBracket);
				if (subList.charAt(0) == '(') {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Parsing childs of thread with no parent.");
					}
					final TreeNode emptyParent = new TreeNode(-1);
					recthreads.add(emptyParent);
					final List<TreeNode> childThreads = new ArrayList<TreeNode>();
					parse(subList, childThreads);
					emptyParent.addChilds(childThreads);
				} else {
					final List<TreeNode> childThreads = new ArrayList<TreeNode>();
					parse(subList, childThreads);
					recthreads.addAll(childThreads);
				}
				pos += closingBracket + 1;
			} while (pos < threadList.length());
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder("List: ").append(recthreads).toString());
			}
		} else {
			throw new Exception("Found not expected character: " + threadList.charAt(0));
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private int getMessageID(final String threadList) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder("Parsing messageID: ").append(threadList).toString());
		}
		int pos = 0;
		while (pos < threadList.length()) {
			final char actual = threadList.charAt(pos);
			if (actual < '0' || actual > '9') {
				break;
			}
			pos++;
		}
		if (pos == 0) {
			return -1;
		}
		final int id = Integer.parseInt(threadList.substring(0, pos));
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder("Parsed number: ").append(id).toString());
		}
		return id;
	}

	/**
	 * @param threadList
	 * @return
	 */
	private int findMatchingBracket(final String threadList) {
		int openingBrackets = 0;
		int pos = 0;
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder("findAccordingBracket: ").append(threadList).toString());
		}
		do {
			final char actual = threadList.charAt(pos);
			if (actual == '(') {
				openingBrackets++;
			} else if (actual == ')') {
				openingBrackets--;
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder("Char: ").append(actual).append(" Pos ").append(pos).toString());
			}
			pos++;
		} while (openingBrackets > 0 && pos < threadList.length());
		pos--;
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder("Found: ").append(pos).toString());
		}
		return pos;
	}

	/**
	 * 
	 */
	public List getParsedList() {
		return threads;
	}

	public static List<TreeNode> pullUpFirst(final List threads) {
		final List<TreeNode> newthreads = new ArrayList<TreeNode>();
		final int size = threads.size();
		for (int i = 0; i < size; i++) {
			TreeNode actual = (TreeNode) threads.get(i);
			if (actual.msgNum == -1) {
				final List<TreeNode> childs = actual.getChilds();
				actual = childs.remove(0);
				newthreads.add(actual);
				actual.addChilds(childs);
			} else {
				newthreads.add(actual);
			}
		}
		return newthreads;
	}

}
