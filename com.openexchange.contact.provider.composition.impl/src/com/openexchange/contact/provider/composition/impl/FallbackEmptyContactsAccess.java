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

package com.openexchange.contact.provider.composition.impl;

import java.util.Collections;
import java.util.List;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.provider.extensions.WarningsAware;
import com.openexchange.contact.provider.folder.FallbackFolderContactsAccess;
import com.openexchange.exception.OXException;

/**
 * {@link FallbackEmptyContactsAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class FallbackEmptyContactsAccess extends FallbackFolderContactsAccess implements WarningsAware {

    private final OXException error;

    /**
     * Initializes a new {@link FallbackEmptyContactsAccess}.
     *
     * @param account The underlying Contacts account
     * @param error The error to include in the accesses' warnings, or <code>null</code> if not defined
     */
    public FallbackEmptyContactsAccess(ContactsAccount account, OXException error) {
        super(account);
        this.error = error;
    }

    @Override
    public List<OXException> getWarnings() {
        return null == error ? Collections.emptyList() : Collections.singletonList(error);
    }

    @Override
    public ContactsFolder getFolder(String folderId) throws OXException {
        throw ContactsProviderExceptionCodes.FOLDER_NOT_FOUND.create(folderId);
    }

    @Override
    public List<ContactsFolder> getVisibleFolders() throws OXException {
        return Collections.emptyList();
    }

}

