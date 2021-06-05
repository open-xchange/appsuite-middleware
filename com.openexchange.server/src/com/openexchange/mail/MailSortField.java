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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link MailSortField} - An enumeration of sortable mail list fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailSortField {

    /**
     * From
     */
    FROM(MailListField.FROM),
    /**
     * To
     */
    TO(MailListField.TO),
    /**
     * Cc
     */
    CC(MailListField.CC),
    /**
     * Subject
     */
    SUBJECT(MailListField.SUBJECT),
    /**
     * Size
     */
    SIZE(MailListField.SIZE),
    /**
     * Sent date
     */
    SENT_DATE(MailListField.SENT_DATE),
    /**
     * Received date
     */
    RECEIVED_DATE(MailListField.RECEIVED_DATE),
    /**
     * Color Label
     */
    COLOR_LABEL(MailListField.COLOR_LABEL),
    /**
     * Flag \SEEN
     * <p>
     * Rather to be read as "sort by unseen/unread" since seen mails are sorted to the end while unread ones come first.
     */
    FLAG_SEEN(MailListField.FLAG_SEEN),
    /**
     * Account name
     */
    ACCOUNT_NAME(MailListField.ACCOUNT_NAME),
    /**
     * Flag \ANSWERED (657)
     */
    FLAG_ANSWERED(MailListField.FLAG_ANSWERED),
    /**
     * Flag \FORWARDED (658)
     */
    FLAG_FORWARDED(MailListField.FLAG_FORWARDED),
    /**
     * Flag \DRAFT (659)
     */
    FLAG_DRAFT(MailListField.FLAG_DRAFT),
    /**
     * Flag \FLAGGED (660)
     */
    FLAG_FLAGGED(MailListField.FLAG_FLAGGED),
    /**
     * Flag $HasAttachment
     */
    FLAG_HAS_ATTACHMENT(MailListField.ATTACHMENT)

    ;

    private final int field;
    private final String key;
    private final MailListField listField;

    private MailSortField(MailListField listField) {
        field = listField.getField();
        key = listField.getKey();
        this.listField = listField;
    }

    /**
     * @return The <code>int</code> field value
     */
    public int getField() {
        return field;
    }

    /**
     * @return The JSON key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return The corresponding list field
     */
    public MailListField getListField() {
        return listField;
    }

    /**
     * Checks if specified sort field is one of flag-based sort fields:
     * <ul>
     * <li>{@link #FLAG_SEEN}</li>
     * <li>{@link #FLAG_ANSWERED}</li>
     * <li>{@link #FLAG_FORWARDED}</li>
     * <li>{@link #FLAG_DRAFT}</li>
     * <li>{@link #FLAG_FLAGGED}</li>
     * <li>{@link #FLAG_HAS_ATTACHMENT}</li>
     * </ul>
     *
     * @param sortField The sort field to check
     * @return <code>true</code> if given sort field is one of special flag sort fields; otherwise <code>false</code>
     */
    public static boolean isFlagSortField(MailSortField sortField) {
        return FLAG_SEEN == sortField || FLAG_ANSWERED == sortField || FLAG_FORWARDED == sortField || FLAG_DRAFT == sortField || FLAG_FLAGGED == sortField || FLAG_HAS_ATTACHMENT == sortField;
    }

    private static final MailSortField[] EMPTY_FIELDS = new MailSortField[0];

    /**
     * Creates an array of {@link MailSortField} corresponding to given <code>int</code> values
     *
     * @param fields The <code>int</code> values
     * @return The array of {@link MailSortField} corresponding to given <code>int</code> values
     */
    public static final MailSortField[] getFields(int[] fields) {
        if ((fields == null) || (fields.length == 0)) {
            return EMPTY_FIELDS;
        }
        final MailSortField[] retval = new MailSortField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            retval[i] = getField(fields[i]);
        }
        return retval;
    }

    private static final TIntObjectMap<MailSortField> field2sortfield = new TIntObjectHashMap<MailSortField>(25);

    static {
        final MailSortField[] fields = MailSortField.values();
        for (MailSortField listField : fields) {
            field2sortfield.put(listField.field, listField);
        }
    }

    /**
     * Determines the corresponding {@link MailSortField} constant to given <code>int</code> value
     *
     * @param field The <code>int</code> value
     * @return The corresponding {@link MailSortField} constant
     */
    public static final MailSortField getField(int field) {
        return field2sortfield.get(field);
    }
}
