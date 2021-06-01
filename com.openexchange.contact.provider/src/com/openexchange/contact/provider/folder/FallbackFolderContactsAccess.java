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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsPermission;
import com.openexchange.contact.common.DefaultContactsFolder;
import com.openexchange.contact.common.DefaultContactsPermission;
import com.openexchange.contact.common.UsedForSync;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;

/**
 * {@link FallbackFolderCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public abstract class FallbackFolderContactsAccess extends ReadOnlyFolderContactsAccess {

    /**
     * Initializes a new {@link FallbackFolderContactsAccess}.
     *
     * @param account The underlying account
     */
    protected FallbackFolderContactsAccess(ContactsAccount account) {
        super(account);
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public List<Contact> getContacts(List<ContactID> contactIds) throws OXException {
        if (null == contactIds || contactIds.isEmpty()) {
            return Collections.emptyList();
        }
        throw ContactsProviderExceptionCodes.CONTACT_NOT_FOUND_IN_FOLDER.create(contactIds.get(0).getFolderID(), contactIds.get(0).getObjectID());
    }

    @Override
    public List<Contact> getContacts(String folderId) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public List<Contact> getModifiedContacts(String folderId, Date from) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public List<Contact> getDeletedContacts(String folderId, Date from) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public boolean supports(String folderId, ContactField... fields) throws OXException {
        return false;
    }

    protected DefaultContactsFolder prepareFallbackFolder(String folderId) {
        DefaultContactsFolder folder = new DefaultContactsFolder();
        folder.setId(folderId);
        folder.setName("Account " + account.getAccountId());
        folder.setUsedForSync(UsedForSync.DEACTIVATED);
        folder.setSubscribed(Boolean.TRUE);
        folder.setLastModified(account.getLastModified());
        folder.setPermissions(Collections.singletonList(new DefaultContactsPermission(
            account.getUserId(),
            ContactsPermission.READ_FOLDER, ContactsPermission.READ_ALL_OBJECTS, ContactsPermission.NO_PERMISSIONS,
            ContactsPermission.NO_PERMISSIONS, false, false, 0)));
        return folder;
    }

}
