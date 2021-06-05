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

package com.openexchange.contact.provider.basic;

import java.util.Collections;
import java.util.List;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link FallbackBasicContactsAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public abstract class FallbackBasicContactsAccess implements BasicContactsAccess {

    protected final ContactsAccount account;

    /**
     * Initializes a new {@link FallbackBasicContactsAccess}.
     *
     * @param account The underlying account
     */
    protected FallbackBasicContactsAccess(ContactsAccount account) {
        super();
        this.account = account;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public List<Contact> getContacts(List<String> contactIds) throws OXException {
        if (null == contactIds || contactIds.isEmpty()) {
            return Collections.emptyList();
        }
        throw ContactsProviderExceptionCodes.CONTACT_NOT_FOUND_IN_FOLDER.create(BasicContactsAccess.FOLDER_ID, contactIds.get(0));
    }

    @Override
    public List<Contact> getContacts() throws OXException {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "FallbackBasicContactsAccess [account=" + account + "]";
    }

}
