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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
 * ReplyTextMessageHandler
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class ReplyTextMessageHandler implements MessageHandler {

	private static final String TAG_BR = "<br>";

	private static final String STR_CRLF = "(\\r)?\\n";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ReplyTextMessageHandler.class);

	private static final Pattern PATTERN_HTML_START = Pattern.compile("<html>", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_HTML_END = Pattern.compile("</html>", Pattern.CASE_INSENSITIVE);

	private static final String BLOCKQUOTE_START = "<blockquote type=\"cite\">\n";

	private static final String BLOCKQUOTE_END = "</blockquote>\n<br>&nbsp;";
	
	private static final Properties DUMMY_PROPS;
	
	static {
		DUMMY_PROPS = (Properties) System.getProperties().clone();
		DUMMY_PROPS.put("mail.host", "smtp.dummydomain.com");
		DUMMY_PROPS.put("mail.transport.protocol", "smtp");
	}

	private final SessionObject session;

	private final StringHelper strHelper;

	private final UserSettingMail usm;

	private final String msgUID;

	private String sender;

	private Date sentDate;

	private String replyText;

	private final StringBuilder textBuilder;

	private final StringBuilder nestedTextBuilder;

	private boolean isHtml;

	private boolean isAlternative;

	private boolean textFound;

	public ReplyTextMessageHandler(final SessionObject session, final String msgUID) {
		super();
		this.session = session;
		this.usm = session.getUserSettingMail();
		this.msgUID = msgUID;
		this.textBuilder = new StringBuilder();
		this.nestedTextBuilder = new StringBuilder();
		strHelper = new StringHelper(session.getLocale());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleFrom(final InternetAddress[] fromAddrs) {
		if (fromAddrs != null) {
			sender = MessageUtils.addr2String(fromAddrs);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleRecipient(javax.mail.Message.RecipientType,
	 *      javax.mail.internet.InternetAddress[])
	 */
	public boolean handleRecipient(final RecipientType recipientType, final InternetAddress[] recipientAddrs) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSubject(java.lang.String)
	 */
	public boolean handleSubject(final String subject) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSentDate(java.util.Date)
	 */
	public boolean handleSentDate(final Date sentDate) {
		this.sentDate = (Date) sentDate.clone();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleReceivedDate(java.util.Date)
	 */
	public boolean handleReceivedDate(final Date receivedDate) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleHeaders(java.util.Map)
	 */
	public boolean handleHeaders(final Map<String, String> headerMap) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSystemFlags(javax.mail.Flags.Flag[])
	 */
	public boolean handleSystemFlags(final Flag[] systemFlags) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleUserFlags(java.lang.String[])
	 */
	public boolean handleUserFlags(final String[] userFlags) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlinePlainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlinePlainText(final String plainTextContent, final String baseContentType, final int size,
			final String fileName, final String id) {
		textFound = true;
		if (isAlternative && usm.isDisplayHtmlInlineContent()) {
			/*
			 * User wants to see message's html version
			 */
			return true;
		}
		if (isHtml) {
			/*
			 * Html content was added before
			 */
			textBuilder.append(plainTextContent.replaceAll(STR_CRLF, TAG_BR));
		} else {
			textBuilder.append(plainTextContent);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedPainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final String baseContentType,
			final int size, final String fileName, final String id) {
		return handleInlinePlainText(decodedTextContent, baseContentType, size, fileName, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) {
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
			textBuilder.append(htmlContent);
			isHtml = true;
		} else if (!textFound) {
			if (usm.isDisplayHtmlInlineContent()) {
				/*
				 * Still no text found til here. Insert html content
				 */
				textBuilder.append(htmlContent);
				isHtml = true;
			} else {
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
				if (isHtml) {
					textBuilder.append(content.replaceAll(STR_CRLF, TAG_BR));
				} else {
					textBuilder.append(content);
				}
			}
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
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSpecialPart(javax.mail.Part,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean handleSpecialPart(final Part part, final String baseContentType, final String id) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleImagePart(javax.mail.Part,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleImagePart(final Part part, final String imageCID, final String baseContentType, final String id) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMultipart(javax.mail.Multipart,
	 *      int, java.lang.String)
	 */
	public boolean handleMultipart(final Multipart mp, final int bodyPartCount, final String id) {
		/*
		 * Determine if message is of MIME type multipart/alternative
		 */
		isAlternative = (mp.getContentType().regionMatches(true, 0, "multipart/alternative", 0, 21) && bodyPartCount >= 2);
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
			if (fileName.toLowerCase(session.getLocale()).endsWith(".eml")) {
				/*
				 * Create dummy mail session
				 */
				final Session mailSession = Session.getDefaultInstance(DUMMY_PROPS, null);
				/*
				 * Create message from input stream
				 */
				final Message attachedMsg = new MimeMessage(mailSession, part.getInputStream());
				final ReplyTextMessageHandler msgHandler = new ReplyTextMessageHandler(session, msgUID);
				new MessageDumper(session).dumpMessage(attachedMsg, msgHandler, id);
				this.nestedTextBuilder.append(msgHandler.getReplyText(true));
				this.isHtml |= msgHandler.isHtml;
			}
			return true;
		} catch (final MessagingException e) {
			throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		} catch (final IOException e) {
			throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleNestedMessage(javax.mail.Message,
	 *      java.lang.String)
	 */
	public boolean handleNestedMessage(final Message nestedMsg, final String id) throws OXException {
		final ReplyTextMessageHandler msgHandler = new ReplyTextMessageHandler(session, msgUID);
		try {
			new MessageDumper(session).dumpMessage(nestedMsg, msgHandler, id);
			this.nestedTextBuilder.append(msgHandler.getReplyText(true));
			this.isHtml |= msgHandler.isHtml;
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
	public void handleMessageEnd(final Message msg) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("handleMessageEnd()");
		}
	}

	public boolean isHtml() {
		return this.isHtml;
	}

	public String getReplyText() {
		return getReplyText(true);
	}

	private final String getReplyText(final boolean appendReplyPrefix) {
		if (replyText != null) {
			return replyText;
		}
		/*
		 * Create quoted reply text, that is either a heading "> " at the
		 * beginning of a line on plain text or a surounding blockquote tag in
		 * html
		 */
		final String replyPrefix;
		if (appendReplyPrefix) {
			replyPrefix = getPrefixLine();
		} else {
			replyPrefix = "";
		}
		/*
		 * Surround with quote
		 */
		final String replyTextBody;
		if (isHtml) {
			replyTextBody = quoteHtml(textBuilder.toString());
		} else {
			replyTextBody = MailTools.htmlFormat(quoteText(textBuilder.toString()));
		}
		return (replyText = new StringBuilder(1000).append(replyPrefix).append(replyTextBody).append(nestedTextBuilder)
				.toString());
	}

	private static final String quoteText(final String textContent) {
		return textContent.replaceAll("(?m)^", "> ");
	}

	private static String quoteHtml(final String htmlContent) {
		final StringBuffer sb = new StringBuffer();
		Matcher m = PATTERN_HTML_START.matcher(htmlContent);
		if (m.find()) {
			m.appendReplacement(sb, BLOCKQUOTE_START);
		} else {
			sb.append(BLOCKQUOTE_START);
		}
		m.appendTail(sb);
		m = PATTERN_HTML_END.matcher(sb.toString());
		sb.setLength(0);
		if (m.find()) {
			m.appendReplacement(sb, BLOCKQUOTE_END);
			m.appendTail(sb);
		} else {
			m.appendTail(sb);
			sb.append(BLOCKQUOTE_END);
		}
		return sb.toString();
	}

	private final String getPrefixLine() {
		String retval = strHelper.getString(MailStrings.REPLY_PREFIX);
		retval = retval.replaceFirst("#DATE#", sentDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG,
				session.getLocale()).format(sentDate));
		retval = retval.replaceFirst("#TIME#", sentDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT,
				session.getLocale()).format(sentDate));
		retval = retval.replaceFirst("#SENDER#", sender);
		final String nextLine = "\n\n";
		return MailTools.htmlFormat(new StringBuilder(retval.length() + 3).append(nextLine).append(retval).append(nextLine).toString());
	}

}
