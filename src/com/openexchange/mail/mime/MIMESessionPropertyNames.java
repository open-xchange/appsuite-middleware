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

import javax.mail.Session;

/**
 * {@link MIMESessionPropertyNames} - Provides string constants to set
 * corresponding properties in an instance of {@link Session}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMESessionPropertyNames {

	/**
	 * Prevent instantiation
	 */
	private MIMESessionPropertyNames() {
		super();
	}

	public static final String PROP_MAIL_REPLYALLCC = "mail.replyallcc";

	public static final String PROP_MAIL_ALTERNATES = "mail.alternates";

	public static final String PROP_ALLOWREADONLYSELECT = "mail.imap.allowreadonlyselect";

	public static final String PROP_SMTPHOST = "mail.smtp.host";

	public static final String PROP_SMTPPORT = "mail.smtp.port";

	public static final String PROP_SMTPLOCALHOST = "mail.smtp.localhost";

	public static final String PROP_MAIL_SMTP_AUTH = "mail.smtp.auth";

	public static final String PROP_MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";

	public static final String PROP_MAIL_SMTP_TIMEOUT = "mail.imap.timeout";

	public static final String PROP_MAIL_IMAP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";

	public static final String PROP_MAIL_IMAP_TIMEOUT = "mail.imap.timeout";

	public static final String PROP_MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

	public static final String PROP_MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

	public static final String PROP_MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";

	public static final String PROP_MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

	public static final String PROP_MAIL_IMAP_SOCKET_FACTORY_FALLBACK = "mail.imap.socketFactory.fallback";

	public static final String PROP_MAIL_IMAP_SOCKET_FACTORY_PORT = "mail.imap.socketFactory.port";

	public static final String PROP_MAIL_IMAP_SOCKET_FACTORY_CLASS = "mail.imap.socketFactory.class";

	public static final String PROP_MAIL_IMAP_STARTTLS_ENABLE = "mail.imap.starttls.enable";

	public static final String PROP_MAIL_MIME_CHARSET = "mail.mime.charset";

	public static final String PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT = "mail.imap.connectionpooltimeout";

	public static final String PROP_MAIL_IMAP_CONNECTIONPOOLSIZE = "mail.imap.connectionpoolsize";

	public static final String PROP_MAIL_MIME_DECODETEXT_STRICT = "mail.mime.decodetext.strict";

	public static final String PROP_MAIL_MIME_ENCODEEOL_STRICT = "mail.mime.encodeeol.strict";

	public static final String PROP_MAIL_MIME_BASE64_IGNOREERRORS = "mail.mime.base64.ignoreerrors";

	public static final String PROP_MAIL_DEBUG = "mail.debug";

}
