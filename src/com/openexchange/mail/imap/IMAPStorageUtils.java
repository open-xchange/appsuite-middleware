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

package com.openexchange.mail.imap;

/**
 * IMAPStorageUtils
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPStorageUtils {

	public static final int INDEX_DRAFTS = 0;

	public static final int INDEX_SENT = 1;

	public static final int INDEX_SPAM = 2;

	public static final int INDEX_TRASH = 3;

	public static final int INDEX_CONFIRMED_SPAM = 4;

	public static final int INDEX_CONFIRMED_HAM = 5;

	public static final int INDEX_INBOX = 6;

	public static final int MAIL_PARAM_HARD_DELETE = 1;

	public static final int UNLIMITED_QUOTA = -1;

	public static final int ORDER_ASC = 1;

	public static final int ORDER_DESC = 2;

	/**
	 * Prevent instantiation
	 */
	private IMAPStorageUtils() {
		super();
	}

	/**
	 * Virtual ID of mailbox's root folder
	 * 
	 * @value default
	 */
	public static final String DEFAULT_IMAP_FOLDER_ID = "default";

	public static String prepareMailFolderParam(final String folderStringArg) {
		if (folderStringArg == null) {
			return null;
		} else if (DEFAULT_IMAP_FOLDER_ID.equals(folderStringArg)) {
			return folderStringArg;
		} else if (folderStringArg.startsWith(DEFAULT_IMAP_FOLDER_ID)) {
			return folderStringArg.substring(8);
		}
		return folderStringArg;
	}

	public static String prepareFullname(final String fullname, final char sep) {
		if (DEFAULT_IMAP_FOLDER_ID.equals(fullname)) {
			return fullname;
		}
		return new StringBuilder(32).append(DEFAULT_IMAP_FOLDER_ID).append(sep).append(fullname).toString();
	}
}
