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

package com.openexchange.contact.provider.folder;

import java.util.List;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ReadOnlyFolderContactsAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public abstract class ReadOnlyFolderContactsAccess implements FolderContactsAccess {

    protected final ContactsAccount account;

    /**
     * Initializes a new {@link ReadOnlyFolderContactsAccess}.
     *
     * @param account The underlying account
     */
    protected ReadOnlyFolderContactsAccess(ContactsAccount account) {
        super();
        this.account = account;
    }

    @Override
    public void createContact(String folderId, Contact contact) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public void updateContact(ContactID contactId, Contact contact, long clientTimestamp) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public void deleteContacts(List<ContactID> contactsIds, long clientTimestamp) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public String createFolder(ContactsFolder folder) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public String updateFolder(String folderId, ContactsFolder folder, long clientTimestamp) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public boolean supports(String folderId, ContactField... fields) throws OXException {
        return false;
    }

    protected OXException unsupportedOperation() {
        return ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

}
