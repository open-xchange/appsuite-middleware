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
        if( val.toLowerCase().equals("true") ) {
            return true;
        } else {
            return false;
        }
    }

    public static HashSet<Restriction> array2HashSet(final Restriction[] rarr) {
        if( rarr != null ) {
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
            if( rname.equals(Restriction.SUBADMIN_CAN_CREATE_SUBADMINS) ) {
                if( ! (rval.equals("true") || rval.equals("false")) ) {
                    throw new InvalidDataException(rval + " is not a valid value for " + Restriction.SUBADMIN_CAN_CREATE_SUBADMINS);
                }
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
