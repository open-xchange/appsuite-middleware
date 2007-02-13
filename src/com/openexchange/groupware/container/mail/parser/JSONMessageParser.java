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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.openexchange.ajax.Mail;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.imap.IMAPException;
import com.openexchange.groupware.imap.IMAPProperties;
import com.openexchange.server.ComfireConfig;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.Html2Text;
import com.openexchange.tools.mail.MailTools;
import com.openexchange.tools.mail.UUEncodedMultiPart;
import com.openexchange.tools.mail.UUEncodedPart;
import com.sun.mail.imap.IMAPFolder;

/**
 * JSONMessageParser
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class JSONMessageParser {

	private static final Log LOG = LogFactory.getLog(JSONMessageParser.class);

	private static final String MULTIPART_ALL = "multipart/*";

	private static final String MESSAGE_RFC822 = "message/rfc822";

	private static final String TEXT_HTML = "text/html";

	private static final String UTF_8 = "UTF-8";

	private static final String HTML_BREAK = "<br/>";

	private final SessionObject session;

	private int threadLevel;

	private boolean isHtmlContent;

	private boolean plainTextFound;

	private HashMap<Integer, Boolean> openAttachments;

	private HashSet<String> imageContentIDs;

	private String imageContentIdToLoad;

	private String partToLoad;

	private JSONMessageAttachmentObject loadedJSONMsgAttachObj;
	
	private Part loadedPart;

	private boolean addToMessageObject;

	private boolean getFirstTextContent;

	private String firstTextContent;

	private String msgUID;

	public JSONMessageParser(SessionObject session) {
		this.session = session;
		openAttachments = new HashMap<Integer, Boolean>();
		imageContentIDs = new HashSet<String>();
		addToMessageObject = true;
	}

	public JSONMessageParser(SessionObject session, String partToLoad) {
		this(session);
		this.partToLoad = partToLoad;
		addToMessageObject = (partToLoad == null);
	}

	public JSONMessageParser(JSONMessageParser callingParser) {
		this.session = callingParser.session;
		this.addToMessageObject = callingParser.addToMessageObject;
		this.partToLoad = callingParser.partToLoad;
		this.loadedJSONMsgAttachObj = callingParser.loadedJSONMsgAttachObj;
		this.loadedPart = callingParser.loadedPart;
		this.imageContentIdToLoad = callingParser.imageContentIdToLoad;
		this.imageContentIDs = callingParser.imageContentIDs;
		this.isHtmlContent = callingParser.isHtmlContent;
		this.plainTextFound = callingParser.plainTextFound;
		this.openAttachments = callingParser.openAttachments;
	}

	/**
	 * Finds corresponding message part to given <code>attachmentId</code>.
	 * Invoke this method with <code>attachmentId</code> set to
	 * <code>null</code> to detect if given message contains attachments.
	 * @throws OXException 
	 */
	public JSONMessageAttachmentObject getAttachment(final Message msg, final String partToLoad, final Locale locale)
			throws MessagingException, IOException, JSONException, OXException {
		if (partToLoad == null || partToLoad.length() == 0) {
			return null;
		}
		addToMessageObject = false;
		this.partToLoad = partToLoad;
		parsePart(msg, null, 0, 0, 1, locale, false);
		return loadedJSONMsgAttachObj;
	}
	
	public Part getPart(final Message msg, final String partToLoad, final Locale locale)
			throws MessagingException, IOException, JSONException, OXException {
		if (partToLoad == null || partToLoad.length() == 0) {
			return null;
		}
		addToMessageObject = false;
		this.partToLoad = partToLoad;
		parsePart(msg, null, 0, 0, 1, locale, false);
		return loadedPart;
	}

	public JSONMessageAttachmentObject getImage(final Message msg, final String cid) throws MessagingException,
			IOException, JSONException {
		if (cid == null || cid.length() == 0) {
			return null;
		}
		addToMessageObject = false;
		imageContentIdToLoad = '<' + cid + '>';
		findImage(msg);
		return loadedJSONMsgAttachObj;
	}

	private final void findImage(final Part part) throws MessagingException, IOException {
		if (loadedJSONMsgAttachObj != null) {
			return;
		}
		if (part.isMimeType("image/*")) {
			final String contentId = ((javax.mail.internet.MimePart) part).getContentID();
			if (contentId == null) {
				return;
			}
			if (imageContentIdToLoad.equalsIgnoreCase(contentId)) {
				JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject();
				mao.setSize(part.getSize());
				mao.setFileName(part.getFileName());
				mao.setContentType(part.getContentType());
				mao.setContent(part.getInputStream());
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				loadedJSONMsgAttachObj = mao;
				mao = null;
				return;
			}
		} else if (part.isMimeType(MULTIPART_ALL)) {
			try {
				final Multipart multipartContent = (Multipart) part.getContent();
				for (int i = 0; i < multipartContent.getCount(); i++) {
					final BodyPart bodyPart = multipartContent.getBodyPart(i);
					findImage(bodyPart);
				}
			} catch (ClassCastException e) {
				throw new MessagingException("Multipart content expected but is: "
						+ part.getContent().getClass().getName());
			}
		} else if (part.isMimeType(MESSAGE_RFC822)) {
			try {
				final Message nestedMsg = (Message) part.getContent();
				findImage(nestedMsg);
			} catch (ClassCastException e) {
				throw new MessagingException("Message instance expected but is: "
						+ part.getContent().getClass().getName());
			}
		}

	}

	public boolean hasAttachment(final Message msg) throws IOException, MessagingException {
		if (msg.isMimeType(MULTIPART_ALL)) {
			try {
				final Multipart mp = (Multipart) msg.getContent();
				final boolean b = hasAttachment(mp);
				// System.out.println("\tMessage \"" + msg.getSubject() + " has"
				// + (b ? " at least one attachment" : " NO attachment"));
				return b;
			} catch (ClassCastException e) {
				LOG.error(e);
			}
		}
		return false;
	}

	private final boolean hasAttachment(final Multipart mp) throws MessagingException, IOException {
		boolean attachmentDetected = false;
		for (int index = 0; index < mp.getCount() && !attachmentDetected; index++) {
			final BodyPart bodyPart = mp.getBodyPart(index);
			if (bodyPart.isMimeType(MULTIPART_ALL)) {
				attachmentDetected = hasAttachment((Multipart) bodyPart.getContent());
			} else {
				final boolean markedAsAttachment = (bodyPart.getDisposition() != null && bodyPart.getDisposition()
						.equalsIgnoreCase(Part.ATTACHMENT));
				final boolean isNonTextPart = !bodyPart.isMimeType("text/*");
				attachmentDetected = markedAsAttachment
						|| isNonTextPart
						|| (bodyPart.isMimeType(MESSAGE_RFC822))
						|| (bodyPart.isMimeType("message/delivery-status")
								|| bodyPart.isMimeType("message/disposition-notification")
								|| bodyPart.isMimeType("text/rfc822-headers") || bodyPart.isMimeType("text/x-vcard")
								|| bodyPart.isMimeType("text/vcard") || bodyPart.isMimeType("text/calendar") || bodyPart
								.isMimeType("text/x-vCalendar"));
			}
		}
		return attachmentDetected;
	}

	public ReplyForwardText getReplyMessageText(final Message msg) throws MessagingException, IOException {
		isHtmlContent = false;
		final StringBuilder msgText = new StringBuilder();
		addPartText(msg, 0, 1, msgText, msg.getReceivedDate(), msg.getFrom(), false);
		return new ReplyForwardText(msgText.toString(), isHtmlContent);
	}

	private final void addPartText(final Part part, final int level, final int partCount,
			final StringBuilder replyText, final Date receivedDate, final Address[] from,
			final boolean multipartAlternative) throws MessagingException, IOException {
		/*
		 * Validate Content-Type
		 */
		final String ct = part.getContentType();
		try {
			new ContentType(ct);
		} catch (ParseException e) {
			throw new MessagingException(new StringBuilder("Bad Content-Type: ").append(ct).toString());
		}
		/*
		 * Process body content dependent on its mime type
		 */
		final boolean displayable = ((part.getDisposition() == null || part.getDisposition().equalsIgnoreCase(
				Part.INLINE)) && part.getFileName() == null);
		if (part.isMimeType("text/plain") || part.isMimeType("text/enriched")) {
			if (displayable) {
				if (multipartAlternative
						&& session.getUserConfiguration().getUserSettingMail().isDisplayHtmlInlineContent()) {
					return;
				}
				plainTextFound = true;
				final UUEncodedMultiPart uuencodedMultipart = new UUEncodedMultiPart(getStringObject(part).toString());
				if (uuencodedMultipart.isUUEncoded()) {
					final UUEncodedPart uuencodedPart = uuencodedMultipart.getBodyPart(0);
					addformatedString(replyText, level, uuencodedPart.getCleanText().toString(), getPrefixLine(
							receivedDate, (InternetAddress[]) from), false);

				} else {
					/*
					 * Just plain text.
					 */
					addformatedString(replyText, level, getStringObject(part).toString(), getPrefixLine(receivedDate,
							(InternetAddress[]) from), false);
					return;
				}
			} else if (!plainTextFound) {
				addformatedString(replyText, level, getStringObject(part).toString(), getPrefixLine(receivedDate,
						(InternetAddress[]) from), false);
				return;
			}
		} else if (part.isMimeType(TEXT_HTML)) {
			if (session.getUserConfiguration().getUserSettingMail().isDisplayHtmlInlineContent()) {
				if (!plainTextFound) {
					plainTextFound = true;
					addformatedString(replyText, level, getStringObject(part).toString(), getPrefixLine(receivedDate,
							(InternetAddress[]) from), true);
					return;
				}
			} else {
				if (!plainTextFound || !multipartAlternative) {
					plainTextFound = true;
					/*
					 * Try to convert the given html to regular text
					 */
					final StringBuilder sb = new StringBuilder();
					final Html2Text parser = new Html2Text(sb);
					new ParserDelegator().parse(new StringReader(getStringObject(part).toString()), parser, true);
					addformatedString(replyText, level, sb.toString(), getPrefixLine(receivedDate,
							(InternetAddress[]) from), false);
				}
			}
		} else if (part.isMimeType("multipart/alternative")) {
			try {
				final Multipart multiPart = (Multipart) part.getContent();
				/*
				 * Invalid multipart/alternative content
				 */
				if (multiPart.getCount() < 2) {
					for (int i = 0; i < multiPart.getCount(); i++) {
						final Part multiPartBodyPart = multiPart.getBodyPart(i);
						addPartText(multiPartBodyPart, level, i + 1, replyText, receivedDate, from, false);
					}
					return;
				}
				for (int i = 0; i < multiPart.getCount(); i++) {
					final Part multiPartBodyPart = multiPart.getBodyPart(i);
					addPartText(multiPartBodyPart, level, i + 1, replyText, receivedDate, from, true);
				}
			} catch (ClassCastException cce) {
				throw new MessagingException(new StringBuilder("Unexpected multipart content: ").append(
						part.getContent().getClass().getName()).toString());
			}
		} else if (part.isMimeType(MULTIPART_ALL)) {
			/*
			 * It's a multipart message. Contains attachments!
			 */
			try {
				final Multipart multiPart = (Multipart) part.getContent();
				for (int i = 0; i < multiPart.getCount(); i++) {
					final Part multiPartBodyPart = multiPart.getBodyPart(i);
					addPartText(multiPartBodyPart, level, i + 1, replyText, receivedDate, from, false);
				}
				return;
			} catch (ClassCastException cce) {
				throw new MessagingException(new StringBuilder("Unexpected multipart content: ").append(
						part.getContent().getClass().getName()).toString(), cce);
			}
		} else if (part.isMimeType(MESSAGE_RFC822)) {
			/*
			 * A nested message
			 */
			try {
				final Message nestedMsg = (Message) part.getContent();
				final JSONMessageParser.ReplyForwardText replyTxt = new JSONMessageParser(this)
						.getReplyMessageText(nestedMsg);
				addformatedString(replyText, level + 1, replyTxt.content, null, replyTxt.isHtml);
				return;
			} catch (ClassCastException cce) {
				throw new MessagingException(new StringBuilder("Unexpected nested message content: ").append(
						part.getContent().getClass().getName()).toString(), cce);
			}
		} else {
			/*
			 * Any other mime type. The getContent() method should return a
			 * String or an InputStream
			 */
			if (displayable) {
				addformatedString(replyText, level, getStringObject(part).toString(), getPrefixLine(receivedDate,
						(InternetAddress[]) from), part.isMimeType(TEXT_HTML));
				return;
			}
		}
	}

	private static final Pattern PATTERN_HTML_START = Pattern.compile("<html>", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_HTML_END = Pattern.compile("</html>", Pattern.CASE_INSENSITIVE);

	private static final String BLOCKQUOTE_START = "<blockquote style=\"PADDING-RIGHT: 5px; PADDING-LEFT: 5px; MARGIN-LEFT: 5px; BORDER-LEFT: #0026ff 2px solid; MARGIN-RIGHT: 5px\">\n";

	private static final String BLOCKQUOTE_END = "</blockquote>\n";

	private final void addformatedString(final StringBuilder fillMe, final int level, final String src,
			final String prefixLine, final boolean isHtmlContent) throws IOException {
		if (isHtmlContent) {
			this.isHtmlContent = true;
		}
		final boolean treatAsHtml = (isHtmlContent || this.isHtmlContent);
		if (prefixLine != null) {
			fillMe.append(prefixLine);
			fillMe.append(treatAsHtml ? "<br/><br/>" : "\n\n");
		}
		if (treatAsHtml) {
			String blockquotedHtml = src;
			Matcher m;
			m = PATTERN_HTML_START.matcher(blockquotedHtml);
			blockquotedHtml = m.replaceFirst(BLOCKQUOTE_START);
			m = PATTERN_HTML_END.matcher(blockquotedHtml);
			blockquotedHtml = m.replaceFirst(BLOCKQUOTE_END);
			fillMe.append(blockquotedHtml);
			return;
		}
		final BufferedReader reader = new BufferedReader(new StringReader(src));
		String line = null;
		while ((line = reader.readLine()) != null) {
			for (int i = 0; i <= level; i++) {
				fillMe.append('>');
			}
			fillMe.append(' ');
			fillMe.append(line).append(('\n'));
		}
	}

	private final String getPrefixLine(final Date receivedDate, final InternetAddress[] addr) {
		String retval = null;
		final StringBuilder sender = new StringBuilder(200);
		if (addr[0].getPersonal() == null) {
			sender.append(addr[0].getAddress());
		} else {
			sender.append(addr[0].getPersonal());
		}
		for (int i = 1; i < addr.length; i++) {
			sender.append(", ");
			if (addr[i].getPersonal() == null) {
				sender.append(addr[i].getAddress());
			} else {
				sender.append(addr[i].getPersonal());
			}
		}
		retval = MailStrings.REPLY_PREFIX;
		retval = retval.replaceFirst("#DATE#", receivedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG,
				session.getLocale()).format(receivedDate));
		retval = retval.replaceFirst("#TIME#", receivedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT,
				session.getLocale()).format(receivedDate));
		retval = retval.replaceFirst("#SENDER#", sender.toString());
		return retval;
	}

	public ReplyForwardText getForwardMessageContent(final Message msg, final Locale locale) throws MessagingException, IOException,
			JSONException, OXException {
		isHtmlContent = false;
		partToLoad = null;
		addToMessageObject = false;
		getFirstTextContent = true;
		parseMessage(msg, threadLevel, 0, 0, locale);
		if (firstTextContent == null) {
			firstTextContent = "";
			isHtmlContent = false;
		}
		String forwardPrefix = MailStrings.FORWARD_PREFIX.replaceFirst("#FROM#",
				addr2String((InternetAddress[]) msg.getFrom())).replaceFirst("#TO#",
				addr2String((InternetAddress[]) msg.getRecipients(RecipientType.TO))).replaceFirst(
				"#DATE#",
				msg.getReceivedDate() == null ? "" : DateFormat.getDateInstance(DateFormat.LONG, session.getLocale())
						.format(msg.getReceivedDate())).replaceFirst(
				"#TIME#",
				msg.getReceivedDate() == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT, session.getLocale())
						.format(msg.getReceivedDate())).replaceFirst("#SUBJECT#", msg.getSubject());
		if (forwardPrefix == null) {
			forwardPrefix = "";
		}
		final String doubleBreak = "<br><br>";
		if (isHtmlContent) {
			forwardPrefix = forwardPrefix.replaceAll("(\\r)?\\n", "<br>");
			final Pattern p = Pattern.compile("<body>", Pattern.CASE_INSENSITIVE);
			final Matcher m = p.matcher(firstTextContent);
			final StringBuffer replaceBuffer = new StringBuffer(1000);
			if (m.find()) {
				m.appendReplacement(replaceBuffer, Matcher.quoteReplacement(new StringBuilder(100).append(doubleBreak)
						.append(m.group()).append(forwardPrefix).append(doubleBreak).toString()));
			} else {
				replaceBuffer.append(doubleBreak).append(m.group()).append(forwardPrefix).append(doubleBreak);
			}
			m.appendTail(replaceBuffer);
			firstTextContent = replaceBuffer.toString();
		} else {
			forwardPrefix = forwardPrefix.replaceAll("(\\r)?\\n", "<br>");
			firstTextContent = new StringBuilder(1000).append(doubleBreak).append(forwardPrefix).append(doubleBreak)
					.append(firstTextContent).toString();// replaceAll("<br/?>","\n");
		}
		return new ReplyForwardText(firstTextContent, isHtmlContent);
	}
	
	private static final String addr2String(final InternetAddress[] addrs) {
		final StringBuilder sb = new StringBuilder(200);
		if (addrs[0].getPersonal() == null) {
			sb.append(addrs[0].getAddress());
		} else {
			sb.append(addrs[0].getPersonal());
		}
		for (int i = 1; i < addrs.length; i++) {
			sb.append(", ");
			if (addrs[i].getPersonal() == null) {
				sb.append(addrs[i].getAddress());
			} else {
				sb.append(addrs[i].getPersonal());
			}
		}
		return sb.toString();
	}

	/**
	 * Converts given instance of <code>javax.mail.Message</code> to an
	 * instance of
	 * <code>com.openexchange.groupware.container.mail.MessageObject</code>
	 * 
	 * @param msg
	 * @param threadLevel
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 * @throws JSONException
	 * @throws OXException 
	 */
	public JSONMessageObject parseMessage(final Message msg, final int threadLevel, final Locale locale) throws MessagingException,
			IOException, JSONException, OXException {
		if (msg.getFolder() == null) {
			msgUID = null;
		} else {
			final IMAPFolder imapFolder = (IMAPFolder) msg.getFolder();
			msgUID = new StringBuilder(imapFolder.getFullName()).append(Mail.SEPERATOR).append(imapFolder.getUID(msg))
					.toString();
		}
		return parseMessage(msg, threadLevel, 0, 0, locale);
	}

	private final JSONMessageObject parseMessage(final Message msg, final int threadLevel, final int messageLevel, final int multipartLevel, final Locale locale)
			throws MessagingException, IOException, JSONException, OXException {
		this.threadLevel = threadLevel;
		final JSONMessageObject msgObj = new JSONMessageObject(session.getUserConfiguration().getUserSettingMail(),
				TimeZone.getTimeZone(session.getUserObject().getTimeZone()));
		msgObj.setThreadLevel(threadLevel);
		final Flags flags = msg.getFlags();
		/*
		 * SYSTEM FLAGS
		 */
		final Flags.Flag[] sf = flags.getSystemFlags();
		for (int i = 0; i < sf.length; i++) {
			final Flags.Flag f = sf[i];
			if (f == Flags.Flag.ANSWERED) {
				msgObj.setAnswered(true);
			} else if (f == Flags.Flag.DELETED) {
				msgObj.setDeleted(true);
			} else if (f == Flags.Flag.DRAFT) {
				msgObj.setDraft(true);
			} else if (f == Flags.Flag.FLAGGED) {
				msgObj.setFlagged(true);
			} else if (f == Flags.Flag.RECENT) {
				msgObj.setRecent(true);
			} else if (f == Flags.Flag.SEEN) {
				msgObj.setSeen(true);
			} else {
				/*
				 * Skip it
				 */
				continue;
			}
		}
		/*
		 * USER FLAGS
		 */
		final String[] uf = flags.getUserFlags();
		for (int i = 0; i < uf.length; i++) {
			msgObj.addUserFlag(uf[i]);
		}
		/*
		 * Content has not been sent to client, yet: Flag /SEEN is not set
		 */
		if (!msg.getFlags().contains(Flags.Flag.SEEN)) {
			final String[] dispNotification = msg.getHeader("Disposition-Notification-To");
			if (dispNotification != null && dispNotification.length > 0) {
				final StringBuilder addrsBuf = new StringBuilder();
				for (int i = 0; i < dispNotification.length; i++) {
					if (addrsBuf.length() > 0) {
						addrsBuf.append(',');
					}
					addrsBuf.append(dispNotification[0]);
				}
				msgObj.setDispositionNotification(addrsBuf.toString());
			}
		}
		parsePart(msg, msgObj, messageLevel, 0, 1, locale, false);
		return msgObj;
	}

	private final void parsePart(final Part part, final JSONMessageObject msgObj, final int messageLevelArg,
			final int multipartLevelArg, final int partCountArg, final Locale locale, final boolean multipartAlternative)
			throws MessagingException, IOException, JSONException, OXException {
		if ((!addToMessageObject && loadedJSONMsgAttachObj != null)
				|| (getFirstTextContent && firstTextContent != null)) {
			/*
			 * No need for further message traversal, cause attachment or first
			 * text was already found
			 */
			return;
		}
		/*
		 * Parse message header
		 */
		if (part instanceof Message && addToMessageObject) {
			final Message msg = (Message) part;
			parseMessageEnvelope(msg, msgObj);
		}
		int messageLevel = messageLevelArg;
		int multipartLevel = multipartLevelArg;
		int partCount = partCountArg;
		/*
		 * Part informations
		 */
		final String filename = getFileName(part, messageLevel, multipartLevel, partCount);
		String charset = UTF_8;
		String baseContentType = "application/octet-stream";
		try {
			if (part.getContentType() != null) {
				final ContentType contentType = new ContentType(part.getContentType());
				baseContentType = contentType.getBaseType();
				charset = contentType.getParameter("charset");
			}
		} catch (Exception e) {
			/*
			 * Try to determine MIME type from file name
			 */
			if (filename != null) {
				baseContentType = new MimetypesFileTypeMap().getContentType(new File(filename).getName()).toLowerCase(
						locale);
			}
		}
		final String disposition = part.getDisposition();
		final int size = part.getSize();
		final boolean hasInlineCharacteristics = ((disposition == null || Part.INLINE.equalsIgnoreCase(disposition)) && part
				.getFileName() == null);
		final boolean hasAttachmentCharacteristics = (Part.ATTACHMENT.equalsIgnoreCase(disposition) || part
				.getFileName() != null);
		/*
		 * Process body content dependent on its mime type
		 */
		if (part.isMimeType("text/plain") || part.isMimeType("text/enriched")) {
			/*
			 * Plain text content
			 */
			if (addToMessageObject && multipartAlternative
					&& session.getUserConfiguration().getUserSettingMail().isDisplayHtmlInlineContent()
					&& !plainTextFound) {
				/*
				 * User wants to see html content. Ignore plain-text version of
				 * multipart/alternative, but remember that plaintext was
				 * already found.
				 */
				plainTextFound = true;
			} else {
				/*
				 * Normal directly displayable text. Call the
				 * detachUUencodedFiles() routine to find possible
				 * Outlook-UUencoded attachments.
				 */
				if (hasInlineCharacteristics) {
					final int[] newLevelArr = detachUUencodedFiles(getStringObject(part), true, baseContentType, size,
							msgObj, new int[] { messageLevel, multipartLevel, partCount }, locale);
					messageLevel = newLevelArr[0];
					multipartLevel = newLevelArr[1];
					partCount = newLevelArr[2];
					if ((!addToMessageObject && loadedJSONMsgAttachObj != null)
							|| (getFirstTextContent && firstTextContent != null)) {
						return;
					}
				} else {
					/*
					 * Disposition indicates an attachment. Create
					 * attachment-object
					 */
					final String attachmentID = getAttachmentPositionInMail(messageLevel, multipartLevel, partCount);
					JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(attachmentID);
					mao.setSize(size);
					mao.setContentType(baseContentType);
					mao.setDisposition(Part.ATTACHMENT);
					mao.setFileName(filename);
					if (addToMessageObject) {
						/*
						 * Does content fit into max allowed size?
						 */
						if (size <= IMAPProperties.getAttachmentDisplaySizeLimit()
								&& partIsOpen(baseContentType, partCount, locale)) {
							mao.setContent(getStringObject(part).toString());
							mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
						} else {
							mao.setContent(null);
							mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
						}
						msgObj.addMessageAttachment(mao);
						mao = null;
					} else if (attachmentID.equalsIgnoreCase(partToLoad)) {
						mao.setContent(part.getInputStream());
						mao.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
						loadedJSONMsgAttachObj = mao;
						loadedPart = part;
						mao = null;
						return;
					}
				}
			}
		} else if (part.isMimeType(TEXT_HTML)) {
			final String attachmentID = getAttachmentPositionInMail(messageLevel, multipartLevel, partCount);
			JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(attachmentID);
			mao.setContentType(baseContentType);
			mao.setSize(size);
			mao.setFileName(filename);
			if (addToMessageObject || (getFirstTextContent && firstTextContent == null)) {
				if ((multipartAlternative || !plainTextFound)
						&& session.getUserConfiguration().getUserSettingMail().isDisplayHtmlInlineContent()) {
					if (getFirstTextContent && firstTextContent == null) {
						firstTextContent = getFormattedText(getStringObject(part).toString(), true);
						isHtmlContent = true;
						return;
					}
					/*
					 * Attach text-object
					 */
					mao.setDisposition(Part.INLINE);
					mao.setContent(getFormattedText(getStringObject(part).toString(), true));
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
				} else {
					if (size <= IMAPProperties.getAttachmentDisplaySizeLimit()
							&& partIsOpen(baseContentType, partCount, locale)) {
						/*
						 * Attach link-object
						 */
						mao.setDisposition(Part.ATTACHMENT);
						mao.setContent(getStringObject(part));
						mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
					} else {
						if (!multipartAlternative || !plainTextFound) {
							if (hasAttachmentCharacteristics) {
								mao.setDisposition(Part.ATTACHMENT);
								mao.setContent(null);
								mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
							} else {
								final StringBuilder sb = new StringBuilder();
								try {
									/*
									 * Try to convert the given html to regular
									 * text
									 */
									final Html2Text parser = new Html2Text(sb);
									new ParserDelegator().parse(new StringReader(getStringObject(part).toString()),
											parser, true);
									if (getFirstTextContent && firstTextContent == null) {
										firstTextContent = getFormattedText(sb.toString(), false);
										return;
									}
									mao.setDisposition(Part.INLINE);
									mao.setContent(getFormattedText(sb.toString(), false));
									mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
								} catch (Exception e) {
									final Throwable t = new Throwable(
											"WARN: Unable to parse html2text for message view: "
													+ e.getLocalizedMessage());
									LOG.error(t.getMessage(), t);
								}
							}
						} else {
							mao.setDisposition(Part.ATTACHMENT);
							mao.setContent(null);
							mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
						}
					}
				}
				msgObj.addMessageAttachment(mao);
				mao = null;
			} else if (attachmentID.equalsIgnoreCase(partToLoad)) {
				mao.setContent(part.getInputStream());
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				loadedJSONMsgAttachObj = mao;
				loadedPart = part;
				mao = null;
				return;
			}
		} else if (part.isMimeType("message/delivery-status") || part.isMimeType("message/disposition-notification")
				|| part.isMimeType("text/rfc822-headers") || part.isMimeType("text/x-vcard")
				|| part.isMimeType("text/vcard") || part.isMimeType("text/calendar")
				|| part.isMimeType("text/x-vCalendar")) {
			final String attachmentID = getAttachmentPositionInMail(messageLevel, multipartLevel, partCount);
			JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(attachmentID);
			mao.setSize(size);
			mao.setFileName(filename);
			mao.setContentType(baseContentType);
			mao.setDisposition(Part.ATTACHMENT);
			if (addToMessageObject) {
				if (partIsOpen(baseContentType, partCount, locale)) {
					if (part.isMimeType("text/x-vcard") || part.isMimeType("text/vcard")) {
						mao.setContent(getvCardParameter(getStringObject(part), baseContentType, charset));
						mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
					} else {
						mao.setContent(getStringObject(part));
						mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
					}
				} else {
					mao.setContent(null);
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				}
				msgObj.addMessageAttachment(mao);
				mao = null;
			} else if (attachmentID.equalsIgnoreCase(partToLoad)) {
				mao.setContent(part.getInputStream());
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				loadedJSONMsgAttachObj = mao;
				loadedPart = part;
				mao = null;
				return;
			}
		} else if (part.isMimeType("image/*")) {
			final String contentId = ((javax.mail.internet.MimePart) part).getContentID();
			final String attachmentID = getAttachmentPositionInMail(messageLevel, multipartLevel, partCount);
			JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(attachmentID);
			mao.setSize(size);
			mao.setFileName(filename);
			mao.setContentType(baseContentType);
			if (addToMessageObject) {
				if ((imageContentIDs.contains(contentId)
						|| session.getUserConfiguration().getUserSettingMail().isDisplayHtmlInlineContent() || contentId == null)
						&& imageContentIdToLoad == null) {
					mao.setDisposition(Part.ATTACHMENT);
					mao.setContent(null);
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				} else if (imageContentIdToLoad != null && contentId != null
						&& contentId.indexOf(imageContentIdToLoad) != -1) {
					mao.setDisposition(Part.INLINE);
					mao.setContent(getContentBytes(part.getInputStream()));
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_BYTE_ARRAY);
				} else {
					mao.setDisposition(Part.ATTACHMENT);
					mao.setContent(null);
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				}
				msgObj.addMessageAttachment(mao);
				mao = null;
			} else if (attachmentID.equalsIgnoreCase(partToLoad)) {
				mao.setContent(part.getInputStream());
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				loadedJSONMsgAttachObj = mao;
				loadedPart = part;
				mao = null;
				return;
			}
		} else if (part.isMimeType(MULTIPART_ALL)) {
			final Multipart mp = (Multipart) part.getContent();
			final int count = mp.getCount();
			final boolean isAlternative = (part.isMimeType("multipart/alternative") && count >= 2);
			for (int i = 0; i < mp.getCount(); i++) {
				final Part multiPartBodyPart = mp.getBodyPart(i);
				parsePart(multiPartBodyPart, msgObj, messageLevel, multipartLevel + 1, i + 1, locale, isAlternative);
				if ((!addToMessageObject && loadedJSONMsgAttachObj != null)
						|| (getFirstTextContent && firstTextContent != null)) {
					return;
				}
			}
		} else if (part.isMimeType(MESSAGE_RFC822)) {
			/*
			 * A nested message
			 */
			try {
				final Message nestedMsg = (Message) part.getContent();
				final JSONMessageParser msgParser = new JSONMessageParser(this);
				final JSONMessageObject nestedMsgObj = msgParser.parseMessage(nestedMsg, threadLevel, messageLevel + 1, multipartLevel, locale);
				msgObj.addNestedMessage(nestedMsgObj);
			} catch (ClassCastException cce) {
				throw new MessagingException(new StringBuilder("Unexpected nested message content: ").append(
						part.getContent().getClass().getName()).toString(), cce);
			}
		} else {
			/*
			 * Unknown MIME type
			 */
			final String attachmentID = getAttachmentPositionInMail(messageLevel, multipartLevel, partCount);
			JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(attachmentID);
			mao.setContentType(baseContentType);
			mao.setDisposition(disposition);
			mao.setSize(size);
			mao.setFileName(filename);
			if (addToMessageObject) {
				if (hasInlineCharacteristics) {
					try {
						if (getFirstTextContent && firstTextContent == null) {
							firstTextContent = getFormattedText(getStringObject(part).toString(), false);
							isHtmlContent = part.isMimeType(TEXT_HTML);
							return;
						}
						/*
						 * Attachment is displayable
						 */
						mao.setContent(getFormattedText(getStringObject(part).toString(), false));
						mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
					} catch (MessagingException exc) {
						throw new MessagingException(new StringBuilder("Unexpected part content: ").append(
								part.getContent().getClass().getName()).toString(), exc);
					}
				} else if (part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
					mao.setContent(null);
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				} else {
					mao.setDisposition(Part.ATTACHMENT);
					mao.setContent(null);
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
				}
				msgObj.addMessageAttachment(mao);
				mao = null;
			} else if (attachmentID.equalsIgnoreCase(partToLoad)) {
				if (size <= IMAPProperties.getAttachmentDisplaySizeLimit() && part.isMimeType("text/*")) {
					mao.setContent(getStringObject(part));
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
				}
				// else if (size <=
				// IMAPProperties.getAttachmentDisplaySizeLimit())
				// mao.setContent(getContentBytes(part.getInputStream()));
				else {
					mao.setContent(part.getInputStream());
					mao.setContentID(JSONMessageAttachmentObject.CONTENT_INPUT_STREAM);
				}
				loadedJSONMsgAttachObj = mao;
				loadedPart = part;
				mao = null;
				return;
			}
		}
	}

	private final static String getFileName(final Part p, final int messageLevel, final int multipartLevel,
			final int partCount) throws MessagingException {
		String filename = p.getFileName();
		if (filename == null || isEmptyString(filename)) {
			filename = new StringBuilder(20).append("Part ").append(messageLevel).append('.').append(multipartLevel)
					.append('.').append(partCount).toString();
		} else {
			try {
				filename = MimeUtility.decodeText(filename.replaceAll("\\?==\\?", "?= =?"));
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return filename;
	}

	private final static boolean isEmptyString(final String str) {
		final char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isWhitespace(chars[i])) {
				return false;
			}
		}
		return true;
	}

	private final static byte[] getContentBytes(final InputStream is) throws IOException, MessagingException {
		if (is == null) {
			return null;
		}
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// byte[] bytes = null;
			final byte[] tmpBytes = new byte[8192];
			int bytesRead = -1;
			while ((bytesRead = is.read(tmpBytes, 0, tmpBytes.length)) >= 0) {
				bos.write(tmpBytes, 0, bytesRead);
			}
			return bos.toByteArray();
		} finally {
			is.close();
		}
	}

	private final int[] detachUUencodedFiles(StringBuilder sPart, final boolean inline, final String baseContentType,
			final int contentSize, final JSONMessageObject msgObj, final int[] levelArr, final Locale locale) throws IOException,
			MessagingException {
		try {
			UUEncodedMultiPart uump = new UUEncodedMultiPart(sPart.toString());
			try {
				if (uump.isUUEncoded()) {
					/*
					 * UUEncoded content detected. First, append normal text.
					 */
					JSONMessageAttachmentObject mao = null;
					if (addToMessageObject) {
						/*
						 * Attach formatted text-object
						 */
						mao = new JSONMessageAttachmentObject(getAttachmentPositionInMail(levelArr[0],
								levelArr[1], levelArr[2]));
						mao.setDisposition(inline ? Part.INLINE : null);
						mao.setContentType(baseContentType);
						mao.setContent(getFormattedText(uump.getCleanText(), false));
						mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
						mao.setSize(uump.getCleanText().length());
						msgObj.addMessageAttachment(mao);
						mao = null;
						plainTextFound = (uump.getCleanText().length() > 0);
					} else if (getFirstTextContent && firstTextContent == null && uump.getCleanText().length() > 0) {
						firstTextContent = getFormattedText(uump.getCleanText(), false);
						return levelArr;
					}
					/*
					 * Append all uuencoded attachments
					 */
					mao = new JSONMessageAttachmentObject();
					for (int a = 0; a < uump.getCount(); a++) {
						/*
						 * Increment part count by 1
						 */
						levelArr[2] = levelArr[2] + 1;
						final UUEncodedPart ump = uump.getBodyPart(a);
						String contentType = "application/octet-stream";
						final String filename = ump.getFileName();
						final int size = ump.getFileSize();
						try {
							contentType = new MimetypesFileTypeMap().getContentType(
									new File(filename.toLowerCase(locale)).getName()).toLowerCase(locale);
						} catch (Exception e) {
							final Throwable t = new Throwable(new StringBuilder(
									"WARN: Unable to fetch content/type for '").append(filename).append("': ")
									.append(e).toString());
							LOG.error(t.getMessage(), t);
						}
						final String attachmentID = getAttachmentPositionInMail(levelArr[0], levelArr[1], levelArr[2]);
						mao.setPositionInMail(attachmentID);
						mao.setContentType(contentType);
						mao.setFileName(filename);
						mao.setSize(size);
						mao.setDisposition(Part.ATTACHMENT);
						if (addToMessageObject) {
							/*
							 * Content-type indicates mime type text/*
							 */
							if (contentType.startsWith("text/")) {
								/*
								 * Attach link-object with text content
								 */
								mao.setContent(ump.getPart().toString());
								mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
							} else {
								/*
								 * Attach link-object.
								 */
								mao.setContent(null);
								mao.setContentID(JSONMessageAttachmentObject.CONTENT_NONE);
							}
							msgObj.addMessageAttachment((JSONMessageAttachmentObject) mao.clone());
						} else if (attachmentID.equalsIgnoreCase(partToLoad)) {
							/*
							 * Requested message attachment found!
							 */
							mao.setContent(getContentBytes(ump.getInputStream()));
							mao.setContentID(JSONMessageAttachmentObject.CONTENT_BYTE_ARRAY);
							loadedJSONMsgAttachObj = mao;
							mao = null;
							return levelArr;
						}
						mao.reset();
					}
					mao = null;
				} else {
					if (addToMessageObject) {
						/*
						 * No UUEncoded content detected. Treat as normal text
						 * and attach formatted text-object
						 */
						JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject(getAttachmentPositionInMail(
								levelArr[0], levelArr[1], levelArr[2]));
						mao.setDisposition(inline ? Part.INLINE : null);
						mao.setContentType(baseContentType);
						mao.setContent(getFormattedText(sPart.toString(), false));
						mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
						mao.setSize(contentSize);
						msgObj.addMessageAttachment(mao);
						mao = null;
						plainTextFound = (sPart.length() > 0);
					} else if (getFirstTextContent && firstTextContent == null) {
						firstTextContent = getFormattedText(sPart.toString(), false);
						return levelArr;
					}
				}
				return new int[] { levelArr[0], levelArr[1], levelArr[2]};
			} finally {
				uump = null;
				sPart = null;
			}
		} catch (MessagingException e) {
			throw e;
		} catch (Exception e) {
			throw new MessagingException("Exception while working on UUEncoded content", e);
		}
	}

	private static final Pattern PLAIN_TEXT_QUOTE_PATTERN = Pattern.compile("(\\G\\s?>)");

	private final String getFormattedText(String text, final boolean isHtmlContent) throws MessagingException {
		final StringBuilder formattedText = new StringBuilder();
		if (isHtmlContent) {
			String htmlContent = text;
			if (session.getUserConfiguration().getUserSettingMail().isUseColorQuote()) {
				htmlContent = new StringBuilder(doHtmlColorQuoting(htmlContent)).toString();
			}
			if (session.getUserConfiguration().getUserSettingMail().isDisplayHtmlInlineContent()) {
				formattedText.append(filterInlineImages(htmlContent));
			} else {
				formattedText.append(htmlContent);
				formattedText.append('\n');
			}
			return formattedText.toString();
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(text.toString()));
			String line = "";
			int quotelevel_before = 0;
			while ((line = reader.readLine()) != null) {
				if (session.getUserConfiguration().getUserSettingMail().isUseColorQuote()) {
					int quotelevel = 0;
					final Matcher quoteMatcher = PLAIN_TEXT_QUOTE_PATTERN.matcher(line);
					final StringBuffer sb = new StringBuffer(line.length());
					while (quoteMatcher.find()) {
						quotelevel++;
						quoteMatcher.appendReplacement(sb, " ");
					}
					quoteMatcher.appendTail(sb);
					line = MailTools.htmlFormat(sb.toString());
					if (session.getUserConfiguration().getUserSettingMail().isShowGraphicEmoticons()) {
						line = filterEmoticons(line);
					}
					if (quotelevel > quotelevel_before) {
						for (int u = 0; u < (quotelevel - quotelevel_before); u++) {
							final StringBuilder colorBuilder = new StringBuilder();
							final String styleStart = " style=\"color:";
							final String borderColor = "; border-color:";
							final String styleEnd = ";\"";
							if ((u + quotelevel_before) >= IMAPProperties.getQuoteLineColors().length) {
								colorBuilder
										.append(styleStart)
										.append(
												IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
										.append(borderColor)
										.append(
												IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
										.append(styleEnd);
							} else if ((u + quotelevel_before) < IMAPProperties.getQuoteLineColors().length) {
								colorBuilder.append(styleStart).append(
										IMAPProperties.getQuoteLineColors()[u + quotelevel_before]).append(borderColor)
										.append(IMAPProperties.getQuoteLineColors()[u + quotelevel_before]).append(
												styleEnd);
							}
							formattedText.append("<blockquote type=cite").append(colorBuilder).append('>');
						}
					} else if (quotelevel < quotelevel_before) {
						for (int u = 0; u < (quotelevel_before - quotelevel); u++) {
							formattedText.append("</blockquote>");
						}
					}
					formattedText.append(MailTools.formatHrefLinks(line)).append(HTML_BREAK);
					quotelevel_before = quotelevel;
				} else {
					line = MailTools.formatHrefLinks(MailTools.htmlFormat(line));
					if (session.getUserConfiguration().getUserSettingMail().isShowGraphicEmoticons()) {
						line = filterEmoticons(line);
					}
					formattedText.append(line).append(HTML_BREAK);
				}
			}
		} catch (Exception ioe) {
			LOG.error(ioe.getMessage(), ioe);
			throw new MessagingException("The content of this part cannot be displayed", ioe);
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException ioe) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
		return formattedText.toString();
	}

	private static final Pattern HTML_QUOTE_PATTERN = Pattern.compile("(\\G\\s?&gt;)");

	private final String doHtmlColorQuoting(final String htmlContent) throws MessagingException {
		final StringBuffer htmlBuffer = new StringBuffer(htmlContent.length());
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(htmlContent));
			int quotelevel_before = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				int quotelevel = 0;
				final Matcher quoteMatcher = HTML_QUOTE_PATTERN.matcher(line);
				final StringBuffer quoteBuffer = new StringBuffer(line.length());
				while (quoteMatcher.find()) {
					quotelevel++;
					quoteMatcher.appendReplacement(quoteBuffer, " ");
				}
				quoteMatcher.appendTail(quoteBuffer);
				line = quoteBuffer.toString();
				if (quotelevel > quotelevel_before) {
					for (int u = 0; u < (quotelevel - quotelevel_before); u++) {
						final StringBuilder colorBuilder = new StringBuilder();
						final String styleStart = " style=\"color:";
						final String borderColor = "; border-color:";
						final String styleEnd = ";\"";
						if ((u + quotelevel_before) >= IMAPProperties.getQuoteLineColors().length) {
							colorBuilder
									.append(styleStart)
									.append(
											IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
									.append(borderColor)
									.append(
											IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
									.append(styleEnd);
						} else if ((u + quotelevel_before) < IMAPProperties.getQuoteLineColors().length) {
							colorBuilder.append(styleStart).append(
									IMAPProperties.getQuoteLineColors()[u + quotelevel_before]).append(borderColor)
									.append(IMAPProperties.getQuoteLineColors()[u + quotelevel_before])
									.append(styleEnd);
						}
						htmlBuffer.append("<blockquote type=cite").append(colorBuilder).append('>');
					}
				} else if (quotelevel < quotelevel_before) {
					for (int u = 0; u < (quotelevel_before - quotelevel); u++) {
						htmlBuffer.append("</blockquote>");
					}
				}
				htmlBuffer.append(line).append('\n');
				quotelevel_before = quotelevel;
			}
			return htmlBuffer.toString();
		} catch (Exception ioe) {
			LOG.error(ioe.getMessage(), ioe);
			throw new MessagingException("Colour-Quoting failed", ioe);
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException ioe) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
	}

	private final boolean partIsOpen(final String contentType, final int partCount, final Locale locale) {
		// TODO: REMOVE THIS METHOD!!!!!!!!!!!!!1
		// html preview on = open it as default
		final Integer key = Integer.valueOf(partCount);
		if (contentType.toLowerCase(locale).startsWith("text/htm")
				&& session.getUserConfiguration().getUserSettingMail().isDisplayHtmlInlineContent()
				&& !openAttachments.containsKey(key)) {
			openAttachments.put(key, Boolean.TRUE);
		}
		return openAttachments.containsKey(key) ? openAttachments.get(key).booleanValue() : false;
	}

	private static final Pattern IMG_PATTERN = Pattern.compile("(<img[^>]*>)");

	private static final Pattern CID_PATTERN = Pattern.compile("cid:([^\\s>]*)|\"cid:([^\"]*)\"");

	private final String filterInlineImages(final String content) {
		String reval = content;
		try {
			final Matcher imgMatcher = IMG_PATTERN.matcher(reval);
			final StringBuffer sb = new StringBuffer(reval.length());
			while (imgMatcher.find()) {
				final String foundImg = imgMatcher.group(1);
				final Matcher cidMatcher = CID_PATTERN.matcher(foundImg);
				final StringBuffer cidBuffer = new StringBuffer(foundImg.length());
				while (cidMatcher.find()) {
					final String cid = (cidMatcher.group(1) == null ? cidMatcher.group(2) : cidMatcher.group(1));
					imageContentIDs.add(cid);
					final StringBuilder linkBuilder = new StringBuilder().append("\"/ajax/mail?").append(
							Mail.PARAMETER_SESSION).append('=').append(session.getSecret()).append('&').append(
							Mail.PARAMETER_ACTION).append('=').append(Mail.ACTION_MATTACH).append('&').append(
							Mail.PARAMETER_ID).append('=').append(msgUID).append('&').append(Mail.PARAMETER_MAILCID)
							.append('=').append(cid).append('"');
					cidMatcher.appendReplacement(cidBuffer, Matcher.quoteReplacement(linkBuilder.toString()));
				}
				cidMatcher.appendTail(cidBuffer);
				imgMatcher.appendReplacement(sb, Matcher.quoteReplacement(cidBuffer.toString()));
			}
			imgMatcher.appendTail(sb);
			reval = sb.toString();
		} catch (Exception e) {
			LOG.warn("Unable to filter cid Images: " + e.getMessage());
		}
		return reval;
	}

	private static final String[] EMOTICONS = new String[] { ";-\\)", "20.gif", ";\\)", "20.gif", ":-D", "19.gif",
			":D", "19.gif", ":-\\)", "18.gif", ":\\)", "18.gif", ":-\\(", "14.gif", ":\\(", "14.gif", ":-O", "09.gif",
			":O", "09.gif", ":-\\*", "07.gif" };

	private final String filterEmoticons(final String line) {
		String retval = line;
		try {
			for (int a = 0; a < EMOTICONS.length; a += 2) {
				final String imgDir = ComfireConfig.properties.getProperty("IMGPATH")+"/";
				final StringBuilder replacementBuilder = new StringBuilder(" <img src=\"").append(imgDir)
						.append("/smilies/").append(EMOTICONS[a + 1]).append("\" align=\"absmiddle\">");
				retval = retval.replaceAll("(^| )" + EMOTICONS[a], replacementBuilder.toString());
			}
		} catch (Exception e) {
			LOG.warn("Unable to filter Emoticons: " + e.getMessage());
		}
		return retval;
	}

	private StringBuilder getvCardParameter(final StringBuilder text, final String ct, final String charset) {
		final StringBuilder ret = new StringBuilder();
		try {
			// ComfireLogger.log("ERROR: getvCardParameter(...): parse vCard
			// parameter: ct='" + ct + "', ch='" + charset
			// + "'", ComfireLogger.ERROR);
			// VersitDefinition def = Versit.getDefinition(ct);
			// VersitDefinition.Reader r = def.getReader(new
			// ByteArrayInputStream(text.toString().getBytes(charset)),
			// charset);
			// VersitObject vo = def.parse(r);
			//
			// OXConverter oxc = new OXConverter(no);
			// try {
			// oxcontact = oxc.convertContact(vo);
			// } finally {
			// oxc.close();
			// }
			//
			// if (oxcontact.getFirstname() != null) {
			// ret.append(oxcontact.getFirstname() + " ");
			// }
			// if (oxcontact.getLastname() != null) {
			// ret.append(oxcontact.getLastname() + "\n");
			// }
			// if (oxcontact.get(OXContact.DEPARTMENT) != null) {
			// ret.append(oxcontact.get(OXContact.DEPARTMENT) + "\n");
			// }
			// if (oxcontact.get(OXContact.COMPANY) != null) {
			// ret.append(oxcontact.get(OXContact.COMPANY) + "\n");
			// }
			// vo = null;
			// oxcontact = null;
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			return text;
		}
		return ret;
	}

	private static final void parseMessageEnvelope(final Message msg, final JSONMessageObject msgObj)
			throws JSONException, MessagingException {
		Address[] addrs;
		/*
		 * FROM
		 */
		if ((addrs = msg.getFrom()) != null) {
			fillAddresses((InternetAddress[]) addrs, msgObj.getFrom());
		}
		/*
		 * TO
		 */
		if ((addrs = msg.getRecipients(Message.RecipientType.TO)) != null) {
			fillAddresses((InternetAddress[]) addrs, msgObj.getTo());
		}
		/*
		 * CC
		 */
		if ((addrs = msg.getRecipients(Message.RecipientType.CC)) != null) {
			fillAddresses((InternetAddress[]) addrs, msgObj.getCc());
		}
		/*
		 * BCC
		 */
		if ((addrs = msg.getRecipients(Message.RecipientType.BCC)) != null) {
			fillAddresses((InternetAddress[]) addrs, msgObj.getBcc());
		}
		/*
		 * SUBJECT
		 */
		msgObj.setSubject(msg.getSubject());
		/*
		 * SENT DATE
		 */
		msgObj.setSentDate(msg.getSentDate());
		/*
		 * RECEIVED DATE
		 */
		msgObj.setReceivedDate(msg.getReceivedDate());
		/*
		 * Headers
		 */
		for (final Enumeration e = msg.getAllHeaders(); e.hasMoreElements();) {
			final Header hdr = (Header) e.nextElement();
			if ("Disposition-Notification-To".equalsIgnoreCase(hdr.getName())) {
				/*
				 * Disposition-Notification: Indicate an expected read ack if this
				 * header is available and message has not been read before
				 */
				if (!(msg.isSet(Flags.Flag.SEEN))) {
					msgObj.setDispositionNotification(hdr.getValue());
				}
			} else if ("X-Priority".equalsIgnoreCase(hdr.getName())) {
				/*
				 * Priority
				 */
				final String[] tmp = hdr.getValue().split(" +");
				msgObj.setPriority(Integer.parseInt(tmp[0]));
			} else if ("X-Msgref".equalsIgnoreCase(hdr.getName())) {
				/*
				 * Message Reference
				 */
				final String[] tmp = hdr.getValue().split(" +");
				msgObj.setMsgref(tmp[0]);
			} else if ("X-Mailer".equalsIgnoreCase(hdr.getName())) {
				msgObj.addHeader(hdr.getName(), hdr.getValue());
			} else {
				continue;
				//msgObj.addHeader(hdr.getName(), hdr.getValue());
			}
		}
	}

	private static final void fillAddresses(final InternetAddress[] addrs, final List<InternetAddress> addrsList) {
		addrsList.addAll(Arrays.asList(addrs));
	}

	private static final StringBuilder getStringObject(final Part p) throws MessagingException {
		final StringBuilder text = new StringBuilder(1000);
		InputStream is = null;
		BufferedReader br = null;
		try {
			try {
				br = new BufferedReader(new StringReader((String) p.getContent()));
			} catch (ClassCastException cce) {
				br = getBufferedReaderFromPart(p);
			}
			String contentLine = null;
			while ((contentLine = br.readLine()) != null) {
				text.append(contentLine).append('\n');
			}
		} catch (UnsupportedEncodingException e) {
			try {
				br = getBufferedReaderFromPart(p);
				String contentLine = null;
				while ((contentLine = br.readLine()) != null) {
					text.append(contentLine).append('\n');
				}
			} catch (Exception innerExc) {
				throw new MessagingException(innerExc.getMessage(), innerExc);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new MessagingException(e.getMessage());
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException ioe) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
		return text;
	}

	private static final BufferedReader getBufferedReaderFromPart(final Part p) throws MessagingException {
		BufferedReader br = null;
		InputStream partInputStream = null;
		try {
			partInputStream = p.getInputStream();
			if (!(partInputStream instanceof BufferedInputStream)) {
				partInputStream = new BufferedInputStream(partInputStream);
			}
			br = new BufferedReader(new InputStreamReader(partInputStream));
		} catch (Exception ee) {
			/*
			 * Common way does not work, try to read from raw stream
			 */
			if (p instanceof MimeBodyPart) {
				final MimeBodyPart mpb = (MimeBodyPart) p;
				partInputStream = mpb.getRawInputStream();
				if (!(partInputStream instanceof BufferedInputStream)) {
					partInputStream = new BufferedInputStream(partInputStream);
				}
				br = new BufferedReader(new InputStreamReader(partInputStream));
			} else if (p instanceof MimeMessage) {
				final MimeMessage mm = (MimeMessage) p;
				partInputStream = mm.getRawInputStream();
				if (!(partInputStream instanceof BufferedInputStream)) {
					partInputStream = new BufferedInputStream(partInputStream);
				}
				br = new BufferedReader(new InputStreamReader(partInputStream));
			}
		}
		return br;
	}

	public static final String getAttachmentPositionInMail(final int messageLevel, final int multipartLevel, final int partCount) {
		return new StringBuilder().append(messageLevel).append('.').append(multipartLevel).append('.').append(partCount).toString();
	}

	public static class ReplyForwardText {

		private final boolean isHtml;

		private final String content;

		public ReplyForwardText(String content, boolean isHtml) {
			this.content = content;
			this.isHtml = isHtml;
		}

		public String getContent() {
			return content;
		}

		public boolean isHtml() {
			return isHtml;
		}

	}

}
