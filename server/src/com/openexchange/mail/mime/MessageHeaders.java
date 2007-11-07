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

/**
 * MessageHeaders
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MessageHeaders {

	/**
	 * Prevent instantiation
	 */
	private MessageHeaders() {
		super();
	}

	public static final String HDR_FROM = "From";

	public static final HeaderName FROM = HeaderName.valueOf(HDR_FROM);

	public static final String HDR_TO = "To";

	public static final HeaderName TO = HeaderName.valueOf(HDR_TO);

	public static final String HDR_CC = "Cc";

	public static final HeaderName CC = HeaderName.valueOf(HDR_CC);

	public static final String HDR_BCC = "Bcc";

	public static final HeaderName BCC = HeaderName.valueOf(HDR_BCC);

	public static final String HDR_DATE = "Date";

	public static final HeaderName DATE = HeaderName.valueOf(HDR_DATE);

	public static final String HDR_REPLY_TO = "Reply-To";

	public static final HeaderName REPLY_TO = HeaderName.valueOf(HDR_REPLY_TO);

	public static final String HDR_SUBJECT = "Subject";

	public static final HeaderName SUBJECT = HeaderName.valueOf(HDR_SUBJECT);

	public static final String HDR_MESSAGE_ID = "Message-ID";

	public static final HeaderName MESSAGE_ID = HeaderName.valueOf(HDR_MESSAGE_ID);

	public static final String HDR_IN_REPLY_TO = "In-Reply-To";

	public static final HeaderName IN_REPLY_TO = HeaderName.valueOf(HDR_IN_REPLY_TO);

	public static final String HDR_REFERENCES = "References";

	public static final HeaderName REFERENCES = HeaderName.valueOf(HDR_REFERENCES);

	public static final String HDR_X_PRIORITY = "X-Priority";

	public static final HeaderName X_PRIORITY = HeaderName.valueOf(HDR_X_PRIORITY);

	public static final String HDR_DISP_NOT_TO = "Disposition-Notification-To";

	public static final HeaderName DISP_NOT_TO = HeaderName.valueOf(HDR_DISP_NOT_TO);

	public static final String HDR_CONTENT_DISPOSITION = "Content-Disposition";

	public static final HeaderName CONTENT_DISPOSITION = HeaderName.valueOf(HDR_CONTENT_DISPOSITION);

	public static final String HDR_CONTENT_TYPE = "Content-Type";

	public static final HeaderName CONTENT_TYPE = HeaderName.valueOf(HDR_CONTENT_TYPE);

	public static final String HDR_MIME_VERSION = "MIME-Version";

	public static final HeaderName MIME_VERSION = HeaderName.valueOf(HDR_MIME_VERSION);

	public static final String HDR_DISP_TO = HDR_DISP_NOT_TO;

	public static final HeaderName DISP_TO = HeaderName.valueOf(HDR_DISP_TO);

	public static final String HDR_ORGANIZATION = "Organization";

	public static final HeaderName ORGANIZATION = HeaderName.valueOf(HDR_ORGANIZATION);

	public static final String HDR_X_MAILER = "X-Mailer";

	public static final HeaderName X_MAILER = HeaderName.valueOf(HDR_X_MAILER);

	public static final String HDR_ADDR_DELIM = ",";

	public static final String HDR_X_SPAM_FLAG = "X-Spam-Flag";

	public static final HeaderName X_SPAM_FLAG = HeaderName.valueOf(HDR_X_SPAM_FLAG);

	public static final String HDR_CONTENT_ID = "Content-ID";

	public static final HeaderName CONTENT_ID = HeaderName.valueOf(HDR_CONTENT_ID);

	public static final String HDR_CONTENT_TRANSFER_ENC = "Content-Transfer-Encoding";

	public static final HeaderName CONTENT_TRANSFER_ENC = HeaderName.valueOf(HDR_CONTENT_TRANSFER_ENC);

	public static final String HDR_DISPOSITION = "Content-Disposition";

	public static final HeaderName DISPOSITION = HeaderName.valueOf(HDR_DISPOSITION);

	public static final String HDR_X_OX_MARKER = "X-OX-Marker";

	public static final HeaderName X_OX_MARKER = HeaderName.valueOf(HDR_X_OX_MARKER);

}
