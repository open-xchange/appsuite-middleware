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

package com.openexchange.imap.user2acl;

public enum IMAPServer {
	/**
	 * Courier: <code>com.openexchange.imap.user2acl.CourierUser2ACL</code>
	 */
	COURIER("Courier", "com.openexchange.imap.user2acl.CourierUser2ACL"),
	/**
	 * Cyrus: <code>com.openexchange.imap.user2acl.CyrusUser2ACL</code>
	 */
	CYRUS("Cyrus", "com.openexchange.imap.user2acl.CyrusUser2ACL");

	private final String impl;

	private final String name;

	private IMAPServer(final String name, final String impl) {
		this.name = name;
		this.impl = impl;
	}

	/**
	 * Gets the class name of {@link User2ACL} implementation
	 * 
	 * @return The class name of {@link User2ACL} implementation
	 */
	public String getImpl() {
		return impl;
	}

	/**
	 * Gets the IMAP server's alias name
	 * 
	 * @return The IMAP server's alias name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the class name of {@link User2ACL} implementation that corresponds
	 * to specified name.
	 * 
	 * @param name
	 *            The IMAP server name
	 * @return The class name of {@link User2ACL} implementation or
	 *         <code>null</code> if none matches.
	 */
	public static final String getIMAPServerImpl(final String name) {
		final IMAPServer[] imapServers = IMAPServer.values();
		for (int i = 0; i < imapServers.length; i++) {
			if (imapServers[i].getName().equalsIgnoreCase(name)) {
				return imapServers[i].getImpl();
			}
		}
		return null;
	}
}