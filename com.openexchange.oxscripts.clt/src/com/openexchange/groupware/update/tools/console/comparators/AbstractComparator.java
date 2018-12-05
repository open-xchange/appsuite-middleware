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

package com.openexchange.groupware.update.tools.console.comparators;

import java.util.Comparator;
import java.util.List;

/**
 * {@link AbstractComparator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
abstract class AbstractComparator implements Comparator<List<Object>> {

    private final Class<?> clazz;
    private final int indexPosition;

    /**
     * Initialises a new {@link AbstractComparator}.
     */
    public AbstractComparator(Class<?> clazz, int indexPosition) {
        super();
        this.clazz = clazz;
        this.indexPosition = indexPosition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(List<Object> o1, List<Object> o2) {
        Object object1 = o1.get(indexPosition);
        Object object2 = o2.get(indexPosition);
        if (null == object1) {
            return null == object2 ? 0 : -1;
        }
        if (null == object2) {
            return 1;
        }
        if (false == o1.getClass().isAssignableFrom(clazz)) {
            return false == o2.getClass().isAssignableFrom(clazz) ? 0 : -1;
        }
        if (false == o2.getClass().isAssignableFrom(clazz)) {
            return 1;
        }
        return innerCompare(object1, object2);
    }

    /**
     * Compares the two objects
     * 
     * @param o1 The first object
     * @param o2 The second object
     * @return return one of -1, 0, or 1 according to whether
     *         first object is less than, equal to, or greater than the second.
     */
    protected abstract int innerCompare(Object o1, Object o2);
}
