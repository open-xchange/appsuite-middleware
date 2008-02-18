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

package com.openexchange.imap.sort;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getCacheFetchProfile;
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getCacheFields;
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getFetchProfile;
import static com.openexchange.mail.utils.StorageUtility.EMPTY_MSGS;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.StorageUtility.DummyAddress;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPSort}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPSort {

	/**
	 * {@link MailComparator}
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class MailComparator implements Comparator<Message> {

		private static final String STR_EMPTY = "";

		private final boolean descendingDir;

		private final MailListField sortField;

		private final Locale locale;

		private static abstract class FieldComparer {

			public final Locale locale;

			public Collator collator;

			public FieldComparer(final Locale locale) {
				this.locale = locale;
			}

			public Collator getCollator() {
				if (collator == null) {
					collator = Collator.getInstance(locale);
					collator.setStrength(Collator.SECONDARY);
				}
				return collator;
			}

			public abstract int compareFields(final Message msg1, final Message msg2) throws MessagingException;
		}

		private final FieldComparer fieldComparer;

		public MailComparator(final boolean descendingDirection, final Locale locale) {
			this(MailListField.SENT_DATE, descendingDirection, locale);
		}

		public MailComparator(final MailListField sortField, final boolean descendingDirection, final Locale locale) {
			this.sortField = sortField;
			this.descendingDir = descendingDirection;
			this.locale = locale;
			fieldComparer = createFieldComparer(this.sortField, this.locale);
		}

		private static int compareAddrs(final Address[] addrs1, final Address[] addrs2, final Locale locale,
				final Collator collator) {
			if (isEmptyAddrArray(addrs1) && !isEmptyAddrArray(addrs2)) {
				return -1;
			} else if (!isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
				return 1;
			} else if (isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
				return 0;
			}
			return collator.compare(getCompareStringFromAddress(addrs1[0], locale), getCompareStringFromAddress(
					addrs2[0], locale));
		}

		private static boolean isEmptyAddrArray(final Address[] addrs) {
			return (addrs == null || addrs.length == 0);
		}

		private static String getCompareStringFromAddress(final Address addr, final Locale locale) {
			if (addr instanceof DummyAddress) {
				final DummyAddress da1 = (DummyAddress) addr;
				return da1.getAddress().toLowerCase(Locale.ENGLISH);
			} else if (addr instanceof InternetAddress) {
				final InternetAddress ia1 = (InternetAddress) addr;
				final String personal = ia1.getPersonal();
				if (personal != null && personal.length() > 0) {
					/*
					 * Personal is present. Skip leading quotes.
					 */
					return personal.charAt(0) == '\'' || personal.charAt(0) == '"' ? personal.substring(1).toLowerCase(
							locale) : personal.toLowerCase(locale);
				}
				return ia1.getAddress().toLowerCase(Locale.ENGLISH);
			} else {
				return STR_EMPTY;
			}
		}

		private static Integer compareReferences(final Object o1, final Object o2) {
			if (o1 == null && o2 != null) {
				return Integer.valueOf(-1);
			} else if (o1 != null && o2 == null) {
				return Integer.valueOf(1);
			} else if (o1 == null && o2 == null) {
				return Integer.valueOf(0);
			}
			/*
			 * Both references are not null
			 */
			return null;
		}

		private static FieldComparer createFieldComparer(final MailListField sortCol, final Locale locale) {
			switch (sortCol) {
			case SENT_DATE:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final Date d1 = msg1.getSentDate();
						final Date d2 = msg2.getSentDate();
						final Integer refComp = compareReferences(d1, d2);
						return refComp == null ? d1.compareTo(d2) : refComp.intValue();
					}
				};
			case RECEIVED_DATE:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final Date d1 = msg1.getReceivedDate();
						final Date d2 = msg2.getReceivedDate();
						final Integer refComp = compareReferences(d1, d2);
						return refComp == null ? d1.compareTo(d2) : refComp.intValue();
					}
				};
			case FROM:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						return compareAddrs(msg1.getFrom(), msg2.getFrom(), this.locale, getCollator());
					}
				};
			case TO:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						return compareAddrs(msg1.getRecipients(Message.RecipientType.TO), msg2
								.getRecipients(Message.RecipientType.TO), this.locale, getCollator());
					}
				};
			case SUBJECT:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final String sub1 = msg1.getSubject() == null ? STR_EMPTY : msg1.getSubject();
						final String sub2 = msg2.getSubject() == null ? STR_EMPTY : msg2.getSubject();
						return getCollator().compare(sub1, sub2);
					}
				};
			case FLAG_SEEN:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						final boolean isSeen1 = msg1.isSet(Flags.Flag.SEEN);
						final boolean isSeen2 = msg2.isSet(Flags.Flag.SEEN);
						if (isSeen1 && isSeen2) {
							return 0;
						} else if (!isSeen1 && !isSeen2) {
							final boolean isRecent1 = msg1.isSet(Flags.Flag.RECENT);
							final boolean isRecent2 = msg2.isSet(Flags.Flag.RECENT);
							if (isRecent1 && isRecent2) {
								return 0;
							} else if (!isRecent1 && !isRecent2) {
								return 0;
							} else if (isRecent1 && !isRecent2) {
								return 1;
							} else if (!isRecent1 && isRecent2) {
								return -1;
							}
						} else if (isSeen1 && !isSeen2) {
							return 1;
						} else if (!isSeen1 && isSeen2) {
							return -1;
						}
						return 0;
					}
				};
			case SIZE:
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
						return Integer.valueOf(msg1.getSize()).compareTo(Integer.valueOf(msg2.getSize()));
					}
				};
			case COLOR_LABEL:
				if (IMAPConfig.isUserFlagsEnabled()) {
					return new FieldComparer(locale) {
						@Override
						public int compareFields(final Message msg1, final Message msg2) throws MessagingException {
							final Integer cl1 = getColorFlag(msg1.getFlags().getUserFlags());
							final Integer cl2 = getColorFlag(msg2.getFlags().getUserFlags());
							return cl1.compareTo(cl2);
						}
					};
				}
				return new FieldComparer(locale) {
					@Override
					public int compareFields(final Message msg1, final Message msg2) {
						return 0;
					}
				};
			default:
				throw new UnsupportedOperationException("Unknown sort column value " + sortCol);
			}
		}

		private static Integer getColorFlag(final String[] userFlags) {
			for (int i = 0; i < userFlags.length; i++) {
				if (userFlags[i].startsWith(MailMessage.COLOR_LABEL_PREFIX)) {
					return Integer.valueOf(userFlags[i].substring(3));
				}
			}
			return Integer.valueOf(MailMessage.COLOR_LABEL_NONE);
		}

		public int compare(final Message msg1, final Message msg2) {
			try {
				int comparedTo = fieldComparer.compareFields(msg1, msg2);
				if (descendingDir) {
					comparedTo *= (-1);
				}
				return comparedTo;
			} catch (final MessagingException e) {
				LOG.error(e.getMessage(), e);
				return 0;
			}
		}
	} // End of class declaration for MailComparator

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPSort.class);

	/**
	 * No instantiation
	 */
	private IMAPSort() {
		super();
	}

	/**
	 * Gets a new instance of {@link Comparator} designed to sort a collection
	 * of {@link Message} objects
	 * 
	 * @param sortField
	 *            The sort field
	 * @param orderDir
	 *            {@link OrderDirection#DESC} for descending order or
	 *            {@link OrderDirection#ASC} for ascending order
	 * @param locale
	 *            The locale (needed for proper sorting by textual content)
	 * @return
	 */
	public static Comparator<Message> getMessageComparator(final MailListField sortField,
			final OrderDirection orderDir, final Locale locale) {
		return new MailComparator(sortField, orderDir == OrderDirection.DESC, locale);
	}

	/**
	 * Sorts messages located in given IMAP folder
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @param filter
	 *            Pre-Selected messages' sequence numbers to sort or
	 *            <code>null</code> to sort all
	 * @param fields
	 *            The desired fields
	 * @param sortFieldArg
	 *            The sort field
	 * @param orderDir
	 *            The order direction
	 * @param locale
	 *            The locale
	 * @param usedFields
	 *            The set to fill with actually used fields
	 * @return Sorted messages
	 * @throws MessagingException
	 */
	public static Message[] sortMessages(final IMAPFolder imapFolder, final int[] filter, final MailListField[] fields,
			final MailListField sortFieldArg, final OrderDirection orderDir, final Locale locale,
			final Set<MailListField> usedFields, final IMAPConfig imapConfig) throws MessagingException {
		boolean applicationSort = true;
		Message[] msgs = null;
		final MailListField sortField = sortFieldArg == null ? MailListField.RECEIVED_DATE : sortFieldArg;
		final int size = filter == null ? imapFolder.getMessageCount() : filter.length;
		/*
		 * Perform an IMAP-based sort if IMAP sort is enabled through config or
		 * number of messages to sort exceeds limit.
		 */
		if (imapConfig.isImapSort() || (size >= IMAPConfig.getMailFetchLimit())) {
			try {
				long start = System.currentTimeMillis();
				final int[] seqNums;
				if (filter == null) {
					/*
					 * Sort all
					 */
					seqNums = IMAPCommandsCollection.getServerSortList(imapFolder, getSortCritForIMAPCommand(sortField,
							orderDir == OrderDirection.DESC));
				} else {
					/*
					 * Only sort pre-selected messages
					 */
					seqNums = IMAPCommandsCollection.getServerSortList(imapFolder, getSortCritForIMAPCommand(sortField,
							orderDir == OrderDirection.DESC), filter);
				}
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(128).append("IMAP sort took ").append(
							(System.currentTimeMillis() - start)).append("msec").toString());
				}
				if (seqNums == null || seqNums.length == 0) {
					return EMPTY_MSGS;
				}
				final FetchProfile fetchProfile;
				if (seqNums.length < IMAPConfig.getMailFetchLimit()) {
					fetchProfile = getCacheFetchProfile();
					usedFields.addAll(getCacheFields());
				} else {
					fetchProfile = getFetchProfile(fields, IMAPConfig.isFastFetch());
					usedFields.addAll(Arrays.asList(fields));
				}
				start = System.currentTimeMillis();
				msgs = new FetchIMAPCommand(imapFolder, seqNums, fetchProfile, false, true).doCommand();
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(128).append("IMAP fetch for ").append(seqNums.length).append(
							" messages took ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
				if (msgs == null || msgs.length == 0) {
					return EMPTY_MSGS;
				}
				applicationSort = false;
			} catch (final MessagingException e) {
				if (LOG.isWarnEnabled()) {
					final IMAPException imapException = new IMAPException(IMAPException.Code.IMAP_SORT_FAILED, e, e
							.getMessage());
					LOG.warn(imapException.getLocalizedMessage(), imapException);
				}
				applicationSort = true;
			}
		}
		if (applicationSort) {
			/*
			 * Select all messages if user does not want a search being
			 * performed
			 */
			if (filter == null) {
				final int allMsgCount = size;
				final FetchProfile fetchProfile;
				if (allMsgCount < IMAPConfig.getMailFetchLimit()) {
					fetchProfile = getCacheFetchProfile();
					usedFields.addAll(getCacheFields());
				} else {
					fetchProfile = getFetchProfile(fields, sortField, IMAPConfig.isFastFetch());
					usedFields.addAll(Arrays.asList(fields));
					usedFields.add(sortField);
				}
				final long start = System.currentTimeMillis();
				msgs = new FetchIMAPCommand(imapFolder, fetchProfile, allMsgCount).doCommand();
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(128).append("IMAP fetch for ").append(allMsgCount).append(
							" messages took ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
			} else {
				final FetchProfile fetchProfile;
				if (filter.length < IMAPConfig.getMailFetchLimit()) {
					fetchProfile = getCacheFetchProfile();
					usedFields.addAll(getCacheFields());
				} else {
					fetchProfile = getFetchProfile(fields, sortField, IMAPConfig.isFastFetch());
					usedFields.addAll(Arrays.asList(fields));
					usedFields.add(sortField);
				}
				final long start = System.currentTimeMillis();
				msgs = new FetchIMAPCommand(imapFolder, filter, fetchProfile, false, false).doCommand();
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(128).append("IMAP fetch for ").append(filter.length).append(
							" messages took ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
			}
			if (msgs == null || msgs.length == 0) {
				return EMPTY_MSGS;
			}
			final List<Message> msgList = Arrays.asList(msgs);
			Collections.sort(msgList, new MailComparator(sortField, orderDir == OrderDirection.DESC, locale));
			msgList.toArray(msgs);
		}
		return msgs;
	}

	/**
	 * Generates the sort criteria corresponding to specified sort field and
	 * order direction.
	 * <p>
	 * Example:<br>
	 * {@link MailListField#SENT_DATE} in descending order is turned to
	 * <code>"REVERSE DATE"</code>.
	 * 
	 * @param sortField
	 *            The sort field
	 * @param descendingDirection
	 *            The order direction
	 * @return The sort criteria ready for being used inside IMAP's <i>SORT</i>
	 *         command
	 * @throws MessagingException
	 *             If an unsupported sort field is specified
	 */
	private static String getSortCritForIMAPCommand(final MailListField sortField, final boolean descendingDirection)
			throws MessagingException {
		final StringBuilder imapSortCritBuilder = new StringBuilder(16).append(descendingDirection ? "REVERSE " : "");
		switch (sortField) {
		case SENT_DATE:
			imapSortCritBuilder.append("DATE");
			break;
		case RECEIVED_DATE:
			imapSortCritBuilder.append("ARRIVAL");
			break;
		case FROM:
			imapSortCritBuilder.append("FROM");
			break;
		case TO:
			imapSortCritBuilder.append("TO");
			break;
		case SUBJECT:
			imapSortCritBuilder.append("SUBJECT");
			break;
		case FOLDER:
			imapSortCritBuilder.append("FOLDER");
			break;
		case SIZE:
			imapSortCritBuilder.append("SIZE");
			break;
		default:
			throw new MessagingException("Field " + sortField + " NOT supported for IMAP-sided sorting");
		}
		return imapSortCritBuilder.toString();
	}

}
