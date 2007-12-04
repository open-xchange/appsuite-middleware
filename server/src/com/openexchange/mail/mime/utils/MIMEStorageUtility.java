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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.FetchProfile;
import javax.mail.UIDFolder;

import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.mime.MessageHeaders;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.UIDSet;

/**
 * {@link MIMEStorageUtility} - Provides MIME-specific storage utility methods
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMEStorageUtility {

	/**
	 * No instance
	 */
	private MIMEStorageUtility() {
		super();
	}

	private static final FetchProfile CACHE_FETCH_PROFILE = new FetchProfile();

	private static final Collection<MailListField> CACHE_FIELDS;

	private static final FetchProfile UID_FETCH_PROFILE = new FetchProfile();

	static {
		CACHE_FETCH_PROFILE.add(FetchProfile.Item.ENVELOPE);
		CACHE_FETCH_PROFILE.add(FetchProfile.Item.FLAGS);
		CACHE_FETCH_PROFILE.add(FetchProfile.Item.CONTENT_INFO);
		CACHE_FETCH_PROFILE.add(UIDFolder.FetchProfileItem.UID);
		CACHE_FETCH_PROFILE.add(IMAPFolder.FetchProfileItem.SIZE);
		CACHE_FETCH_PROFILE.add(MessageHeaders.HDR_X_PRIORITY);
		UID_FETCH_PROFILE.add(UIDFolder.FetchProfileItem.UID);
		CACHE_FIELDS = fetchProfile2MailListFields(CACHE_FETCH_PROFILE);
	}

	/**
	 * The corresponding fields to fetch profile obtained by
	 * {@link #getCacheFetchProfile()}
	 * 
	 * @return The corresponding fields to the fetch profile obtained by
	 *         {@link #getCacheFetchProfile()}
	 */
	public static Collection<MailListField> getCacheFields() {
		return CACHE_FIELDS;
	}

	/**
	 * Messages which are pre-filled with the fetch profile returned by this
	 * method are completely pre-filles with all data that can occur in a
	 * listing. Thus these messages can be put into cache for future list
	 * requests
	 * 
	 * @return An appropriate instance of {@link FetchProfile} to pre-fill
	 *         messages ready for being put into cache
	 */
	public static FetchProfile getCacheFetchProfile() {
		return CACHE_FETCH_PROFILE;
	}

	/**
	 * @return An instance of {@link FetchProfile} which only prefills messages
	 *         with their UIDs
	 */
	public static FetchProfile getUIDFetchProfile() {
		return UID_FETCH_PROFILE;
	}

	/**
	 * Creates an appropriate collection of {@link MailListField} enumeration
	 * constants from given fetch profile
	 * 
	 * @param fetchProfile
	 *            The fetch profile
	 * @return An appropriate collection of {@link MailListField} enumeration
	 *         constants
	 */
	public static Collection<MailListField> fetchProfile2MailListFields(final FetchProfile fetchProfile) {
		final Set<MailListField> set = new HashSet<MailListField>();
		/*
		 * Folder is always set
		 */
		set.add(MailListField.FOLDER_ID);
		if (fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
			/*
			 * From, To, Cc, Bcc, ReplyTo, Subject and Date
			 */
			set.add(MailListField.FROM);
			set.add(MailListField.TO);
			set.add(MailListField.CC);
			set.add(MailListField.BCC);
			set.add(MailListField.SUBJECT);
			set.add(MailListField.RECEIVED_DATE);
			set.add(MailListField.SENT_DATE);
			set.add(MailListField.SIZE);
		} else {
			set.add(MailListField.RECEIVED_DATE);
			if (fetchProfile.contains(IMAPFolder.FetchProfileItem.SIZE)) {
				set.add(MailListField.SIZE);
			}
		}
		if (fetchProfile.contains(UIDFolder.FetchProfileItem.UID)) {
			set.add(MailListField.ID);
		}
		if (fetchProfile.contains(FetchProfile.Item.CONTENT_INFO)) {
			set.add(MailListField.ATTACHMENT);
		}
		if (fetchProfile.contains(FetchProfile.Item.FLAGS)) {
			set.add(MailListField.FLAGS);
			set.add(MailListField.COLOR_LABEL);
			set.add(MailListField.FLAG_SEEN);
		}
		if (fetchProfile.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
			set.add(MailListField.FROM);
			set.add(MailListField.TO);
			set.add(MailListField.CC);
			set.add(MailListField.BCC);
			set.add(MailListField.SUBJECT);
			set.add(MailListField.DISPOSITION_NOTIFICATION_TO);
			set.add(MailListField.PRIORITY);
			set.add(MailListField.SENT_DATE);
		} else {
			if (fetchProfile.contains(MessageHeaders.HDR_FROM)) {
				set.add(MailListField.FROM);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_TO)) {
				set.add(MailListField.TO);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_CC)) {
				set.add(MailListField.CC);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_BCC)) {
				set.add(MailListField.BCC);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_SUBJECT)) {
				set.add(MailListField.SUBJECT);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_DISP_NOT_TO)) {
				set.add(MailListField.DISPOSITION_NOTIFICATION_TO);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_X_PRIORITY)) {
				set.add(MailListField.PRIORITY);
			}
		}
		return set;
	}

	/**
	 * Turns given array of <code>long</code> into an array of
	 * <code>com.sun.mail.imap.protocol.UIDSet</code> which in turn can be
	 * used for a varieties of <code>IMAPProtocol</code> methods.
	 * 
	 * @param uids -
	 *            the UIDs
	 * @return an array of <code>com.sun.mail.imap.protocol.UIDSet</code>
	 */
	public static UIDSet[] toUIDSet(final long[] uids) {
		final List<UIDSet> sets = new ArrayList<UIDSet>(uids.length);
		for (int i = 0; i < uids.length; i++) {
			long current = uids[i];
			final UIDSet set = new UIDSet();
			set.start = current;
			/*
			 * Look for contiguous UIDs
			 */
			Inner: for (++i; i < uids.length; i++) {
				final long next = uids[i];
				if (next == current + 1) {
					current = next;
				} else {
					/*
					 * Break in sequence. Need to reexamine this message at the
					 * top of the outer loop, so decrement 'i' to cancel the
					 * outer loop's auto-increment
					 */
					i--;
					break Inner;
				}
			}
			set.end = current;
			sets.add(set);
		}
		if (sets.isEmpty()) {
			return null;
		}
		return sets.toArray(new UIDSet[sets.size()]);
	}

	/**
	 * Gets the appropriate IMAP fetch profile
	 * 
	 * @param fields
	 *            The fields
	 * @param sortField
	 *            The sort field
	 * @return The appropriate IMAP fetch profile
	 */
	public static FetchProfile getFetchProfile(final MailListField[] fields, final MailListField sortField) {
		return getFetchProfile(fields, null, sortField);
	}

	private static final Set<MailListField> ENV_FIELDS;

	static {
		ENV_FIELDS = new HashSet<MailListField>(8);
		/*
		 * The Envelope is an aggregation of the common attributes of a Message:
		 * From, To, Cc, Bcc, ReplyTo, Subject and Date.
		 */
		ENV_FIELDS.add(MailListField.FROM);
		ENV_FIELDS.add(MailListField.TO);
		ENV_FIELDS.add(MailListField.CC);
		ENV_FIELDS.add(MailListField.BCC);
		ENV_FIELDS.add(MailListField.SUBJECT);
		ENV_FIELDS.add(MailListField.SENT_DATE);
		/*
		 * Add the two extra fetch profile items contained in JavaMail's
		 * ENVELOPE constant
		 */
		ENV_FIELDS.add(MailListField.RECEIVED_DATE);
		ENV_FIELDS.add(MailListField.SIZE);
	}

	/**
	 * Gets the appropriate IMAP fetch profile
	 * 
	 * @param fields
	 *            The fields
	 * @param searchFields
	 *            The search fields
	 * @param sortField
	 *            The sort field
	 * @return The appropriate IMAP fetch profile
	 */
	public static FetchProfile getFetchProfile(final MailListField[] fields, final MailListField[] searchFields,
			final MailListField sortField) {
		final FetchProfile retval = new FetchProfile();
		/*
		 * Use a set to avoid duplicate entries
		 */
		final Set<MailListField> set = new HashSet<MailListField>();
		if (fields != null) {
			set.addAll(Arrays.asList(fields));
		}
		if (searchFields != null) {
			set.addAll(Arrays.asList(searchFields));
		}
		if (sortField != null) {
			set.add(sortField);
		}
		/*
		 * Check which fields are contained in fetch profile item "ENVELOPE"
		 */
		if (IMAPConfig.isFastFetch() && set.removeAll(ENV_FIELDS)) {
			/*
			 * Add ENVELOPE since set of fields has changed
			 */
			retval.add(FetchProfile.Item.ENVELOPE);
		}
		if (!set.isEmpty()) {
			final int size = set.size();
			final Iterator<MailListField> iter = set.iterator();
			for (int i = 0; i < size; i++) {
				addFetchItem(retval, iter.next());
			}
		}
		return retval;
	}

	private static void addFetchItem(final FetchProfile fp, final MailListField field) {
		switch (field) {
		case ID:
			fp.add(UIDFolder.FetchProfileItem.UID);
			break;
		case ATTACHMENT:
			fp.add(FetchProfile.Item.CONTENT_INFO);
			break;
		case FROM:
			fp.add(MessageHeaders.HDR_FROM);
			break;
		case TO:
			fp.add(MessageHeaders.HDR_TO);
			break;
		case CC:
			fp.add(MessageHeaders.HDR_CC);
			break;
		case BCC:
			fp.add(MessageHeaders.HDR_BCC);
			break;
		case SUBJECT:
			fp.add(MessageHeaders.HDR_SUBJECT);
			break;
		case SIZE:
			fp.add(IMAPFolder.FetchProfileItem.SIZE);
			break;
		case SENT_DATE:
			fp.add(MessageHeaders.HDR_DATE);
			break;
		case FLAGS:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		case DISPOSITION_NOTIFICATION_TO:
			fp.add(MessageHeaders.HDR_DISP_NOT_TO);
			break;
		case PRIORITY:
			fp.add(MessageHeaders.HDR_X_PRIORITY);
			break;
		case COLOR_LABEL:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		case FLAG_SEEN:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		default:
			return;
		}
	}
}
