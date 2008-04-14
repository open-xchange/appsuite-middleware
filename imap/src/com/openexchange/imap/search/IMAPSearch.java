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

package com.openexchange.imap.search;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getFetchProfile;
import static com.openexchange.mail.utils.StorageUtility.getAllAddresses;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SizeTerm;
import javax.mail.search.SubjectTerm;

import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.text.Html2TextConverter;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.tools.Collections.SmartIntArray;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPSearch}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPSearch {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPSearch.class);

	/**
	 * No instantiation
	 */
	private IMAPSearch() {
		super();
	}

	/**
	 * Searches messages in given IMAP folder
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @param searchTerm
	 *            The search term
	 * 
	 * @return Filtered messages' sequence numbers according to search term
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws MailException
	 *             If a searching fails
	 */
	public static int[] searchMessages(final IMAPFolder imapFolder,
			final com.openexchange.mail.search.SearchTerm<?> searchTerm, final IMAPConfig imapConfig)
			throws MessagingException, MailException {
		final int msgCount = imapFolder.getMessageCount();
		/*
		 * Perform an IMAP-based search if IMAP search is enabled through config
		 * or number of messages to search in exceeds limit.
		 */
		final boolean hasSearchCapability;
		{
			final IMAPCapabilities imapCapabilities = (IMAPCapabilities) imapConfig.getCapabilities();
			hasSearchCapability = imapCapabilities.hasIMAP4() || imapCapabilities.hasIMAP4rev1();
		}
		if (imapConfig.isImapSearch() || (hasSearchCapability && (msgCount >= MailConfig.getMailFetchLimit()))) {
			try {
				final SearchTerm term = getSearchTerm(searchTerm);
				final long start = System.currentTimeMillis();
				final int[] matchSeqNums = imapFolder.getProtocol().search(term);
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(128).append("IMAP search took ").append(
							(System.currentTimeMillis() - start)).append("msec").toString());
				}
				return matchSeqNums;
			} catch (final Throwable t) {
				if (LOG.isWarnEnabled()) {
					final IMAPException imapException = new IMAPException(IMAPException.Code.IMAP_SEARCH_FAILED, t, t
							.getMessage());
					LOG.warn(imapException.getLocalizedMessage(), imapException);
				}
			}
		}
		final MailField[] searchFields;
		{
			final Set<MailField> fields = MailField.getMailFieldsFromSearchTerm(searchTerm);
			searchFields = fields.toArray(new MailField[fields.size()]);
		}
		final Message[] allMsgs;
		{
			final long start = System.currentTimeMillis();
			allMsgs = new FetchIMAPCommand(imapFolder, getFetchProfile(searchFields, IMAPConfig.isFastFetch()),
					msgCount).doCommand();
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		}
		final SmartIntArray sia = new SmartIntArray(allMsgs.length / 2);
		final SearchTermMatcher searchTermMatcher = createSearchTermMatcher(searchTerm);
		for (int i = 0; i < allMsgs.length; i++) {
			if (searchTermMatcher.matches(searchTerm, allMsgs[i])) {
				sia.append(allMsgs[i].getMessageNumber());
			}
		}
		return sia.toArray();
	}

	/**
	 * Generates a {@link SearchTerm} from given instance of
	 * {@link com.openexchange.mail.search.SearchTerm}
	 * 
	 * @param mailSearchTerm
	 *            The mail search term
	 * @return A corresponding instance of {@link SearchTerm}
	 */
	private static SearchTerm getSearchTerm(final com.openexchange.mail.search.SearchTerm<?> mailSearchTerm) {
		SearchTerm searchTerm = null;
		if (mailSearchTerm instanceof com.openexchange.mail.search.HeaderTerm) {
			final String[] hdr = ((com.openexchange.mail.search.HeaderTerm) mailSearchTerm).getPattern();
			searchTerm = new HeaderTerm(hdr[0], hdr[1]);
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.FlagTerm) {
			int flags = ((com.openexchange.mail.search.FlagTerm) mailSearchTerm).getPattern().intValue();
			final boolean set;
			if (flags < 0) {
				set = false;
				flags *= -1;
			} else {
				set = true;
			}
			searchTerm = new FlagTerm(MIMEMessageConverter.convertMailFlags(flags), set);
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.FromTerm) {
			searchTerm = new FromStringTerm(((com.openexchange.mail.search.FromTerm) mailSearchTerm).getPattern());
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.ToTerm) {
			searchTerm = new RecipientStringTerm(Message.RecipientType.TO,
					((com.openexchange.mail.search.ToTerm) mailSearchTerm).getPattern());
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.CcTerm) {
			searchTerm = new RecipientStringTerm(Message.RecipientType.CC,
					((com.openexchange.mail.search.CcTerm) mailSearchTerm).getPattern());
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.BccTerm) {
			searchTerm = new RecipientStringTerm(Message.RecipientType.BCC,
					((com.openexchange.mail.search.BccTerm) mailSearchTerm).getPattern());
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.SubjectTerm) {
			searchTerm = new SubjectTerm(((com.openexchange.mail.search.SubjectTerm) mailSearchTerm).getPattern());
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.SizeTerm) {
			final int[] dat = ((com.openexchange.mail.search.SizeTerm) mailSearchTerm).getPattern();
			final int ct;
			if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
				ct = ComparisonTerm.EQ;
			} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
				ct = ComparisonTerm.LT;
			} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
				ct = ComparisonTerm.GT;
			} else {
				ct = ComparisonTerm.EQ;
			}
			searchTerm = new SizeTerm(ct, dat[1]);
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.SentDateTerm) {
			final long[] dat = ((com.openexchange.mail.search.SentDateTerm) mailSearchTerm).getPattern();
			final int ct;
			if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
				ct = ComparisonTerm.EQ;
			} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
				ct = ComparisonTerm.LT;
			} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
				ct = ComparisonTerm.GT;
			} else {
				ct = ComparisonTerm.EQ;
			}
			searchTerm = new SentDateTerm(ct, new Date(dat[1]));
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.ReceivedDateTerm) {
			final long[] dat = ((com.openexchange.mail.search.ReceivedDateTerm) mailSearchTerm).getPattern();
			final int ct;
			if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
				ct = ComparisonTerm.EQ;
			} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
				ct = ComparisonTerm.LT;
			} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
				ct = ComparisonTerm.GT;
			} else {
				ct = ComparisonTerm.EQ;
			}
			searchTerm = new ReceivedDateTerm(ct, new Date(dat[1]));
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.ANDTerm) {
			final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ANDTerm) mailSearchTerm)
					.getPattern();
			searchTerm = new AndTerm(getSearchTerm(terms[0]), getSearchTerm(terms[1]));
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.ORTerm) {
			final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ORTerm) mailSearchTerm)
					.getPattern();
			searchTerm = new OrTerm(getSearchTerm(terms[0]), getSearchTerm(terms[1]));
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.BodyTerm) {
			searchTerm = new BodyTerm(((com.openexchange.mail.search.BodyTerm) mailSearchTerm).getPattern());
		} else if (mailSearchTerm instanceof com.openexchange.mail.search.BooleanTerm) {
			searchTerm = BooleanSearchTerm.getInstance(((com.openexchange.mail.search.BooleanTerm) mailSearchTerm)
					.getPattern().booleanValue());
		}
		return searchTerm;
	}

	private static final class BooleanSearchTerm extends SearchTerm {

		private static final BooleanSearchTerm TRUE = new BooleanSearchTerm(true);

		private static final BooleanSearchTerm FALSE = new BooleanSearchTerm(false);

		public static BooleanSearchTerm getInstance(final boolean value) {
			return value ? TRUE : FALSE;
		}

		private static final long serialVersionUID = -8073302646525000957L;

		private final boolean value;

		private BooleanSearchTerm(final boolean value) {
			super();
			this.value = value;
		}

		@Override
		public boolean match(final Message msg) {
			return value;
		}

	}

	/**
	 * {@link SearchTermMatcher} - A search term matcher
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static interface SearchTermMatcher {
		/**
		 * Checks if given message matches specified search term
		 * 
		 * @param searchTerm
		 *            The search term
		 * @param msg
		 *            The message to check
		 * @return <code>true</code> if message matches specified search term;
		 *         otherwise <code>false</code>
		 * @throws MailException
		 *             If check fails
		 */
		public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, Message msg)
				throws MailException;
	}

	/**
	 * Generates the corresponding instance of {@link SearchTermMatcher} to
	 * specified search term
	 * 
	 * @param term
	 *            The search term
	 * @return The corresponding instance of {@link SearchTermMatcher}
	 */
	private static SearchTermMatcher createSearchTermMatcher(final com.openexchange.mail.search.SearchTerm<?> term) {
		if (term instanceof com.openexchange.mail.search.HeaderTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final String[] hdr = ((com.openexchange.mail.search.HeaderTerm) searchTerm).getPattern();
					final String[] val;
					try {
						val = msg.getHeader(hdr[0]);
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
					if ((val == null || val.length == 0) && (hdr[1] == null)) {
						return true;
					}
					boolean found = false;
					for (int i = 0; i < val.length && !found; i++) {
						found = (val[i].toLowerCase(Locale.ENGLISH).indexOf(hdr[1]) != -1);
					}
					return found;
				}
			};
		} else if (term instanceof com.openexchange.mail.search.FlagTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					int flags = ((com.openexchange.mail.search.FlagTerm) term).getPattern().intValue();
					final boolean set;
					if (flags < 0) {
						set = false;
						flags *= -1;
					} else {
						set = true;
					}
					final Flags flagsObj = MIMEMessageConverter.convertMailFlags(flags);
					final Flags msgFlags;
					try {
						msgFlags = msg.getFlags();
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
					return set ? msgFlags.contains(flagsObj) : !msgFlags.contains(flagsObj);
				}
			};
		} else if (term instanceof com.openexchange.mail.search.FromTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					try {
						return (getAllAddresses((InternetAddress[]) msg.getFrom()).toLowerCase()
								.indexOf(
										((com.openexchange.mail.search.FromTerm) term).getPattern().toLowerCase(
												Locale.ENGLISH)) != -1);
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
				}
			};
		} else if (term instanceof com.openexchange.mail.search.ToTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					try {
						return (getAllAddresses((InternetAddress[]) msg.getRecipients(Message.RecipientType.TO))
								.toLowerCase().indexOf(
										((com.openexchange.mail.search.ToTerm) term).getPattern().toLowerCase(
												Locale.ENGLISH)) != -1);
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
				}
			};
		} else if (term instanceof com.openexchange.mail.search.CcTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					try {
						return (getAllAddresses((InternetAddress[]) msg.getRecipients(Message.RecipientType.CC))
								.toLowerCase().indexOf(
										((com.openexchange.mail.search.CcTerm) term).getPattern().toLowerCase(
												Locale.ENGLISH)) != -1);
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
				}
			};
		} else if (term instanceof com.openexchange.mail.search.BccTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					try {
						return (getAllAddresses((InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC))
								.toLowerCase().indexOf(
										((com.openexchange.mail.search.BccTerm) term).getPattern().toLowerCase(
												Locale.ENGLISH)) != -1);
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
				}
			};
		} else if (term instanceof com.openexchange.mail.search.SubjectTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final String subject;
					try {
						subject = msg.getSubject();
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
					if (subject != null) {
						return (subject.toLowerCase(Locale.ENGLISH).indexOf(
								((com.openexchange.mail.search.SubjectTerm) term).getPattern().toLowerCase(
										Locale.ENGLISH)) != -1);
					}
					return false;
				}
			};
		} else if (term instanceof com.openexchange.mail.search.SizeTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final int size;
					try {
						size = msg.getSize();
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
					final int[] dat = ((com.openexchange.mail.search.SizeTerm) term).getPattern();
					if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
						return size == dat[1];
					} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
						return size < dat[1];
					} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
						return size > dat[1];
					} else {
						return size == dat[1];
					}
				}
			};
		} else if (term instanceof com.openexchange.mail.search.SentDateTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final Date sentDate;
					try {
						sentDate = msg.getSentDate();
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
					if (null == sentDate) {
						return false;
					}
					final long[] dat = ((com.openexchange.mail.search.SentDateTerm) term).getPattern();
					if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
						return dat[1] == sentDate.getTime();
					} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
						return dat[1] > sentDate.getTime();
					} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
						return dat[1] < sentDate.getTime();
					} else {
						return dat[1] == sentDate.getTime();
					}
				}
			};
		} else if (term instanceof com.openexchange.mail.search.ReceivedDateTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final Date receivedDate;
					try {
						receivedDate = msg.getReceivedDate();
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					}
					if (null == receivedDate) {
						return false;
					}
					final long[] dat = ((com.openexchange.mail.search.ReceivedDateTerm) term).getPattern();
					if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
						return dat[1] == receivedDate.getTime();
					} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
						return dat[1] > receivedDate.getTime();
					} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
						return dat[1] < receivedDate.getTime();
					} else {
						return dat[1] == receivedDate.getTime();
					}
				}
			};
		} else if (term instanceof com.openexchange.mail.search.ANDTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ANDTerm) term)
							.getPattern();
					return createSearchTermMatcher(terms[0]).matches(terms[0], msg)
							&& createSearchTermMatcher(terms[1]).matches(terms[1], msg);
				}
			};
		} else if (term instanceof com.openexchange.mail.search.ORTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ORTerm) term)
							.getPattern();
					return createSearchTermMatcher(terms[0]).matches(terms[0], msg)
							|| createSearchTermMatcher(terms[1]).matches(terms[1], msg);
				}
			};
		} else if (term instanceof com.openexchange.mail.search.BodyTerm) {
			return new SearchTermMatcher() {
				public boolean matches(final com.openexchange.mail.search.SearchTerm<?> searchTerm, final Message msg)
						throws MailException {
					final String pattern = ((com.openexchange.mail.search.BodyTerm) term).getPattern();
					final String text;
					try {
						text = getTextContent(msg);
					} catch (final MessagingException e) {
						throw MIMEMailException.handleMessagingException(e);
					} catch (final IOException e) {
						throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
					}
					if (text != null) {
						return (text.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) > -1);
					}
					return false;
				}
			};
		}
		return null;
	}

	/**
	 * Extracts textual content out of given message's body
	 * 
	 * @param msg
	 *            The message whose textual content shall be extracted
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private static String getTextContent(final Message msg) throws MailException, MessagingException, IOException {
		if (ContentType.isMimeType(msg.getContentType(), "multipart/*")) {
			final Multipart multipart = (Multipart) msg.getContent();
			final int count = multipart.getCount();
			for (int i = 0; i < count; i++) {
				final String text = getPartTextContent(multipart.getBodyPart(i));
				if (text != null) {
					return text;
				}
			}
		}
		return getPartTextContent(msg);
	}

	/**
	 * Extracts textual content out of given part's body
	 * 
	 * @param part
	 *            The part
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private static String getPartTextContent(final Part part) throws MailException, MessagingException, IOException {
		final ContentType ct = new ContentType(part.getContentType());
		if (ct.isMimeType("text/htm*")) {
			return new Html2TextConverter().convert(MessageUtility.readMimePart(part, ct));
		} else if (ct.isMimeType("text/*")) {
			return MessageUtility.readMimePart(part, ct);
		}
		return null;
	}
}
