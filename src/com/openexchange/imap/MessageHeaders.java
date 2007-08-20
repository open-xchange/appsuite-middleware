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

/**
 * MessageHeaders
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class MessageHeaders {

	/**
	 * Prevent instantiation
	 */
	private MessageHeaders() {
		super();
	}
	
	public static final String HDR_FROM = "From";

	public static final String HDR_TO = "To";

	public static final String HDR_CC = "Cc";

	public static final String HDR_BCC = "Bcc";

	public static final String HDR_DATE = "Date";

	public static final String HDR_REPLY_TO = "Reply-To";

	public static final String HDR_SUBJECT = "Subject";

	public static final String HDR_MESSAGE_ID = "Message-ID";

	public static final String HDR_IN_REPLY_TO = "In-Reply-To";

	public static final String HDR_REFERENCES = "References";

	public static final String HDR_X_PRIORITY = "X-Priority";

	public static final String HDR_DISP_NOT_TO = "Disposition-Notification-To";
	
	public static final String HDR_CONTENT_DISPOSITION = "Content-Disposition";

	public static final String HDR_CONTENT_TYPE = "Content-Type";

	public static final String HDR_MIME_VERSION = "MIME-Version";

	public static final String HDR_DISP_TO = HDR_DISP_NOT_TO;

	public static final String HDR_ORGANIZATION = "Organization";

	public static final String HDR_X_MAILER = "X-Mailer";

	public static final String HDR_ADDR_DELIM = ",";

	public static final String HDR_X_SPAM_FLAG = "X-Spam-Flag";
	
	public static final String HDR_CONTENT_ID = "Content-ID";

}
