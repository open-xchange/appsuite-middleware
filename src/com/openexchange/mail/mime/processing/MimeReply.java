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

package com.openexchange.mail.mime.processing;

import static com.openexchange.smtp.filler.SMTPMessageFiller.getContentIDs;
import static com.openexchange.smtp.filler.SMTPMessageFiller.hasEmbeddedImages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.StringHelper;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInterfaceImpl;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.ComposedMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.InlineContentHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.ContentType;

/**
 * {@link MimeReply}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MimeReply {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MimeReply.class);

	private static final String PREFIX_RE = "Re: ";

	/*
	 * Parameter constants
	 */
	private static final String PARAM_CHARSET = "charset";

	/**
	 * No instantiation
	 */
	private MimeReply() {
		super();
	}

	/**
	 * Composes a reply message from specified original message based on MIME
	 * objects from <code>JavaMail</code> API
	 * 
	 * @param originalMsg
	 *            The referenced original message
	 * @param replyAll
	 *            <code>true</code> to reply to all; otherwise
	 *            <code>false</code>
	 * @param session
	 *            The session containing needed user data
	 * @param mailSession
	 *            The mail session
	 * @return An instance of {@link MailMessage} representing an user-editable
	 *         reply mail
	 * @throws MailException
	 *             If reply mail cannot be composed
	 */
	public static MailMessage getReplyMail(final MimeMessage originalMsg, final boolean replyAll,
			final SessionObject session, final Session mailSession) throws MailException {
		try {
			final UserSettingMail usm = session.getUserSettingMail();
			/*
			 * New MIME message with a dummy session
			 */
			final MimeMessage replyMsg = new MimeMessage(MIMEDefaultSession.getDefaultSession());
			/*
			 * Set headers of reply message
			 */
			final String subjectPrefix = PREFIX_RE;
			String subjectHdrValue = originalMsg.getHeader(MessageHeaders.HDR_SUBJECT, null);
			if (subjectHdrValue == null) {
				subjectHdrValue = "";
			}
			final String rawSubject = MimeUtility.unfold(subjectHdrValue);
			try {
				final String decodedSubject = MessageUtility.decodeMultiEncodedHeader(MimeUtility
						.decodeText(rawSubject));
				final String newSubject = decodedSubject.regionMatches(true, 0, subjectPrefix, 0, 4) ? decodedSubject
						: new StringBuilder().append(subjectPrefix).append(decodedSubject).toString();
				replyMsg.setSubject(newSubject, MailConfig.getDefaultMimeCharset());
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
				/*
				 * Handle raw value: setting prefix to raw subject value still
				 * leaves a valid and correct encoded header
				 */
				replyMsg.setHeader(MessageHeaders.HDR_SUBJECT, new StringBuilder().append(subjectPrefix).append(
						rawSubject).toString());
			}
			/*
			 * Set the appropiate recipients
			 */
			final InternetAddress[] recipientAddrs;
			if (originalMsg.getHeader(MessageHeaders.HDR_REPLY_TO) == null) {
				/*
				 * Set from as recipient
				 */
				recipientAddrs = (InternetAddress[]) originalMsg.getFrom();
			} else {
				/*
				 * Message holds header 'Reply-To'
				 */
				final String replyToStr = originalMsg.getHeader(MessageHeaders.HDR_REPLY_TO,
						MessageHeaders.HDR_ADDR_DELIM);
				if (replyToStr == null) {
					recipientAddrs = new InternetAddress[0];
				} else {
					recipientAddrs = InternetAddress.parseHeader(MimeUtility.unfold(replyToStr), true);
				}
			}
			if (replyAll) {
				/*
				 * Create a filter which is used to sort out addresses before
				 * adding them to either field 'To' or 'Cc'
				 */
				final Set<InternetAddress> filter = new HashSet<InternetAddress>();
				/*
				 * Add user's address to filter
				 */
				if (InternetAddress.getLocalAddress(mailSession) != null) {
					filter.add(InternetAddress.getLocalAddress(mailSession));
				}
				/*
				 * Add any other address the user is known by to filter
				 */
				final String alternates = mailSession.getProperty(MIMESessionPropertyNames.PROP_MAIL_ALTERNATES);
				if (alternates != null) {
					filter.addAll(Arrays.asList(MessageUtility.parseAddressList(alternates, false)));
				}
				/*
				 * Add user's aliases to filter
				 */
				final String[] userAddrs = session.getUserObject().getAliases();
				if (userAddrs != null && userAddrs.length > 0) {
					final StringBuilder addrBuilder = new StringBuilder();
					addrBuilder.append(userAddrs[0]);
					for (int i = 1; i < userAddrs.length; i++) {
						addrBuilder.append(',').append(userAddrs[i]);
					}
					filter.addAll(Arrays.asList(MessageUtility.parseAddressList(addrBuilder.toString(), false)));
				}
				/*
				 * Determine if other original recipients should be added to Cc
				 */
				final boolean replyallcc = "true".equalsIgnoreCase(mailSession
						.getProperty(MIMESessionPropertyNames.PROP_MAIL_REPLYALLCC));
				/*
				 * Filter recipients from 'Reply-To'/'From' field
				 */
				final Set<InternetAddress> filteredAddrs = filter(filter, recipientAddrs);
				/*
				 * Add filtered recipients from 'To' field
				 */
				String hdrVal = originalMsg.getHeader(MessageHeaders.HDR_TO, MessageHeaders.HDR_ADDR_DELIM);
				InternetAddress[] toAddrs = null;
				if (hdrVal != null) {
					filteredAddrs.addAll(filter(filter, (toAddrs = MessageUtility.parseAddressList(hdrVal, true))));
				}
				/*
				 * ... and add filtered addresses to either 'To' or 'Cc' field
				 */
				if (!filteredAddrs.isEmpty()) {
					if (replyallcc) {
						replyMsg.addRecipients(RecipientType.CC, filteredAddrs
								.toArray(new InternetAddress[filteredAddrs.size()]));
					} else {
						replyMsg.addRecipients(RecipientType.TO, filteredAddrs
								.toArray(new InternetAddress[filteredAddrs.size()]));
					}
				} else if (toAddrs != null) {
					final Set<InternetAddress> tmpSet = new HashSet<InternetAddress>(Arrays.asList(recipientAddrs));
					tmpSet.removeAll(Arrays.asList(toAddrs));
					if (tmpSet.isEmpty()) {
						/*
						 * The message was sent from the user to hisself. In
						 * this special case allow user's own address in field
						 * 'To' to avoid an empty 'To' field
						 */
						replyMsg.addRecipients(RecipientType.TO, recipientAddrs);
					}
				}
				/*
				 * Filter recipients from 'Cc' field
				 */
				filteredAddrs.clear();
				hdrVal = originalMsg.getHeader(MessageHeaders.HDR_CC, MessageHeaders.HDR_ADDR_DELIM);
				if (hdrVal != null) {
					filteredAddrs.addAll(filter(filter, MessageUtility.parseAddressList(MimeUtility.unfold(hdrVal),
							true)));
				}
				if (!filteredAddrs.isEmpty()) {
					replyMsg.addRecipients(RecipientType.CC, filteredAddrs.toArray(new InternetAddress[filteredAddrs
							.size()]));
				}
				/*
				 * Filter recipients from 'Bcc' field
				 */
				filteredAddrs.clear();
				hdrVal = originalMsg.getHeader(MessageHeaders.HDR_BCC, MessageHeaders.HDR_ADDR_DELIM);
				if (hdrVal != null) {
					filteredAddrs.addAll(filter(filter, MessageUtility.parseAddressList(MimeUtility.unfold(hdrVal),
							true)));
				}
				if (!filteredAddrs.isEmpty()) {
					replyMsg.addRecipients(RecipientType.BCC, filteredAddrs.toArray(new InternetAddress[filteredAddrs
							.size()]));
				}
			} else {
				/*
				 * Plain reply: Just add recipients from 'Reply-To'/'From' field
				 */
				replyMsg.addRecipients(RecipientType.TO, recipientAddrs);
			}
			/*
			 * Set mail text of reply message
			 */
			if (usm.isIgnoreOriginalMailTextOnReply()) {
				/*
				 * Add empty text content as message's body
				 */
				replyMsg.setText("", MailConfig.getDefaultMimeCharset(), "plain");
				replyMsg.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
				replyMsg.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMETypes.MIME_TEXT_PLAIN_TEMPL.replaceFirst(
						"#CS#", MailConfig.getDefaultMimeCharset()));
				final MailMessage replyMail = MIMEMessageConverter.convertMessage(replyMsg);
				return replyMail;
			}
			/*
			 * Add reply text
			 */
			final ContentType retvalContentType = new ContentType();
			final String replyText;
			{
				final List<String> list = new ArrayList<String>();
				generateReplyText(originalMsg, retvalContentType, new StringHelper(session.getLocale()), session
						.getLocale(), usm, mailSession, list);
				final StringBuilder replyTextBuilder = new StringBuilder(8192 * 2);
				for (int i = list.size() - 1; i >= 0; i--) {
					replyTextBuilder.append(list.get(i));
				}
				if (retvalContentType.getParameter(PARAM_CHARSET) == null) {
					retvalContentType.addParameter(PARAM_CHARSET, MailConfig.getDefaultMimeCharset());
				}
				replyText = replyTextBuilder.toString();
			}
			/*
			 * Compose reply mail
			 */
			final MailMessage replyMail;
			if (retvalContentType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL) && hasEmbeddedImages(replyText)) {
				/*
				 * Prepare to append inline content
				 */
				final Multipart multiRelated = new MimeMultipart("related");
				{
					final MimeBodyPart text = new MimeBodyPart();
					text.setText(replyText, retvalContentType.getParameter(PARAM_CHARSET), retvalContentType
							.getSubType());
					text.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
					text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, retvalContentType.toString());
					multiRelated.addBodyPart(text);
				}
				replyMsg.setContent(multiRelated);
				replyMsg.saveChanges();
				replyMail = new ComposedMailMessage(MIMEMessageConverter.convertMessage(replyMsg));
				/*
				 * Append inline content
				 */
				appendInlineContent(originalMsg, (ComposedMailMessage) replyMail, getContentIDs(replyText));
			} else {
				/*
				 * Set message's content directly to reply text
				 */
				replyMsg.setText(replyText, retvalContentType.getParameter(PARAM_CHARSET), retvalContentType
						.getSubType());
				replyMsg.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
				replyMsg.setHeader(MessageHeaders.HDR_CONTENT_TYPE, retvalContentType.toString());
				replyMsg.saveChanges();
				replyMail = MIMEMessageConverter.convertMessage(replyMsg);
			}
			return replyMail;
		} catch (final MessagingException e) {
			throw MIMEMailException.handleMessagingException(e);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}

	}

	private static void appendInlineContent(final MimeMessage originalMsg, final ComposedMailMessage replyMail,
			final List<String> cids) throws MailException {
		final MailMessage originalMail = MIMEMessageConverter.convertMessage(originalMsg);
		final InlineContentHandler handler = new InlineContentHandler(cids);
		new MailMessageParser().parseMailMessage(originalMail, handler);
		final List<MailPart> inlineContents = handler.getInlineContents();
		for (MailPart mailPart : inlineContents) {
			replyMail.addAdditionalParts(mailPart);
		}
	}

	private static final Set<InternetAddress> EMPTY_SET = new HashSet<InternetAddress>(0);

	/**
	 * Filters given address array against given filter set. All addresses
	 * currently contained in filter set are removed and all adresses not
	 * contained in filter set are added to filter set for future invocations
	 * 
	 * @param filter
	 *            The current address filter
	 * @param addrs
	 *            The address list to filter
	 * @return The filtered set of addresses
	 */
	private static Set<InternetAddress> filter(final Set<InternetAddress> filter, final InternetAddress[] addrs) {
		if (addrs == null) {
			return EMPTY_SET;
		}
		final Set<InternetAddress> set = new HashSet<InternetAddress>(Arrays.asList(addrs));
		/*
		 * Remove all addresses from set which are contained in filter
		 */
		set.removeAll(filter);
		/*
		 * Add new addresses to filter
		 */
		filter.addAll(set);
		return set;
	}

	/**
	 * Gathers all text bodies and appends them to given text builder
	 * 
	 * @param msg
	 *            The root message
	 * @param retvalContentType
	 *            The return value's content type
	 * @param strHelper
	 *            The i18n string helper
	 * @return <code>true</code> if any text was found; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 * @throws MessagingException
	 * @throws IOException
	 */
	private static boolean generateReplyText(final Message msg, final ContentType retvalContentType,
			final StringHelper strHelper, final Locale locale, final UserSettingMail usm, final Session mailSession,
			final List<String> replyTexts) throws MailException, MessagingException, IOException {
		final StringBuilder textBuilder = new StringBuilder(8192);
		final ContentType contentType = new ContentType(msg.getContentType());
		boolean found = false;
		if (contentType.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
			found |= gatherAllTextContents((Multipart) msg.getContent(), contentType, retvalContentType, textBuilder,
					strHelper, usm, mailSession, locale, replyTexts);
		} else if (contentType.isMimeType(MIMETypes.MIME_TEXT_ALL)) {
			if (retvalContentType.getPrimaryType() == null) {
				final String text = MimeProcessingUtility.handleInlineTextPart(msg, contentType, usm);
				retvalContentType.setContentType(contentType);
				textBuilder.append(text);
			} else {
				final String text = MimeProcessingUtility.handleInlineTextPart(msg, contentType, usm);
				MimeProcessingUtility.appendRightVersion(retvalContentType, contentType, text, textBuilder);
			}
			found = true;
		}
		if (found) {
			final boolean isHtml = retvalContentType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL);
			String replyPrefix = strHelper.getString(MailStrings.REPLY_PREFIX);
			replyPrefix = replyPrefix.replaceFirst("#DATE#", msg.getSentDate() == null ? "" : DateFormat
					.getDateInstance(DateFormat.LONG, locale).format(msg.getSentDate()));
			replyPrefix = replyPrefix.replaceFirst("#TIME#", msg.getSentDate() == null ? "" : DateFormat
					.getTimeInstance(DateFormat.SHORT, locale).format(msg.getSentDate()));
			{
				final InternetAddress[] from = (InternetAddress[]) msg.getFrom();
				replyPrefix = replyPrefix.replaceFirst("#SENDER#", from == null || from.length == 0 ? "" : from[0]
						.toUnicodeString());
			}
			{
				final String nextLine = "\n\n";
				if (isHtml) {
					replyPrefix = MessageUtility.htmlFormat(new StringBuilder(replyPrefix.length() + 3).append(nextLine)
							.append(replyPrefix).append(nextLine).toString());
				} else {
					replyPrefix = new StringBuilder(replyPrefix.length() + 3).append(nextLine).append(replyPrefix)
							.append(nextLine).toString();
				}
			}
			/*
			 * Surround with quote
			 */
			final String replyTextBody;
			if (isHtml) {
				replyTextBody = quoteHtml(textBuilder.toString());
			} else {
				replyTextBody = quoteText(textBuilder.toString());
			}
			textBuilder.setLength(0);
			textBuilder.append(replyPrefix);
			textBuilder.append(replyTextBody);
		}
		replyTexts.add(textBuilder.toString());
		// parentTextBuilder.append(textBuilder);
		return found;
	}

	/**
	 * Gathers all text bodies and appends them to given text builder
	 * 
	 * @param mp
	 *            The root multipart
	 * @param retvalContentType
	 *            The return value's content type
	 * @param textBuilder
	 *            The text builder
	 * @return <code>true</code> if any text was found; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 * @throws MessagingException
	 * @throws IOException
	 */
	private static boolean gatherAllTextContents(final Multipart mp, final ContentType mpContentType,
			final ContentType retvalContentType, final StringBuilder textBuilder, final StringHelper strHelper,
			final UserSettingMail usm, final Session mailSession, final Locale locale, final List<String> replyTexts)
			throws MailException, MessagingException, IOException {
		final int count = mp.getCount();
		final ContentType partContentType = new ContentType();
		boolean found = false;
		if ((mpContentType.isMimeType(MIMETypes.MIME_MULTIPART_ALTERNATIVE) || mpContentType
				.isMimeType(MIMETypes.MIME_MULTIPART_RELATED))
				&& usm.isDisplayHtmlInlineContent() && count >= 2) {
			/*
			 * Get html content
			 */
			for (int i = 0; i < count && !found; i++) {
				final BodyPart part = mp.getBodyPart(i);
				partContentType.setContentType(part.getContentType());
				if (partContentType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL) && MimeProcessingUtility.isInline(part)) {
					if (retvalContentType.getPrimaryType() == null) {
						retvalContentType.setContentType(partContentType);
						textBuilder.append(MessageUtility.readPart(part, partContentType));
					} else {
						final String text = MimeProcessingUtility.handleInlineTextPart(part, partContentType, usm);
						MimeProcessingUtility.appendRightVersion(retvalContentType, partContentType, text, textBuilder);
					}
					found = true;
				} else if (partContentType.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
					found |= gatherAllTextContents((Multipart) part.getContent(), partContentType, retvalContentType,
							textBuilder, strHelper, usm, mailSession, locale, replyTexts);
				}
			}
		}
		if (!found) {
			/*
			 * Get any text content
			 */
			for (int i = 0; i < count && !found; i++) {
				final BodyPart part = mp.getBodyPart(i);
				partContentType.setContentType(part.getContentType());
				if (partContentType.isMimeType(MIMETypes.MIME_TEXT_ALL) && MimeProcessingUtility.isInline(part)) {
					if (retvalContentType.getPrimaryType() == null) {
						final String text = MimeProcessingUtility.handleInlineTextPart(part, partContentType, usm);
						retvalContentType.setContentType(partContentType);
						textBuilder.append(text);
					} else {
						final String text = MimeProcessingUtility.handleInlineTextPart(part, partContentType, usm);
						MimeProcessingUtility.appendRightVersion(retvalContentType, partContentType, text, textBuilder);
					}
					found = true;
				} else if (partContentType.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
					found |= gatherAllTextContents((Multipart) part.getContent(), partContentType, retvalContentType,
							textBuilder, strHelper, usm, mailSession, locale, replyTexts);
				}
			}
		}
		/*
		 * Look for enclosed messages in any case
		 */
		for (int i = count - 1; i >= 0; i--) {
			final BodyPart part = mp.getBodyPart(i);
			partContentType.setContentType(part.getContentType());
			if (partContentType.isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
				final Message enclosedMsg = (Message) part.getContent();
				found |= generateReplyText(enclosedMsg, retvalContentType, strHelper, locale, usm, mailSession,
						replyTexts);
			} else if (part.getFileName() != null && part.getFileName().toLowerCase(Locale.ENGLISH).endsWith(".eml")) {
				/*
				 * Create message from input stream
				 */
				final Message attachedMsg = new MimeMessage(mailSession, part.getInputStream());
				found |= generateReplyText(attachedMsg, retvalContentType, strHelper, locale, usm, mailSession,
						replyTexts);
			}
		}
		return found;
	}

	private static String quoteText(final String textContent) {
		return textContent.replaceAll("(?m)^", "> ");
	}

	private static final Pattern PATTERN_HTML_START = Pattern.compile("<html>", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_HTML_END = Pattern.compile("</html>", Pattern.CASE_INSENSITIVE);

	private static final String BLOCKQUOTE_START = "<blockquote type=\"cite\" style=\"margin-left: 0px; padding-left: 10px; border-left: solid 1px blue;\">\n";

	private static final String BLOCKQUOTE_END = "</blockquote>\n<br>&nbsp;";

	private static String quoteHtml(final String htmlContent) {
		final StringBuffer sb = new StringBuffer(htmlContent.length());
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
}
