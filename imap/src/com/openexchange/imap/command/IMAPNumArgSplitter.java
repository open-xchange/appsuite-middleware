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

package com.openexchange.imap.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.mail.Message;

/**
 * {@link IMAPNumArgSplitter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPNumArgSplitter {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPNumArgSplitter.class);

	private static interface Tokenizer {
		public String getNext(int index);
	}

	/**
	 * Prevent instantiation
	 */
	private IMAPNumArgSplitter() {
		super();
	}

	private static final int MAX_IMAP_COMMAND_LENGTH = 16300;

	/**
	 * Since an IMAP command MUST NOT exceed the maximum command length of the
	 * imap server, which is 16384 bytes, this method creates an appropriate
	 * array of command arguments which can then be used with an instance of
	 * <code>{@link AbstractIMAPCommand}</code>
	 * 
	 * @param arr -
	 *            <code>int</code> array of message sequence numbers
	 * @return an appropriate array of command arguments
	 */
	public static String[] split(final int[] arr) {
		return split(new Tokenizer() {
			public String getNext(final int index) {
				return String.valueOf(arr[index]);
			}
		}, arr.length);
	}

	/**
	 * Since an IMAP command MUST NOT exceed the maximum command length of the
	 * imap server, which is 16384 bytes, this method creates an appropriate
	 * array of command arguments which can then be used with an instance of
	 * <code>{@link AbstractIMAPCommand}</code>
	 * 
	 * @param arr -
	 *            <code>long</code> array of message UIDs
	 * @return an appropriate array of command arguments
	 */
	public static String[] split(final long[] arr) {
		return split(new Tokenizer() {
			public String getNext(final int index) {
				return String.valueOf(arr[index]);
			}
		}, arr.length);
	}

	/**
	 * Since an IMAP command MUST NOT exceed the maximum command length of the
	 * imap server, which is 16384 bytes, this method creates an appropriate
	 * array of command arguments which can then be used with an instance of
	 * <code>{@link AbstractIMAPCommand}</code>
	 * 
	 * @param arr -
	 *            <code>Message</code> array
	 * @return an appropriate array of command arguments
	 */
	public static String[] split(final Message[] arr) {
		return split(new Tokenizer() {
			public String getNext(final int index) {
				return String.valueOf(arr[index].getMessageNumber());
			}
		}, arr.length);
	}

	/**
	 * Given array of sequence numbers is first transformed into a valid IMAP
	 * command's number argument and then split into max. IMAP command length
	 * pieces
	 * 
	 * @param arr -
	 *            the array of sequence numbers
	 * @param keepOrder -
	 *            whether the values' ordering in array parameter
	 *            <code>arr</code> shall be kept or not; if ordering does not
	 *            care a more compact number argument for IMAP command is going
	 *            to be created by grouping sequential numbers e.g.
	 *            <code>1,2,3,4,5 -> 1:5</code>
	 * @return an appropriate array of command arguments
	 */
	public static String[] splitSeqNumArg(final int[] arr, final boolean keepOrder) {
		final List<Integer> l = new ArrayList<Integer>(arr.length);
		for (int i = 0; i < arr.length; i++) {
			l.add(Integer.valueOf(arr[i]));
		}
		if (!keepOrder) {
			Collections.sort(l);
		}
		return split(getNumArg(l), MAX_IMAP_COMMAND_LENGTH);
	}

	/**
	 * Given array of sequence numbers is first transformed into a valid IMAP
	 * command's number argument and then split into max. IMAP command length
	 * pieces
	 * 
	 * @param arr -
	 *            the array of sequence numbers
	 * @param keepOrder -
	 *            whether the values' ordering in array parameter
	 *            <code>arr</code> shall be kept or not; if ordering does not
	 *            care a more compact number argument for IMAP command is going
	 *            to be created by grouping sequential numbers e.g.
	 *            <code>1,2,3,4,5 -> 1:5</code>
	 * @return an appropriate array of command arguments
	 */
	public static String[] splitMessageArg(final Message[] arr, final boolean keepOrder) {
		final List<Integer> l = new ArrayList<Integer>(arr.length);
		for (int i = 0; i < arr.length; i++) {
			l.add(Integer.valueOf(arr[i].getMessageNumber()));
		}
		if (!keepOrder) {
			Collections.sort(l);
		}
		return split(getNumArg(l), MAX_IMAP_COMMAND_LENGTH);
	}

	/**
	 * Given array of sequence numbers is first transformed into a valid IMAP
	 * command's number argument and then split into max. IMAP command length
	 * pieces
	 * 
	 * @param arr -
	 *            the array of sequence numbers
	 * @param keepOrder -
	 *            whether the values' ordering in array parameter
	 *            <code>arr</code> shall be kept or not; if ordering does not
	 *            care a more compact number argument for IMAP command is going
	 *            to be created by grouping sequential numbers e.g.
	 *            <code>1,2,3,4,5 -> 1:5</code>
	 * @return an appropriate array of command arguments
	 */
	public static String[] splitUIDArg(final long[] arr, final boolean keepOrder) {
		final List<Long> l = new ArrayList<Long>(arr.length);
		for (int i = 0; i < arr.length; i++) {
			l.add(Long.valueOf(arr[i]));
		}
		if (!keepOrder) {
			Collections.sort(l);
		}
		return split(getNumArg(l), MAX_IMAP_COMMAND_LENGTH);
	}

	/**
	 * <p>
	 * Generates a number argument valid for IMAP commands expecting message's
	 * sequence numbers or UIDs. That is contiguous numbers may be abbreviated
	 * as a sequence representation e.g. <code>5:24</code> meaning all numbers
	 * beginning from 5 ending with 24. Non-contiguous numbers must be
	 * delimited using a comma.
	 * <p>
	 * <b>NOTE:</b> This routine does not take care if the resulting argument
	 * in addition to rest of IMAP command exceeds the max. length of 16384
	 * bytes
	 * <p>
	 * A resulting string can look like this:
	 * <code>10031:10523,10525:11020,11022:11027,11030:11047,11050:11051,11053,11055:11558</code>
	 * 
	 * @param l -
	 *            the list of numbers; either sequence numbers or UIDs
	 * @return the number argument
	 */
	public static String getNumArg(final List<? extends Number> l) {
		final int size = l.size();
		final Iterator<? extends Number> iter = l.iterator();
		long prev = iter.next().longValue();
		boolean contiguous = false;
		final StringBuilder sb = new StringBuilder(size * 4);
		sb.append(prev);
		for (int i = 1; i < size; i++) {
			final long current = iter.next().longValue();
			if (prev + 1 == current) {
				prev++;
				contiguous = true;
			} else if (contiguous) {
				sb.append(':').append(prev);
				sb.append(',');
				sb.append(current);
				prev = current;
				contiguous = false;
			} else {
				sb.append(',');
				sb.append(current);
				prev = current;
			}
		}
		if (contiguous) {
			sb.append(':').append(prev);
		}
		return sb.toString();
	}

	private static String[] split(final Tokenizer tokenizer, final int length) {
		final int initCap = (length / MAX_IMAP_COMMAND_LENGTH);
		final List<String> tmp = new ArrayList<String>(initCap == 0 ? 10 : initCap);
		final StringBuilder sb = new StringBuilder(MAX_IMAP_COMMAND_LENGTH);
		sb.append(tokenizer.getNext(0));
		for (int i = 1; i < length; i++) {
			final String sUid = tokenizer.getNext(i);
			if (sb.length() + sUid.length() + 1 > MAX_IMAP_COMMAND_LENGTH) {
				tmp.add(sb.toString());
				sb.setLength(0);
			} else {
				sb.append(',');
			}
			sb.append(sUid);
		}
		tmp.add(sb.toString());
		return tmp.toArray(new String[tmp.size()]);
	}

	private static String[] split(final String numArg, final int maxLen) {
		final int len = numArg.length();
		final int initCap = (len / maxLen);
		final List<String> tmp = new ArrayList<String>(initCap == 0 ? 2 : initCap);
		int offset = 0;
		while (offset < len) {
			int endPos = offset + maxLen;
			if (endPos < len) {
				char c = numArg.charAt(endPos);
				while (c != ',' && endPos > -1) {
					c = numArg.charAt(--endPos);
				}
			} else {
				endPos = len;
			}
			if (endPos <= offset) {
				final int p = numArg.indexOf(',', offset);
				if (LOG.isWarnEnabled()) {
					LOG.warn(new StringBuilder("Token does not fit into given max size of ").append(maxLen).append(
							" bytes: ").append(numArg.substring(offset, p)).toString());
				}
				offset = p + 1;
			} else {
				tmp.add(numArg.substring(offset, endPos));
				offset = endPos + 1;
			}
		}
		return tmp.toArray(new String[tmp.size()]);
	}

}
