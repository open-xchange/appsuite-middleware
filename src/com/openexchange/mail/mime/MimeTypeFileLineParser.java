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

package com.openexchange.mail.mime;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link MimeTypeFileLineParser} - Parses entries in MIME type files like:
 * 
 * <pre>
 * type=magnus-internal/cgi	exts=cgi,exe,bat
 * </pre>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MimeTypeFileLineParser {

	private static final Pattern PAT_VAL = Pattern.compile("(?:[^\"][\\p{ASCII}&&[^\\s\"]]*|\"[\\p{ASCII}&&[^\"]]+\")");

	private String type;

	private final Set<String> extensions;

	/**
	 * Initializes a new MIME type file's line parser
	 * 
	 * @param entry
	 *            The MIME type file entry; e.g.
	 *            <code>type=magnus-internal/cgi	exts=cgi,exe,bat</code>
	 */
	public MimeTypeFileLineParser(final String entry) {
		super();
		extensions = new TreeSet<String>();
		parse(entry);
	}

	private static final String STR_TYPE = "type=";

	private static final String STR_EXTS = "exts=";

	private void parse(final String entry) {
		int pos = -1;
		if ((pos = entry.toLowerCase().indexOf(STR_TYPE)) != -1) {
			final Matcher m = PAT_VAL.matcher(entry);
			if (m.find(pos + 5)) {
				type = m.group();
			}
		}
		if ((pos = entry.toLowerCase().indexOf(STR_EXTS)) != -1) {
			final Matcher m = PAT_VAL.matcher(entry);
			if (m.find(pos + 5)) {
				final String sExts = m.group();
				final String[] exts;
				if (sExts.charAt(0) == '"' && sExts.charAt(sExts.length() - 1) == '"') {
					exts = sExts.substring(1, sExts.length() - 1).split("[ \t\n\r\f]*,[ \t\n\r\f]*");
				} else {
					exts = m.group().split(" *, *");
				}
				extensions.addAll(Arrays.asList(exts));
			}
		}
	}

	/**
	 * Gets the extensions
	 * 
	 * @return the extensions
	 */
	public Set<String> getExtensions() {
		return Collections.unmodifiableSet(extensions);
	}

	/**
	 * Gets the type
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

}
