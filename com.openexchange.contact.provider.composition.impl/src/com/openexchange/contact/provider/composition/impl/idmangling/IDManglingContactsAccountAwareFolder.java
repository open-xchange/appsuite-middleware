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

package com.openexchange.contact.provider.composition.impl.idmangling;

import com.openexchange.contact.common.AccountAwareContactsFolder;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsFolder;

/**
 * {@link IDManglingContactsAccountAwareFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class IDManglingContactsAccountAwareFolder extends IDManglingContactsFolder implements AccountAwareContactsFolder {

    private final ContactsAccount account;

    /**
     * Initializes a new {@link IDManglingContactsAccountAwareFolder}.
     *
     * @param delegate The contacts folder delegate
     * @param account The underlying contacts account
     * @param newId The new identifier to hide the delegate's one
     */
    public IDManglingContactsAccountAwareFolder(ContactsFolder delegate, ContactsAccount account, String newId) {
        super(delegate, newId);
        this.account = account;
    }

    @Override
    public ContactsAccount getAccount() {
        return account;
    }
}
