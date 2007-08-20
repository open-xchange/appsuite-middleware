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

package com.openexchange.tools.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UUEncodeMultiPart Find possible UUEncode attachments in "normal" text (like
 * Outlook do) and converts them to UUEncodePart Objects.
 * 
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public class UUEncodedMultiPart {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UUEncodedMultiPart.class);

	private final List<UUEncodedPart> uuencodeParts;

	private StringBuilder text;

	private int count = -1;

	/**
	 * Constructs a UUEncodeMultiPart object.
	 */
	public UUEncodedMultiPart() {
		uuencodeParts = new ArrayList<UUEncodedPart>();
	}

	/**
	 * Constructs a UUEncodeMultiPart object.
	 */
	public UUEncodedMultiPart(final String content) {
		this();
		setContent(content);
	}

	/**
	 * A convenience method for setting this part's content.
	 * 
	 * @param content
	 *            Set the content of this UUEncodeMultiPart.
	 */
	private final void setContent(final String content) {
		findUUEncodedAttachmentCount(content);
		count = uuencodeParts.size();
		// now we should seperate normal text from attachments
		if (count >= 1) {
			final UUEncodedPart uuencodedPart = uuencodeParts.get(0);
			if (uuencodedPart.getIndexStart() != -1) {
				text = new StringBuilder(content.substring(0, uuencodedPart.getIndexStart()));
			}
		}
	}

	/**
	 * @return <code>true</code> if content is uuencoded, <code>false</code>
	 *         otherwise
	 */
	public boolean isUUEncoded() {
		return (count >= 1);
	}

	private static final Pattern PAT_UUENCODED = Pattern.compile(
			"(^begin |\r?\nbegin )([0-7]{3} )(\\S+\r?\n)(.+?)(\r?\n`\r?\nend)", Pattern.DOTALL);

	/**
	 * Try to find attachments recursive. Must containing the "begin" and "end"
	 * parameter, and specified tokens as well. Usualy looks like:
	 * 
	 * <pre>
	 * begin 600 filename.doc
	 * ...many data...
	 * `
	 * end
	 * </pre>
	 */
	private final void findUUEncodedAttachmentCount(final String sBodyPart) {
		final Matcher m = PAT_UUENCODED.matcher(sBodyPart);
		while (m.find()) {
			try {
				final int skip = examineBeginToken(m.group(1));
				uuencodeParts.add(new UUEncodedPart(m.start(1) + skip, m.start(5), m.group().substring(skip),
						cleanAtom(m.group(3))));
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
				break;
			}
		}
	}

	private static final int examineBeginToken(final String beginToken) {
		int count = 0;
		char c = beginToken.charAt(count);
		while (Character.isWhitespace(c)) {
			c = beginToken.charAt(++count);
		}
		return count;
	}

	private static final String cleanAtom(final String atom) {
		return atom.replaceAll("\r?\n", "");
	}

	/**
	 * Return the "cleaned" text, without the content of the uuencoded
	 * attachments
	 * 
	 * @return The "cleaned" text
	 */
	public String getCleanText() {
		return text.toString();
	}

	/**
	 * Return the number of enclosed Part objects.
	 * 
	 * @return number of parts
	 */
	public int getCount() {
		return (count);
	}

	/**
	 * Get the specified Part. Parts are numbered starting at 0.
	 * 
	 * @param index -
	 *            the index of the desired Part
	 * @return the Part
	 */
	public UUEncodedPart getBodyPart(final int index) {
		return (uuencodeParts.get(index));
	}

	/**
	 * Remove the part at specified location (starting from 0). Shifts all the
	 * parts after the removed part down one.
	 * 
	 * @param index -
	 *            Index of the part to remove
	 */
	public void removeBodyPart(final int index) {
		uuencodeParts.remove(index);
	}

}
