package com.openexchange.admin.reseller.rmi;

import java.util.HashSet;
import java.util.Map;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;


public class OXResellerTools {

    public interface ClosureInterface {
        boolean checkAgainstCorrespondingRestrictions(final String string);
    }

    public static void checkRestrictions(final HashSet<Restriction> restrictions, final Map<String, Restriction> validRestrictions, String name, final ClosureInterface interf) throws InvalidDataException {
        // The duplicate check is not needed any more because the HashSet prevents duplicates through the equals method
        // of the restriction object which only deals with the name
        for (final Restriction r :  restrictions) {
            final String rname = r.getName();
            final String rval = r.getValue();
            if (null == rname) {
                throw new InvalidDataException("Restriction name must be set");
            }
            if (interf.checkAgainstCorrespondingRestrictions(rname)) {
                throw new InvalidDataException("Restriction " + rname + " cannot be applied to " + name);
            }
            if (null == rval) {
                throw new InvalidDataException("Restriction value must be set");
            }
            final Restriction restriction = validRestrictions.get(rname);
            if (null == restriction) {
                throw new InvalidDataException("No restriction named " + rname + " found in database");
            } else {
                r.setId(restriction.getId());
            }
        }
    }

}
