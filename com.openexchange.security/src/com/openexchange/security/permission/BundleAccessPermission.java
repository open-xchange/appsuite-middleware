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

package com.openexchange.security.permission;

import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.regex.Pattern;

/**
 * {@link BundleAccessPermission}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class BundleAccessPermission extends BasicPermission {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -7381745640062943101L;

	/**
	 * Initializes a new {@link BundleAccessPermission}
	 *
	 * @param bundleSymbolicName
	 *            The bundle symbolic name
	 * @throws NullPointerException
	 *             if <code>bundleSymbolicName</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>bundleSymbolicName</code> is empty.
	 */
	public BundleAccessPermission(final String bundleSymbolicName) {
		super(bundleSymbolicName);
	}

	@Override
	public boolean implies(final Permission permission) {
		if ((permission == null) || (permission.getClass() != getClass())) {
			return false;
		}
		return Pattern.matches(wildcardToRegex(getName()), permission.getName());
	}

	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != getClass())) {
			return false;
		}
		final BundleAccessPermission bp = (BundleAccessPermission) obj;
		return getName().equals(bp.getName());
	}

	@Override
	public PermissionCollection newPermissionCollection() {
		return new BundleAccessPermissionCollection();
	}

	/**
	 * Converts specified wildcard string to a regular expression
	 *
	 * @param wildcard
	 *            The wildcard string to convert
	 * @return An appropriate regular expression ready for being used in a
	 *         {@link Pattern pattern}
	 */
	private static String wildcardToRegex(final String wildcard) {
		final StringBuilder s = new StringBuilder(wildcard.length());
		s.append('^');
		final int len = wildcard.length();
		for (int i = 0; i < len; i++) {
			final char c = wildcard.charAt(i);
			if (c == '*') {
				s.append(".*");
			} else if (c == '?') {
				s.append('.');
			} else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{'
					|| c == '}' || c == '|' || c == '\\') {
				/*
				 * Escape special regular expression characters
				 */
				s.append('\\');
				s.append(c);
			} else {
				s.append(c);
			}
		}
		s.append('$');
		return (s.toString());
	}

}
