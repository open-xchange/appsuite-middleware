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

package com.openexchange.groupware.container.mail.parser;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.StringHelper;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.Html2TextConverter;
import com.openexchange.tools.mail.MailTools;
import com.openexchange.tools.mail.UUEncodedPart;

/**
 * ForwardTextMessageHandler
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class ForwardTextMessageHandler implements MessageHandler {

	private static final Pattern PATTERN_BODY = Pattern.compile("<body>", Pattern.CASE_INSENSITIVE);

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ForwardTextMessageHandler.class);
	
	private static final String STR_EMPTY = "";

	private final SessionObject session;

	private final StringHelper strHelper;

	private final String msgUID;

	private final UserSettingMail usm;

	private String firstText;

	private boolean isHtml;

	private String from;

	private String to;

	private Date receivedDate;

	private String subject;

	private String preparedText;

	private boolean isAlternative;

	private boolean textFound;

	public ForwardTextMessageHandler(final SessionObject session, final String msgUID) {
		super();
		this.session = session;
		this.usm = session.getUserSettingMail();
		this.msgUID = msgUID;
		this.firstText = null;
		strHelper = new StringHelper(session.getLocale());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
		this.from = MessageUtils.addr2String(fromAddrs);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleRecipient(javax.mail.Message.RecipientType,
	 *      javax.mail.internet.InternetAddress[])
	 */
	public boolean handleRecipient(final RecipientType recipientType, final InternetAddress[] recipientAddrs)
			throws OXException {
		if (RecipientType.TO.equals(recipientType)) {
			this.to = MessageUtils.addr2String(recipientAddrs);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSubject(java.lang.String)
	 */
	public boolean handleSubject(final String subject) throws OXException {
		this.subject = subject;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSentDate(java.util.Date)
	 */
	public boolean handleSentDate(final Date sentDate) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleReceivedDate(java.util.Date)
	 */
	public boolean handleReceivedDate(final Date receivedDate) throws OXException {
		this.receivedDate = (Date) receivedDate.clone();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleHeaders(java.util.Map)
	 */
	public boolean handleHeaders(final Map<String, String> headerMap) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSystemFlags(javax.mail.Flags.Flag[])
	 */
	public boolean handleSystemFlags(final Flag[] systemFlags) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleUserFlags(java.lang.String[])
	 */
	public boolean handleUserFlags(final String[] userFlags) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlinePlainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlinePlainText(final String plainTextContent, final String baseContentType, final int size,
			final String fileName, final String id) throws OXException {
		textFound = true;
		if (isAlternative && usm.isDisplayHtmlInlineContent()) {
			/*
			 * User wants to see message's html version
			 */
			return true;
		}
		this.firstText = MailTools.htmlFormat(plainTextContent);
		this.isHtml = false;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedPainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final String baseContentType,
			final int size, final String fileName, final String id) throws OXException {
		return handleInlinePlainText(decodedTextContent, baseContentType, size, fileName, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineHtml(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineHtml(final String htmlContent, final String baseContentType, final int size,
			final String fileName, final String id) throws OXException {
		if (isAlternative && usm.isDisplayHtmlInlineContent()) {
			/*
			 * multipart/alternative and user wants to see content's html
			 * version
			 */
			this.firstText = htmlContent;
			this.isHtml = true;
			return false;
		} else if (!textFound) {
			if (usm.isDisplayHtmlInlineContent()) {
				/*
				 * Still no text found til here. Insert html content
				 */
				this.firstText = htmlContent;
				this.isHtml = true;
				return false;
			}
			/*
			 * Still no text found til here. Insert text-converted html
			 * content
			 */
			final Html2TextConverter converter = new Html2TextConverter();
			final String content;
			try {
				content = converter.convertWithQuotes(htmlContent);
			} catch (final IOException e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
			this.firstText = MailTools.htmlFormat(content);
			this.isHtml = false;
			return false;
		}
		/*
		 * No multipart/alternative or user does not want to see content's html
		 * version or text was already found. Leave it.
		 */
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleAttachment(javax.mail.Part,
	 *      boolean, java.lang.String, java.lang.String)
	 */
	public boolean handleAttachment(final Part part, final boolean isInline, final String baseContentType,
			final String fileName, final String id) throws OXException {
		try {
			if (isInline && part.isMimeType("text/*")) {
				this.isHtml = part.isMimeType("text/html");
				this.firstText = isHtml ? MessageUtils.readPart(part) : MailTools.htmlFormat(MessageUtils.readPart(part));
				return false;
			}
			return true;
		} catch (final MessagingException e) {
			throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSpecialPart(javax.mail.Part,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean handleSpecialPart(final Part part, final String baseContentType, final String id) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleImagePart(javax.mail.Part,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleImagePart(final Part part, final String imageCID, final String baseContentType, final String id)
			throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMultipart(javax.mail.Multipart,
	 *      int, java.lang.String)
	 */
	public boolean handleMultipart(final Multipart mp, final int bodyPartCount, final String id) throws OXException {
		/*
		 * Determine if message is of MIME type multipart/alternative
		 */
		isAlternative = (mp.getContentType().regionMatches(true, 0, "multipart/alternative", 0, 21) && bodyPartCount >= 2);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleNestedMessage(javax.mail.Message,
	 *      java.lang.String)
	 */
	public boolean handleNestedMessage(final Message nestedMsg, final String id) throws OXException {
		final ForwardTextMessageHandler msgHandler = new ForwardTextMessageHandler(session, this.msgUID);
		try {
			new MessageDumper(session).dumpMessage(nestedMsg, msgHandler, id);
			if (msgHandler.firstText != null) {
				this.firstText = msgHandler.firstText;
				this.isHtml = msgHandler.isHtml;
				return false;
			}
			return true;
		} catch (final MessagingException e) {
			throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMessageEnd()
	 */
	public void handleMessageEnd(final Message msg) throws OXException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("handleMessageEnd()");
		}
	}

	public boolean isHtml() {
		return isHtml;
	}

	public String getForwardText() {
		if (preparedText != null) {
			return preparedText;
		}
		if (firstText == null) {
			preparedText = STR_EMPTY;
			isHtml = false;
		} else {
			preparedText = firstText;
		}
		String forwardPrefix = strHelper.getString(MailStrings.FORWARD_PREFIX);
		forwardPrefix = forwardPrefix.replaceFirst("#FROM#", from == null ? STR_EMPTY : from);
		forwardPrefix = forwardPrefix.replaceFirst("#TO#", to == null ? STR_EMPTY : to);
		try {
			forwardPrefix = forwardPrefix.replaceFirst("#DATE#", receivedDate == null ? STR_EMPTY : DateFormat.getDateInstance(
					DateFormat.LONG, session.getLocale()).format(receivedDate));
		} catch (final Throwable t) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(t.getMessage(), t);
			}
			forwardPrefix = forwardPrefix.replaceFirst("#DATE#", STR_EMPTY);
		}
		try {
			forwardPrefix = forwardPrefix.replaceFirst("#TIME#", receivedDate == null ? STR_EMPTY : DateFormat.getTimeInstance(
					DateFormat.SHORT, session.getLocale()).format(receivedDate));
		} catch (final Throwable t) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(t.getMessage(), t);
			}
			forwardPrefix = forwardPrefix.replaceFirst("#TIME#", STR_EMPTY);
		}
		forwardPrefix = forwardPrefix.replaceFirst("#SUBJECT#", subject);
		forwardPrefix = MailTools.htmlFormat(forwardPrefix);
		final String doubleBreak = "<br><br>";
		if (isHtml) {
			final Matcher m = PATTERN_BODY.matcher(preparedText);
			final StringBuffer replaceBuffer = new StringBuffer(1000);
			if (m.find()) {
				m.appendReplacement(replaceBuffer, Matcher.quoteReplacement(new StringBuilder(100).append(doubleBreak)
						.append(m.group()).append(forwardPrefix).append(doubleBreak).toString()));
			} else {
				replaceBuffer.append(doubleBreak).append(forwardPrefix).append(doubleBreak);
			}
			m.appendTail(replaceBuffer);
			preparedText = replaceBuffer.toString();
		} else {
			preparedText = new StringBuilder(1000).append(doubleBreak).append(forwardPrefix).append(doubleBreak)
					.append(preparedText).toString();// replaceAll("<br/?>","\n");
		}
		return preparedText;
	}

}
