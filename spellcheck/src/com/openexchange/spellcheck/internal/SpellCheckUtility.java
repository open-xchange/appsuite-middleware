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

package com.openexchange.spellcheck.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.openexchange.spellcheck.SpellCheckException;

/**
 * {@link SpellCheckUtility} - Provides various utility methods belonging to
 * spell check.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpellCheckUtility {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SpellCheckUtility.class);

	/**
	 * Initializes a new {@link SpellCheckUtility}
	 */
	private SpellCheckUtility() {
		super();
	}

	/**
	 * Converts given HTML text into a {@link javax.swing.text.Document}
	 * 
	 * @param html
	 *            The HTML text
	 * @return The HTML document filled with given HTML text
	 * @throws IOException
	 *             On any I/O error
	 */
	public static Document html2Document(final String html) throws IOException {
		final Document doc = new HTMLDocument();
		try {
			new HTMLEditorKit().read(new StringReader(html), doc, 0);
		} catch (final BadLocationException e) {
			/*
			 * Cannot occur
			 */
			LOG.error(e.getLocalizedMessage(), e);
		}
		return doc;
	}

	private static final Pattern PAT_LOCALE = Pattern.compile("([a-z]{2})(?:_([A-Z]{2})(?:_([A-Z]{2}))?)?");

	/**
	 * Parses given locale string into an instance of {@link Locale}
	 * 
	 * @param localeStr
	 *            The locale string to parse
	 * @return The parsed instance of {@link Locale}
	 * @throws SpellCheckException
	 *             If locale string is invalid
	 */
	public static Locale parseLocaleString(final String localeStr) throws SpellCheckException {
		final Matcher m = PAT_LOCALE.matcher(localeStr);
		if (!m.matches()) {
			throw new SpellCheckException(SpellCheckException.Code.INVALID_LOCALE_STR, localeStr);
		}
		final String country = m.group(2);
		if (null == country) {
			return new Locale(m.group(1));
		}
		final String variant = m.group(3);
		if (null == variant) {
			return new Locale(m.group(1), country);
		}
		return new Locale(m.group(1), country, variant);
	}
}
