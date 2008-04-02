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

import com.openexchange.mail.MailField;
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

	private static final Collection<MailField> CACHE_FIELDS;

	private static final MailField[] CACHE_FIELDS_ARR;

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
		CACHE_FIELDS_ARR = CACHE_FIELDS.toArray(new MailField[CACHE_FIELDS.size()]);
	}

	/**
	 * The corresponding fields to fetch profile obtained by
	 * {@link #getCacheFetchProfile()}
	 * 
	 * @return The corresponding fields to the fetch profile obtained by
	 *         {@link #getCacheFetchProfile()}
	 */
	public static Collection<MailField> getCacheFields() {
		return CACHE_FIELDS;
	}

	/**
	 * The corresponding fields to fetch profile obtained by
	 * {@link #getCacheFetchProfile()}
	 * 
	 * @return The corresponding fields to the fetch profile obtained by
	 *         {@link #getCacheFetchProfile()}
	 */
	public static MailField[] getCacheFieldsArray() {
		return CACHE_FIELDS_ARR;
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
	public static Collection<MailField> fetchProfile2MailListFields(final FetchProfile fetchProfile) {
		final Set<MailField> set = new HashSet<MailField>();
		/*
		 * Folder is always set
		 */
		set.add(MailField.FOLDER_ID);
		if (fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
			/*
			 * From, To, Cc, Bcc, ReplyTo, Subject and Date
			 */
			set.add(MailField.FROM);
			set.add(MailField.TO);
			set.add(MailField.CC);
			set.add(MailField.BCC);
			set.add(MailField.SUBJECT);
			set.add(MailField.RECEIVED_DATE);
			set.add(MailField.SENT_DATE);
			set.add(MailField.SIZE);
		} else {
			set.add(MailField.RECEIVED_DATE);
			if (fetchProfile.contains(IMAPFolder.FetchProfileItem.SIZE)) {
				set.add(MailField.SIZE);
			}
		}
		if (fetchProfile.contains(UIDFolder.FetchProfileItem.UID)) {
			set.add(MailField.ID);
		}
		if (fetchProfile.contains(FetchProfile.Item.CONTENT_INFO)) {
			set.add(MailField.CONTENT_TYPE);
		}
		if (fetchProfile.contains(FetchProfile.Item.FLAGS)) {
			set.add(MailField.FLAGS);
			set.add(MailField.COLOR_LABEL);
		}
		if (fetchProfile.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
			set.add(MailField.FROM);
			set.add(MailField.TO);
			set.add(MailField.CC);
			set.add(MailField.BCC);
			set.add(MailField.SUBJECT);
			set.add(MailField.DISPOSITION_NOTIFICATION_TO);
			set.add(MailField.PRIORITY);
			set.add(MailField.SENT_DATE);
		} else {
			if (fetchProfile.contains(MessageHeaders.HDR_FROM)) {
				set.add(MailField.FROM);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_TO)) {
				set.add(MailField.TO);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_CC)) {
				set.add(MailField.CC);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_BCC)) {
				set.add(MailField.BCC);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_SUBJECT)) {
				set.add(MailField.SUBJECT);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_DISP_NOT_TO)) {
				set.add(MailField.DISPOSITION_NOTIFICATION_TO);
			}
			if (fetchProfile.contains(MessageHeaders.HDR_X_PRIORITY)) {
				set.add(MailField.PRIORITY);
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
	 * Gets the appropriate fetch profile.
	 * <p>
	 * <b>Note</b> that {@link MailField#BODY} and {@link MailField#FULL} are
	 * discarded since no corresponding fetch profile item exists and therefore
	 * should be handled separately.
	 * 
	 * @param fields
	 *            The fields
	 * @param preferEnvelope
	 *            <code>true</code> to prefer ENVELOPE instead of single fetch
	 *            items; otherwise <code>false</code>
	 * @return The appropriate IMAP fetch profile
	 */
	public static FetchProfile getFetchProfile(final MailField[] fields, final boolean preferEnvelope) {
		return getFetchProfile(fields, null, preferEnvelope);
	}

	/**
	 * Gets the appropriate fetch profile
	 * <p>
	 * <b>Note</b> that {@link MailField#BODY} and {@link MailField#FULL} are
	 * discarded since no corresponding fetch profile item exists and therefore
	 * should be handled separately.
	 * 
	 * @param fields
	 *            The fields
	 * @param sortField
	 *            The sort field
	 * @param preferEnvelope
	 *            <code>true</code> to prefer ENVELOPE instead of single fetch
	 *            items; otherwise <code>false</code>
	 * @return The appropriate IMAP fetch profile
	 */
	public static FetchProfile getFetchProfile(final MailField[] fields, final MailField sortField,
			final boolean preferEnvelope) {
		return getFetchProfile(fields, null, sortField, preferEnvelope);
	}

	private static final Set<MailField> ENV_FIELDS;

	static {
		ENV_FIELDS = new HashSet<MailField>(8);
		/*
		 * The Envelope is an aggregation of the common attributes of a Message:
		 * From, To, Cc, Bcc, ReplyTo, Subject and Date.
		 */
		ENV_FIELDS.add(MailField.FROM);
		ENV_FIELDS.add(MailField.TO);
		ENV_FIELDS.add(MailField.CC);
		ENV_FIELDS.add(MailField.BCC);
		ENV_FIELDS.add(MailField.SUBJECT);
		ENV_FIELDS.add(MailField.SENT_DATE);
		/*
		 * Discard the two extra fetch profile items contained in JavaMail's
		 * ENVELOPE constant: RFC822.SIZE and INTERNALDATE
		 */
		// ENV_FIELDS.add(MailListField.RECEIVED_DATE);
		// ENV_FIELDS.add(MailListField.SIZE);
	}

	/**
	 * Gets the appropriate fetch profile
	 * <p>
	 * <b>Note</b> that {@link MailField#BODY} and {@link MailField#FULL} are
	 * discarded since no corresponding fetch profile item exists and therefore
	 * should be handled separately.
	 * 
	 * @param fields
	 *            The fields
	 * @param searchFields
	 *            The search fields
	 * @param sortField
	 *            The sort field
	 * @param preferEnvelope
	 *            <code>true</code> to prefer ENVELOPE instead of single fetch
	 *            items; otherwise <code>false</code>
	 * @return The appropriate IMAP fetch profile
	 */
	public static FetchProfile getFetchProfile(final MailField[] fields, final MailField[] searchFields,
			final MailField sortField, final boolean preferEnvelope) {
		final FetchProfile retval = new FetchProfile();
		/*
		 * Use a set to avoid duplicate entries
		 */
		final Set<MailField> set = new HashSet<MailField>();
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
		if (preferEnvelope && set.removeAll(ENV_FIELDS)) {
			/*
			 * Add ENVELOPE since set of fields has changed
			 */
			retval.add(FetchProfile.Item.ENVELOPE);
		}
		if (!set.isEmpty()) {
			final int size = set.size();
			final Iterator<MailField> iter = set.iterator();
			for (int i = 0; i < size; i++) {
				addFetchItem(retval, iter.next());
			}
		}
		return retval;
	}

	private static void addFetchItem(final FetchProfile fp, final MailField field) {
		switch (field) {
		case HEADERS:
			fp.add(IMAPFolder.FetchProfileItem.HEADERS);
			break;
		case ID:
			fp.add(UIDFolder.FetchProfileItem.UID);
			break;
		case CONTENT_TYPE:
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
			fp.add(FetchProfile.Item.FLAGS);
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
		default:
			return;
		}
	}
}
