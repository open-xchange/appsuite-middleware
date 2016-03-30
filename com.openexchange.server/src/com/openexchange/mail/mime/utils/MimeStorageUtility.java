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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.UIDFolder;
import com.openexchange.mail.MailField;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MessageHeaders;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.UIDSet;

/**
 * {@link MimeStorageUtility} - Provides MIME-specific storage utility methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeStorageUtility {

    /**
     * No instantiation.
     */
    private MimeStorageUtility() {
        super();
    }

    private static final FetchProfile CACHE_FETCH_PROFILE = new FetchProfile();

    private static final Collection<MailField> CACHE_FIELDS;

    private static final MailField[] CACHE_FIELDS_ARR;

    static {
        // Cache fetch profile
        CACHE_FETCH_PROFILE.add(FetchProfile.Item.ENVELOPE);
        CACHE_FETCH_PROFILE.add(FetchProfile.Item.FLAGS);
        CACHE_FETCH_PROFILE.add(FetchProfile.Item.CONTENT_INFO);
        CACHE_FETCH_PROFILE.add(UIDFolder.FetchProfileItem.UID);
        CACHE_FETCH_PROFILE.add(FetchProfile.Item.SIZE);
        CACHE_FETCH_PROFILE.add(MessageHeaders.HDR_IMPORTANCE);
        CACHE_FETCH_PROFILE.add(MessageHeaders.HDR_X_PRIORITY);
        // CACHE_FETCH_PROFILE.add(IMAPFolder.FetchProfileItem.HEADERS);

        // Cache fields
        final Collection<MailField> fields = fetchProfile2MailListFields(CACHE_FETCH_PROFILE);
        fields.add(MailField.ACCOUNT_NAME);
        CACHE_FIELDS = fields;
        CACHE_FIELDS_ARR = CACHE_FIELDS.toArray(new MailField[CACHE_FIELDS.size()]);
    }

    /**
     * Clones specified fetch profile:
     *
     * @param toClone The fetch profile to clone
     * @return The clone fetch profile
     */
    public static FetchProfile cloneFetchProfile(final FetchProfile toClone) {
        if (null == toClone) {
            return null;
        }
        final FetchProfile clone = new FetchProfile();
        for (final Item item : toClone.getItems()) {
            clone.add(item);
        }
        for (final String headerName : toClone.getHeaderNames()) {
            clone.add(headerName);
        }
        return clone;
    }

    /**
     * The corresponding fields to fetch profile obtained by {@link #getCacheFetchProfile()}
     *
     * @return The corresponding fields to the fetch profile obtained by {@link #getCacheFetchProfile()}
     */
    public static Collection<MailField> getCacheFields() {
        return CACHE_FIELDS;
    }

    /**
     * The corresponding fields to fetch profile obtained by {@link #getCacheFetchProfile()}
     *
     * @return The corresponding fields to the fetch profile obtained by {@link #getCacheFetchProfile()}
     */
    public static MailField[] getCacheFieldsArray() {
        return CACHE_FIELDS_ARR;
    }

    /**
     * Messages which are pre-filled with the fetch profile returned by this method are completely pre-filles with all data that can occur
     * in a listing. Thus these messages can be put into cache for future list requests
     *
     * @return An appropriate instance of {@link FetchProfile} to pre-fill messages ready for being put into cache
     */
    public static FetchProfile getCacheFetchProfile() {
        return CACHE_FETCH_PROFILE;
    }

    /**
     * @return A new instance of {@link FetchProfile} which only prefills messages with their UIDs
     */
    public static FetchProfile getUIDFetchProfile() {
        final FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        return fp;
    }

    /**
     * @return A new instance of {@link FetchProfile} which only prefills messages with their flags
     */
    public static FetchProfile getFlagsFetchProfile() {
        final FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        return fp;
    }

    /**
     * Creates an appropriate collection of {@link MailField} enumeration constants from given fetch profile
     *
     * @param fetchProfile The fetch profile
     * @return An appropriate collection of {@link MailField} enumeration constants
     */
    public static Collection<MailField> fetchProfile2MailListFields(final FetchProfile fetchProfile) {
        final EnumSet<MailField> set = EnumSet.noneOf(MailField.class);
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
            set.add(MailField.HEADERS);
            /*
             * set.add(MailField.FROM); set.add(MailField.TO); set.add(MailField.CC); set.add(MailField.BCC); set.add(MailField.SUBJECT);
             * set.add(MailField.DISPOSITION_NOTIFICATION_TO); set.add(MailField.PRIORITY); set.add(MailField.SENT_DATE);
             */
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
            if (fetchProfile.contains(MessageHeaders.HDR_IMPORTANCE)) {
                set.add(MailField.PRIORITY);
            }
            if (fetchProfile.contains(MessageHeaders.HDR_X_PRIORITY)) {
                set.add(MailField.PRIORITY);
            }
        }
        return set;
    }

    /**
     * Turns given array of <code>long</code> into an array of <code>com.sun.mail.imap.protocol.UIDSet</code> which in turn can be used for
     * a varieties of <code>IMAPProtocol</code> methods.
     *
     * @param uids - the UIDs
     * @return an array of <code>com.sun.mail.imap.protocol.UIDSet</code>
     */
    public static UIDSet[] toUIDSet(long[] uids) {
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
                     * Break in sequence. Need to reexamine this message at the top of the outer loop, so decrement 'i' to cancel the outer
                     * loop's auto-increment
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
     * <b>Note</b> that {@link MailField#BODY} and {@link MailField#FULL} are discarded since no corresponding fetch profile item exists and
     * therefore should be handled separately.
     *
     * @param fields The fields
     * @param preferEnvelope <code>true</code> to prefer ENVELOPE instead of single fetch items; otherwise <code>false</code>
     * @return The appropriate IMAP fetch profile
     */
    public static FetchProfile getFetchProfile(MailField[] fields, boolean preferEnvelope) {
        return getFetchProfile(fields, null, preferEnvelope);
    }

    /**
     * Gets the appropriate fetch profile
     * <p>
     * <b>Note</b> that {@link MailField#BODY} and {@link MailField#FULL} are discarded since no corresponding fetch profile item exists and
     * therefore should be handled separately.
     *
     * @param fields The fields
     * @param sortField The sort field
     * @param preferEnvelope <code>true</code> to prefer ENVELOPE instead of single fetch items; otherwise <code>false</code>
     * @return The appropriate IMAP fetch profile
     */
    public static FetchProfile getFetchProfile(MailField[] fields, MailField sortField, boolean preferEnvelope) {
        return getFetchProfile(fields, null, sortField, preferEnvelope);
    }

    private static final EnumSet<MailField> ENV_FIELDS;

    static {
        final EnumSet<MailField> set = EnumSet.noneOf(MailField.class);
        /*
         * The Envelope is an aggregation of the common attributes of a Message: From, To, Cc, Bcc, ReplyTo, Subject and Date.
         */
        set.add(MailField.FROM);
        set.add(MailField.TO);
        set.add(MailField.CC);
        set.add(MailField.BCC);
        set.add(MailField.SUBJECT);
        set.add(MailField.SENT_DATE);
        /*
         * Discard the two extra fetch profile items contained in JavaMail's ENVELOPE constant: RFC822.SIZE and INTERNALDATE
         */
        // set.add(MailListField.RECEIVED_DATE);
        // set.add(MailListField.SIZE);
        ENV_FIELDS = set;
    }

    /**
     * Checks if given field is covered by ENVELOPE fields.
     *
     * @param field The field to check
     * @return <code>true</code> for ENVELOPE field; otherwise <code>false</code>
     */
    public static boolean isEnvelopeField(MailField field) {
        return ENV_FIELDS.contains(field);
    }

    private static final EnumSet<MailField> ENUM_SET_FULL =
        EnumSet.complementOf(EnumSet.of(MailField.BODY, MailField.FULL, MailField.ACCOUNT_NAME));

    private static final List<HeaderName> ENV_LIST =
        Arrays.asList(HeaderName.valuesOf("From", "To", "Cc", "Bcc", "Subject", "Date", "ReplyTo"));

    /**
     * Gets the appropriate fetch profile
     * <p>
     * <b>Note</b> that {@link MailField#BODY} and {@link MailField#FULL} are discarded since no corresponding fetch profile item exists and
     * therefore should be handled separately.
     *
     * @param fields The fields
     * @param searchFields The search fields
     * @param sortField The sort field
     * @param preferEnvelope <code>true</code> to prefer ENVELOPE instead of single fetch items; otherwise <code>false</code>
     * @return The appropriate IMAP fetch profile
     */
    public static FetchProfile getFetchProfile(MailField[] fields, MailField[] searchFields, MailField sortField, boolean preferEnvelope) {
        return getFetchProfile(fields, null, searchFields, sortField, preferEnvelope);
    }

    /**
     * Gets the appropriate fetch profile
     * <p>
     * <b>Note</b> that {@link MailField#BODY} and {@link MailField#FULL} are discarded since no corresponding fetch profile item exists and
     * therefore should be handled separately.
     *
     * @param fields The fields
     * @param headerNames The header names
     * @param searchFields The search fields
     * @param sortField The sort field
     * @param preferEnvelope <code>true</code> to prefer ENVELOPE instead of single fetch items; otherwise <code>false</code>
     * @return The appropriate IMAP fetch profile
     */
    public static FetchProfile getFetchProfile(MailField[] fields, String[] headerNames, MailField[] searchFields, MailField sortField, boolean preferEnvelope) {
        MailField[] arr;
        {
            List<MailField> list = Arrays.asList(fields);
            EnumSet<MailField> fieldSet = list.isEmpty() ? EnumSet.noneOf(MailField.class) : EnumSet.copyOf(list);
            if (fieldSet.contains(MailField.FULL)) {
                arr = ENUM_SET_FULL.toArray(new MailField[ENUM_SET_FULL.size()]);
            } else {
                arr = fields;
            }
        }
        /*
         * Use a set to avoid duplicate entries
         */
        EnumSet<MailField> set = EnumSet.noneOf(MailField.class);
        if (arr != null) {
            set.addAll(Arrays.asList(arr));
            arr = null;
        }
        if (searchFields != null) {
            set.addAll(Arrays.asList(searchFields));
        }
        if (sortField != null) {
            set.add(sortField);
        }
        /*
         * Set of header names
         */
        Set<HeaderName> names = null == headerNames ? Collections.<HeaderName>emptySet() : new HashSet<HeaderName>(Arrays.asList(HeaderName.valuesOf(headerNames)));
        /*
         * Check which fields are contained in fetch profile item "ENVELOPE"
         */
        FetchProfile fetchProfile = new FetchProfile();
        if (preferEnvelope && set.removeAll(ENV_FIELDS)) {
            /*
             * Add ENVELOPE since set of fields has changed
             */
            fetchProfile.add(FetchProfile.Item.ENVELOPE);
            /*
             * Remove header names covered by ENVELOPE
             */
            names.removeAll(ENV_LIST);
        }
        /*
         * Proceed
         */
        if (!set.isEmpty()) {
            /*
             * Check set against header names
             */
            if (set.contains(MailField.FROM)) {
                names.remove(HeaderName.valueOf("From"));
            }
            if (set.contains(MailField.TO)) {
                names.remove(HeaderName.valueOf("To"));
            }
            if (set.contains(MailField.CC)) {
                names.remove(HeaderName.valueOf("Cc"));
            }
            if (set.contains(MailField.BCC)) {
                names.remove(HeaderName.valueOf("Bcc"));
            }
            if (set.contains(MailField.SUBJECT)) {
                names.remove(HeaderName.valueOf("Subject"));
            }
            if (set.contains(MailField.SENT_DATE)) {
                names.remove(HeaderName.valueOf("Date"));
            }
            if (set.contains(MailField.DISPOSITION_NOTIFICATION_TO)) {
                names.remove(HeaderName.valueOf("Disposition-Notification-To"));
            }
            if (set.contains(MailField.PRIORITY)) {
                names.remove(HeaderName.valueOf("X-Priority"));
                names.remove(HeaderName.valueOf("Importance"));
            }
            /*
             * Iterate fields
             */
            for (MailField mailField : set) {
                addFetchItem(fetchProfile, mailField);
            }
            /*
             * Iterate header names
             */
            for (HeaderName headerName : names) {
                fetchProfile.add(headerName.toString());
            }
        }
        return fetchProfile;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * A fetch item to extend a fetch profile.
     */
    public static final class FetchItem extends FetchProfile.Item {

        /**
         * Initializes a new {@link FetchItem}.
         *
         * @param name The name
         */
        public FetchItem(String name) {
            super(name);
        }

    }

    /**
     * This is the original mailbox item.
     */
    public static final FetchProfile.Item ORIGINAL_MAILBOX = new FetchItem("ORIGINAL-MAILBOX");

    /**
     * This is the original UID item.
     */
    public static final FetchProfile.Item ORIGINAL_UID = new FetchItem("ORIGINAL-UID");

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static final EnumMap<MailField, FetchProfile.Item> FIELD2ITEM;
    private static final EnumMap<MailField, List<String>> FIELD2STRING;

    static {
        /*
         * Item map
         */
        final EnumMap<MailField, FetchProfile.Item> field2item = new EnumMap<MailField, FetchProfile.Item>(MailField.class);
        field2item.put(MailField.HEADERS, IMAPFolder.FetchProfileItem.HEADERS);
        field2item.put(MailField.ID, UIDFolder.FetchProfileItem.UID);
        field2item.put(MailField.CONTENT_TYPE, FetchProfile.Item.CONTENT_INFO);
        field2item.put(MailField.MIME_TYPE, FetchProfile.Item.CONTENT_INFO);
        field2item.put(MailField.SIZE, FetchProfile.Item.SIZE);
        field2item.put(MailField.FLAGS, FetchProfile.Item.FLAGS);
        field2item.put(MailField.COLOR_LABEL, FetchProfile.Item.FLAGS);
        field2item.put(MailField.ORIGINAL_ID, ORIGINAL_MAILBOX);
        field2item.put(MailField.ORIGINAL_FOLDER_ID, ORIGINAL_UID);
        FIELD2ITEM = field2item;
        /*
         * String map
         */
        final EnumMap<MailField, List<String>> field2string = new EnumMap<MailField, List<String>>(MailField.class);
        field2string.put(MailField.FROM, Collections.singletonList(MessageHeaders.HDR_FROM));
        field2string.put(MailField.TO, Collections.singletonList(MessageHeaders.HDR_TO));
        field2string.put(MailField.CC, Collections.singletonList(MessageHeaders.HDR_CC));
        field2string.put(MailField.BCC, Collections.singletonList(MessageHeaders.HDR_BCC));
        field2string.put(MailField.SUBJECT, Collections.singletonList(MessageHeaders.HDR_SUBJECT));
        field2string.put(MailField.SENT_DATE, Collections.singletonList(MessageHeaders.HDR_DATE));
        field2string.put(MailField.DISPOSITION_NOTIFICATION_TO, Collections.singletonList(MessageHeaders.HDR_DISP_NOT_TO));
        field2string.put(MailField.PRIORITY, Arrays.asList(MessageHeaders.HDR_IMPORTANCE, MessageHeaders.HDR_X_PRIORITY));
        FIELD2STRING = field2string;
    }

    /**
     * Adds specified field to given fetch profile.
     *
     * @param fp The fetch profile to add to
     * @param field The field to add
     */
    public static void addFetchItem(final FetchProfile fp, final MailField field) {
        Item item = FIELD2ITEM.get(field);
        if (null != item) {
            fp.add(item);
            return;
        }

        List<String> strings = FIELD2STRING.get(field);
        if (null != strings) {
            for (final String string : strings) {
                fp.add(string);
            }
        }
    }

}
