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

    ;

    private final int field;
    private final String key;
    private final MailListField listField;

    private MailSortField(final MailListField listField) {
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
     * </ul>
     *
     * @param sortField The sort field to check
     * @return <code>true</code> if given sort field is one of special flag sort fields; otherwise <code>false</code>
     */
    public static boolean isFlagSortField(MailSortField sortField) {
        return FLAG_SEEN == sortField || FLAG_ANSWERED == sortField || FLAG_FORWARDED == sortField || FLAG_DRAFT == sortField || FLAG_FLAGGED == sortField;
    }

    private static final MailSortField[] EMPTY_FIELDS = new MailSortField[0];

    /**
     * Creates an array of {@link MailSortField} corresponding to given <code>int</code> values
     *
     * @param fields The <code>int</code> values
     * @return The array of {@link MailSortField} corresponding to given <code>int</code> values
     */
    public static final MailSortField[] getFields(final int[] fields) {
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
        for (final MailSortField listField : fields) {
            field2sortfield.put(listField.field, listField);
        }
    }

    /**
     * Determines the corresponding {@link MailSortField} constant to given <code>int</code> value
     *
     * @param field The <code>int</code> value
     * @return The corresponding {@link MailSortField} constant
     */
    public static final MailSortField getField(final int field) {
        return field2sortfield.get(field);
    }
}
