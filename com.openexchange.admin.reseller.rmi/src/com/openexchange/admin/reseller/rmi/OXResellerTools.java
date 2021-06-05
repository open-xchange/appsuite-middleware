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
package com.openexchange.admin.reseller.rmi;

import java.util.HashSet;
import java.util.Map;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;


public class OXResellerTools {

    public interface ClosureInterface {
        boolean checkAgainstCorrespondingRestrictions(final String string);
    }

    public static boolean isTrue(final String val) {
        return ( val.toLowerCase().equals("true") );
    }

    public static HashSet<Restriction> array2HashSet(final Restriction[] rarr) {
        if ( rarr != null ) {
            HashSet<Restriction> ret = new HashSet<Restriction>();
            for(final Restriction r : rarr) {
                ret.add(r);
            }
            return ret;
        }
        return null;
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
            if ( rname.equals(Restriction.SUBADMIN_CAN_CREATE_SUBADMINS) ) {
                if ( ! (rval.equals("true") || rval.equals("false")) ) {
                    throw new InvalidDataException(rval + " is not a valid value for " + Restriction.SUBADMIN_CAN_CREATE_SUBADMINS);
                }
            }
            final Restriction restriction = validRestrictions.get(rname);
            if (null == restriction) {
                throw new InvalidDataException("No restriction named " + rname + " found in database");
            }
            r.setId(restriction.getId());
        }
    }

}
