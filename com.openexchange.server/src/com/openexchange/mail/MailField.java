/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.openexchange.mail.search.SearchTerm;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link MailField} - An enumeration of mail fields to define which fields to pre-fill.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailField {

    /**
     * The implementation-specific unique mail ID<br>
     * <b>[low cost]</b>
     */
    ID(MailListField.ID),
    /**
     * The folder ID or full name<br>
     * <b>[low cost]</b>
     */
    FOLDER_ID(MailListField.FOLDER_ID),
    /**
     * Whether message contains attachments or not<br>
     */
    ATTACHMENT(MailListField.ATTACHMENT),
    /**
     * The Content-Type
     * <b>[low cost]</b>
     */
    CONTENT_TYPE(MailListField.MIME_TYPE),
    /**
     * The MIME type<br>
     * <b>[low cost]</b>
     */
    MIME_TYPE(MailListField.MIME_TYPE),
    /**
     * From<br>
     * <b>[low cost]</b>
     */
    FROM(MailListField.FROM),
    /**
     * To<br>
     * <b>[low cost]</b>
     */
    TO(MailListField.TO),
    /**
     * Cc<br>
     * <b>[low cost]</b>
     */
    CC(MailListField.CC),
    /**
     * Bcc<br>
     * <b>[low cost]</b>
     */
    BCC(MailListField.BCC),
    /**
     * Subject<br>
     * <b>[low cost]</b>
     */
    SUBJECT(MailListField.SUBJECT),
    /**
     * Size<br>
     * <b>[low cost]</b>
     */
    SIZE(MailListField.SIZE),
    /**
     * Sent date corresponds to <code>Date</code> header<br>
     * <b>[low cost]</b>
     */
    SENT_DATE(MailListField.SENT_DATE),
    /**
     * Received date represent the internal mail server's timestamp on arrival<br>
     * <b>[low cost]</b>
     */
    RECEIVED_DATE(MailListField.RECEIVED_DATE),
    /**
     * Flags<br>
     * <b>[low cost]</b>
     */
    FLAGS(MailListField.FLAGS),
    /**
     * Thread level<br>
     * <b>[low cost]</b>
     */
    THREAD_LEVEL(MailListField.THREAD_LEVEL),
    /**
     * Email address in <code>Disposition-Notification-To</code> header<br>
     * <b>[low cost]</b>
     */
    DISPOSITION_NOTIFICATION_TO(MailListField.DISPOSITION_NOTIFICATION_TO),
    /**
     * Integer value of <code>X-Priority</code> header<br>
     * <b>[low cost]</b>
     */
    PRIORITY(MailListField.PRIORITY),
    /**
     * Color Label<br>
     * <b>[low cost]</b>
     */
    COLOR_LABEL(MailListField.COLOR_LABEL),
    /**
     * Account name<br>
     * <b>[low cost]</b>
     */
    ACCOUNT_NAME(MailListField.ACCOUNT_NAME),
    /**
     * To peek the mail body (\Seen flag is left unchanged)<br>
     * <b>[high cost]</b>
     */
    BODY(null),
    /**
     * To fetch all message headers<br>
     * <b>[high cost]</b>
     */
    HEADERS(null),
    /**
     * To fully pre-fill mail incl. headers and peeked body (\Seen flag is left unchanged)<br>
     * <b>[high cost]</b>
     */
    FULL(null),
    /**
     * Special field to signal support for continuation.
     */
    SUPPORTS_CONTINUATION(null),
    /**
     * The original mail ID.
     *
     * @since v7.8.0
     */
    ORIGINAL_ID(MailListField.ORIGINAL_ID),
    /**
     * The original folder ID
     *
     * @since v7.8.0
     */
    ORIGINAL_FOLDER_ID(MailListField.ORIGINAL_FOLDER_ID),
    /**
     * The attachment name.
     *
     * @since v7.8.2
     */
    ATTACHMENT_NAME(null),
    /**
     * The message's text preview only if immediately available
     *
     * @since v7.10.0
     */
    TEXT_PREVIEW_IF_AVAILABLE(MailListField.TEXT_PREVIEW_IF_AVAILABLE),
    /**
     * The message's text preview; generate it if absent (may be slow)
     *
     * @since v7.10.0
     */
    TEXT_PREVIEW(MailListField.TEXT_PREVIEW),
    /**
     * The message's authentication overall result (light version); maps to <code>"Authentication-Results"</code> header
     *
     * @since v7.10.0
     */
    AUTHENTICATION_OVERALL_RESULT(MailListField.AUTHENTICATION_OVERALL_RESULT),
    /**
     * The message's authentication mechanism results (heavy version); maps to <code>"Authentication-Results"</code> header
     *
     * @since v7.10.0
     */
    AUTHENTICATION_MECHANISM_RESULTS(MailListField.AUTHENTICATION_MECHANISM_RESULTS),
    ;

    private static final EnumMap<MailListField, MailField> LIST_FIELDS_MAP = new EnumMap<MailListField, MailField>(MailListField.class);

    private static final TIntObjectMap<MailField> FIELDS_MAP = new TIntObjectHashMap<MailField>(25);

    static {
        final MailField[] fields = MailField.values();
        for (MailField mailField : fields) {
            final MailListField listField = mailField.getListField();
            if (listField != null) {
                LIST_FIELDS_MAP.put(listField, mailField);
                FIELDS_MAP.put(listField.getField(), mailField);
            }
        }
        LIST_FIELDS_MAP.put(MailListField.FLAG_SEEN, MailField.FLAGS);
        LIST_FIELDS_MAP.put(MailListField.FLAG_ANSWERED, MailField.FLAGS);
        LIST_FIELDS_MAP.put(MailListField.FLAG_DRAFT, MailField.FLAGS);
        LIST_FIELDS_MAP.put(MailListField.FLAG_FLAGGED, MailField.FLAGS);
    }

    /**
     * All low cost fields.
     * <p>
     * ID, FOLDER_ID, CONTENT_TYPE, FROM, TO, CC, BCC, SUBJECT, SIZE, SENT_DATE, RECEIVED_DATE, FLAGS, THREAD_LEVEL,
     * DISPOSITION_NOTIFICATION_TO, PRIORITY, COLOR_LABEL
     */
    public static final MailField[] FIELDS_LOW_COST = { ID, FOLDER_ID, ATTACHMENT, CONTENT_TYPE, FROM, TO, CC, BCC, SUBJECT, SIZE, SENT_DATE, RECEIVED_DATE, FLAGS, THREAD_LEVEL, DISPOSITION_NOTIFICATION_TO, PRIORITY, COLOR_LABEL };

    /**
     * All fields except {@link #BODY} and {@link #FULL}.
     * <p>
     * ID, FOLDER_ID, CONTENT_TYPE, FROM, TO, CC, BCC, SUBJECT, SIZE, SENT_DATE, RECEIVED_DATE, FLAGS, THREAD_LEVEL,
     * DISPOSITION_NOTIFICATION_TO, PRIORITY, COLOR_LABEL, HEADERS
     */
    public static final MailField[] FIELDS_WO_BODY = { ID, FOLDER_ID, ATTACHMENT, CONTENT_TYPE, FROM, TO, CC, BCC, SUBJECT, SIZE, SENT_DATE, RECEIVED_DATE, FLAGS, THREAD_LEVEL, DISPOSITION_NOTIFICATION_TO, PRIORITY, COLOR_LABEL, HEADERS };

    private final MailListField listField;

    private MailField(MailListField listField) {
        this.listField = listField;

    }

    /**
     * Gets the corresponding instance of {@link MailListField} or <code>null</code> if none exists.
     *
     * @return The corresponding instance of {@link MailListField} or <code>null</code> if none exists.
     */
    public MailListField getListField() {
        return listField;
    }

    /**
     * Gets the corresponding instances of {@link MailListField} for specified instances of {@link MailField}.
     * <p>
     * Those mail fields which have no corresponding list field are omitted.
     *
     * @param fields The instances of {@link MailField}
     * @return The corresponding instances of {@link MailListField}
     */
    public static final MailListField[] toListFields(MailField[] fields) {
        if (null == fields) {
            return null;
        }
        final List<MailListField> listFields = new ArrayList<MailListField>(fields.length);
        for (MailField mailField : fields) {
            if (null != mailField.getListField()) {
                listFields.add(mailField.getListField());
            }
        }
        return listFields.toArray(new MailListField[listFields.size()]);
    }

    /**
     * Gets the corresponding instances of {@link MailListField} for specified collection of {@link MailField}.
     * <p>
     * Those mail fields which have no corresponding list field are omitted.
     *
     * @param fields The collection of {@link MailField}
     * @return The corresponding instances of {@link MailListField}
     */
    public static final MailListField[] toListFields(Collection<MailField> fields) {
        return null == fields ? null : toListFields(fields.toArray(new MailField[fields.size()]));
    }

    /**
     * Gets the corresponding instances of {@link MailField} for specified instances of {@link MailListField}.
     *
     * @param listFields The instances of {@link MailListField}
     * @return The corresponding instances of {@link MailField}
     */
    public static final MailField[] toFields(MailListField[] listFields) {
        if (null == listFields) {
            return null;
        }
        final MailField[] fields = new MailField[listFields.length];
        for (int i = 0; i < listFields.length; i++) {
            fields[i] = toField(listFields[i]);
        }
        return fields;
    }

    /**
     * Gets the corresponding instance of {@link MailField} for specified instance of {@link MailListField}.
     *
     * @param listField The instance of {@link MailListField}
     * @return The corresponding instance of {@link MailField}
     */
    public static final MailField toField(MailListField listField) {
        return null == listField ? null : LIST_FIELDS_MAP.get(listField);
    }

    private static final MailField[] EMPTY_FIELDS = new MailField[0];

    /**
     * Creates an array of {@link MailField} corresponding to given <code>int</code> values.
     * <p>
     * This is just a convenience method that invokes {@link #getField(int)} for every <code>int</code> value.
     *
     * @see #getField(int)
     * @param fields The <code>int</code> values
     * @return The array of {@link MailField} corresponding to given <code>int</code> values
     */
    public static final MailField[] getFields(int[] fields) {
        if ((fields == null) || (fields.length == 0)) {
            return EMPTY_FIELDS;
        }
        final MailField[] retval = new MailField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            retval[i] = getField(fields[i]);
        }
        return retval;
    }

    /**
     * Creates an array of {@link MailField} corresponding to given <code>int</code> values.
     * <p>
     * Guarantees not to return any <code>null</code> values.
     *
     * @see #getField(int)
     * @param fields The <code>int</code> values
     * @return The array of {@link MailField} containing all fields that match a given <code>int</code> value.
     */
    public static final MailField[] getMatchingFields(int[] fields) {
        if ((fields == null) || (fields.length == 0)) {
            return EMPTY_FIELDS;
        }

        List<MailField> rawFields = new ArrayList<MailField>(fields.length);
        for (int i = 0; i < fields.length; i++) {
            MailField field = getField(fields[i]);
            if (field != null) {
                rawFields.add(field);
            }
        }

        return rawFields.toArray(new MailField[rawFields.size()]);
    }

    /**
     * Maps specified <code>int</code> value to a mail field. A negative <code>int</code> value is mapped to {@link MailField#BODY}.
     * <p>
     * Mail fields which do not hold a corresponding list field are not mappable to an <code>int</code> value; in consequence they are
     * ignored
     *
     * @param field The <code>int</code> value
     * @return The mapped {@link MailField} or <code>null</code> if no corresponding mail field could be found
     */
    public static MailField getField(int field) {
        return field < 0 ? MailField.BODY : FIELDS_MAP.get(field);
    }

    /**
     * Gets the mail fields addressed by given search term
     *
     * @param searchTerm The search term
     * @return The addressed mail fields
     */
    public static Set<MailField> getMailFieldsFromSearchTerm(SearchTerm<?> searchTerm) {
        final EnumSet<MailField> set = EnumSet.noneOf(MailField.class);
        searchTerm.addMailField(set);
        return set;
    }

    /**
     * Checks if specified fields contains one of the given fields
     *
     * @param mailFields The fields
     * @param fieldToCheck The field to check for
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public static boolean contains(MailField[] mailFields, MailField fieldToCheck) {
        if (null == mailFields || fieldToCheck == null) {
            return false;
        }

        for (MailField mailField : mailFields) {
            if (mailField == fieldToCheck) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if specified fields contains one of the given fields
     *
     * @param mailFields The fields
     * @param fieldsToCheck The fields to check for
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public static boolean contains(MailField[] mailFields, MailField... fieldsToCheck) {
        if (null == mailFields || fieldsToCheck == null || fieldsToCheck.length == 0) {
            return false;
        }

        for (MailField mailField : mailFields) {
            for (MailField fieldToCheck : fieldsToCheck) {
                if (mailField == fieldToCheck) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds specified fields to given fields if not yet contained
     *
     * @param mailFields The fields to add to
     * @param fieldsToAdd The fields to add
     * @return The resulting fields extended by missing fields
     */
    public static MailField[] add(MailField[] mailFields, MailField... fieldsToAdd) {
        if (null == mailFields || fieldsToAdd == null || fieldsToAdd.length == 0) {
            return mailFields;
        }

        List<MailField> toAdd = null;
        int numOfFields = mailFields.length;
        for (MailField fieldToAdd : fieldsToAdd) {
            boolean add = true;
            for (int i = numOfFields; add && i-- > 0;) {
                MailField mailField = mailFields[i];
                if (mailField == fieldToAdd) {
                    // Already contained
                    add = false;
                }
            }
            if (add) {
                if (toAdd == null) {
                    toAdd = new ArrayList<>(fieldsToAdd.length);
                }
                toAdd.add(fieldToAdd);
            }
        }

        if (toAdd == null) {
            return mailFields;
        }

        int numOfFieldsToAdd = toAdd.size();
        MailField[] newFields = new MailField[numOfFields + numOfFieldsToAdd];
        System.arraycopy(mailFields, 0, newFields, 0, numOfFields);
        for (MailField addMe : toAdd) {
            newFields[numOfFields++] = addMe;
        }
        return newFields;
    }

}
