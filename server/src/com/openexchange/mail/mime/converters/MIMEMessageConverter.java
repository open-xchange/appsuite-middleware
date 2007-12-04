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

package com.openexchange.mail.mime.converters;

import static com.openexchange.mail.utils.MessageUtility.decodeMultiEncodedHeader;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.hasAttachments;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContainerMessage;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEHeaderLoader;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.dataobjects.MIMEMailMessage;
import com.openexchange.mail.mime.dataobjects.MIMEMailPart;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.tools.mail.ContentType;

/**
 * {@link MIMEMessageConverter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMEMessageConverter {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MIMEMessageConverter.class);

	/*
	 * Content-Type parameter name constants
	 */
	private static final String PARAM_CHARSET = "charset";

	private static interface MailMessageFieldFiller {
		/**
		 * Fills a fields from source instance of {@link Message} in given
		 * destination instance of {@link MailMessage}
		 * 
		 * @param mailMessage
		 *            The mail message to fill
		 * @param msg
		 *            The source message
		 * @throws MessagingException
		 *             If a messaging error occurs
		 * @throws MailException
		 *             If a mail related error occurs
		 */
		public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
				MailException;
	}

	private static abstract class ExtendedMailMessageFieldFiller implements MailMessageFieldFiller {

		final Folder folder;

		public ExtendedMailMessageFieldFiller(final Folder folder) {
			super();
			this.folder = folder;
		}

	}

	/**
	 * Prevent instantiation
	 */
	private MIMEMessageConverter() {
		super();
	}

	/**
	 * Converts given instances of {@link MailMessage} into JavaMail-conform
	 * {@link Message} objects.
	 * <p>
	 * <b>Note</b>: This is just a convenience method that invokes
	 * {@link #convertMailMessage(MailMessage)} for each instance of
	 * {@link MailMessage}
	 * 
	 * @param mails
	 *            The source instances of {@link MailMessage}
	 * @return JavaMail-conform {@link Message} objects.
	 * @throws MailException
	 *             If conversion fails
	 */
	public static Message[] convertMailMessages(final MailMessage[] mails) throws MailException {
		if (null == mails) {
			return null;
		}
		final Message[] retval = new Message[mails.length];
		for (int i = 0; i < retval.length; i++) {
			if (null != mails[i]) {
				retval[i] = convertMailMessage(mails[i]);
			}
		}
		return retval;
	}

	/**
	 * Converts given instance of {@link MailMessage} into a JavaMail-conform
	 * {@link Message} object
	 * 
	 * @param mail
	 *            The source instance of {@link MailMessage}
	 * @return A JavaMail-conform {@link Message} object
	 * @throws MailException
	 *             If conversion fails
	 */
	public static Message convertMailMessage(final MailMessage mail) throws MailException {
		try {
			final MimeMessage mimeMsg = new MimeMessage(MIMEDefaultSession.getDefaultSession());
			final String charset = mail.getContentType().getParameter(PARAM_CHARSET) == null ? MailConfig
					.getDefaultMimeCharset() : mail.getContentType().getParameter(PARAM_CHARSET);
			/*
			 * Set headers
			 */
			final int size = mail.getHeadersSize();
			if (size > 0) {
				final Iterator<Map.Entry<String, String>> iter = mail.getHeadersIterator();
				for (int i = 0; i < size; i++) {
					final Map.Entry<String, String> e = iter.next();
					mimeMsg.setHeader(e.getKey(), e.getValue());
				}
			}
			mimeMsg.setFrom(mail.getFrom()[0]);
			mimeMsg.setRecipients(Message.RecipientType.TO, mail.getTo());
			mimeMsg.setRecipients(Message.RecipientType.CC, mail.getCc());
			mimeMsg.setRecipients(Message.RecipientType.BCC, mail.getBcc());
			mimeMsg.setDisposition(mail.getDisposition());
			if (mail.containsFileName() && mail.getFileName() != null) {
				mimeMsg.setFileName(mail.getFileName());
			}
			if (mail.containsFlags()) {
				parseMimeFlags(mail.getFlags(), mimeMsg);
			}
			if (mail.getSentDate() != null) {
				mimeMsg.setSentDate(mail.getSentDate());
			}
			if (mimeMsg.getHeader(MessageHeaders.HDR_SUBJECT, null) != null) {
				mimeMsg.setSubject(mail.getSubject(), charset);
			}
			/*
			 * Set content
			 */
			addPart(mimeMsg, mail);
			mimeMsg.saveChanges();
			return mimeMsg;
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		} catch (final MailConfigException e) {
			throw new MailException(e);
		} catch (final MailException e) {
			throw new MailException(e);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	private static void addPart(final Part part, final MailPart mailPart) throws MailException, IOException,
			MailConfigException, MessagingException {
		final ContentType contentType = mailPart.getContentType();
		if (contentType.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
			/*
			 * Multipart content
			 */
			final Multipart multipart = new MimeMultipart(contentType.getSubType());
			final int count = mailPart.getEnclosedCount();
			for (int i = 0; i < count; i++) {
				final BodyPart bodyPart = new MimeBodyPart();
				addPart(bodyPart, mailPart.getEnclosedMailPart(i));
				multipart.addBodyPart(bodyPart);
			}
			part.setContent(multipart);
		} else if (contentType.isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
			/*
			 * Message content
			 */
			final Message nestedMsg = convertMailMessage((MailMessage) mailPart.getContent());
			part.setContent(nestedMsg, contentType.toString());
			addPartHeaders(part, mailPart);
		} else if (contentType.isMimeType(MIMETypes.MIME_TEXT_ALL)) {
			/*
			 * Text content
			 */
			addPartHeaders(part, mailPart);
			if (contentType.getParameter(PARAM_CHARSET) == null) {
				contentType.addParameter(PARAM_CHARSET, MailConfig.getDefaultMimeCharset());
			}
			part.setDataHandler(new DataHandler(
					new MessageDataSource(mailPart.getInputStream(), contentType.toString())));
			part.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
			part.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString());
		} else {
			/*
			 * Other content
			 */
			if (contentType.getParameter(PARAM_CHARSET) == null) {
				contentType.addParameter(PARAM_CHARSET, MailConfig.getDefaultMimeCharset());
			}
			addPartHeaders(part, mailPart);
			part.setDataHandler(new DataHandler(
					new MessageDataSource(mailPart.getInputStream(), contentType.toString())));
		}
	}

	private static void addPartHeaders(final Part part, final MailPart mailPart) throws MailException {
		try {
			/*
			 * Set headers
			 */
			final int size = mailPart.getHeadersSize();
			if (size > 0) {
				final Iterator<Map.Entry<String, String>> iter = mailPart.getHeadersIterator();
				for (int i = 0; i < size; i++) {
					final Map.Entry<String, String> e = iter.next();
					part.setHeader(e.getKey(), e.getValue());
				}
			}
			/*
			 * Set disposition & filename
			 */
			part.setDisposition(mailPart.getDisposition());
			if (mailPart.containsContentId()) {
				part.setHeader(MessageHeaders.HDR_CONTENT_ID, mailPart.getContentId());
			}
			if (mailPart.containsFileName() && mailPart.getFileName() != null) {
				part.setFileName(mailPart.getFileName());
			}
			/*
			 * Set content type
			 */
			part.setHeader(MessageHeaders.CONTENT_TYPE.toString(), mailPart.getContentType().toString());
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	private static void parseMimeFlags(final int flags, final MimeMessage msg) throws MessagingException {
		final Flags flagsObj = new Flags();
		if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
			flagsObj.add(Flags.Flag.ANSWERED);
		}
		if ((flags & MailMessage.FLAG_DELETED) > 0) {
			flagsObj.add(Flags.Flag.DELETED);
		}
		if ((flags & MailMessage.FLAG_DRAFT) > 0) {
			flagsObj.add(Flags.Flag.DRAFT);
		}
		if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
			flagsObj.add(Flags.Flag.FLAGGED);
		}
		if ((flags & MailMessage.FLAG_RECENT) > 0) {
			flagsObj.add(Flags.Flag.RECENT);
		}
		if ((flags & MailMessage.FLAG_SEEN) > 0) {
			flagsObj.add(Flags.Flag.SEEN);
		}
		if ((flags & MailMessage.FLAG_USER) > 0) {
			flagsObj.add(Flags.Flag.USER);
		}
		msg.setFlags(flagsObj, true);
	}

	/**
	 * Converts given array of {@link Message} instances to an array of
	 * {@link MailMessage} instances. The single elements of the array are
	 * expected to be instances of {@link ContainerMessage}; meaning the
	 * messages were created through a manual fetch.
	 * <p>
	 * Only the fields specified through parameter <code>fields</code> are
	 * going to be set
	 * 
	 * @see #convertMessages(Message[], Folder, MailListField[]) to convert
	 *      common instances of {@link Message}
	 * 
	 * @param msgs
	 *            The source messages
	 * @param fields
	 *            The fields to fill
	 * @return The converted array of {@link Message} instances
	 * @throws MailException
	 *             If conversion fails
	 * 
	 */
	public static MailMessage[] convertMessages(final Message[] msgs, final MailListField[] fields)
			throws MailException {
		/**
		 * TODO: Change signature to:
		 * 
		 * <pre>
		 * convertMessages(final ContainerMessage[] msgs, final MailListField[] fields)
		 * </pre>
		 */
		try {
			final MailMessageFieldFiller[] fillers = createFieldFillers(fields);
			final MailMessage[] mails = new MIMEMailMessage[msgs.length];
			for (int i = 0; i < mails.length; i++) {
				if (null != msgs[i]) {
					/*
					 * Create with no reference to content
					 */
					mails[i] = new MIMEMailMessage();
					fillMessage(fillers, mails[i], msgs[i]);
				}
			}
			return mails;
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	/**
	 * Converts given array of {@link Message} instances to an array of
	 * {@link MailMessage} instances.
	 * <p>
	 * Only the fields specified through parameter <code>fields</code> are
	 * going to be set
	 * 
	 * @param msgs
	 *            The source messages
	 * @param folder
	 *            The folder containing source messages
	 * @param fields
	 *            The fields to fill
	 * @return The converted array of {@link Message} instances
	 * @throws MailException
	 *             If conversion fails
	 */
	public static MailMessage[] convertMessages(final Message[] msgs, final Folder folder, final MailListField[] fields)
			throws MailException {
		try {
			final MailMessageFieldFiller[] fillers = createFieldFillers(folder, fields);
			final MailMessage[] mails = new MIMEMailMessage[msgs.length];
			for (int i = 0; i < mails.length; i++) {
				if (null != msgs[i]) {
					/*
					 * Create with no reference to content
					 */
					mails[i] = new MIMEMailMessage();
					fillMessage(fillers, mails[i], msgs[i]);
				}
			}
			return mails;
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	private static void fillMessage(final MailMessageFieldFiller[] fillers, final MailMessage mailMessage,
			final Message msg) throws MailException, MessagingException {
		for (final MailMessageFieldFiller filler : fillers) {
			filler.fillField(mailMessage, msg);
		}
	}

	/**
	 * Creates the field fillers and expects the messages to be instances of
	 * {@link ContainerMessage}
	 * 
	 * @param fields
	 *            The fields to fill
	 * @return An array of appropriate {@link MailMessageFieldFiller}
	 *         implementations
	 * @throws MailException
	 *             If field fillers cannot be created
	 */
	private static MailMessageFieldFiller[] createFieldFillers(final MailListField[] fields) throws MailException {
		final MailMessageFieldFiller[] fillers = new MailMessageFieldFiller[fields.length];
		for (int i = 0; i < fields.length; i++) {
			switch (fields[i]) {
			case ID:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setMailId(((ContainerMessage) msg).getUid());
					}
				};
				break;
			case FOLDER_ID:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSeparator(((ContainerMessage) msg).getSeparator());
						mailMessage.setFolder(((ContainerMessage) msg).getFolderFullname());
					}
				};
				break;
			case ATTACHMENT:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MailException,
							MessagingException {
						try {
							mailMessage.setContentType(((ContainerMessage) msg).getContentType());
						} catch (final MailException e) {
							/*
							 * Cannot occur
							 */
							LOG.error(e.getLocalizedMessage(), e);
						}
						mailMessage.setHasAttachment(((ContainerMessage) msg).hasAttachment());
					}
				};
				break;
			case FROM:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addFrom((InternetAddress[]) ((ContainerMessage) msg).getFrom());
					}
				};
				break;
			case TO:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addTo((InternetAddress[]) ((ContainerMessage) msg).getRecipients(RecipientType.TO));
					}
				};
				break;
			case CC:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addCc((InternetAddress[]) ((ContainerMessage) msg).getRecipients(RecipientType.CC));
					}
				};
				break;
			case BCC:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addBcc((InternetAddress[]) ((ContainerMessage) msg)
								.getRecipients(RecipientType.BCC));
					}
				};
				break;
			case SUBJECT:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSubject(((ContainerMessage) msg).getSubject());
					}
				};
				break;
			case SIZE:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSize(((ContainerMessage) msg).getSize());
					}
				};
				break;
			case SENT_DATE:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSentDate(((ContainerMessage) msg).getSentDate());
					}
				};
				break;
			case RECEIVED_DATE:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setReceivedDate(((ContainerMessage) msg).getReceivedDate());
					}
				};
				break;
			case FLAGS:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
							MailException {
						parseFlags(((ContainerMessage) msg).getFlags(), mailMessage);
					}
				};
				break;
			case THREAD_LEVEL:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setThreadLevel(((ContainerMessage) msg).getThreadLevel());
					}
				};
				break;
			case DISPOSITION_NOTIFICATION_TO:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						final String[] val = ((ContainerMessage) msg).getHeader(MessageHeaders.HDR_DISP_NOT_TO);
						if (val != null && val.length > 0) {
							mailMessage.setDispositionNotification(InternetAddress.parse(val[0], true)[0]);
							mailMessage.removeHeader(MessageHeaders.HDR_DISP_NOT_TO);
						}
					}
				};
				break;
			case PRIORITY:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						final String[] val = ((ContainerMessage) msg).getHeader(MessageHeaders.HDR_X_PRIORITY);
						if (val != null && val.length > 0) {
							parsePriority(val[0], mailMessage);
							mailMessage.removeHeader(MessageHeaders.HDR_X_PRIORITY);
						}
					}
				};
				break;
			case MSG_REF:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case COLOR_LABEL:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
							MailException {
						parseFlags(((ContainerMessage) msg).getFlags(), mailMessage);
					}
				};
				break;
			case FOLDER:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case FLAG_SEEN:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
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

	/**
	 * Creates the field fillers and expects the messages to be common instances
	 * of {@link Message}
	 * 
	 * @param folder
	 *            The folder containing the messages
	 * @param fields
	 *            The fields to fill
	 * @return An array of appropriate {@link MailMessageFieldFiller}
	 *         implementations
	 * @throws MailException
	 *             If field fillers cannot be created
	 */
	private static MailMessageFieldFiller[] createFieldFillers(final Folder folder, final MailListField[] fields)
			throws MailException {
		final MailMessageFieldFiller[] fillers = new MailMessageFieldFiller[fields.length];
		for (int i = 0; i < fields.length; i++) {
			switch (fields[i]) {
			case ID:
				fillers[i] = new ExtendedMailMessageFieldFiller(folder) {

					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setMailId(((UIDFolder) folder).getUID(msg));
					}
				};
				break;
			case FOLDER_ID:
				fillers[i] = new ExtendedMailMessageFieldFiller(folder) {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSeparator(folder.getSeparator());
						mailMessage.setFolder(folder.getFullName());
					}
				};
				break;
			case ATTACHMENT:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
							MailException {
						ContentType ct = null;
						try {
							ct = new ContentType(msg.getContentType());
						} catch (final MailException e) {
							/*
							 * Cannot occur
							 */
							LOG.error("Invalid content type: " + msg.getContentType(), e);
							try {
								ct = new ContentType(MIMETypes.MIME_DEFAULT);
							} catch (MailException e1) {
								/*
								 * Cannot occur
								 */
								LOG.error(e1.getLocalizedMessage(), e1);
								return;
							}
						}
						mailMessage.setContentType(ct);
						mailMessage.setHasAttachment(ct.isMimeType(MIMETypes.MIME_MULTIPART_MIXED));
						/*
						 * TODO: Detailed attachment information like done with IMAP's bodystructure information
						 */
						/*try {
							mailMessage.setHasAttachment(ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL)
									&& (ct.isMimeType(MIMETypes.MIME_MULTIPART_MIXED) || hasAttachments((Multipart) msg
											.getContent(), ct.getSubType())));
						} catch (final IOException e) {
							throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
						}*/
					}
				};
				break;
			case FROM:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						try {
							mailMessage.addFrom((InternetAddress[]) msg.getFrom());
						} catch (final AddressException e) {
							final String[] fromHdr = msg.getHeader(MessageHeaders.HDR_FROM);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(fromHdr), e);
							}
							mailMessage.addFrom(new ContainerMessage.DummyAddress(fromHdr[0]));
						}
					}
				};
				break;
			case TO:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						try {
							mailMessage.addTo((InternetAddress[]) msg.getRecipients(RecipientType.TO));
						} catch (final AddressException e) {
							final String[] hdr = msg.getHeader(MessageHeaders.HDR_TO);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
							}
							mailMessage.addTo(new ContainerMessage.DummyAddress(hdr[0]));
						}
					}
				};
				break;
			case CC:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						try {
							mailMessage.addCc((InternetAddress[]) msg.getRecipients(RecipientType.CC));
						} catch (final AddressException e) {
							final String[] hdr = msg.getHeader(MessageHeaders.HDR_CC);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
							}
							mailMessage.addCc(new ContainerMessage.DummyAddress(hdr[0]));
						}
					}
				};
				break;
			case BCC:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						try {
							mailMessage.addBcc((InternetAddress[]) msg.getRecipients(RecipientType.BCC));
						} catch (final AddressException e) {
							final String[] hdr = msg.getHeader(MessageHeaders.HDR_BCC);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
							}
							mailMessage.addBcc(new ContainerMessage.DummyAddress(hdr[0]));
						}
					}
				};
				break;
			case SUBJECT:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSubject(decodeMultiEncodedHeader(msg.getSubject()));
					}
				};
				break;
			case SIZE:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSize(msg.getSize());
					}
				};
				break;
			case SENT_DATE:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSentDate(msg.getSentDate());
					}
				};
				break;
			case RECEIVED_DATE:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setReceivedDate(msg.getReceivedDate());
					}
				};
				break;
			case FLAGS:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
							MailException {
						parseFlags(msg.getFlags(), mailMessage);
					}
				};
				break;
			case THREAD_LEVEL:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * TODO: Thread level
						 */
						mailMessage.setThreadLevel(0);
					}
				};
				break;
			case DISPOSITION_NOTIFICATION_TO:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						final String[] val = msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO);
						if (val != null && val.length > 0) {
							mailMessage.setDispositionNotification(InternetAddress.parse(val[0], true)[0]);
							mailMessage.removeHeader(MessageHeaders.HDR_DISP_NOT_TO);
						}
					}
				};
				break;
			case PRIORITY:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						final String[] val = msg.getHeader(MessageHeaders.HDR_X_PRIORITY);
						if (val != null && val.length > 0) {
							parsePriority(val[0], mailMessage);
							mailMessage.removeHeader(MessageHeaders.HDR_X_PRIORITY);
						}
					}
				};
				break;
			case MSG_REF:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case COLOR_LABEL:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
							MailException {
						parseFlags(msg.getFlags(), mailMessage);
					}
				};
				break;
			case FOLDER:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case FLAG_SEEN:
				fillers[i] = new MailMessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
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

	/**
	 * Creates a message data object from given IMAP message
	 * 
	 * @param msg
	 *            The IMAP message
	 * @return an instance of <code>{@link MailMessage}</code> containing the
	 *         attributes from given IMAP message
	 */
	public static MailMessage convertMessage(final MimeMessage msg) throws MailException {
		try {
			/*
			 * Create with reference to content
			 */
			final MIMEMailMessage mail = new MIMEMailMessage(msg);
			/*
			 * Set all cacheable data
			 */
			if (msg.getFolder() != null) {
				/*
				 * No nested message
				 */
				final Folder f = msg.getFolder();
				mail.setFolder(f.getFullName());
				mail.setSeparator(f.getSeparator());
				mail.setMailId(((UIDFolder) f).getUID(msg));
			}
			setHeaders(msg, mail);
			mail.addFrom((InternetAddress[]) msg.getFrom());
			mail.addTo((InternetAddress[]) msg.getRecipients(Message.RecipientType.TO));
			mail.addCc((InternetAddress[]) msg.getRecipients(Message.RecipientType.CC));
			mail.addBcc((InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC));
			mail.setContentType(msg.getContentType());
			{
				final String[] tmp = msg.getHeader(MessageHeaders.HDR_CONTENT_ID);
				if (tmp != null && tmp.length > 0) {
					mail.setContentId(tmp[0]);
				}
			}
			mail.setDisposition(msg.getDisposition());
			{
				final String dispNot = msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO, null);
				if (dispNot != null) {
					mail.setDispositionNotification(InternetAddress.parse(dispNot, true)[0]);
				}
			}
			mail.removeHeader(MessageHeaders.HDR_DISP_NOT_TO);
			mail.setFileName(decodeMultiEncodedHeader(msg.getFileName()));
			parseFlags(msg.getFlags(), mail);
			parsePriority(mail.getHeader(MessageHeaders.HDR_X_PRIORITY), mail);
			mail.removeHeader(MessageHeaders.HDR_X_PRIORITY);
			if (msg.getReceivedDate() != null) {
				mail.setReceivedDate(msg.getReceivedDate());
			}
			if (msg.getSentDate() != null) {
				mail.setSentDate(msg.getSentDate());
			}
			mail.setSize(msg.getSize());
			mail.setSubject(msg.getSubject());
			return mail;
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	private static final String ENC_BASE64 = "base64";

	/**
	 * Creates a mail part object from given part
	 * 
	 * @param part
	 *            The part
	 * @return an instance of <code>{@link MailPart}</code> containing the
	 *         attributes from given part
	 */
	public static MailPart convertIMAPPart(final Part part) throws MailException {
		try {
			/*
			 * Create with reference to content
			 */
			final MIMEMailPart mailPart = new MIMEMailPart(part);
			/*
			 * Set all cacheable data
			 */
			setHeaders(part, mailPart);
			mailPart.setContentType(part.getContentType());
			{
				final String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_ID);
				if (tmp != null && tmp.length > 0) {
					mailPart.setContentId(tmp[0]);
				}
			}
			mailPart.setDisposition(part.getDisposition());
			mailPart.setFileName(decodeMultiEncodedHeader(part.getFileName()));
			int size = part.getSize();
			if (size == -1) {
				/*
				 * Estimate unknown size: The encoded form of the file is
				 * expanded by 37% for UU encoding and by 35% for base64
				 * encoding (3 bytes become 4 plus control information).
				 */
				final String tansferEnc = (((MimePart) part).getHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, null));
				try {
					size = estimateSize(part.getInputStream(), tansferEnc);
				} catch (final IOException e) {
					try {
						if (part instanceof MimeBodyPart) {
							size = estimateSize(((MimeBodyPart) part).getRawInputStream(), tansferEnc);
						} else if (part instanceof MimeMessage) {
							size = estimateSize(((MimeMessage) part).getRawInputStream(), tansferEnc);
						} else {
							LOG.warn(new StringBuilder(256).append(part.getClass().getCanonicalName()).append(
									"'s size cannot be determined").toString(), e);
						}
					} catch (final IOException e1) {
						LOG.warn(new StringBuilder(256).append(part.getClass().getCanonicalName()).append(
								"'s size cannot be determined").toString(), e1);
					} catch (final MessagingException e1) {
						LOG.warn(new StringBuilder(256).append(part.getClass().getCanonicalName()).append(
								"'s size cannot be determined").toString(), e1);
					}
				}
			}
			mailPart.setSize(size);
			return mailPart;
		} catch (final MessagingException e) {
			throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getLocalizedMessage());
		}
	}

	private static int estimateSize(final InputStream in, final String tansferEnc) throws IOException {
		if (ENC_BASE64.equalsIgnoreCase(tansferEnc)) {
			return (int) (in.available() * 0.65);
		}
		return in.available();
	}

	private static final String[] EMPTY_STRS = new String[0];

	private static void parseFlags(final Flags flags, final MailMessage mailMessage) throws MailException {
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
		if (userFlags != null) {
			/*
			 * Mark message to contain user flags
			 */
			mailMessage.addUserFlags(EMPTY_STRS);
			for (int i = 0; i < userFlags.length; i++) {
				/*
				 * Color Label
				 */
				if (userFlags[i].startsWith(MailMessage.COLOR_LABEL_PREFIX)) {
					mailMessage.setColorLabel(MailMessage.getColorLabelIntValue(userFlags[i]));
				} else {
					mailMessage.addUserFlag(userFlags[i]);
				}
			}
		}
	}

	private static final String STR_CANNOT_LOAD_HEADER = "Cannot load header";

	@SuppressWarnings("unchecked")
	private static void setHeaders(final Part part, final MailPart mailPart) throws MailException {
		/*
		 * HEADERS
		 */
		Map<String, String> headerMap = null;
		try {
			headerMap = new HashMap<String, String>();
			for (final Enumeration<Header> e = part.getAllHeaders(); e.hasMoreElements();) {
				final Header h = e.nextElement();
				headerMap.put(h.getName(), h.getValue());
			}
		} catch (final MessagingException e) {
			if (part instanceof Message && e.getMessage().indexOf(STR_CANNOT_LOAD_HEADER) != -1) {
				/*
				 * Headers could not be loaded
				 */
				headerMap = MIMEHeaderLoader.getInstance().loadHeaders((Message) part, false);
			} else {
				headerMap = new HashMap<String, String>();
			}
		}
		mailPart.addHeaders(headerMap);
	}

	/**
	 * Parses the value of header <code>X-Priority</code>
	 * 
	 * @param priorityStr
	 *            The header value
	 * @param mailMessage
	 *            The mail message to fill
	 */
	public static void parsePriority(final String priorityStr, final MailMessage mailMessage) {
		int priority = MailMessage.PRIORITY_NORMAL;
		if (null != priorityStr) {
			final String[] tmp = priorityStr.split(" +");
			try {
				priority = Integer.parseInt(tmp[0]);
			} catch (final NumberFormatException nfe) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Strange X-Priority header: " + tmp[0], nfe);
				}
				priority = MailMessage.PRIORITY_NORMAL;
			}
		}
		mailMessage.setPriority(priority);
	}
}
