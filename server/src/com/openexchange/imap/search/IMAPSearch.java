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

import static com.openexchange.mail.MailInterfaceImpl.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getFetchProfile;
import static com.openexchange.mail.utils.StorageUtility.getAllAddresses;

import java.io.IOException;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.mime.ContentType;
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
	 * @param searchFields
	 *            The search fields to search in
	 * @param searchPatterns
	 *            The search patterns for search fields
	 * @param linkWithOR
	 *            <code>true</code> to link search terms with a logical OR;
	 *            otherwise to link them with a logical AND
	 * 
	 * @return Filtered messages' sequence numbers according to search criteria
	 * @throws MessagingException
	 * @throws MailException
	 */
	public static int[] searchMessages(final IMAPFolder imapFolder, final MailListField[] searchFields,
			final String[] searchPatterns, final boolean linkWithOR, final IMAPConfig imapConfig)
			throws MessagingException, MailException {
		final int msgCount = imapFolder.getMessageCount();
		/*
		 * Perform an IMAP-based search if IMAP search is enabled through config
		 * or number of messages to search in exceeds limit.
		 */
		if (imapConfig.isImapSearch() || (msgCount >= IMAPConfig.getMailFetchLimit())) {
			try {
				if (searchFields.length != searchPatterns.length) {
					throw new IMAPException(IMAPException.Code.INVALID_SEARCH_PARAMS, Integer
							.valueOf(searchFields.length), Integer.valueOf(searchPatterns.length));
				}
				final SearchTerm searchTerm = getSearchTerm(searchFields, searchPatterns, linkWithOR);
				final long start = System.currentTimeMillis();
				final int[] matchSeqNums = imapFolder.getProtocol().search(searchTerm);
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
		final Message[] allMsgs;
		{
			/**
			 * Formerly:
			 * 
			 * <pre>
			 * final FetchProfile fetchProfile;
			 * if (msgCount &lt; IMAPConfig.getMailFetchLimit()) {
			 * 	fetchProfile = getCacheFetchProfile();
			 * 	usedFields.addAll(getCacheFields());
			 * } else {
			 * 	fetchProfile = getFetchProfile(fields, sortField, IMAPConfig.isFastFetch());
			 * 	usedFields.addAll(Arrays.asList(fields));
			 * 	usedFields.add(sortField);
			 * }
			 * </pre>
			 */
			final long start = System.currentTimeMillis();
			allMsgs = new FetchIMAPCommand(imapFolder, getFetchProfile(searchFields, IMAPConfig.isFastFetch()),
					msgCount).doCommand();
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		}
		final SmartIntArray sia = new SmartIntArray(allMsgs.length / 2);
		final FieldSearcher[] searchers = generateFieldSearchers(searchFields);
		final MessageMatcher msgMatcher = linkWithOR ? ORSearchResult : ANDSearchResult;
		for (int i = 0; i < allMsgs.length; i++) {
			if (msgMatcher.matches(searchers, searchPatterns, allMsgs[i])) {
				sia.append(allMsgs[i].getMessageNumber());
			}
		}
		return sia.toArray();
	}

	/**
	 * Creates an IMAP search term from given search fields and search pattern
	 * for each field
	 * 
	 * @param searchFields -
	 *            the search fields (as defined in <code>MessageObject</code>
	 * @param searchPatterns -
	 *            the search pattern for each field
	 * @param linkWithOR -
	 *            search terms are either logically OR-linked or AND-linked
	 * @return the search term
	 */
	private static SearchTerm getSearchTerm(final MailListField[] searchFields, final String[] searchPatterns,
			final boolean linkWithOR) {
		SearchTerm searchTerm = null;
		for (int i = 0; i < searchFields.length; i++) {
			searchTerm = linkTerm(searchFields[i], searchTerm, searchPatterns[i], linkWithOR);
		}
		return searchTerm;
	}

	private static SearchTerm linkTerm(final MailListField field, final SearchTerm parentTerm, final String pattern,
			final boolean linkWithOR) {
		SearchTerm searchTerm = null;
		switch (field) {
		case FROM:
			searchTerm = new FromStringTerm(pattern);
			break;
		case TO:
			searchTerm = new RecipientStringTerm(Message.RecipientType.TO, pattern);
			break;
		case CC:
			searchTerm = new RecipientStringTerm(Message.RecipientType.CC, pattern);
			break;
		case SUBJECT:
			searchTerm = new SubjectTerm(pattern);
			break;
		default:
			searchTerm = new BodyTerm(pattern);
		}
		if (parentTerm == null) {
			return searchTerm;
		}
		/*
		 * Link with parent term
		 */
		return linkWithOR ? new OrTerm(parentTerm, searchTerm) : new AndTerm(parentTerm, searchTerm);
	}

	private static interface MessageMatcher {
		public boolean matches(FieldSearcher[] searchers, String[] searchPatterns, Message msg) throws MailException;
	}

	private static final MessageMatcher ORSearchResult = new MessageMatcher() {
		public boolean matches(final FieldSearcher[] searchers, final String[] searchPatterns, final Message msg)
				throws MailException {
			boolean result = false;
			for (int k = 0; k < searchers.length && !result; k++) {
				if (searchers[k].find(msg, searchPatterns[k])) {
					result = true;
				}
			}
			return result;
		}
	};

	private static final MessageMatcher ANDSearchResult = new MessageMatcher() {
		public boolean matches(final FieldSearcher[] searchers, final String[] searchPatterns, final Message msg)
				throws MailException {
			boolean result = true;
			for (int k = 0; k < searchers.length && result; k++) {
				if (!searchers[k].find(msg, searchPatterns[k])) {
					result = false;
				}
			}
			return result;
		}
	};

	private static interface FieldSearcher {
		public boolean find(final Message msg, final String pattern) throws MailException;
	}

	private static FieldSearcher[] generateFieldSearchers(final MailListField[] searchFields) {
		final FieldSearcher[] searchers = new FieldSearcher[searchFields.length];
		for (int i = 0; i < searchFields.length; i++) {
			switch (searchFields[i]) {
			case FROM:
				searchers[i] = new FieldSearcher() {
					public boolean find(final Message msg, final String pattern) throws MailException {
						try {
							if (msg.getFrom() != null) {
								return (getAllAddresses((InternetAddress[]) msg.getFrom()).toLowerCase().indexOf(
										pattern.toLowerCase(Locale.ENGLISH)) != -1);
							}
						} catch (final MessagingException e) {
							throw IMAPException.handleMessagingException(e);
						}
						return false;
					}
				};
				break;
			case TO:
				searchers[i] = new FieldSearcher() {
					public boolean find(final Message msg, final String pattern) throws MailException {
						try {
							if (msg.getRecipients(Message.RecipientType.TO) != null) {
								return (getAllAddresses((InternetAddress[]) msg.getRecipients(Message.RecipientType.TO))
										.toLowerCase().indexOf(pattern.toLowerCase(Locale.ENGLISH)) != -1);
							}
						} catch (final MessagingException e) {
							throw IMAPException.handleMessagingException(e);
						}
						return false;
					}
				};
				break;
			case CC:
				searchers[i] = new FieldSearcher() {
					public boolean find(final Message msg, final String pattern) throws MailException {
						try {
							if (msg.getRecipients(Message.RecipientType.CC) != null) {
								return (getAllAddresses((InternetAddress[]) msg.getRecipients(Message.RecipientType.CC))
										.toLowerCase().indexOf(pattern.toLowerCase(Locale.ENGLISH)) != -1);
							}
						} catch (final MessagingException e) {
							throw IMAPException.handleMessagingException(e);
						}
						return false;
					}
				};
				break;
			case SUBJECT:
				searchers[i] = new FieldSearcher() {
					public boolean find(final Message msg, final String pattern) throws MailException {
						try {
							final String subject = msg.getSubject();
							if (subject != null) {
								return (subject.toLowerCase(Locale.ENGLISH)
										.indexOf(pattern.toLowerCase(Locale.ENGLISH)) != -1);
							}
							return false;
						} catch (final MessagingException e) {
							throw IMAPException.handleMessagingException(e);
						}
					}
				};
				break;
			default:
				searchers[i] = new FieldSearcher() {
					public boolean find(final Message msg, final String pattern) throws MailException {
						try {
							final String text = getTextContent(msg);
							if (text != null) {
								return (text.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) > -1);
							}
							return false;
						} catch (final IOException e) {
							throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getLocalizedMessage());
						} catch (final MessagingException e) {
							throw IMAPException.handleMessagingException(e);
						}
					}
				};
			}
		}
		return searchers;
	}

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
