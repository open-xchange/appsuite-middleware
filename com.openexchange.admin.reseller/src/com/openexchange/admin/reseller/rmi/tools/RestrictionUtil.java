/**
 * 
 */
package com.openexchange.admin.reseller.rmi.tools;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;


/**
 * @author choeger
 *
 */
public class RestrictionUtil {

    /**
     * Creates a {@link Hashtable} from a {@link HashSet} using the getName() method of {@link Restriction}
     * as key and the {@link Restriction} itself as a value. 
     * @param res
     * @return
     */
    public static Hashtable<String, Restriction> restrictionHashSet2Hashtable(HashSet<Restriction> res) {
        Hashtable<String, Restriction> ret = new Hashtable<String, Restriction>();
        final Iterator<Restriction> i = res.iterator();
        while( i.hasNext() ) {
            final Restriction r = i.next();
            ret.put(r.getName(), r);
        }
        return ret;
    }

}
