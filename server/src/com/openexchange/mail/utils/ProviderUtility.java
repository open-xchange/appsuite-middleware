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

package com.openexchange.mail.utils;

/**
 * {@link ProviderUtility}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ProviderUtility {

	/**
	 * Initializes a new {@link ProviderUtility}
	 */
	private ProviderUtility() {
		super();
	}

	/**
	 * Extracts the protocol from specified server URL:<br>
	 * <code>(&lt;protocol&gt;://)?(&lt;host&gt;)(:&lt;port&gt;)?</code>
	 * 
	 * @param serverUrl
	 *            The server URL
	 * @param fallback
	 *            The fallback protocol if URL does not contain a protocol
	 * @return Extracted protocol or <code>fallback</code> parameter
	 */
	public static String extractProtocol(final String serverUrl, final String fallback) {
		final int len = serverUrl.length();
		char c = '\0';
		String protocol = null;
		/*
		 * Parse protocol out of URL
		 */
		for (int i = 0; (i < len) && ((c = serverUrl.charAt(i)) != '/'); i++) {
			if ((c == ':') && ((c = serverUrl.charAt(i + 1)) == '/') && ((c = serverUrl.charAt(i + 2)) == '/')) {
				final String s = serverUrl.substring(0, i).toLowerCase();
				if (isValidProtocol(s)) {
					protocol = s;
				}
				break;
			}
		}
		if (null == protocol) {
			return fallback;
		}
		return protocol;
	}

	private static boolean isValidProtocol(final String protocol) {
		final int len = protocol.length();
		if (len < 1) {
			return false;
		}
		char c = protocol.charAt(0);
		if (!Character.isLetter(c)) {
			return false;
		}
		for (int i = 1; i < len; i++) {
			c = protocol.charAt(i);
			if (!Character.isLetterOrDigit(c) && (c != '.') && (c != '+') && (c != '-')) {
				return false;
			}
		}
		return true;
	}
}
