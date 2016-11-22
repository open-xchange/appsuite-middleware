package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;


/**
 * Unit tests for DBWeightComparator
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.2
 */
public class DBWeightComparatorTest {

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
