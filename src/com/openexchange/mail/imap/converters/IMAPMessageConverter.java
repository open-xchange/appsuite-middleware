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
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.imap.IMAPUtils;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.imap.IMAPException;
import com.openexchange.mail.imap.dataobjects.IMAPMailMessage;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * IMAPMessageConverter
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPMessageConverter {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPMessageConverter.class);

	private static final String MIME_SUBTYPE_ALTERNATIVE = "ALTERNATIVE";

	private static final String MIME_SUBTYPE_MIXED = "MIXED";

	private static interface MessageFieldFiller {
		/**
		 * Fills a fields from source message in given mail message
		 * 
		 * @param mailMessage
		 *            The mail message to fill
		 * @param msg
		 *            The source message
		 * @throws MessagingException
		 * @throws IMAPException
		 */
		public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
				IMAPException;
	}

	/**
	 * Prevent instantiation
	 */
	private IMAPMessageConverter() {
		super();
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
	 * @param fields
	 *            The fields to fill
	 * @return converted array of {@link Message} instances
	 * @throws IMAPException
	 */
	public static MailMessage[] convertIMAPMessages(final Message[] msgs, final MailListField[] fields)
			throws IMAPException {
		try {
			final MessageFieldFiller[] fillers = createFieldFillers(fields);
			final MailMessage[] retval = new IMAPMailMessage[msgs.length];
			for (int i = 0; i < retval.length; i++) {
				fillMessage(fillers, retval[i], msgs[i]);
			}
			return retval;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e);
		}
	}

	private static void fillMessage(final MessageFieldFiller[] fillers, final MailMessage mailMessage, final Message msg)
			throws IMAPException, MessagingException {
		for (final MessageFieldFiller filler : fillers) {
			filler.fillField(mailMessage, msg);
		}
	}

	private static MessageFieldFiller[] createFieldFillers(final MailListField[] fields) throws IMAPException {
		final MessageFieldFiller[] fillers = new MessageFieldFiller[fields.length];
		for (int i = 0; i < fields.length; i++) {
			switch (fields[i]) {
			case ID:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setUid(((MessageCacheObject) msg).getUid());
					}
				};
				break;
			case FOLDER_ID:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setFolder(((MessageCacheObject) msg).getFolderFullname());
					}
				};
				break;
			case ATTACHMENT:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						final BODYSTRUCTURE bodystructure = ((MessageCacheObject) msg).getBodystructure();
						mailMessage
								.setHasAttachment(bodystructure.isMulti()
										&& (MIME_SUBTYPE_MIXED.equalsIgnoreCase(bodystructure.subtype) || hasAttachments(bodystructure)));
					}
				};
				break;
			case FROM:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addFrom((InternetAddress[]) ((MessageCacheObject) msg).getFrom());
					}
				};
				break;
			case TO:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addTo((InternetAddress[]) ((MessageCacheObject) msg)
								.getRecipients(RecipientType.TO));
					}
				};
				break;
			case CC:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addCc((InternetAddress[]) ((MessageCacheObject) msg)
								.getRecipients(RecipientType.CC));
					}
				};
				break;
			case BCC:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.addBcc((InternetAddress[]) ((MessageCacheObject) msg)
								.getRecipients(RecipientType.BCC));
					}
				};
				break;
			case SUBJECT:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSubject(((MessageCacheObject) msg).getSubject());
					}
				};
				break;
			case SIZE:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSize(((MessageCacheObject) msg).getSize());
					}
				};
				break;
			case SENT_DATE:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setSentDate(((MessageCacheObject) msg).getSentDate());
					}
				};
				break;
			case RECEIVED_DATE:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setReceivedDate(((MessageCacheObject) msg).getReceivedDate());
					}
				};
				break;
			case FLAGS:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
							IMAPException {
						parseFlags(((MessageCacheObject) msg).getFlags(), mailMessage);
					}
				};
				break;
			case THREAD_LEVEL:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						mailMessage.setThreadLevel(((MessageCacheObject) msg).getThreadLevel());
					}
				};
				break;
			case DISPOSITION_NOTIFICATION_TO:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						final String[] val = ((MessageCacheObject) msg).getHeader(MessageHeaders.HDR_DISP_NOT_TO);
						if (val != null && val.length > 0) {
							mailMessage.setDispositionNotification(val[0]);
						}
					}
				};
				break;
			case PRIORITY:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						final String[] val = ((MessageCacheObject) msg).getHeader(MessageHeaders.HDR_X_PRIORITY);
						if (val != null && val.length > 0) {
							parsePriority(val[0], mailMessage);
						}
					}
				};
				break;
			case MSG_REF:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case COLOR_LABEL:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException,
							IMAPException {
						parseFlags(((MessageCacheObject) msg).getFlags(), mailMessage);
					}
				};
				break;
			case FOLDER:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			case FLAG_SEEN:
				fillers[i] = new MessageFieldFiller() {
					public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
						/*
						 * Ignore
						 */
					}
				};
				break;
			default:
				throw new IMAPException(IMAPException.Code.INVALID_FIELD, fields[i].toString());
			}
		}
		return fillers;
	}

	private static boolean hasAttachments(final BODYSTRUCTURE bodystructure) {
		if (bodystructure.isMulti()) {
			if (MIME_SUBTYPE_ALTERNATIVE.equalsIgnoreCase(bodystructure.subtype) && bodystructure.bodies.length > 2) {
				return true;
			} else if (bodystructure.bodies.length > 1) {
				return true;
			} else {
				boolean found = false;
				for (int i = 0; i < bodystructure.bodies.length && !found; i++) {
					found |= hasAttachments(bodystructure.bodies[i]);
				}
				return found;
			}
		}
		return false;
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
			retval.setFolder(msg.getFolder().getFullName());
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

	private static void parseFlags(final Flags flags, final MailMessage mailMessage) throws IMAPException {
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
				try {
					mailMessage.setColorLabel(JSONMessageObject.getColorLabelIntValue(userFlags[i]));
				} catch (final OXException e) {
					throw new IMAPException(e);
				}
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

	private static void parsePriority(final String priorityStr, final MailMessage mailMessage) {
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
