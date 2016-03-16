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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * {@link MailFields} - Container for instances of {@link MailField} providing common set-specific methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFields {

    /** The mail field universe */
    private static final MailField[] VALUES = MailField.values();

    /**
     * Adds the specified fields to given array of {@code MailField} instances.
     *
     * @param fields The {@code MailField} array to add to
     * @param toAdd The fields to add
     * @return The resulting {@code MailField} array;<br>
     *         either newly created if one of given fields was absent or the passed array as-is if nothing needed to be changed
     */
    public static MailField[] addIfAbsent(MailField[] fields, MailField... toAdd) {
        if (null == fields) {
            return null;
        }
        MailFields mailFields = new MailFields(fields);
        boolean changed = false;
        if (null != toAdd) {
            for (MailField mailField : toAdd) {
                if (mailField != null && !mailFields.arr[mailField.ordinal()]) {
                    mailFields.arr[mailField.ordinal()] = true;
                    changed = true;
                }
            }
        }
        return changed ? mailFields.toArray() : fields;
    }

    /**
     * Generates an array for specified {@code MailField} constants.
     *
     * @param fields The {@code MailField} constants
     * @return The resulting {@code MailField} array
     */
    public static MailField[] toArray(MailField... fields) {
        if (null == fields) {
            return null;
        }
        return new MailFields(fields).toArray();
    }

    /**
     * Gets the mail fields for given columns.
     *
     * @param columns The columns
     * @return The mail fields
     */
    public static MailFields valueOf(int[] columns) {
        return null == columns ? new MailFields() : new MailFields(MailField.getFields(columns));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------ //

    private final boolean[] arr;

    /**
     * Initializes an empty instance of {@link MailFields}
     */
    public MailFields() {
        super();
        arr = new boolean[VALUES.length];
        Arrays.fill(arr, false);
    }

    /**
     * Initializes an instance of {@link MailFields} with specified flag.
     */
    public MailFields(boolean initValue) {
        super();
        arr = new boolean[VALUES.length];
        Arrays.fill(arr, initValue);
    }

    /**
     * Initializes a new instance of {@link MailFields} pre-filled with specified array of {@link MailField} constants.
     *
     * @param mailField The mail field to add
     * @param mailFields Further mail fields to add
     */
    public MailFields(MailField mailField, MailField... mailFields) {
        this();
        if (null != mailField) {
            arr[mailField.ordinal()] = true;
        }
        if (null != mailFields) {
            for (MailField mf : mailFields) {
                if (null != mf) {
                    arr[mf.ordinal()] = true;
                }
            }
        }
    }

    /**
     * Initializes a new instance of {@link MailFields} pre-filled with specified array of {@link MailField} constants.
     *
     * @param mailFields The mail fields to add
     */
    public MailFields(MailField[] mailFields) {
        this();
        if (null != mailFields) {
            for (MailField mailField : mailFields) {
                if (null != mailField) {
                    arr[mailField.ordinal()] = true;
                }
            }
        }
    }

    /**
     * Initializes a new instance of {@link MailFields} pre-filled with specified collection of {@link MailField} constants.
     *
     * @param mailFields The collection of mail fields to add
     */
    public MailFields(Collection<MailField> mailFields) {
        this();
        if (null != mailFields) {
            for (MailField mailField : mailFields) {
                if (null != mailField) {
                    arr[mailField.ordinal()] = true;
                }
            }
        }
    }

    /**
     * Copy constructor: Initializes a new {@link MailFields} from specified mail fields.
     *
     * @param mailFields The mail fields
     */
    public MailFields(MailFields mailFields) {
        super();
        arr = new boolean[VALUES.length];
        if (null != mailFields) {
            System.arraycopy(mailFields.arr, 0, arr, 0, arr.length);
        }
    }

    /**
     * Gets the byte array representation.
     *
     * @return The byte array representation
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[arr.length];
        for (int i = bytes.length; i-- > 0;) {
            bytes[i] = arr[i] ? (byte) 1 : 0;
        }
        return bytes;
    }

    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }

    /**
     * Gets the size.
     *
     * @return The size
     */
    public int size() {
        int size = 0;
        for (int i = arr.length; i-- > 0;) {
            if (arr[i]) {
                size++;
            }
        }
        return size;
    }

    /**
     * Adds specified {@link MailField} constant.
     *
     * @param mailField The mail field to add
     * @return This instance with mail field added
     */
    public MailFields add(MailField mailField) {
        if (mailField != null) {
            arr[mailField.ordinal()] = true;
        }
        return this;
    }

    /**
     * Adds specified {@link MailField} constants.
     *
     * @param mailFields The mail fields to add
     */
    public void addAll(MailField[] mailFields) {
        if (mailFields != null) {
            for (MailField mailField : mailFields) {
                arr[mailField.ordinal()] = true;
            }
        }
    }

    /**
     * Adds specified collection of {@link MailField} constants.
     *
     * @param mailFields The collection of {@link MailField} constants to add
     */
    public void addAll(Collection<MailField> mailFields) {
        if (mailFields != null) {
            for (MailField mailField : mailFields) {
                arr[mailField.ordinal()] = true;
            }
        }
    }

    /**
     * Removes specified {@link MailField} constant.
     *
     * @param mailField The mail field to remove
     * @return <code>true</code> if specified {@link MailField} constant was contained; otherwise <code>false</code>
     */
    public boolean removeMailField(MailField mailField) {
        if (mailField == null) {
            return false;
        }
        int ordinal = mailField.ordinal();
        if (arr[ordinal]) {
            arr[ordinal] = false;
            return true;
        }
        return false;
    }

    /**
     * Removes specified {@link MailField} constants.
     *
     * @param mailFields The mail fields to remove
     */
    public void removeMailFields(MailField[] mailFields) {
        if (mailFields != null) {
            for (MailField mailField : mailFields) {
                arr[mailField.ordinal()] = false;
            }
        }
    }

    /**
     * Checks if specified {@link MailField} constant is contained.
     *
     * @param mailField The mail field to check
     * @return <code>true</code> if specified {@link MailField} constant is contained; otherwise <code>false</code>.
     */
    public boolean contains(MailField mailField) {
        return null != mailField &&  arr[mailField.ordinal()];
    }

    /**
     * Checks if any of specified mail field constants is contained.
     *
     * @param mailFields The mail fields to check
     * @return <code>true</code> if any of specified mail field constants is contained; otherwise <code>false</code>.
     */
    public boolean containsAny(MailFields mailFields) {
        if (mailFields == null) {
            return false;
        }
        boolean[] otherArr = mailFields.arr;
        for (int i = otherArr.length; i-- > 0;) {
            if (otherArr[i] && arr[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all of specified mail field constants is contained.
     *
     * @param mailFields The mail fields to check
     * @return <code>true</code> if all of specified mail field constants is contained; otherwise <code>false</code>.
     */
    public boolean containsAll(MailFields mailFields) {
        if (mailFields == null) {
            return false;
        }
        boolean[] otherArr = mailFields.arr;
        for (int i = otherArr.length; i-- > 0;) {
            if (otherArr[i] && !arr[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes from this instance all of its fields that are contained in the specified mail fields.
     *
     * @param otherFields The mail fields which will be removed from this instance
     * @return <code>true</code> if this instance changed as a result of the call; otherwise <code>false</code>
     */
    public boolean removeAll(MailFields otherFields) {
        if (otherFields == null) {
            return false;
        }
        boolean[] otherArr = otherFields.arr;
        boolean retval = false;
        for (int i = otherArr.length; i-- > 0;) {
            if (otherArr[i] && arr[i]) {
                arr[i] = false;
                retval = true;
            }
        }
        return retval;
    }

    /**
     * Retains only the fields in this instance that are contained in the specified mail fields.
     *
     * @param otherFields The mail fields which this instance will retain
     * @return <code>true</code> if this instance changed as a result of the call; otherwise <code>false</code>
     */
    public boolean retainAll(MailFields otherFields) {
        if (otherFields == null) {
            return false;
        }
        boolean[] otherArr = otherFields.arr;
        boolean retval = false;
        for (int i = otherArr.length; i-- > 0;) {
            if (!otherArr[i] && arr[i]) {
                arr[i] = false;
                retval = true;
            }
        }
        return retval;
    }

    /**
     * Checks if this instance contains no fields.
     *
     * @return <code>true</code> if this instance contains no fields; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        boolean retval = true;
        for (int i = arr.length; retval && i-- > 0;) {
            retval = !arr[i];
        }
        return retval;
    }

    /**
     * Returns a newly created array of {@link MailField} constants
     *
     * @return A newly created array of {@link MailField} constants
     */
    public MailField[] toArray() {
        List<MailField> l = new ArrayList<MailField>(arr.length);
        for (int i = arr.length; i-- > 0;) {
            if (arr[i]) {
                l.add(VALUES[i]);
            }
        }
        return l.toArray(new MailField[l.size()]);
    }

    /**
     * Returns a newly created {@link Set set} of {@link MailField} constants.
     *
     * @return A newly created {@link Set set} of {@link MailField} constants
     */
    public Set<MailField> toSet() {
        EnumSet<MailField> set = EnumSet.noneOf(MailField.class);
        for (int i = arr.length; i-- > 0;) {
            if (arr[i]) {
                set.add(VALUES[i]);
            }
        }
        return set;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(arr);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MailFields)) {
            return false;
        }
        MailFields other = (MailFields) obj;
        if (!Arrays.equals(arr, other.arr)) {
            return false;
        }
        return true;
    }

}
