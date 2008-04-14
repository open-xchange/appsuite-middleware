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

package com.openexchange.mail.search;

import static com.openexchange.mail.utils.StorageUtility.getAllAddresses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.text.Html2TextConverter;
import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link Searcher} - Provides methods to check if a single mail message matches
 * a search term.
 * <p>
 * Moreover it provides a method to search for matching mail messages in a given
 * message array with a given search term.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class Searcher {

	private static final class ComposedSearchTermMatcher implements SearchTermMatcher {

		private SearchTermMatcher firstMatcher;

		private final boolean isOR;

		private SearchTermMatcher secondMatcher;

		/**
		 * Initializes a new {@link ComposedSearchTermMatcher}
		 * 
		 * @param isOR
		 *            Whether the composed search term is an {@link ORTerm} or
		 *            an {@link ANDTerm}
		 */
		public ComposedSearchTermMatcher(final boolean isOR) {
			super();
			this.isOR = isOR;
		}

		@SuppressWarnings(STR_UNCHECKED)
		private SearchTermMatcher getFirstMatcher(final Class<? extends SearchTerm> firstClass) {
			if (null == firstMatcher) {
				firstMatcher = createMatcher(firstClass);
			}
			return firstMatcher;
		}

		@SuppressWarnings(STR_UNCHECKED)
		private SearchTermMatcher getSecondMatcher(final Class<? extends SearchTerm> secondClass) {
			if (null == secondMatcher) {
				secondMatcher = createMatcher(secondClass);
			}
			return secondMatcher;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.openexchange.mail.search.Searcher.SearchTermMatcher#matches(com.openexchange.mail.dataobjects.MailMessage,
		 *      com.openexchange.mail.search.SearchTerm)
		 */
		public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) throws MailException {
			if (isOR) {
				final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ORTerm) searchTerm)
						.getPattern();
				return getFirstMatcher(terms[0].getClass()).matches(mailMessage, terms[0])
						|| getSecondMatcher(terms[1].getClass()).matches(mailMessage, terms[1]);
			}
			final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ANDTerm) searchTerm)
					.getPattern();
			return getFirstMatcher(terms[0].getClass()).matches(mailMessage, terms[0])
					&& getSecondMatcher(terms[1].getClass()).matches(mailMessage, terms[1]);
		}

	}

	private static interface SearchTermMatcher {
		/**
		 * Checks if specified mail message matches given search term
		 * 
		 * @param mailMessage
		 *            The mail message to check
		 * @param searchTerm
		 *            The search term to apply
		 * @return <code>true</code> if specified mail message matches given
		 *         search term; otherwise <code>false</code>
		 * @throws MailException
		 *             If checking mail message against search term fails
		 */
		public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) throws MailException;
	}

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(Searcher.class);

	private static final String STR_UNCHECKED = "unchecked";

	@SuppressWarnings(STR_UNCHECKED)
	private static SearchTermMatcher createMatcher(final Class<? extends SearchTerm> termClass) {
		if (com.openexchange.mail.search.HeaderTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					final String[] hdr = ((com.openexchange.mail.search.HeaderTerm) searchTerm).getPattern();
					final String val = mailMessage.getHeader(hdr[0]);
					if (val == null) {
						if (hdr[1] == null) {
							return true;
						}
						return false;
					}
					return (val.toLowerCase(Locale.ENGLISH).indexOf(hdr[1]) != -1);
				}
			};
		} else if (com.openexchange.mail.search.FlagTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					int flags = ((com.openexchange.mail.search.FlagTerm) searchTerm).getPattern().intValue();
					final boolean set;
					if (flags < 0) {
						set = false;
						flags *= -1;
					} else {
						set = true;
					}
					final int result = (mailMessage.getFlags() & flags);
					return set ? (result == flags) : (result == 0);
				}
			};
		} else if (com.openexchange.mail.search.FromTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					return (getAllAddresses(mailMessage.getFrom()).toLowerCase(Locale.ENGLISH).indexOf(
							((com.openexchange.mail.search.FromTerm) searchTerm).getPattern().toLowerCase(
									Locale.ENGLISH)) != -1);
				}
			};
		} else if (com.openexchange.mail.search.ToTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					return (getAllAddresses(mailMessage.getTo()).toLowerCase(Locale.ENGLISH)
							.indexOf(
									((com.openexchange.mail.search.ToTerm) searchTerm).getPattern().toLowerCase(
											Locale.ENGLISH)) != -1);
				}
			};
		} else if (com.openexchange.mail.search.CcTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					return (getAllAddresses(mailMessage.getCc()).toLowerCase(Locale.ENGLISH)
							.indexOf(
									((com.openexchange.mail.search.CcTerm) searchTerm).getPattern().toLowerCase(
											Locale.ENGLISH)) != -1);
				}
			};
		} else if (com.openexchange.mail.search.BccTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					return (getAllAddresses(mailMessage.getBcc()).toLowerCase(Locale.ENGLISH).indexOf(
							((com.openexchange.mail.search.BccTerm) searchTerm).getPattern()
									.toLowerCase(Locale.ENGLISH)) != -1);
				}
			};
		} else if (com.openexchange.mail.search.SubjectTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					final String subject = mailMessage.getSubject();
					final String pattern = ((com.openexchange.mail.search.SubjectTerm) searchTerm).getPattern();
					if (subject == null) {
						if (null == pattern) {
							return true;
						}
						return false;
					}
					if (null == pattern) {
						return false;
					}
					return (subject.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) != -1);
				}
			};
		} else if (com.openexchange.mail.search.SizeTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					final long size = mailMessage.getSize();
					final int[] dat = ((com.openexchange.mail.search.SizeTerm) searchTerm).getPattern();
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
		} else if (com.openexchange.mail.search.SentDateTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					final Date sentDate = mailMessage.getSentDate();
					if (null == sentDate) {
						return false;
					}
					final long[] dat = ((com.openexchange.mail.search.SentDateTerm) searchTerm).getPattern();
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
		} else if (com.openexchange.mail.search.ReceivedDateTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) {
					final Date receivedDate = mailMessage.getReceivedDate();
					if (null == receivedDate) {
						return false;
					}
					final long[] dat = ((com.openexchange.mail.search.ReceivedDateTerm) searchTerm).getPattern();
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
		} else if (com.openexchange.mail.search.ANDTerm.class.equals(termClass)) {
			return new ComposedSearchTermMatcher(false);
		} else if (com.openexchange.mail.search.ORTerm.class.equals(termClass)) {
			return new ComposedSearchTermMatcher(true);
		} else if (com.openexchange.mail.search.BodyTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm)
						throws MailException {
					final String pattern = ((com.openexchange.mail.search.BodyTerm) searchTerm).getPattern();
					final String text = getTextContent(mailMessage);
					if (text == null) {
						if (null == pattern) {
							return true;
						}
						return false;
					}
					if (null == pattern) {
						return false;
					}
					return (text.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) > -1);
				}
			};
		} else if (com.openexchange.mail.search.BooleanTerm.class.equals(termClass)) {
			return new SearchTermMatcher() {
				public boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm)
						throws MailException {
					return ((com.openexchange.mail.search.BooleanTerm) searchTerm).getPattern().booleanValue();
				}
			};
		} else {
			LOG.error("Unknown search term class: " + termClass.getName());
			return null;
		}
	}

	/**
	 * Extracts textual content out of given part's body
	 * 
	 * @param mailPart
	 *            The part
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 */
	private static String getPartTextContent(final MailPart mailPart) throws MailException {
		if (!mailPart.getContentType().isMimeType("text/*")) {
			/*
			 * No textual content
			 */
			return null;
		}
		String charset = mailPart.getContentType().getCharsetParameter();
		if (null == charset) {
			charset = MailConfig.getDefaultMimeCharset();
		}
		try {
			if (mailPart.getContentType().isMimeType("text/htm*")) {
				return new Html2TextConverter().convert(MessageUtility.readMailPart(mailPart, charset));
			}
			return MessageUtility.readMailPart(mailPart, charset);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	/**
	 * Extracts textual content out of given mail part's body
	 * 
	 * @param mailPart
	 *            The mail message whose textual content shall be extracted
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 */
	private static String getTextContent(final MailPart mailPart) throws MailException {
		if (mailPart.getContentType().isMimeType("multipart/*")) {
			final int count = mailPart.getEnclosedCount();
			for (int i = 0; i < count; i++) {
				final String text = getTextContent(mailPart.getEnclosedMailPart(i));
				if (text != null) {
					return text;
				}
			}
		}
		return getPartTextContent(mailPart);
	}

	/**
	 * Checks if specified mail message matches given search term
	 * 
	 * @param mailMessage
	 *            The mail message to check
	 * @param searchTerm
	 *            The search term to apply
	 * @return <code>true</code> if specified mail message matches given
	 *         search term; otherwise <code>false</code>
	 * @throws MailException
	 *             If checking mail message against search term fails
	 */
	public static boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) throws MailException {
		final SearchTermMatcher matcher = createMatcher(searchTerm.getClass());
		if (null != matcher) {
			return matcher.matches(mailMessage, searchTerm);
		}
		return false;
	}

	/**
	 * Applies specified search term against given instances of
	 * {@link MailMessage}
	 * 
	 * @param mailMessage
	 *            The mail messages to check
	 * @param searchTerm
	 *            The search term to apply
	 * @return The matching mail messages in order of appearance
	 * @throws MailException
	 *             If checking mail messages against search term fails
	 */
	public static MailMessage[] matches(final MailMessage[] mailMessages, final SearchTerm<?> searchTerm)
			throws MailException {
		final SearchTermMatcher matcher = createMatcher(searchTerm.getClass());
		if (null == matcher) {
			return new MailMessage[0];
		}
		final List<MailMessage> matched = new ArrayList<MailMessage>();
		for (final MailMessage mailMessage : mailMessages) {
			if (matcher.matches(mailMessage, searchTerm)) {
				matched.add(mailMessage);
			}
		}
		return matched.toArray(new MailMessage[matched.size()]);
	}

	/**
	 * Initializes a new {@link Searcher}
	 */
	private Searcher() {
		super();
	}
}
