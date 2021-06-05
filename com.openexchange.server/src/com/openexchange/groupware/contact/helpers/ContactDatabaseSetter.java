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

import static com.openexchange.java.Autoboxing.i;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;


/**
 * {@link ContactDatabaseSetter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactDatabaseSetter extends AbstractContactSwitcherWithDelegate {

    public ContactDatabaseSetter() {
        super();
        final ContactSwitcher setter = new ContactSetter();
        super.setDelegate(setter);
    }

    /**
     * @see com.openexchange.groupware.contact.helpers.AbstractContactSwitcherWithDelegate#markasdistributionlist(java.lang.Object[])
     */
    @Override
    public Object markasdistributionlist(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("MarkAsDistributionList");
        }
        final Contact conObj = (Contact) objects[0];
        if (objects[1] == null) {
            return conObj;
        }


        final boolean value = castIntegerToBoolean(objects[1]);
        conObj.setMarkAsDistributionlist(value);
        return conObj;
    }

    /**
     * @see com.openexchange.groupware.contact.helpers.AbstractContactSwitcherWithDelegate#privateflag(java.lang.Object[])
     */
    @Override
    public Object privateflag(Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("PrivateFlag");
        }
        final Contact conObj = (Contact) objects[0];
        if (objects[1] == null) {
            return conObj;
        }

        final boolean value = castIntegerToBoolean(objects[1]);
        conObj.setPrivateFlag(value);
        return conObj;
    }

    @Override
    public Object creationdate(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CreationDate");
        }
        final Contact conObj = (Contact) objects[0];
        if (objects[1] == null) {
            return conObj;
        }

        final long timestamp = (Long) objects[1];
        final Date value = new Date(timestamp);
        conObj.setCreationDate(value);
        return conObj;
    }

    @Override
    public Object lastmodified(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("LastModified");
        }
        final Contact conObj = (Contact) objects[0];
        if (objects[1] == null) {
            return conObj;
        }

        final long timestamp = (Long) objects[1];
        final Date value = new Date(timestamp);
        conObj.setLastModified(value);
        return conObj;
    }

    private boolean castIntegerToBoolean(Object object) throws OXException {
        if (object instanceof Integer) {
            final int value = i((Integer) object);
            final boolean boolValue;
            if (value == 0) {
                boolValue = false;
            } else {
                boolValue = true;
            }

            return boolValue;
        }

       throw ContactExceptionCodes.UNEXPECTED_ERROR.create("Could not cast to boolean. Object was not an integer.");
    }
}
