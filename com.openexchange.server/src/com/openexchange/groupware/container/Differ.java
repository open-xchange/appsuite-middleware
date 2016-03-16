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

package com.openexchange.groupware.container;

import java.util.Date;

/**
 * {@link Differ}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class Differ<T extends DataObject> {

    /**
     * Calculates the Difference of two Objects. If the Objects do not differ the return value is null.
     *
     * @param original
     * @param update
     * @return
     */
    public abstract Difference getDifference(T original, T update);

    public abstract int getColumn();

    public static boolean isDifferent(DataObject original, DataObject update, int column) {

        if (!update.contains(column)) { // no update
            return false;
        }

        if (!original.contains(column) && update.contains(column)) { // set
            return true;
        }

        // Both set
        Object v1 = unpack(original.get(column));
        Object v2 = unpack(update.get(column));
        if (v1 == v2) { // Same reference, works on most autoboxed primitives.
            return false;
        }

        if (v1 == null) {
            return true;
        }

        if (v2 == null) {
            return true;
        }

        if (v1.equals(v2)) {
            return false;
        }

        return true;
    }

    // Some Objects may need unpacking to ignore certain object features (like timezones in dates, we don't care for those)
    private static Object unpack(Object object) {
        if (Date.class.isInstance(object)) {
            return ((Date)object).getTime();
        }
        return object;
    }

    protected Difference isArrayDifferent(Object[] original, Object[] update) {

        Difference difference = new Difference();

        boolean isDifferent = false;

        for (Object o : original) {
            boolean found = false;
            for (Object u : update) {
                if (o.equals(u)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.getRemoved().add(o);
                isDifferent = true;
            }
        }

        for (Object u : update) {
            boolean found = false;
            for (Object o : original) {
                if (u.equals(o)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.getAdded().add(u);
                isDifferent = true;
            }
        }

        return isDifferent ? difference : null;
    }

}
