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

package com.openexchange.groupware.container.mail;

import static com.openexchange.mail.utils.MessageUtility.parseAddressList;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.servlet.UnsynchronizedByteArrayOutputStream;

/**
 * MailObject
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailObject {

	public static final int DONT_SET = -2;

	private String fromAddr;

	private String[] toAddrs;

	private String[] ccAddrs;

	private String[] bccAddrs;

	private String subject;

	private String text;

	private String contentType;

	private boolean requestReadReceipt;

	private final Session session;

	private final int objectId;

	private final int folderId;

	private final int module;

	public MailObject(final Session sessionObj, final int objectId, final int folderId, final int module) {
		super();
		this.session = sessionObj;
		this.objectId = objectId;
		this.folderId = folderId;
		this.module = module;
	}

	private final void validateMailObject() throws MailException {
		if (fromAddr == null || fromAddr.length() == 0) {
			throw new MailException(MailException.Code.MISSING_FIELD, "From");
		} else if (toAddrs == null || toAddrs.length == 0) {
			throw new MailException(MailException.Code.MISSING_FIELD, "To");
		} else if (contentType == null || contentType.length() == 0) {
			throw new MailException(MailException.Code.MISSING_FIELD, "Content-Type");
		} else if (subject == null) {
			throw new MailException(MailException.Code.MISSING_FIELD, "Subject");
		} else if (text == null) {
			throw new MailException(MailException.Code.MISSING_FIELD, "Text");
		}
	}

	private final static String STR_CHARSET = "charset";

	private final static String HEADER_XPRIORITY = "X-Priority";

	private final static String HEADER_DISPNOTTO = "Disposition-Notification-Toy";

	private final static String HEADER_XOXREMINDER = "X-OX-Reminder";

	private final static String VALUE_PRIORITYNROM = "3 (normal)";

	private final static String HEADER_ORGANIZATION = "Organization";

	private final static String HEADER_X_MAILER = "X-Mailer";

	public final void send() throws MailException {
		try {
			validateMailObject();
			final MimeMessage msg = new MimeMessage(MIMEDefaultSession.getDefaultSession());
			/*
			 * Set from
			 */
			InternetAddress[] internetAddrs = parseAddressList(fromAddr, false);
			msg.setFrom(internetAddrs[0]);
			/*
			 * Set to
			 */
			String tmp = Arrays.toString(toAddrs);
			tmp = tmp.substring(1, tmp.length() - 1);
			internetAddrs = parseAddressList(tmp, false);
			msg.setRecipients(RecipientType.TO, internetAddrs);
			/*
			 * Set cc
			 */
			if (ccAddrs != null && ccAddrs.length > 0) {
				tmp = Arrays.toString(ccAddrs);
				tmp = tmp.substring(1, tmp.length() - 1);
				internetAddrs = parseAddressList(tmp, false);
				msg.setRecipients(RecipientType.CC, internetAddrs);
			}
			/*
			 * Set bcc
			 */
			if (bccAddrs != null && bccAddrs.length > 0) {
				tmp = Arrays.toString(bccAddrs);
				tmp = tmp.substring(1, tmp.length() - 1);
				internetAddrs = parseAddressList(tmp, false);
				msg.setRecipients(RecipientType.BCC, internetAddrs);
			}
			final ContentType ct = new ContentType(contentType);
			if (ct.getParameter(STR_CHARSET) == null) {
				/*
				 * Ensure a charset is set
				 */
				ct.addParameter(STR_CHARSET, MailConfig.getDefaultMimeCharset());
			}
			/*
			 * Set subject
			 */
			msg.setSubject(subject, ct.getParameter(STR_CHARSET));
			/*
			 * Set content and its type
			 */
			if ("text".equalsIgnoreCase(ct.getPrimaryType())) {
				if ("html".equalsIgnoreCase(ct.getSubType()) || "htm".equalsIgnoreCase(ct.getSubType())) {
					msg.setContent(text, ct.toString());
				} else if ("plain".equalsIgnoreCase(ct.getSubType()) || "enriched".equalsIgnoreCase(ct.getSubType())) {
					if (ct.getParameter(STR_CHARSET) == null) {
						msg.setText(text);
					} else {
						msg.setText(text, ct.getParameter(STR_CHARSET));
					}
				} else {
					throw new MailException(MailException.Code.UNSUPPORTED_MIME_TYPE, ct.toString());
				}
			} else {
				throw new MailException(MailException.Code.UNSUPPORTED_MIME_TYPE, ct.toString());
			}
			/*
			 * Disposition notification
			 */
			if (requestReadReceipt) {
				msg.setHeader(HEADER_DISPNOTTO, fromAddr);
			}
			/*
			 * Set priority
			 */
			msg.setHeader(HEADER_XPRIORITY, VALUE_PRIORITYNROM);
			/*
			 * Set mailer TODO: Read in mailer from file
			 */
			msg.setHeader(HEADER_X_MAILER, "Open-Xchange v6.0 Mailer");
			/*
			 * Set organization TODO: read in organization from file
			 */
			msg.setHeader(HEADER_ORGANIZATION, "Open-Xchange, Inc.");
			/*
			 * Set ox reference
			 */
			if (folderId != DONT_SET) {
				msg.setHeader(HEADER_XOXREMINDER, new StringBuilder().append(this.objectId).append(',').append(
						this.folderId).append(',').append(module).toString());
			}
			/*
			 * Set sent date
			 */
			if (msg.getSentDate() == null) {
				final long current = System.currentTimeMillis();
				final TimeZone userTimeZone = TimeZone.getTimeZone(UserStorage.getUser(session.getUserId(),
						session.getContext()).getTimeZone());
				msg.setSentDate(new Date(current + userTimeZone.getOffset(current)));
			}
			/*
			 * Finally send mail
			 */
			final MailTransport transport = MailTransport.getInstance(session, MailConnection
					.getInstance(session));
			final UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream();
			msg.writeTo(bos);
			transport.sendRawMessage(bos.toByteArray());
		} catch (final MessagingException e) {
			throw MIMEMailException.handleMessagingException(e);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	public void addBccAddr(final String addr) {
		bccAddrs = addAddr(addr, bccAddrs);
	}

	public String[] getBccAddrs() {
		return bccAddrs;
	}

	public void setBccAddrs(final String[] bccAddrs) {
		this.bccAddrs = bccAddrs;
	}

	public void addCcAddr(final String addr) {
		ccAddrs = addAddr(addr, ccAddrs);
	}

	public String[] getCcAddrs() {
		return ccAddrs;
	}

	public void setCcAddrs(final String[] ccAddrs) {
		this.ccAddrs = ccAddrs;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public String getFromAddr() {
		return fromAddr;
	}

	public void setFromAddr(final String fromAddr) {
		this.fromAddr = fromAddr;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public void addToAddr(final String addr) {
		toAddrs = addAddr(addr, toAddrs);
	}

	public String[] getToAddrs() {
		return toAddrs;
	}

	public void setToAddrs(final String[] toAddrs) {
		this.toAddrs = toAddrs;
	}

	public boolean isRequestReadReceipt() {
		return requestReadReceipt;
	}

	public void setRequestReadReceipt(final boolean requestReadReceipt) {
		this.requestReadReceipt = requestReadReceipt;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	private static final String[] addAddr(final String addr, String[] arr) {
		if (arr == null) {
			return new String[] { addr };
		}
		final String[] tmp = arr;
		arr = new String[tmp.length + 1];
		System.arraycopy(tmp, 0, arr, 0, tmp.length);
		arr[arr.length - 1] = addr;
		return arr;
	}

}
