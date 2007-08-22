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

package com.openexchange.mail.imap.converters;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.imap.IMAPUtils;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.imap.IMAPException;
import com.openexchange.mail.imap.dataobjects.IMAPMailMessage;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;

/**
 * IMAPMessageConverter
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPMessageConverter {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPMessageConverter.class);

	/**
	 * Prevent instantiation
	 */
	private IMAPMessageConverter() {
		super();
	}

	/**
	 * Creates a message data object from given IMAP message
	 * 
	 * @param msg
	 *            The IMAP message
	 * @return an instance of <code>{@link MailMessage}</code> containing the
	 *         attributes from given IMAP message
	 */
	public static MailMessage convertIMAPMessage(final IMAPMessage msg) throws IMAPException {
		try {
			final IMAPMailMessage retval = new IMAPMailMessage(msg);
			/*
			 * Set all cacheable data
			 */
			setHeaders(msg, retval);
			retval.addFrom((InternetAddress[]) msg.getFrom());
			retval.addTo((InternetAddress[]) msg.getRecipients(IMAPMessage.RecipientType.TO));
			retval.addCc((InternetAddress[]) msg.getRecipients(IMAPMessage.RecipientType.CC));
			retval.addBcc((InternetAddress[]) msg.getRecipients(IMAPMessage.RecipientType.BCC));
			retval.setContentType(msg.getContentType());
			retval.setDisposition(msg.getDisposition());
			retval.setDispositionNotification(msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO, null));
			retval.setFileName(msg.getFileName());
			parseFlags(msg.getFlags(), retval);
			parsePriority(retval.getHeader(MessageHeaders.HDR_X_PRIORITY), retval);
			if (msg.getReceivedDate() != null) {
				retval.setReceivedDate(msg.getReceivedDate());
			}
			if (msg.getSentDate() != null) {
				retval.setSentDate(msg.getSentDate());
			}
			retval.setSize(msg.getSize());
			retval.setSubject(msg.getSubject());
			retval.setUid(((IMAPFolder) msg.getFolder()).getUID(msg));
			return retval;
		} catch (final OXException e) {
			throw new IMAPException(e);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e);
		}
	}

	private static void parseFlags(final Flags flags, final IMAPMailMessage mailMessage) throws OXException {
		{
			int retval = 0;
			if (flags.contains(Flags.Flag.ANSWERED)) {
				retval |= MailMessage.FLAG_ANSWERED;
			}
			if (flags.contains(Flags.Flag.DELETED)) {
				retval |= MailMessage.FLAG_DELETED;
			}
			if (flags.contains(Flags.Flag.DRAFT)) {
				retval |= MailMessage.FLAG_DRAFT;
			}
			if (flags.contains(Flags.Flag.FLAGGED)) {
				retval |= MailMessage.FLAG_FLAGGED;
			}
			if (flags.contains(Flags.Flag.RECENT)) {
				retval |= MailMessage.FLAG_RECENT;
			}
			if (flags.contains(Flags.Flag.SEEN)) {
				retval |= MailMessage.FLAG_SEEN;
			}
			if (flags.contains(Flags.Flag.USER)) {
				retval |= MailMessage.FLAG_USER;
			}
			mailMessage.setFlags(retval);
		}
		final String[] userFlags = flags.getUserFlags();
		for (int i = 0; i < userFlags.length; i++) {
			/*
			 * Color Label
			 */
			if (userFlags[i].startsWith(JSONMessageObject.COLOR_LABEL_PREFIX)) {
				mailMessage.setColorLabel(JSONMessageObject.getColorLabelIntValue(userFlags[i]));
			} else {
				mailMessage.addUserFlag(userFlags[i]);
			}
		}
	}

	private static final String STR_CANNOT_LOAD_HEADER = "Cannot load header";

	private static void setHeaders(final IMAPMessage msg, final IMAPMailMessage mailMessage) throws MessagingException {
		/*
		 * HEADERS
		 */
		Map<String, String> headerMap = null;
		try {
			headerMap = new HashMap<String, String>();
			for (final Enumeration e = msg.getAllHeaders(); e.hasMoreElements();) {
				final Header h = (Header) e.nextElement();
				headerMap.put(h.getName(), h.getValue());
			}
		} catch (final MessagingException e) {
			if (e.getMessage().indexOf(STR_CANNOT_LOAD_HEADER) != -1) {
				/*
				 * Headers could not be loaded
				 */
				try {
					headerMap = IMAPUtils.loadBrokenHeaders(msg, false);
				} catch (final ProtocolException e1) {
					LOG.error(e.getMessage(), e);
					headerMap = new HashMap<String, String>();
				}
			} else {
				headerMap = new HashMap<String, String>();
			}
		}
		mailMessage.addHeaders(headerMap);
	}

	private static void parsePriority(final String priorityStr, final IMAPMailMessage mailMessage) {
		int priority = JSONMessageObject.PRIORITY_NORMAL;
		if (null != priorityStr) {
			final String[] tmp = priorityStr.split(" +");
			try {
				priority = Integer.parseInt(tmp[0]);
			} catch (final NumberFormatException nfe) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Strange X-Priority header: " + tmp[0], nfe);
				}
				priority = JSONMessageObject.PRIORITY_NORMAL;
			}
		}
		mailMessage.setPriority(priority);
	}
}
