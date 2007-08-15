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
import java.util.Date;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.imap.IMAPUtils;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.UUEncodedPart;

/**
 * AttachmentFinderMessageHandler
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AttachmentFinderMessageHandler implements MessageHandler {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AttachmentFinderMessageHandler.class);

	private final Flags storedMsgFlags;

	private final SessionObject session;

	private boolean hasAttachment;

	private boolean isFlagged;

	private boolean isMultipart;

	/**
	 * Creates a new <code>AttachmentFinderMessageHandler</code> instance
	 */
	public AttachmentFinderMessageHandler(final SessionObject session, final Flags storedMsgFlags) {
		super();
		this.session = session;
		this.storedMsgFlags = storedMsgFlags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
	 */
	public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
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
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleSubject(java.lang.String)
	 */
	public boolean handleSubject(final String subject) throws OXException {
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
		for (int i = 0; i < userFlags.length; i++) {
			if (JSONMessageObject.USER_FLAG_ATTACHMENT.equals(userFlags[i])) {
				/*
				 * Message was already marked to have attachment(s)
				 */
				hasAttachment = true;
				isFlagged = true;
				return false;
			} else if (JSONMessageObject.USER_FLAG_NO_ATTACHMENT.equals(userFlags[i])) {
				/*
				 * Message was already marked to have no attachment
				 */
				hasAttachment = false;
				isFlagged = true;
				return false;
			}
		}
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
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedPainText(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final String baseContentType,
			final int size, final String fileName, final String id) throws OXException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
	 *      java.lang.String)
	 */
	public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
		hasAttachment = (part.getFileName() != null);
		return !hasAttachment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleInlineHtml(java.lang.String,
	 *      java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public boolean handleInlineHtml(final String htmlContent, final String baseContentType, final int size,
			final String fileName, final String id) throws OXException {
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
			hasAttachment = (isMultipart && (isAttachment(part) || !part.isMimeType("text/*")));
			return !hasAttachment;
		} catch (MessagingException e) {
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
		try {
			hasAttachment = (isMultipart && isAttachment(part));
			return !hasAttachment;
		} catch (MessagingException e) {
			throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleImagePart(javax.mail.Part,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean handleImagePart(final Part part, final String imageCID, final String baseContentType, final String id)
			throws OXException {
		try {
			hasAttachment = (isMultipart && isAttachment(part));
			return !hasAttachment;
		} catch (MessagingException e) {
			throw MailInterfaceImpl.handleMessagingException(e, session.getIMAPProperties(), session.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMultipart(javax.mail.Multipart,
	 *      int, java.lang.String)
	 */
	public boolean handleMultipart(final Multipart mp, final int bodyPartCount, final String id) throws OXException {
		if (!isMultipart) {
			/*
			 * In the simplest case, a message of MIME type multipart/mixed with
			 * more than one body part is likely a message with attachments. So
			 * this is treated as a necessary pre-condition
			 */
			isMultipart = (bodyPartCount > 1);
			return isMultipart;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleNestedMessage(javax.mail.Message,
	 *      java.lang.String)
	 */
	public boolean handleNestedMessage(final Message nestedMsg, final String id) throws OXException {
		hasAttachment = isMultipart;
		return !hasAttachment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.mail.parser.MessageHandler#handleMessageEnd()
	 */
	public void handleMessageEnd(final Message msg) throws OXException {
		if (!isFlagged) {
			final Folder fld = msg.getFolder();
			if ("false".equalsIgnoreCase(MailInterfaceImpl.getDefaultIMAPProperties().getProperty(
					"mail.imap.allowreadonlyselect", "false"))
					&& IMAPUtils.isReadOnly(fld)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder().append("Folder ").append(fld.getFullName()).append(" is read-only")
							.toString());
				}
				return;
			}
			/*
			 * Message has no user flag, that provides the information whether
			 * an attachment is present or not. so, its content was touched
			 * during processing and user flag is not set, yet
			 */
			boolean closeFolder = false;
			try {
				if (!fld.isOpen()) {
					fld.open(Folder.READ_WRITE);
					MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(true);
					closeFolder = true;
				} else if (fld.getMode() == Folder.READ_ONLY) {
					try {
						fld.close(false);
					} finally {
						MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
					}
					fld.open(Folder.READ_WRITE);
					MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(true);
					closeFolder = true;
				}
				/*
				 * Message was not seen before. Since we have to access
				 * message's content to determine if message has attachments the
				 * first time, the flag //SEEN is implicitely set. Reset this
				 * flag.
				 */
				if (!storedMsgFlags.contains(Flags.Flag.SEEN)) {
					msg.setFlag(Flags.Flag.SEEN, false);
				}
				/*
				 * Message is not flagged and is no draft
				 */
				if (!storedMsgFlags.contains(Flags.Flag.DRAFT)) {
					setAttachUserFlag(msg, hasAttachment);
				}
			} catch (final MessagingException e) {
				throw MailInterfaceImpl.handleMessagingException(e);
			} finally {
				if (closeFolder) {
					try {
						fld.close(false);
					} catch (final IllegalStateException e) {
						LOG.error(e.getMessage(), e);
					} catch (final MessagingException e) {
						LOG.error(e.getMessage(), e);
					} finally {
						MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
					}
				}
			}
		}
	}

	private final boolean isAttachment(final Part part) throws MessagingException {
		return (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && part.getFileName() != null);
	}

	public boolean hasAttachment() {
		return hasAttachment;
	}

	/**
	 * This method does not just returns a boolean value indicating, if
	 * traversed message contains at least one attachment.
	 * 
	 * @returns <code>true</code> if traversed message contains at least one
	 *          attachment, <code>false</code> otherwise
	 */
	public static boolean hasAttachment(final Message msg, final SessionObject session) throws MessagingException {
		final AttachmentFinderMessageHandler msgHandler = new AttachmentFinderMessageHandler(session, msg.getFlags());
		try {
			new MessageDumper(session).dumpMessage(msg, msgHandler);
		} catch (Throwable e) {
			/*
			 * Error in encoded stream
			 */
			LOG.error(new StringBuilder("Exception in message ").append(MessageUtils.getMessageUniqueIdentifier(msg))
					.append(": ").append(e.getMessage()).toString(), e);
			return false;
		}
		return msgHandler.hasAttachment;
	}

	private static final void setAttachUserFlag(final Message msg, final boolean hasAttachment)
			throws MessagingException {
		final Folder fld = msg.getFolder();
		boolean closeFld = false;
		try {
			if (!fld.isOpen()) {
				fld.open(Folder.READ_WRITE);
				MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(true);
				closeFld = true;
			} else if (fld.getMode() != Folder.READ_WRITE) {
				try {
					fld.close(false);
				} finally {
					MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
				}
				fld.open(Folder.READ_WRITE);
				MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(true);
				closeFld = true;
			}
			final Flags attachFlags = new Flags(hasAttachment ? JSONMessageObject.USER_FLAG_ATTACHMENT
					: JSONMessageObject.USER_FLAG_NO_ATTACHMENT);
			msg.setFlags(attachFlags, true);
		} finally {
			if (closeFld) {
				try {
					fld.close(false);
				} finally {
					MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
				}
			}
		}
	}

}
