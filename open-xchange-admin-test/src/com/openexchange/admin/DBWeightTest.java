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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.admin;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import static junit.framework.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import com.openexchange.admin.storage.mysqlStorage.DBWeightComparator;
import com.openexchange.admin.storage.mysqlStorage.DatabaseHandle;

/**
 * {@link DBWeightTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class DBWeightTest {

    public DBWeightTest() {
        super();
    }

    @Test
    public void testLowValues() {
        List<DatabaseHandle> dbList = new ArrayList<DatabaseHandle>();
        dbList.add(db(1, 100, 2, 1000));
        dbList.add(db(2, 100, 1, 1000));
        sort(dbList, reverseOrder(new DBWeightComparator(totalUnits(dbList), totalWeight(dbList))));
        assertEquals("Algorithm did not sort correctly.", 2, i(first(dbList).getId()));
    }

    @Test
    public void testDevPrototyp() {
        List<DatabaseHandle> dbList = new ArrayList<DatabaseHandle>();
        dbList.add(db(1, 100, 67909, 150000));
        dbList.add(db(2, 100, 2044, 150000));
        sort(dbList, reverseOrder(new DBWeightComparator(totalUnits(dbList), totalWeight(dbList))));
        assertEquals("Algorithm did not sort correctly.", 2, i(first(dbList).getId()));
    }

    @Test
    public void testLargeValues() {
        List<DatabaseHandle> dbList = new ArrayList<DatabaseHandle>();
        dbList.add(db(1, 100, 999999999, 1000000000));
        dbList.add(db(2, 100, 999999998, 1000000000));
        sort(dbList, reverseOrder(new DBWeightComparator(totalUnits(dbList), totalWeight(dbList))));
        assertEquals("Algorithm did not sort correctly.", 2, i(first(dbList).getId()));
    }

    @Test
    public void testOneZero() {
        List<DatabaseHandle> dbList = new ArrayList<DatabaseHandle>();
        dbList.add(db(1, 100, 1, 1000));
        dbList.add(db(2, 100, 0, 1000));
        sort(dbList, reverseOrder(new DBWeightComparator(totalUnits(dbList), totalWeight(dbList))));
        assertEquals("Algorithm did not sort correctly.", 2, i(first(dbList).getId()));
    }

    @Test
    public void testOneFull() {
        List<DatabaseHandle> dbList = new ArrayList<DatabaseHandle>();
        dbList.add(db(1, 100, 1000000000, 1000000000));
        dbList.add(db(2, 100, 999999999, 1000000000));
        sort(dbList, reverseOrder(new DBWeightComparator(totalUnits(dbList), totalWeight(dbList))));
        assertEquals("Algorithm did not sort correctly.", 2, i(first(dbList).getId()));
    }

    private DatabaseHandle db(final int id, final int weight, final int currentUnits, final int maxUnits) {
        final DatabaseHandle retval = new DatabaseHandle();
        retval.setId(I(id));
        retval.setClusterWeight(I(weight));
        retval.setCount(currentUnits);
        retval.setMaxUnits(I(maxUnits));
        return retval;
    }

    private int totalUnits(final Collection<DatabaseHandle> list) {
        int retval = 0;
        for (final DatabaseHandle db : list) {
            retval += db.getCount();
        }
        return retval;
    }

    private int totalWeight(final Collection<DatabaseHandle> list) {
        int retval = 0;
        for (final DatabaseHandle db : list) {
            retval += i(db.getClusterWeight());
        }
        return retval;
    }

    private DatabaseHandle first(final Collection<DatabaseHandle> list) {
        Iterator<DatabaseHandle> iter = list.iterator();
        DatabaseHandle retval = null;
        if (iter.hasNext()) {
            retval = iter.next();
        }
        return retval;
    }
}
