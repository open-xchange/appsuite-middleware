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

package com.openexchange.mail.mime.utils;

import static com.openexchange.mail.utils.MessageUtility.decodeMultiEncodedHeader;
import static javax.mail.internet.MimeUtility.unfold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.ParseException;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContainerMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.session.Session;

/**
 * {@link MIMEMessageUtility}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMEMessageUtility {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MIMEMessageUtility.class);

	/**
	 * No instantiation
	 */
	private MIMEMessageUtility() {
		super();
	}

	private static final Pattern PATTERN_EMBD_IMG = Pattern.compile("(<img.*src=\"?cid:)([^\"]+)(\"?[^/>]*/?>)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern PATTERN_EMBD_IMG_ALT = Pattern.compile(
			"(<img.*src=\"?)([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)(\"?[^/>]*/?>)", Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);

	/**
	 * Detects if given html content contains inlined images
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * &lt;img src=&quot;cid:s345asd845@12drg&quot;&gt;
	 * </pre>
	 * 
	 * @param htmlContent
	 *            The html content
	 * @return <code>true</code> if given html content contains inlined
	 *         images; otherwise <code>false</code>
	 */
	public static boolean hasEmbeddedImages(final String htmlContent) {
		return PATTERN_EMBD_IMG.matcher(htmlContent).find() || PATTERN_EMBD_IMG_ALT.matcher(htmlContent).find();
	}

	/**
	 * Gathers all occuring content IDs in html content and returns them as a
	 * list
	 * 
	 * @param htmlContent
	 *            The html content
	 * @return an instance of <code>{@link List}</code> containing all
	 *         occuring content IDs
	 */
	public static List<String> getContentIDs(final String htmlContent) {
		final List<String> retval = new ArrayList<String>();
		Matcher m = PATTERN_EMBD_IMG.matcher(htmlContent);
		while (m.find()) {
			retval.add(m.group(2));
		}
		m = PATTERN_EMBD_IMG_ALT.matcher(htmlContent);
		while (m.find()) {
			retval.add(m.group(2));
		}
		return retval;
	}

	/**
	 * Compares (case insensitive) the given values of message header
	 * "Content-ID". The leading/trailing character '<code>&lt;</code>'/'<code>&gt;</code>'
	 * are ignored during comparison
	 * 
	 * @param contentId1Arg
	 *            The first content ID
	 * @param contentId2Arg
	 *            The second content ID
	 * @return <code>true</code> if both are equal; otherwise
	 *         <code>false</code>
	 */
	public static boolean equalsCID(final String contentId1Arg, final String contentId2Arg) {
		if (null != contentId1Arg && null != contentId2Arg) {
			final String contentId1 = contentId1Arg.charAt(0) == '<' ? contentId1Arg.substring(1, contentId1Arg
					.length() - 1) : contentId1Arg;
			final String contentId2 = contentId2Arg.charAt(0) == '<' ? contentId2Arg.substring(1, contentId2Arg
					.length() - 1) : contentId2Arg;
			return contentId1.equalsIgnoreCase(contentId2);
		}
		return false;
	}

	public static final Pattern PATTERN_REF_IMG = Pattern.compile(
			"(<img[^/>]*?)(src=\")([^\"]+)(id=)([^\"&]+)(?:(&[^\"]+\")|(\"))([^/>]*/?>)", Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);

	/**
	 * Detects if given html content contains references to local image files
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * &lt;img src=&quot;[url-to-image]&amp;id=123dfr567zh&quot;&gt;
	 * </pre>
	 * 
	 * @param htmlContent
	 *            The html content
	 * @param session
	 *            The user session
	 * @return <code>true</code> if given html content contains references to
	 *         local image files; otherwise <code>false</code>
	 */
	public static boolean hasReferencedLocalImages(final String htmlContent, final Session session) {
		final Matcher m = PATTERN_REF_IMG.matcher(htmlContent);
		while (m.find()) {
			if (session.touchUploadedFile(m.group(5))) {
				return true;
			}
		}
		return false;
	}

	private static final String PARAM_FILENAME = "filename";

	/**
	 * Determines specified part's real filename if any available
	 * 
	 * @param part
	 *            The part whose filename should be determined
	 * @return The part's real filename or <code>null</code> if none present
	 */
	public static String getRealFilename(final MailPart part) {
		if (part.getFileName() != null) {
			return part.getFileName();
		}
		final String hdr = part.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION);
		if (hdr == null) {
			return getContentTypeFilename(part);
		}
		try {
			final String retval = new ContentDisposition(hdr).getParameter(PARAM_FILENAME);
			if (retval == null) {
				return getContentTypeFilename(part);
			}
			return retval;
		} catch (final ParseException e) {
			return getContentTypeFilename(part);
		}
	}

	private static final String PARAM_NAME = "name";

	private static String getContentTypeFilename(final MailPart part) {
		if (part.containsContentType()) {
			return part.getContentType().getParameter(PARAM_NAME);
		}
		final String hdr = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
		if (hdr == null || hdr.length() == 0) {
			return null;
		}
		try {
			return new ContentType(hdr).getParameter(PARAM_NAME);
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	private static interface ContainerMessageFiller {
		void fillField(Message source, ContainerMessage dest) throws MailException, MessagingException;
	}

	/**
	 * Converts given instances of {@link Message} to a cacheable
	 * {@link ContainerMessage} object.
	 * 
	 * @param msgs
	 *            The source messages to convert
	 * @param fields
	 *            The fields to convert for corresponding list request
	 * @return The converted messages
	 * @throws MailException
	 *             If a mail error occurs
	 */
	public static Message[] convertToContainerMessages(final Message[] msgs, final MailListField[] fields)
			throws MailException {
		if (null == msgs) {
			return null;
		}
		final ContainerMessage[] retval = new ContainerMessage[msgs.length];
		final ContainerMessageFiller[] fillers = createContainerMessageFillers(fields);
		for (int i = 0; i < msgs.length; i++) {
			try {
				final ContainerMessage msg = new ContainerMessage(msgs[i].getFolder().getFullName(), msgs[i]
						.getFolder().getSeparator(), msgs[i].getMessageNumber());
				for (ContainerMessageFiller filler : fillers) {
					filler.fillField(msgs[i], msg);
				}
			} catch (final MessagingException e) {
				LOG.error(e.getLocalizedMessage(), e);
				retval[i] = null;
			}
		}
		return retval;
	}

	private static ContainerMessageFiller[] createContainerMessageFillers(final MailListField[] fields)
			throws MailException {
		final ContainerMessageFiller[] fillers = new ContainerMessageFiller[fields.length];
		for (int i = 0; i < fields.length; i++) {
			switch (fields[i]) {
			case ID:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						dest.setUid(((UIDFolder) src.getFolder()).getUID(src));
					}
				};
				break;
			case FOLDER_ID:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						/*
						 * Separator and folder fullname already set on
						 * container message creation
						 */
					}
				};
				break;
			case ATTACHMENT:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MailException,
							MessagingException {
						ContentType ct = null;
						try {
							ct = new ContentType(src.getContentType());
						} catch (final MailException e) {
							if (LOG.isWarnEnabled()) {
								LOG.warn(e.getMessage(), e);
							}
							/*
							 * Try with less strict parsing
							 */
							try {
								ct = new ContentType(src.getContentType(), false);
							} catch (final MailException ie) {
								LOG.error(ie.getMessage(), ie);
								ct = new ContentType(MIMETypes.MIME_DEFAULT);
							}
						}
						dest.setContentType(ct);
						try {
							dest.setHasAttachment(ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL)
									&& (MULTI_SUBTYPE_MIXED.equalsIgnoreCase(ct.getSubType()) || hasAttachments(
											(Multipart) src.getContent(), ct.getSubType())));
						} catch (final IOException e) {
							throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
						}
					}
				};
				break;
			case FROM:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						try {
							dest.addFrom(src.getFrom());
						} catch (final AddressException e) {
							final String[] fromHdr = src.getHeader(MessageHeaders.HDR_FROM);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(fromHdr), e);
							}
							dest
									.addFrom(new InternetAddress[] { new ContainerMessage.DummyAddress(
											unfold(fromHdr[0])) });
						}
					}
				};
				break;
			case TO:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						try {
							dest.setRecipients(Message.RecipientType.TO, src.getRecipients(Message.RecipientType.TO));
						} catch (final AddressException e) {
							final String[] hdr = src.getHeader(MessageHeaders.HDR_TO);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
							}
							dest.setRecipients(Message.RecipientType.TO,
									new InternetAddress[] { new ContainerMessage.DummyAddress(unfold(hdr[0])) });
						}
					}
				};
				break;
			case CC:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						try {
							dest.setRecipients(Message.RecipientType.CC, src.getRecipients(Message.RecipientType.CC));
						} catch (final AddressException e) {
							final String[] hdr = src.getHeader(MessageHeaders.HDR_CC);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
							}
							dest.setRecipients(Message.RecipientType.CC,
									new InternetAddress[] { new ContainerMessage.DummyAddress(unfold(hdr[0])) });
						}
					}
				};
				break;
			case BCC:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						try {
							dest.setRecipients(Message.RecipientType.BCC, src.getRecipients(Message.RecipientType.BCC));
						} catch (final AddressException e) {
							final String[] hdr = src.getHeader(MessageHeaders.HDR_BCC);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
							}
							dest.setRecipients(Message.RecipientType.BCC,
									new InternetAddress[] { new ContainerMessage.DummyAddress(unfold(hdr[0])) });
						}
					}
				};
				break;
			case SUBJECT:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						dest.setSubject(decodeMultiEncodedHeader(src.getSubject()));
					}
				};
				break;
			case SIZE:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						dest.setSize(src.getSize());
					}
				};
				break;
			case SENT_DATE:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						dest.setSentDate(src.getSentDate());
					}
				};
				break;
			case RECEIVED_DATE:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						dest.setReceivedDate(src.getReceivedDate());
					}
				};
				break;
			case FLAGS:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException,
							MailException {
						dest.setFlags(src.getFlags());
					}
				};
				break;
			case THREAD_LEVEL:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						/*
						 * Nothing that can be read from source message
						 */
					}
				};
				break;
			case DISPOSITION_NOTIFICATION_TO:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						final String[] sa = src.getHeader(MessageHeaders.HDR_DISP_NOT_TO);
						if (null != sa) {
							dest.addHeader(MessageHeaders.HDR_DISP_NOT_TO, sa[0]);
						}
					}
				};
				break;
			case PRIORITY:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						final String[] sa = src.getHeader(MessageHeaders.HDR_X_PRIORITY);
						if (null != sa) {
							dest.setHeader(MessageHeaders.HDR_X_PRIORITY, sa[0]);
						}
					}
				};
				break;
			case MSG_REF:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case COLOR_LABEL:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case FOLDER:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case FLAG_SEEN:
				fillers[i] = new ContainerMessageFiller() {
					public void fillField(final Message src, final ContainerMessage dest) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			default:
				throw new MailException(MailException.Code.INVALID_FIELD, fields[i].toString());
			}
		}
		return fillers;
	}

	/*
	 * Mulipart subtype constants
	 */
	private static final String MULTI_SUBTYPE_ALTERNATIVE = "ALTERNATIVE";

	private static final String MULTI_SUBTYPE_MIXED = "MIXED";

	private static final String MULTI_SUBTYPE_SIGNED = "SIGNED";

	/**
	 * Checks if given multipart contains (file) attachments
	 * 
	 * @param mp
	 *            The multipart to examine
	 * @param subtype
	 *            The multipart's subtype multipart contains (file) attachments;
	 *            otherwise <code>false</code>
	 * @return <code>true</code> if given
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws MailException
	 *             If a mail error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public static boolean hasAttachments(final Multipart mp, final String subtype) throws MessagingException,
			MailException, IOException {
		if (MULTI_SUBTYPE_ALTERNATIVE.equalsIgnoreCase(subtype)) {
			return (mp.getCount() > 2);
		} else if (MULTI_SUBTYPE_SIGNED.equalsIgnoreCase(subtype)) {
			return (mp.getCount() > 2);
		} else if (mp.getCount() > 1) {
			return true;
		} else {
			boolean found = false;
			final int count = mp.getCount();
			final ContentType ct = new ContentType();
			for (int i = 0; i < count && !found; i++) {
				final BodyPart part = mp.getBodyPart(i);
				ct.setContentType(part.getContentType());
				if (ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
					found |= hasAttachments((Multipart) part.getContent(), ct.getSubType());
				}
			}
			return found;
		}
	}
}
