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

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;


/**
 * {@link ContactDatabaseGetter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactDatabaseGetter extends AbstractContactSwitcherWithDelegate {

    public ContactDatabaseGetter() {
        super();
        final ContactGetter getter = new ContactGetter();
        super.setDelegate(getter);
    }


    /**
     * @see com.openexchange.groupware.contact.helpers.AbstractContactSwitcherWithDelegate#creationdate(java.lang.Object[])
     */
    @Override
    public Object creationdate(Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CreationDate");
        }
        final Contact conObj = (Contact) objects[0];
        final Date value = conObj.getCreationDate();

        return value.getTime();
    }

    /**
     * @see com.openexchange.groupware.contact.helpers.AbstractContactSwitcherWithDelegate#lastmodified(java.lang.Object[])
     */
    @Override
    public Object lastmodified(Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("LastModified");
        }
        final Contact conObj = (Contact) objects[0];
        final Date value = conObj.getLastModified();

        return value.getTime();
    }

}
