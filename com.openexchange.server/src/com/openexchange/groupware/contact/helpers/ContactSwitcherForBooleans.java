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

package com.openexchange.groupware.contact.helpers;

import com.openexchange.exception.OXException;

/**
 * This switcher can translate all kinds of objects given to a boolean value.
 *
 * This is necessary for CSV files - mostly for the PRIVATE flag, which
 * may be -depending on the Outlook version- either "true", "yes",
 * "private" or whatever... so, rather than calling it "BooleanFinder",
 * one might call it "TruthSeeks", because it finds out whether a certain
 * value might be translated to <code>true</code>, considering everything
 * else <code>false</code>.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ContactSwitcherForBooleans extends AbstractContactSwitcherWithDelegate {

    public String[] trueValues = { "y", "yes", "true", "privat", "private", "priv\u00e9", "1" };

    public Object[] determineBooleanValue(final Object[] objects) {
        final Object obj = objects[1];

        Boolean boolValue = Boolean.FALSE;
        if (obj instanceof String) {
            //check strings
            String comp = (String) obj;
            for (final String trueVal : trueValues) {
                if (trueVal.equals(comp.toLowerCase())) {
                    boolValue = Boolean.TRUE;
                }
            }
        } else if (obj instanceof Boolean) {
            //check boolean object
            boolValue = (Boolean) obj;
        } else if (obj instanceof Integer) {
            // check Integer object
            final Integer comp = (Integer) obj;
            if (comp.compareTo(Integer.valueOf(0)) > 0) {
                boolValue = Boolean.TRUE;
            }
        }

        objects[1] = boolValue;
        return objects;

    }

    @Override
    public Object privateflag(final Object... objects) throws OXException {
        return delegate.privateflag(determineBooleanValue(objects));
    }

    @Override
    public Object markasdistributionlist(Object[] objects) throws OXException {
        return delegate.markasdistributionlist(determineBooleanValue(objects));
    }

}
